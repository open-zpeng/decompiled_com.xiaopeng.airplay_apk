package org.fourthline.cling.model;

import java.util.List;
/* loaded from: classes.dex */
public class ValidationException extends Exception {
    public List<ValidationError> errors;

    public ValidationException(String s) {
        super(s);
    }

    public ValidationException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ValidationException(String s, List<ValidationError> errors) {
        super(s);
        this.errors = errors;
    }

    public List<ValidationError> getErrors() {
        return this.errors;
    }
}
