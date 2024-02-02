package org.fourthline.cling.support.model.dlna;

import java.util.EnumMap;
import java.util.Map;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.support.model.Protocol;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.dlna.DLNAAttribute;
import org.seamless.util.MimeType;
/* loaded from: classes.dex */
public class DLNAProtocolInfo extends ProtocolInfo {
    protected final Map<DLNAAttribute.Type, DLNAAttribute> attributes;

    public DLNAProtocolInfo(String s) throws InvalidValueException {
        super(s);
        this.attributes = new EnumMap(DLNAAttribute.Type.class);
        parseAdditionalInfo();
    }

    public DLNAProtocolInfo(MimeType contentFormatMimeType) {
        super(contentFormatMimeType);
        this.attributes = new EnumMap(DLNAAttribute.Type.class);
    }

    public DLNAProtocolInfo(DLNAProfiles profile) {
        super(MimeType.valueOf(profile.getContentFormat()));
        this.attributes = new EnumMap(DLNAAttribute.Type.class);
        this.attributes.put(DLNAAttribute.Type.DLNA_ORG_PN, new DLNAProfileAttribute(profile));
        this.additionalInfo = getAttributesString();
    }

    public DLNAProtocolInfo(DLNAProfiles profile, EnumMap<DLNAAttribute.Type, DLNAAttribute> attributes) {
        super(MimeType.valueOf(profile.getContentFormat()));
        this.attributes = new EnumMap(DLNAAttribute.Type.class);
        this.attributes.putAll(attributes);
        this.attributes.put(DLNAAttribute.Type.DLNA_ORG_PN, new DLNAProfileAttribute(profile));
        this.additionalInfo = getAttributesString();
    }

    public DLNAProtocolInfo(Protocol protocol, String network, String contentFormat, String additionalInfo) {
        super(protocol, network, contentFormat, additionalInfo);
        this.attributes = new EnumMap(DLNAAttribute.Type.class);
        parseAdditionalInfo();
    }

    public DLNAProtocolInfo(Protocol protocol, String network, String contentFormat, EnumMap<DLNAAttribute.Type, DLNAAttribute> attributes) {
        super(protocol, network, contentFormat, "");
        this.attributes = new EnumMap(DLNAAttribute.Type.class);
        this.attributes.putAll(attributes);
        this.additionalInfo = getAttributesString();
    }

    public DLNAProtocolInfo(ProtocolInfo template) {
        this(template.getProtocol(), template.getNetwork(), template.getContentFormat(), template.getAdditionalInfo());
    }

    public boolean contains(DLNAAttribute.Type type) {
        return this.attributes.containsKey(type);
    }

    public DLNAAttribute getAttribute(DLNAAttribute.Type type) {
        return this.attributes.get(type);
    }

    public Map<DLNAAttribute.Type, DLNAAttribute> getAttributes() {
        return this.attributes;
    }

    protected String getAttributesString() {
        DLNAAttribute.Type[] values;
        String s = "";
        for (DLNAAttribute.Type type : DLNAAttribute.Type.values()) {
            String value = this.attributes.containsKey(type) ? this.attributes.get(type).getString() : null;
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

    protected void parseAdditionalInfo() {
        DLNAAttribute.Type type;
        if (this.additionalInfo != null) {
            String[] atts = this.additionalInfo.split(";");
            for (String att : atts) {
                String[] attNameValue = att.split("=");
                if (attNameValue.length == 2 && (type = DLNAAttribute.Type.valueOfAttributeName(attNameValue[0])) != null) {
                    DLNAAttribute dlnaAttrinute = DLNAAttribute.newInstance(type, attNameValue[1], getContentFormat());
                    this.attributes.put(type, dlnaAttrinute);
                }
            }
        }
    }
}
