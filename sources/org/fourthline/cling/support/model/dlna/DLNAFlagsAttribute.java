package org.fourthline.cling.support.model.dlna;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Locale;
/* loaded from: classes.dex */
public class DLNAFlagsAttribute extends DLNAAttribute<EnumSet<DLNAFlags>> {
    public DLNAFlagsAttribute() {
        setValue(EnumSet.noneOf(DLNAFlags.class));
    }

    public DLNAFlagsAttribute(DLNAFlags... flags) {
        if (flags != null && flags.length > 0) {
            DLNAFlags first = flags[0];
            if (flags.length > 1) {
                System.arraycopy(flags, 1, flags, 0, flags.length - 1);
                setValue(EnumSet.of(first, flags));
                return;
            }
            setValue(EnumSet.of(first));
        }
    }

    @Override // org.fourthline.cling.support.model.dlna.DLNAAttribute
    public void setString(String s, String cf) throws InvalidDLNAProtocolAttributeException {
        DLNAFlags[] values;
        EnumSet<DLNAFlags> value = EnumSet.noneOf(DLNAFlags.class);
        try {
            int parseInt = Integer.parseInt(s.substring(0, s.length() - 24), 16);
            for (DLNAFlags op : DLNAFlags.values()) {
                int code = op.getCode() & parseInt;
                if (op.getCode() == code) {
                    value.add(op);
                }
            }
        } catch (Exception e) {
        }
        if (value.isEmpty()) {
            throw new InvalidDLNAProtocolAttributeException("Can't parse DLNA flags integer from: " + s);
        }
        setValue(value);
    }

    @Override // org.fourthline.cling.support.model.dlna.DLNAAttribute
    public String getString() {
        int code = 0;
        Iterator it = getValue().iterator();
        while (it.hasNext()) {
            DLNAFlags op = (DLNAFlags) it.next();
            code |= op.getCode();
        }
        return String.format(Locale.ROOT, "%08x%024x", Integer.valueOf(code), 0);
    }
}
