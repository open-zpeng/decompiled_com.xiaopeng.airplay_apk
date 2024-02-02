package org.fourthline.cling.model.meta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.ServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.action.ActionExecutor;
import org.fourthline.cling.model.state.StateVariableAccessor;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.ServiceType;
/* loaded from: classes.dex */
public class LocalService<T> extends Service<LocalDevice, LocalService> {
    protected final Map<Action, ActionExecutor> actionExecutors;
    protected ServiceManager manager;
    protected final Map<StateVariable, StateVariableAccessor> stateVariableAccessors;
    protected final Set<Class> stringConvertibleTypes;
    protected final boolean supportsQueryStateVariables;

    public LocalService(ServiceType serviceType, ServiceId serviceId, Action[] actions, StateVariable[] stateVariables) throws ValidationException {
        super(serviceType, serviceId, actions, stateVariables);
        this.manager = null;
        this.actionExecutors = new HashMap();
        this.stateVariableAccessors = new HashMap();
        this.stringConvertibleTypes = new HashSet();
        this.supportsQueryStateVariables = true;
    }

    public LocalService(ServiceType serviceType, ServiceId serviceId, Map<Action, ActionExecutor> actionExecutors, Map<StateVariable, StateVariableAccessor> stateVariableAccessors, Set<Class> stringConvertibleTypes, boolean supportsQueryStateVariables) throws ValidationException {
        super(serviceType, serviceId, (Action[]) actionExecutors.keySet().toArray(new Action[actionExecutors.size()]), (StateVariable[]) stateVariableAccessors.keySet().toArray(new StateVariable[stateVariableAccessors.size()]));
        this.supportsQueryStateVariables = supportsQueryStateVariables;
        this.stringConvertibleTypes = stringConvertibleTypes;
        this.stateVariableAccessors = stateVariableAccessors;
        this.actionExecutors = actionExecutors;
    }

    public synchronized void setManager(ServiceManager<T> manager) {
        if (this.manager != null) {
            throw new IllegalStateException("Manager is final");
        }
        this.manager = manager;
    }

    public synchronized ServiceManager<T> getManager() {
        if (this.manager == null) {
            throw new IllegalStateException("Unmanaged service, no implementation instance available");
        }
        return this.manager;
    }

    public boolean isSupportsQueryStateVariables() {
        return this.supportsQueryStateVariables;
    }

    public Set<Class> getStringConvertibleTypes() {
        return this.stringConvertibleTypes;
    }

    public boolean isStringConvertibleType(Object o) {
        return o != null && isStringConvertibleType((Class) o.getClass());
    }

    public boolean isStringConvertibleType(Class clazz) {
        return ModelUtil.isStringConvertibleType(getStringConvertibleTypes(), clazz);
    }

    public StateVariableAccessor getAccessor(String stateVariableName) {
        StateVariable sv = getStateVariable(stateVariableName);
        if (sv != null) {
            return getAccessor(sv);
        }
        return null;
    }

    public StateVariableAccessor getAccessor(StateVariable stateVariable) {
        return this.stateVariableAccessors.get(stateVariable);
    }

    public ActionExecutor getExecutor(String actionName) {
        Action action = getAction(actionName);
        if (action != null) {
            return getExecutor(action);
        }
        return null;
    }

    public ActionExecutor getExecutor(Action action) {
        return this.actionExecutors.get(action);
    }

    @Override // org.fourthline.cling.model.meta.Service
    public Action getQueryStateVariableAction() {
        return getAction(QueryStateVariableAction.ACTION_NAME);
    }

    @Override // org.fourthline.cling.model.meta.Service
    public String toString() {
        return super.toString() + ", Manager: " + this.manager;
    }
}
