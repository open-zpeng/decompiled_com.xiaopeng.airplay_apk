package org.fourthline.cling.model;
/* loaded from: classes.dex */
public class ValidationError {
    private Class clazz;
    private String message;
    private String propertyName;

    public ValidationError(Class clazz, String message) {
        this.clazz = clazz;
        this.message = message;
    }

    public ValidationError(Class clazz, String propertyName, String message) {
        this.clazz = clazz;
        this.propertyName = propertyName;
        this.message = message;
    }

    public Class getClazz() {
        return this.clazz;
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public String getMessage() {
        return this.message;
    }

    public String toString() {
        return getClass().getSimpleName() + " (Class: " + getClazz().getSimpleName() + ", propertyName: " + getPropertyName() + "): " + this.message;
    }
}
