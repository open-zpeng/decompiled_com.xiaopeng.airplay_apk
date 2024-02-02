package org.fourthline.cling.model;
/* loaded from: classes.dex */
public class UnsupportedDataException extends RuntimeException {
    private static final long serialVersionUID = 661795454401413339L;
    protected Object data;

    public UnsupportedDataException(String s) {
        super(s);
    }

    public UnsupportedDataException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public UnsupportedDataException(String s, Throwable throwable, Object data) {
        super(s, throwable);
        this.data = data;
    }

    public Object getData() {
        return this.data;
    }
}
