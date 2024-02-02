package org.fourthline.cling.support.contentdirectory;
/* loaded from: classes.dex */
public enum ContentDirectoryErrorCode {
    NO_SUCH_OBJECT(701, "The specified ObjectID is invalid"),
    UNSUPPORTED_SORT_CRITERIA(709, "Unsupported or invalid sort criteria"),
    CANNOT_PROCESS(720, "Cannot process the request");
    
    private int code;
    private String description;

    ContentDirectoryErrorCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public static ContentDirectoryErrorCode getByCode(int code) {
        ContentDirectoryErrorCode[] values;
        for (ContentDirectoryErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }
}
