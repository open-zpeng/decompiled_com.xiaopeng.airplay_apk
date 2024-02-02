package org.fourthline.cling.registry;

import java.util.List;
import org.fourthline.cling.model.ValidationError;
/* loaded from: classes.dex */
public class RegistrationException extends RuntimeException {
    public List<ValidationError> errors;

    public RegistrationException(String s) {
        super(s);
    }

    public RegistrationException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public RegistrationException(String s, List<ValidationError> errors) {
        super(s);
        this.errors = errors;
    }

    public List<ValidationError> getErrors() {
        return this.errors;
    }
}
