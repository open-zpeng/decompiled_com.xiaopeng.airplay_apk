package org.fourthline.cling.support.model.dlna;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Locale;
/* loaded from: classes.dex */
public class DLNAOperationsAttribute extends DLNAAttribute<EnumSet<DLNAOperations>> {
    public DLNAOperationsAttribute() {
        setValue(EnumSet.of(DLNAOperations.NONE));
    }

    public DLNAOperationsAttribute(DLNAOperations... op) {
        if (op != null && op.length > 0) {
            DLNAOperations first = op[0];
            if (op.length > 1) {
                System.arraycopy(op, 1, op, 0, op.length - 1);
                setValue(EnumSet.of(first, op));
                return;
            }
            setValue(EnumSet.of(first));
        }
    }

    @Override // org.fourthline.cling.support.model.dlna.DLNAAttribute
    public void setString(String s, String cf) throws InvalidDLNAProtocolAttributeException {
        DLNAOperations[] values;
        EnumSet<DLNAOperations> value = EnumSet.noneOf(DLNAOperations.class);
        try {
            int parseInt = Integer.parseInt(s, 16);
            for (DLNAOperations op : DLNAOperations.values()) {
                int code = op.getCode() & parseInt;
                if (op != DLNAOperations.NONE && op.getCode() == code) {
                    value.add(op);
                }
            }
        } catch (NumberFormatException e) {
        }
        if (value.isEmpty()) {
            throw new InvalidDLNAProtocolAttributeException("Can't parse DLNA operations integer from: " + s);
        }
        setValue(value);
    }

    @Override // org.fourthline.cling.support.model.dlna.DLNAAttribute
    public String getString() {
        int code = DLNAOperations.NONE.getCode();
        Iterator it = getValue().iterator();
        while (it.hasNext()) {
            DLNAOperations op = (DLNAOperations) it.next();
            code |= op.getCode();
        }
        return String.format(Locale.ROOT, "%02x", Integer.valueOf(code));
    }
}
