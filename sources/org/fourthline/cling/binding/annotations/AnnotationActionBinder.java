package org.fourthline.cling.binding.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.model.Constants;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.action.ActionExecutor;
import org.fourthline.cling.model.action.MethodActionExecutor;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.profile.RemoteClientInfo;
import org.fourthline.cling.model.state.GetterStateVariableAccessor;
import org.fourthline.cling.model.state.StateVariableAccessor;
import org.fourthline.cling.model.types.Datatype;
import org.seamless.util.Reflections;
/* loaded from: classes.dex */
public class AnnotationActionBinder {
    private static Logger log = Logger.getLogger(AnnotationLocalServiceBinder.class.getName());
    protected UpnpAction annotation;
    protected Method method;
    protected Map<StateVariable, StateVariableAccessor> stateVariables;
    protected Set<Class> stringConvertibleTypes;

    public AnnotationActionBinder(Method method, Map<StateVariable, StateVariableAccessor> stateVariables, Set<Class> stringConvertibleTypes) {
        this.annotation = (UpnpAction) method.getAnnotation(UpnpAction.class);
        this.stateVariables = stateVariables;
        this.method = method;
        this.stringConvertibleTypes = stringConvertibleTypes;
    }

    public UpnpAction getAnnotation() {
        return this.annotation;
    }

    public Map<StateVariable, StateVariableAccessor> getStateVariables() {
        return this.stateVariables;
    }

    public Method getMethod() {
        return this.method;
    }

    public Set<Class> getStringConvertibleTypes() {
        return this.stringConvertibleTypes;
    }

    public Action appendAction(Map<Action, ActionExecutor> actions) throws LocalServiceBindingException {
        String name;
        if (getAnnotation().name().length() != 0) {
            name = getAnnotation().name();
        } else {
            name = AnnotationLocalServiceBinder.toUpnpActionName(getMethod().getName());
        }
        Logger logger = log;
        logger.fine("Creating action and executor: " + name);
        List<ActionArgument> inputArguments = createInputArguments();
        Map<ActionArgument<LocalService>, StateVariableAccessor> outputArguments = createOutputArguments();
        inputArguments.addAll(outputArguments.keySet());
        ActionArgument<LocalService>[] actionArguments = (ActionArgument[]) inputArguments.toArray(new ActionArgument[inputArguments.size()]);
        Action action = new Action(name, actionArguments);
        ActionExecutor executor = createExecutor(outputArguments);
        actions.put(action, executor);
        return action;
    }

    protected ActionExecutor createExecutor(Map<ActionArgument<LocalService>, StateVariableAccessor> outputArguments) {
        return new MethodActionExecutor(outputArguments, getMethod());
    }

    protected List<ActionArgument> createInputArguments() throws LocalServiceBindingException {
        List<ActionArgument> list = new ArrayList<>();
        Annotation[][] params = getMethod().getParameterAnnotations();
        int annotatedParams = 0;
        int annotatedParams2 = 0;
        while (annotatedParams2 < params.length) {
            Annotation[] param = params[annotatedParams2];
            int annotatedParams3 = annotatedParams;
            for (Annotation paramAnnotation : param) {
                if (paramAnnotation instanceof UpnpInputArgument) {
                    UpnpInputArgument inputArgumentAnnotation = (UpnpInputArgument) paramAnnotation;
                    annotatedParams3++;
                    String argumentName = inputArgumentAnnotation.name();
                    StateVariable stateVariable = findRelatedStateVariable(inputArgumentAnnotation.stateVariable(), argumentName, getMethod().getName());
                    if (stateVariable != null) {
                        validateType(stateVariable, getMethod().getParameterTypes()[annotatedParams2]);
                        ActionArgument inputArgument = new ActionArgument(argumentName, inputArgumentAnnotation.aliases(), stateVariable.getName(), ActionArgument.Direction.IN);
                        list.add(inputArgument);
                    } else {
                        throw new LocalServiceBindingException("Could not detected related state variable of argument: " + argumentName);
                    }
                }
            }
            annotatedParams2++;
            annotatedParams = annotatedParams3;
        }
        if (annotatedParams < getMethod().getParameterTypes().length && !RemoteClientInfo.class.isAssignableFrom(this.method.getParameterTypes()[this.method.getParameterTypes().length - 1])) {
            throw new LocalServiceBindingException("Method has parameters that are not input arguments: " + getMethod().getName());
        }
        return list;
    }

    protected Map<ActionArgument<LocalService>, StateVariableAccessor> createOutputArguments() throws LocalServiceBindingException {
        UpnpOutputArgument[] out;
        Map<ActionArgument<LocalService>, StateVariableAccessor> map = new LinkedHashMap<>();
        UpnpAction actionAnnotation = (UpnpAction) getMethod().getAnnotation(UpnpAction.class);
        if (actionAnnotation.out().length == 0) {
            return map;
        }
        boolean hasMultipleOutputArguments = actionAnnotation.out().length > 1;
        for (UpnpOutputArgument outputArgumentAnnotation : actionAnnotation.out()) {
            String argumentName = outputArgumentAnnotation.name();
            StateVariable stateVariable = findRelatedStateVariable(outputArgumentAnnotation.stateVariable(), argumentName, getMethod().getName());
            if (stateVariable == null && outputArgumentAnnotation.getterName().length() > 0) {
                stateVariable = findRelatedStateVariable(null, null, outputArgumentAnnotation.getterName());
            }
            if (stateVariable == null) {
                throw new LocalServiceBindingException("Related state variable not found for output argument: " + argumentName);
            }
            StateVariableAccessor accessor = findOutputArgumentAccessor(stateVariable, outputArgumentAnnotation.getterName(), hasMultipleOutputArguments);
            log.finer("Found related state variable for output argument '" + argumentName + "': " + stateVariable);
            ActionArgument outputArgument = new ActionArgument(argumentName, stateVariable.getName(), ActionArgument.Direction.OUT, !hasMultipleOutputArguments);
            map.put(outputArgument, accessor);
        }
        return map;
    }

