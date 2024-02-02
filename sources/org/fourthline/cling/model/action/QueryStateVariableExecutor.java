package org.fourthline.cling.model.action;

import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.QueryStateVariableAction;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.state.StateVariableAccessor;
import org.fourthline.cling.model.types.ErrorCode;
/* loaded from: classes.dex */
public class QueryStateVariableExecutor extends AbstractActionExecutor {
    @Override // org.fourthline.cling.model.action.AbstractActionExecutor
    protected void execute(ActionInvocation<LocalService> actionInvocation, Object serviceImpl) throws Exception {
        if (actionInvocation.getAction() instanceof QueryStateVariableAction) {
            if (!actionInvocation.getAction().getService().isSupportsQueryStateVariables()) {
                actionInvocation.setFailure(new ActionException(ErrorCode.INVALID_ACTION, "This service does not support querying state variables"));
                return;
            } else {
                executeQueryStateVariable(actionInvocation, serviceImpl);
                return;
            }
        }
        throw new IllegalStateException("This class can only execute QueryStateVariableAction's, not: " + actionInvocation.getAction());
    }

    protected void executeQueryStateVariable(ActionInvocation<LocalService> actionInvocation, Object serviceImpl) throws Exception {
        LocalService service = actionInvocation.getAction().getService();
        String stateVariableName = actionInvocation.getInput(QueryStateVariableAction.INPUT_ARG_VAR_NAME).toString();
        StateVariable stateVariable = service.getStateVariable(stateVariableName);
        if (stateVariable == null) {
            ErrorCode errorCode = ErrorCode.ARGUMENT_VALUE_INVALID;
            throw new ActionException(errorCode, "No state variable found: " + stateVariableName);
        }
        StateVariableAccessor accessor = service.getAccessor(stateVariable.getName());
        if (accessor == null) {
            ErrorCode errorCode2 = ErrorCode.ARGUMENT_VALUE_INVALID;
            throw new ActionException(errorCode2, "No accessor for state variable, can't read state: " + stateVariableName);
        }
        try {
            setOutputArgumentValue(actionInvocation, actionInvocation.getAction().getOutputArgument(QueryStateVariableAction.OUTPUT_ARG_RETURN), accessor.read(stateVariable, serviceImpl).toString());
        } catch (Exception ex) {
            throw new ActionException(ErrorCode.ACTION_FAILED, ex.getMessage());
        }
    }
}
