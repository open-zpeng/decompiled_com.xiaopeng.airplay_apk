package org.fourthline.cling.support.model.dlna.message.header;

import java.util.EnumMap;
import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.support.model.dlna.DLNAAttribute;
/* loaded from: classes.dex */
public class ContentFeaturesHeader extends DLNAHeader<EnumMap<DLNAAttribute.Type, DLNAAttribute>> {
    public ContentFeaturesHeader() {
        setValue(new EnumMap(DLNAAttribute.Type.class));
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        DLNAAttribute.Type type;
        if (s.length() != 0) {
            String[] atts = s.split(";");
            for (String att : atts) {
                String[] attNameValue = att.split("=");
                if (attNameValue.length == 2 && (type = DLNAAttribute.Type.valueOfAttributeName(attNameValue[0])) != null) {
                    DLNAAttribute dlnaAttrinute = DLNAAttribute.newInstance(type, attNameValue[1], "");
                    getValue().put((EnumMap<DLNAAttribute.Type, DLNAAttribute>) type, (DLNAAttribute.Type) dlnaAttrinute);
                }
            }
        }
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        DLNAAttribute.Type[] values;
        String s = "";
        for (DLNAAttribute.Type type : DLNAAttribute.Type.values()) {
            String value = getValue().containsKey(type) ? getValue().get(type).getString() : null;
            if (value != null && value.length() != 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(s);
                sb.append(s.length() == 0 ? "" : ";");
                sb.append(type.getAttributeName());
                sb.append("=");
                sb.append(value);
                s = sb.toString();
            }
        }
        return s;
    }
}
