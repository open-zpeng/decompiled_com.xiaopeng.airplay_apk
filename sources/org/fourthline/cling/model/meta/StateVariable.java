package org.fourthline.cling.model.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.Validatable;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.Datatype;
/* loaded from: classes.dex */
public class StateVariable<S extends Service> implements Validatable {
    private static final Logger log = Logger.getLogger(StateVariable.class.getName());
    private final StateVariableEventDetails eventDetails;
    private final String name;
    private S service;
    private final StateVariableTypeDetails type;

    public StateVariable(String name, StateVariableTypeDetails type) {
        this(name, type, new StateVariableEventDetails());
    }

    public StateVariable(String name, StateVariableTypeDetails type, StateVariableEventDetails eventDetails) {
        this.name = name;
        this.type = type;
        this.eventDetails = eventDetails;
    }

    public String getName() {
        return this.name;
    }

    public StateVariableTypeDetails getTypeDetails() {
        return this.type;
    }

    public StateVariableEventDetails getEventDetails() {
        return this.eventDetails;
    }

    public S getService() {
        return this.service;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setService(S service) {
        if (this.service != null) {
            throw new IllegalStateException("Final value has been set already, model is immutable");
        }
        this.service = service;
    }

    @Override // org.fourthline.cling.model.Validatable
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();
        if (getName() == null || getName().length() == 0) {
            Class<?> cls = getClass();
            errors.add(new ValidationError(cls, "name", "StateVariable without name of: " + getService()));
        } else if (!ModelUtil.isValidUDAName(getName())) {
            Logger logger = log;
            logger.warning("UPnP specification violation of: " + getService().getDevice());
            Logger logger2 = log;
            logger2.warning("Invalid state variable name: " + this);
        }
        errors.addAll(getTypeDetails().validate());
        return errors;
    }

    public boolean isModeratedNumericType() {
        return Datatype.Builtin.isNumeric(getTypeDetails().getDatatype().getBuiltin()) && getEventDetails().getEventMinimumDelta() > 0;
    }

    public StateVariable<S> deepCopy() {
        return new StateVariable<>(getName(), getTypeDetails(), getEventDetails());
    }

    public String toString() {
        String[] allowedValues;
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(getClass().getSimpleName());
        sb.append(", Name: ");
        sb.append(getName());
        sb.append(", Type: ");
        sb.append(getTypeDetails().getDatatype().getDisplayString());
        sb.append(")");
        if (!getEventDetails().isSendEvents()) {
            sb.append(" (No Events)");
        }
        if (getTypeDetails().getDefaultValue() != null) {
            sb.append(" Default Value: ");
            sb.append("'");
            sb.append(getTypeDetails().getDefaultValue());
            sb.append("'");
        }
        if (getTypeDetails().getAllowedValues() != null) {
            sb.append(" Allowed Values: ");
            for (String s : getTypeDetails().getAllowedValues()) {
                sb.append(s);
                sb.append("|");
            }
        }
        return sb.toString();
    }
}
