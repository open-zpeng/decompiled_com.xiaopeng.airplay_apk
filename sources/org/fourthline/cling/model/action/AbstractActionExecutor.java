package org.fourthline.cling.model.action;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.Command;
import org.fourthline.cling.model.ServiceManager;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.state.StateVariableAccessor;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.InvalidValueException;
import org.seamless.util.Exceptions;
/* loaded from: classes.dex */
public abstract class AbstractActionExecutor implements ActionExecutor {
    private static Logger log = Logger.getLogger(AbstractActionExecutor.class.getName());
    protected Map<ActionArgument<LocalService>, StateVariableAccessor> outputArgumentAccessors;

    protected abstract void execute(ActionInvocation<LocalService> actionInvocation, Object obj) throws Exception;

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractActionExecutor() {
        this.outputArgumentAccessors = new HashMap();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractActionExecutor(Map<ActionArgument<LocalService>, StateVariableAccessor> outputArgumentAccessors) {
        this.outputArgumentAccessors = new HashMap();
        this.outputArgumentAccessors = outputArgumentAccessors;
    }

    public Map<ActionArgument<LocalService>, StateVariableAccessor> getOutputArgumentAccessors() {
        return this.outputArgumentAccessors;
    }

    @Override // org.fourthline.cling.model.action.ActionExecutor
    public void execute(final ActionInvocation<LocalService> actionInvocation) {
        Logger logger = log;
        logger.fine("Invoking on local service: " + actionInvocation);
        LocalService service = actionInvocation.getAction().getService();
        try {
            if (service.getManager() == null) {
                throw new IllegalStateException("Service has no implementation factory, can't get service instance");
            }
            service.getManager().execute(new Command() { // from class: org.fourthline.cling.model.action.AbstractActionExecutor.1
                @Override // org.fourthline.cling.model.Command
                public void execute(ServiceManager serviceManager) throws Exception {
                    AbstractActionExecutor.this.execute(actionInvocation, serviceManager.getImplementation());
                }

                public String toString() {
                    return "Action invocation: " + actionInvocation.getAction();
                }
            });
        } catch (InterruptedException ex) {
            if (log.isLoggable(Level.FINE)) {
                Logger logger2 = log;
                logger2.fine("InterruptedException thrown by service, wrapping in invocation and returning: " + ex);
                log.log(Level.FINE, "Exception root cause: ", Exceptions.unwrap(ex));
            }
            actionInvocation.setFailure(new ActionCancelledException(ex));
        } catch (ActionException ex2) {
            if (log.isLoggable(Level.FINE)) {
                Logger logger3 = log;
                logger3.fine("ActionException thrown by service, wrapping in invocation and returning: " + ex2);
                log.log(Level.FINE, "Exception root cause: ", Exceptions.unwrap(ex2));
            }
            actionInvocation.setFailure(ex2);
        } catch (Throwable t) {
            Throwable rootCause = Exceptions.unwrap(t);
            if (log.isLoggable(Level.FINE)) {
                Logger logger4 = log;
                logger4.fine("Execution has thrown, wrapping root cause in ActionException and returning: " + t);
                log.log(Level.FINE, "Exception root cause: ", rootCause);
            }
            actionInvocation.setFailure(new ActionException(ErrorCode.ACTION_FAILED, rootCause.getMessage() != null ? rootCause.getMessage() : rootCause.toString(), rootCause));
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Object readOutputArgumentValues(Action<LocalService> action, Object instance) throws Exception {
        Object[] results = new Object[action.getOutputArguments().length];
        log.fine("Attempting to retrieve output argument values using accessor: " + results.length);
        ActionArgument[] outputArguments = action.getOutputArguments();
        int length = outputArguments.length;
        int i = 0;
        int i2 = 0;
        while (i2 < length) {
            ActionArgument outputArgument = outputArguments[i2];
            log.finer("Calling acccessor method for: " + outputArgument);
            StateVariableAccessor accessor = getOutputArgumentAccessors().get(outputArgument);
            if (accessor != null) {
                log.fine("Calling accessor to read output argument value: " + accessor);
                results[i] = accessor.read(instance);
                i2++;
                i++;
            } else {
                throw new IllegalStateException("No accessor bound for: " + outputArgument);
            }
        }
        if (results.length == 1) {
            return results[0];
        }
        if (results.length > 0) {
            return results;
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setOutputArgumentValue(ActionInvocation<LocalService> actionInvocation, ActionArgument<LocalService> argument, Object result) throws ActionException {
        LocalService service = actionInvocation.getAction().getService();
        if (result != null) {
            try {
                if (service.isStringConvertibleType(result)) {
                    log.fine("Result of invocation matches convertible type, setting toString() single output argument value");
                    actionInvocation.setOutput(new ActionArgumentValue<>(argument, result.toString()));
                } else {
                    log.fine("Result of invocation is Object, setting single output argument value");
                    actionInvocation.setOutput(new ActionArgumentValue<>(argument, result));
                }
                return;
            } catch (InvalidValueException ex) {
                ErrorCode errorCode = ErrorCode.ARGUMENT_VALUE_INVALID;
                throw new ActionException(errorCode, "Wrong type or invalid value for '" + argument.getName() + "': " + ex.getMessage(), ex);
            }
        }
        log.fine("Result of invocation is null, not setting any output argument value(s)");
    }
}
