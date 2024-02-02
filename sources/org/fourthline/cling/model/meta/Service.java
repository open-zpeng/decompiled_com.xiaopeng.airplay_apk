package org.fourthline.cling.model.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.fourthline.cling.model.ServiceReference;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.ServiceType;
/* loaded from: classes.dex */
public abstract class Service<D extends Device, S extends Service> {
    private static final Logger log = Logger.getLogger(Service.class.getName());
    private final Map<String, Action> actions;
    private D device;
    private final ServiceId serviceId;
    private final ServiceType serviceType;
    private final Map<String, StateVariable> stateVariables;

    public abstract Action getQueryStateVariableAction();

    public Service(ServiceType serviceType, ServiceId serviceId) throws ValidationException {
        this(serviceType, serviceId, null, null);
    }

    public Service(ServiceType serviceType, ServiceId serviceId, Action<S>[] actions, StateVariable<S>[] stateVariables) throws ValidationException {
        this.actions = new HashMap();
        this.stateVariables = new HashMap();
        this.serviceType = serviceType;
        this.serviceId = serviceId;
        if (actions != null) {
            for (Action<S> action : actions) {
                this.actions.put(action.getName(), action);
                action.setService(this);
            }
        }
        if (stateVariables != null) {
            for (StateVariable<S> stateVariable : stateVariables) {
                this.stateVariables.put(stateVariable.getName(), stateVariable);
                stateVariable.setService(this);
            }
        }
    }

    public ServiceType getServiceType() {
        return this.serviceType;
    }

    public ServiceId getServiceId() {
        return this.serviceId;
    }

    public boolean hasActions() {
        return getActions() != null && getActions().length > 0;
    }

    public Action<S>[] getActions() {
        if (this.actions == null) {
            return null;
        }
        return (Action[]) this.actions.values().toArray(new Action[this.actions.values().size()]);
    }

    public boolean hasStateVariables() {
        return getStateVariables() != null && getStateVariables().length > 0;
    }

    public StateVariable<S>[] getStateVariables() {
        if (this.stateVariables == null) {
            return null;
        }
        return (StateVariable[]) this.stateVariables.values().toArray(new StateVariable[this.stateVariables.values().size()]);
    }

    public D getDevice() {
        return this.device;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setDevice(D device) {
        if (this.device != null) {
            throw new IllegalStateException("Final value has been set already, model is immutable");
        }
        this.device = device;
    }

    public Action<S> getAction(String name) {
        if (this.actions == null) {
            return null;
        }
        return this.actions.get(name);
    }

    public StateVariable<S> getStateVariable(String name) {
        if (QueryStateVariableAction.VIRTUAL_STATEVARIABLE_INPUT.equals(name)) {
            return new StateVariable<>(QueryStateVariableAction.VIRTUAL_STATEVARIABLE_INPUT, new StateVariableTypeDetails(Datatype.Builtin.STRING.getDatatype()));
        }
        if (QueryStateVariableAction.VIRTUAL_STATEVARIABLE_OUTPUT.equals(name)) {
            return new StateVariable<>(QueryStateVariableAction.VIRTUAL_STATEVARIABLE_OUTPUT, new StateVariableTypeDetails(Datatype.Builtin.STRING.getDatatype()));
        }
        if (this.stateVariables == null) {
            return null;
        }
        return this.stateVariables.get(name);
    }

    public StateVariable<S> getRelatedStateVariable(ActionArgument argument) {
        return getStateVariable(argument.getRelatedStateVariableName());
    }

    public Datatype<S> getDatatype(ActionArgument argument) {
        return getRelatedStateVariable(argument).getTypeDetails().getDatatype();
    }

    public ServiceReference getReference() {
        return new ServiceReference(getDevice().getIdentity().getUdn(), getServiceId());
    }

    public List<ValidationError> validate() {
        Action[] actions;
        StateVariable[] stateVariables;
        List<ValidationError> errors = new ArrayList<>();
        if (getServiceType() == null) {
            errors.add(new ValidationError(getClass(), "serviceType", "Service type/info is required"));
        }
        if (getServiceId() == null) {
            errors.add(new ValidationError(getClass(), "serviceId", "Service ID is required"));
        }
        if (hasStateVariables()) {
            for (StateVariable stateVariable : getStateVariables()) {
                errors.addAll(stateVariable.validate());
            }
        }
        if (hasActions()) {
            for (Action action : getActions()) {
                List<ValidationError> actionErrors = action.validate();
                if (actionErrors.size() > 0) {
                    this.actions.remove(action.getName());
                    log.warning("Discarding invalid action of service '" + getServiceId() + "': " + action.getName());
                    for (ValidationError actionError : actionErrors) {
                        log.warning("Invalid action '" + action.getName() + "': " + actionError);
                    }
                }
            }
        }
        return errors;
    }

    public String toString() {
        return "(" + getClass().getSimpleName() + ") ServiceId: " + getServiceId();
    }
}
