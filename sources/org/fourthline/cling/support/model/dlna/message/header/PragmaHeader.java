package org.fourthline.cling.support.model.dlna.message.header;

import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.model.types.PragmaType;
/* loaded from: classes.dex */
public class PragmaHeader extends DLNAHeader<List<PragmaType>> {
    public PragmaHeader() {
        setValue(new ArrayList());
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (s.length() != 0) {
            if (s.endsWith(";")) {
                s = s.substring(0, s.length() - 1);
            }
            String[] list = s.split("\\s*;\\s*");
            List<PragmaType> value = new ArrayList<>();
            for (String pragma : list) {
                value.add(PragmaType.valueOf(pragma));
            }
            return;
        }
        throw new InvalidHeaderException("Invalid Pragma header value: " + s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        List<PragmaType> v = getValue();
        String r = "";
        for (PragmaType pragma : v) {
            StringBuilder sb = new StringBuilder();
            sb.append(r);
            sb.append(r.length() == 0 ? "" : ",");
            sb.append(pragma.getString());
            r = sb.toString();
        }
        return r;
    }
}
