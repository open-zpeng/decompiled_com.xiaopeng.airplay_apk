package org.fourthline.cling.model.action;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.profile.ClientInfo;
import org.fourthline.cling.model.types.InvalidValueException;
/* loaded from: classes.dex */
public class ActionInvocation<S extends Service> {
    protected final Action<S> action;
    protected final ClientInfo clientInfo;
    protected ActionException failure;
    protected Map<String, ActionArgumentValue<S>> input;
    protected Map<String, ActionArgumentValue<S>> output;

    public ActionInvocation(Action<S> action) {
        this(action, null, null, null);
    }

    public ActionInvocation(Action<S> action, ClientInfo clientInfo) {
        this(action, null, null, clientInfo);
    }

    public ActionInvocation(Action<S> action, ActionArgumentValue<S>[] input) {
        this(action, input, null, null);
    }

    public ActionInvocation(Action<S> action, ActionArgumentValue<S>[] input, ClientInfo clientInfo) {
        this(action, input, null, clientInfo);
    }

    public ActionInvocation(Action<S> action, ActionArgumentValue<S>[] input, ActionArgumentValue<S>[] output) {
        this(action, input, output, null);
    }

    public ActionInvocation(Action<S> action, ActionArgumentValue<S>[] input, ActionArgumentValue<S>[] output, ClientInfo clientInfo) {
        this.input = new LinkedHashMap();
        this.output = new LinkedHashMap();
        this.failure = null;
        if (action == null) {
            throw new IllegalArgumentException("Action can not be null");
        }
        this.action = action;
        setInput(input);
        setOutput(output);
        this.clientInfo = clientInfo;
    }

    public ActionInvocation(ActionException failure) {
        this.input = new LinkedHashMap();
        this.output = new LinkedHashMap();
        this.failure = null;
        this.action = null;
        this.input = null;
        this.output = null;
        this.failure = failure;
        this.clientInfo = null;
    }

    public Action<S> getAction() {
        return this.action;
    }

    public ActionArgumentValue<S>[] getInput() {
        return (ActionArgumentValue[]) this.input.values().toArray(new ActionArgumentValue[this.input.size()]);
    }

    public ActionArgumentValue<S> getInput(String argumentName) {
        return getInput(getInputArgument(argumentName));
    }

    public ActionArgumentValue<S> getInput(ActionArgument<S> argument) {
        return this.input.get(argument.getName());
    }

    public Map<String, ActionArgumentValue<S>> getInputMap() {
        return Collections.unmodifiableMap(this.input);
    }

    public ActionArgumentValue<S>[] getOutput() {
        return (ActionArgumentValue[]) this.output.values().toArray(new ActionArgumentValue[this.output.size()]);
    }

    public ActionArgumentValue<S> getOutput(String argumentName) {
        return getOutput(getOutputArgument(argumentName));
    }

    public Map<String, ActionArgumentValue<S>> getOutputMap() {
        return Collections.unmodifiableMap(this.output);
    }

    public ActionArgumentValue<S> getOutput(ActionArgument<S> argument) {
        return this.output.get(argument.getName());
    }

    public void setInput(String argumentName, Object value) throws InvalidValueException {
        setInput(new ActionArgumentValue<>(getInputArgument(argumentName), value));
    }

    public void setInput(ActionArgumentValue<S> value) {
        this.input.put(value.getArgument().getName(), value);
    }

    public void setInput(ActionArgumentValue<S>[] input) {
        if (input == null) {
            return;
        }
        for (ActionArgumentValue<S> argumentValue : input) {
            this.input.put(argumentValue.getArgument().getName(), argumentValue);
        }
    }

    public void setOutput(String argumentName, Object value) throws InvalidValueException {
        setOutput(new ActionArgumentValue<>(getOutputArgument(argumentName), value));
    }

    public void setOutput(ActionArgumentValue<S> value) {
        this.output.put(value.getArgument().getName(), value);
    }

    public void setOutput(ActionArgumentValue<S>[] output) {
        if (output == null) {
            return;
        }
        for (ActionArgumentValue<S> argumentValue : output) {
            this.output.put(argumentValue.getArgument().getName(), argumentValue);
        }
    }

    protected ActionArgument<S> getInputArgument(String name) {
        ActionArgument<S> argument = getAction().getInputArgument(name);
        if (argument == null) {
            throw new IllegalArgumentException("Argument not found: " + name);
        }
        return argument;
    }

    protected ActionArgument<S> getOutputArgument(String name) {
        ActionArgument<S> argument = getAction().getOutputArgument(name);
        if (argument == null) {
            throw new IllegalArgumentException("Argument not found: " + name);
        }
        return argument;
    }

    public ActionException getFailure() {
        return this.failure;
    }

    public void setFailure(ActionException failure) {
        this.failure = failure;
    }

    public ClientInfo getClientInfo() {
        return this.clientInfo;
    }

    public String toString() {
        return "(" + getClass().getSimpleName() + ") " + getAction();
    }
}
