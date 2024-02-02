package org.fourthline.cling.binding.annotations;

import java.util.Set;
import java.util.logging.Logger;
import org.fourthline.cling.binding.AllowedValueProvider;
import org.fourthline.cling.binding.AllowedValueRangeProvider;
import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.meta.StateVariableAllowedValueRange;
import org.fourthline.cling.model.meta.StateVariableEventDetails;
import org.fourthline.cling.model.meta.StateVariableTypeDetails;
import org.fourthline.cling.model.state.StateVariableAccessor;
import org.fourthline.cling.model.types.Datatype;
/* loaded from: classes.dex */
public class AnnotationStateVariableBinder {
    private static Logger log = Logger.getLogger(AnnotationLocalServiceBinder.class.getName());
    protected StateVariableAccessor accessor;
    protected UpnpStateVariable annotation;
    protected String name;
    protected Set<Class> stringConvertibleTypes;

    public AnnotationStateVariableBinder(UpnpStateVariable annotation, String name, StateVariableAccessor accessor, Set<Class> stringConvertibleTypes) {
        this.annotation = annotation;
        this.name = name;
        this.accessor = accessor;
        this.stringConvertibleTypes = stringConvertibleTypes;
    }

    public UpnpStateVariable getAnnotation() {
        return this.annotation;
    }

    public String getName() {
        return this.name;
    }

    public StateVariableAccessor getAccessor() {
        return this.accessor;
    }

    public Set<Class> getStringConvertibleTypes() {
        return this.stringConvertibleTypes;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public StateVariable createStateVariable() throws LocalServiceBindingException {
        Logger logger = log;
        logger.fine("Creating state variable '" + getName() + "' with accessor: " + getAccessor());
        Datatype datatype = createDatatype();
        String defaultValue = createDefaultValue(datatype);
        String[] allowedValues = null;
        if (Datatype.Builtin.STRING.equals(datatype.getBuiltin())) {
            if (getAnnotation().allowedValueProvider() != Void.TYPE) {
                allowedValues = getAllowedValuesFromProvider();
            } else if (getAnnotation().allowedValues().length > 0) {
                allowedValues = getAnnotation().allowedValues();
            } else if (getAnnotation().allowedValuesEnum() != Void.TYPE) {
                allowedValues = getAllowedValues(getAnnotation().allowedValuesEnum());
            } else if (getAccessor() != null && getAccessor().getReturnType().isEnum()) {
                allowedValues = getAllowedValues(getAccessor().getReturnType());
            } else {
                Logger logger2 = log;
                logger2.finer("Not restricting allowed values (of string typed state var): " + getName());
            }
            if (allowedValues != null && defaultValue != null) {
                boolean foundValue = false;
                int length = allowedValues.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    String s = allowedValues[i];
                    if (!s.equals(defaultValue)) {
                        i++;
                    } else {
                        foundValue = true;
                        break;
                    }
                }
                if (!foundValue) {
                    throw new LocalServiceBindingException("Default value '" + defaultValue + "' is not in allowed values of: " + getName());
                }
            }
        }
        StateVariableAllowedValueRange allowedValueRange = null;
        if (Datatype.Builtin.isNumeric(datatype.getBuiltin())) {
            if (getAnnotation().allowedValueRangeProvider() != Void.TYPE) {
                allowedValueRange = getAllowedRangeFromProvider();
            } else if (getAnnotation().allowedValueMinimum() > 0 || getAnnotation().allowedValueMaximum() > 0) {
                allowedValueRange = getAllowedValueRange(getAnnotation().allowedValueMinimum(), getAnnotation().allowedValueMaximum(), getAnnotation().allowedValueStep());
            } else {
                Logger logger3 = log;
                logger3.finer("Not restricting allowed value range (of numeric typed state var): " + getName());
            }
            if (defaultValue != null && allowedValueRange != null) {
                try {
                    long v = Long.valueOf(defaultValue).longValue();
                    if (!allowedValueRange.isInRange(v)) {
                        throw new LocalServiceBindingException("Default value '" + defaultValue + "' is not in allowed range of: " + getName());
                    }
                } catch (Exception e) {
                    throw new LocalServiceBindingException("Default value '" + defaultValue + "' is not numeric (for range checking) of: " + getName());
                }
            }
        }
        boolean sendEvents = getAnnotation().sendEvents();
        if (sendEvents && getAccessor() == null) {
            throw new LocalServiceBindingException("State variable sends events but has no accessor for field or getter: " + getName());
        }
        int eventMaximumRateMillis = 0;
        int eventMinimumDelta = 0;
        if (sendEvents) {
            if (getAnnotation().eventMaximumRateMilliseconds() > 0) {
                Logger logger4 = log;
                logger4.finer("Moderating state variable events using maximum rate (milliseconds): " + getAnnotation().eventMaximumRateMilliseconds());
                eventMaximumRateMillis = getAnnotation().eventMaximumRateMilliseconds();
            }
            if (getAnnotation().eventMinimumDelta() > 0 && Datatype.Builtin.isNumeric(datatype.getBuiltin())) {
                Logger logger5 = log;
                logger5.finer("Moderating state variable events using minimum delta: " + getAnnotation().eventMinimumDelta());
                eventMinimumDelta = getAnnotation().eventMinimumDelta();
            }
        }
        StateVariableTypeDetails typeDetails = new StateVariableTypeDetails(datatype, defaultValue, allowedValues, allowedValueRange);
        StateVariableEventDetails eventDetails = new StateVariableEventDetails(sendEvents, eventMaximumRateMillis, eventMinimumDelta);
        return new StateVariable(getName(), typeDetails, eventDetails);
    }

