package org.fourthline.cling.model.action;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.profile.RemoteClientInfo;
import org.fourthline.cling.model.state.StateVariableAccessor;
import org.fourthline.cling.model.types.ErrorCode;
import org.seamless.util.Reflections;
/* loaded from: classes.dex */
public class MethodActionExecutor extends AbstractActionExecutor {
    private static Logger log = Logger.getLogger(MethodActionExecutor.class.getName());
    protected Method method;

    public MethodActionExecutor(Method method) {
        this.method = method;
    }

    public MethodActionExecutor(Map<ActionArgument<LocalService>, StateVariableAccessor> outputArgumentAccessors, Method method) {
        super(outputArgumentAccessors);
        this.method = method;
    }

    public Method getMethod() {
        return this.method;
    }

    @Override // org.fourthline.cling.model.action.AbstractActionExecutor
    protected void execute(ActionInvocation<LocalService> actionInvocation, Object serviceImpl) throws Exception {
        Object result;
        Object[] inputArgumentValues = createInputArgumentValues(actionInvocation, this.method);
        if (!actionInvocation.getAction().hasOutputArguments()) {
            Logger logger = log;
            logger.fine("Calling local service method with no output arguments: " + this.method);
            Reflections.invoke(this.method, serviceImpl, inputArgumentValues);
            return;
        }
        boolean isVoid = this.method.getReturnType().equals(Void.TYPE);
        Logger logger2 = log;
        logger2.fine("Calling local service method with output arguments: " + this.method);
        boolean isArrayResultProcessed = true;
        if (isVoid) {
            log.fine("Action method is void, calling declared accessors(s) on service instance to retrieve ouput argument(s)");
            Reflections.invoke(this.method, serviceImpl, inputArgumentValues);
            result = readOutputArgumentValues(actionInvocation.getAction(), serviceImpl);
        } else if (isUseOutputArgumentAccessors(actionInvocation)) {
            log.fine("Action method is not void, calling declared accessor(s) on returned instance to retrieve ouput argument(s)");
            Object returnedInstance = Reflections.invoke(this.method, serviceImpl, inputArgumentValues);
            result = readOutputArgumentValues(actionInvocation.getAction(), returnedInstance);
        } else {
            log.fine("Action method is not void, using returned value as (single) output argument");
            result = Reflections.invoke(this.method, serviceImpl, inputArgumentValues);
            isArrayResultProcessed = false;
        }
        ActionArgument<LocalService>[] outputArgs = actionInvocation.getAction().getOutputArguments();
        if (!isArrayResultProcessed || !(result instanceof Object[])) {
            if (outputArgs.length == 1) {
                setOutputArgumentValue(actionInvocation, outputArgs[0], result);
                return;
            }
            ErrorCode errorCode = ErrorCode.ACTION_FAILED;
            throw new ActionException(errorCode, "Method return does not match required number of output arguments: " + outputArgs.length);
        }
        Object[] results = (Object[]) result;
        Logger logger3 = log;
        logger3.fine("Accessors returned Object[], setting output argument values: " + results.length);
        for (int i = 0; i < outputArgs.length; i++) {
            setOutputArgumentValue(actionInvocation, outputArgs[i], results[i]);
        }
    }

    protected boolean isUseOutputArgumentAccessors(ActionInvocation<LocalService> actionInvocation) {
        ActionArgument[] outputArguments;
        for (ActionArgument argument : actionInvocation.getAction().getOutputArguments()) {
            if (getOutputArgumentAccessors().get(argument) != null) {
                return true;
            }
        }
        return false;
    }

    protected Object[] createInputArgumentValues(ActionInvocation<LocalService> actionInvocation, Method method) throws ActionException {
        int i;
        Constructor<String> ctor;
        int i2;
        LocalService service = actionInvocation.getAction().getService();
        List values = new ArrayList();
        ActionArgument<LocalService>[] inputArguments = actionInvocation.getAction().getInputArguments();
        int length = inputArguments.length;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        while (i5 < length) {
            ActionArgument<LocalService> argument = inputArguments[i5];
            Class methodParameterType = method.getParameterTypes()[i4];
            ActionArgumentValue<LocalService> inputValue = actionInvocation.getInput(argument);
            if (methodParameterType.isPrimitive() && (inputValue == null || inputValue.toString().length() == 0)) {
                throw new ActionException(ErrorCode.ARGUMENT_VALUE_INVALID, "Primitive action method argument '" + argument.getName() + "' requires input value, can't be null or empty string");
            }
            if (inputValue == null) {
                values.add(i4, null);
                i4++;
                i = i3;
            } else {
                String inputCallValueString = inputValue.toString();
                if (inputCallValueString.length() <= 0 || !service.isStringConvertibleType(methodParameterType) || methodParameterType.isEnum()) {
                    i = i3;
                    values.add(i4, inputValue.getValue());
                    i4++;
                } else {
                    try {
                        Class<?>[] clsArr = new Class[1];
                        clsArr[i3] = String.class;
                        ctor = methodParameterType.getConstructor(clsArr);
                        log.finer("Creating new input argument value instance with String.class constructor of type: " + methodParameterType);
                        i = 0;
                        i2 = i4 + 1;
                    } catch (Exception e) {
                        ex = e;
                    }
                    try {
                        values.add(i4, ctor.newInstance(inputCallValueString));
                        i4 = i2;
                    } catch (Exception e2) {
                        ex = e2;
                        log.warning("Error preparing action method call: " + method);
                        log.warning("Can't convert input argument string to desired type of '" + argument.getName() + "': " + ex);
                        throw new ActionException(ErrorCode.ARGUMENT_VALUE_INVALID, "Can't convert input argument string to desired type of '" + argument.getName() + "': " + ex);
                    }
                }
            }
            i5++;
            i3 = i;
        }
        if (method.getParameterTypes().length > 0 && RemoteClientInfo.class.isAssignableFrom(method.getParameterTypes()[method.getParameterTypes().length - 1])) {
            if (!(actionInvocation instanceof RemoteActionInvocation) || ((RemoteActionInvocation) actionInvocation).getRemoteClientInfo() == null) {
                values.add(i4, null);
            } else {
                log.finer("Providing remote client info as last action method input argument: " + method);
                values.add(i4, ((RemoteActionInvocation) actionInvocation).getRemoteClientInfo());
            }
        }
        return values.toArray(new Object[values.size()]);
    }
}
