package org.fourthline.cling.support.model.dlna.types;
/* loaded from: classes.dex */
public class CodedDataBuffer {
    private Long size;
    private TransferMechanism tranfer;

    /* loaded from: classes.dex */
    public enum TransferMechanism {
        IMMEDIATELY,
        TIMESTAMP,
        OTHER
    }

    public CodedDataBuffer(Long size, TransferMechanism transfer) {
        this.size = size;
        this.tranfer = transfer;
    }

    public Long getSize() {
        return this.size;
    }

    public TransferMechanism getTranfer() {
        return this.tranfer;
    }
}