    protected Datatype createDatatype() throws LocalServiceBindingException {
        String declaredDatatype = getAnnotation().datatype();
        if (declaredDatatype.length() == 0 && getAccessor() != null) {
            Class returnType = getAccessor().getReturnType();
            Logger logger = log;
            logger.finer("Using accessor return type as state variable type: " + returnType);
            if (ModelUtil.isStringConvertibleType(getStringConvertibleTypes(), returnType)) {
                log.finer("Return type is string-convertible, using string datatype");
                return Datatype.Default.STRING.getBuiltinType().getDatatype();
            }
            Datatype.Default defaultDatatype = Datatype.Default.getByJavaType(returnType);
            if (defaultDatatype != null) {
                Logger logger2 = log;
                logger2.finer("Return type has default UPnP datatype: " + defaultDatatype);
                return defaultDatatype.getBuiltinType().getDatatype();
            }
        }
        if ((declaredDatatype == null || declaredDatatype.length() == 0) && (getAnnotation().allowedValues().length > 0 || getAnnotation().allowedValuesEnum() != Void.TYPE)) {
            log.finer("State variable has restricted allowed values, hence using 'string' datatype");
            declaredDatatype = "string";
        }
        if (declaredDatatype == null || declaredDatatype.length() == 0) {
            throw new LocalServiceBindingException("Could not detect datatype of state variable: " + getName());
        }
        Logger logger3 = log;
        logger3.finer("Trying to find built-in UPnP datatype for detected name: " + declaredDatatype);
        Datatype.Builtin builtin = Datatype.Builtin.getByDescriptorName(declaredDatatype);
        if (builtin != null) {
            Logger logger4 = log;
            logger4.finer("Found built-in UPnP datatype: " + builtin);
            return builtin.getDatatype();
        }
        throw new LocalServiceBindingException("No built-in UPnP datatype found, using CustomDataType (TODO: NOT IMPLEMENTED)");
    }

    protected String createDefaultValue(Datatype datatype) throws LocalServiceBindingException {
        if (getAnnotation().defaultValue().length() != 0) {
            try {
                datatype.valueOf(getAnnotation().defaultValue());
                Logger logger = log;
                logger.finer("Found state variable default value: " + getAnnotation().defaultValue());
                return getAnnotation().defaultValue();
            } catch (Exception ex) {
                throw new LocalServiceBindingException("Default value doesn't match datatype of state variable '" + getName() + "': " + ex.getMessage());
            }
        }
        return null;
    }

    protected String[] getAllowedValues(Class enumType) throws LocalServiceBindingException {
        if (!enumType.isEnum()) {
            throw new LocalServiceBindingException("Allowed values type is not an Enum: " + enumType);
        }
        Logger logger = log;
        logger.finer("Restricting allowed values of state variable to Enum: " + getName());
        String[] allowedValueStrings = new String[enumType.getEnumConstants().length];
        for (int i = 0; i < enumType.getEnumConstants().length; i++) {
            Object o = enumType.getEnumConstants()[i];
            if (o.toString().length() > 32) {
                throw new LocalServiceBindingException("Allowed value string (that is, Enum constant name) is longer than 32 characters: " + o.toString());
            }
            Logger logger2 = log;
            logger2.finer("Adding allowed value (converted to string): " + o.toString());
            allowedValueStrings[i] = o.toString();
        }
        return allowedValueStrings;
    }

    protected StateVariableAllowedValueRange getAllowedValueRange(long min, long max, long step) throws LocalServiceBindingException {
        if (max < min) {
            throw new LocalServiceBindingException("Allowed value range maximum is smaller than minimum: " + getName());
        }
        return new StateVariableAllowedValueRange(min, max, step);
    }

    protected String[] getAllowedValuesFromProvider() throws LocalServiceBindingException {
        Class provider = getAnnotation().allowedValueProvider();
        if (!AllowedValueProvider.class.isAssignableFrom(provider)) {
            throw new LocalServiceBindingException("Allowed value provider is not of type " + AllowedValueProvider.class + ": " + getName());
        }
        try {
            return ((AllowedValueProvider) provider.newInstance()).getValues();
        } catch (Exception ex) {
            throw new LocalServiceBindingException("Allowed value provider can't be instantiated: " + getName(), ex);
        }
    }

    protected StateVariableAllowedValueRange getAllowedRangeFromProvider() throws LocalServiceBindingException {
        Class provider = getAnnotation().allowedValueRangeProvider();
        if (!AllowedValueRangeProvider.class.isAssignableFrom(provider)) {
            throw new LocalServiceBindingException("Allowed value range provider is not of type " + AllowedValueRangeProvider.class + ": " + getName());
        }
        try {
            AllowedValueRangeProvider providerInstance = (AllowedValueRangeProvider) provider.newInstance();
            return getAllowedValueRange(providerInstance.getMinimum(), providerInstance.getMaximum(), providerInstance.getStep());
        } catch (Exception ex) {
            throw new LocalServiceBindingException("Allowed value range provider can't be instantiated: " + getName(), ex);
        }
    }
}
