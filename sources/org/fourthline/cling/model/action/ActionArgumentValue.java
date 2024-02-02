package org.fourthline.cling.model.action;

import org.fourthline.cling.model.VariableValue;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.InvalidValueException;
/* loaded from: classes.dex */
public class ActionArgumentValue<S extends Service> extends VariableValue {
    private final ActionArgument<S> argument;

    public ActionArgumentValue(ActionArgument<S> argument, Object value) throws InvalidValueException {
        super(argument.getDatatype(), (value == null || !value.getClass().isEnum()) ? value : value.toString());
        this.argument = argument;
    }

    public ActionArgument<S> getArgument() {
        return this.argument;
    }
}