    protected StateVariableAccessor findOutputArgumentAccessor(StateVariable stateVariable, String getterName, boolean multipleArguments) throws LocalServiceBindingException {
        boolean isVoid = getMethod().getReturnType().equals(Void.TYPE);
        if (isVoid) {
            if (getterName != null && getterName.length() > 0) {
                Logger logger = log;
                logger.finer("Action method is void, will use getter method named: " + getterName);
                Method getter = Reflections.getMethod(getMethod().getDeclaringClass(), getterName);
                if (getter == null) {
                    throw new LocalServiceBindingException("Declared getter method '" + getterName + "' not found on: " + getMethod().getDeclaringClass());
                }
                validateType(stateVariable, getter.getReturnType());
                return new GetterStateVariableAccessor(getter);
            }
            Logger logger2 = log;
            logger2.finer("Action method is void, trying to find existing accessor of related: " + stateVariable);
            return getStateVariables().get(stateVariable);
        } else if (getterName != null && getterName.length() > 0) {
            Logger logger3 = log;
            logger3.finer("Action method is not void, will use getter method on returned instance: " + getterName);
            Method getter2 = Reflections.getMethod(getMethod().getReturnType(), getterName);
            if (getter2 == null) {
                throw new LocalServiceBindingException("Declared getter method '" + getterName + "' not found on return type: " + getMethod().getReturnType());
            }
            validateType(stateVariable, getter2.getReturnType());
            return new GetterStateVariableAccessor(getter2);
        } else if (!multipleArguments) {
            Logger logger4 = log;
            logger4.finer("Action method is not void, will use the returned instance: " + getMethod().getReturnType());
            validateType(stateVariable, getMethod().getReturnType());
            return null;
        } else {
            return null;
        }
    }

    protected StateVariable findRelatedStateVariable(String declaredName, String argumentName, String methodName) throws LocalServiceBindingException {
        String methodPropertyName;
        StateVariable relatedStateVariable = null;
        if (declaredName != null && declaredName.length() > 0) {
            relatedStateVariable = getStateVariable(declaredName);
        }
        if (relatedStateVariable == null && argumentName != null && argumentName.length() > 0) {
            String actualName = AnnotationLocalServiceBinder.toUpnpStateVariableName(argumentName);
            log.finer("Finding related state variable with argument name (converted to UPnP name): " + actualName);
            relatedStateVariable = getStateVariable(argumentName);
        }
        if (relatedStateVariable == null && argumentName != null && argumentName.length() > 0) {
            String actualName2 = Constants.ARG_TYPE_PREFIX + AnnotationLocalServiceBinder.toUpnpStateVariableName(argumentName);
            log.finer("Finding related state variable with prefixed argument name (converted to UPnP name): " + actualName2);
            relatedStateVariable = getStateVariable(actualName2);
        }
        if (relatedStateVariable == null && methodName != null && methodName.length() > 0 && (methodPropertyName = Reflections.getMethodPropertyName(methodName)) != null) {
            log.finer("Finding related state variable with method property name: " + methodPropertyName);
            return getStateVariable(AnnotationLocalServiceBinder.toUpnpStateVariableName(methodPropertyName));
        }
        return relatedStateVariable;
    }

    protected void validateType(StateVariable stateVariable, Class type) throws LocalServiceBindingException {
        Datatype.Default expectedDefaultMapping;
        if (ModelUtil.isStringConvertibleType(getStringConvertibleTypes(), type)) {
            expectedDefaultMapping = Datatype.Default.STRING;
        } else {
            expectedDefaultMapping = Datatype.Default.getByJavaType(type);
        }
        Logger logger = log;
        logger.finer("Expecting '" + stateVariable + "' to match default mapping: " + expectedDefaultMapping);
        if (expectedDefaultMapping != null && !stateVariable.getTypeDetails().getDatatype().isHandlingJavaType(expectedDefaultMapping.getJavaType())) {
            throw new LocalServiceBindingException("State variable '" + stateVariable + "' datatype can't handle action argument's Java type (change one): " + expectedDefaultMapping.getJavaType());
        } else if (expectedDefaultMapping == null && stateVariable.getTypeDetails().getDatatype().getBuiltin() != null) {
            throw new LocalServiceBindingException("State variable '" + stateVariable + "' should be custom datatype (action argument type is unknown Java type): " + type.getSimpleName());
        } else {
            log.finer("State variable matches required argument datatype (or can't be validated because it is custom)");
        }
    }

    protected StateVariable getStateVariable(String name) {
        for (StateVariable stateVariable : getStateVariables().keySet()) {
            if (stateVariable.getName().equals(name)) {
                return stateVariable;
            }
        }
        return null;
    }
}
