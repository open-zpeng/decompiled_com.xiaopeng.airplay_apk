package org.fourthline.cling.support.avtransport.lastchange;

import java.util.Set;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.lastchange.LastChangeParser;
/* loaded from: classes.dex */
public class AVTransportLastChangeParser extends LastChangeParser {
    public static final String NAMESPACE_URI = "urn:schemas-upnp-org:metadata-1-0/AVT/";
    public static final String SCHEMA_RESOURCE = "org/fourthline/cling/support/avtransport/metadata-1.01-avt.xsd";

    @Override // org.fourthline.cling.support.lastchange.LastChangeParser
    protected String getNamespace() {
        return NAMESPACE_URI;
    }

    @Override // org.seamless.xml.SAXParser
    protected Source[] getSchemaSources() {
        if (ModelUtil.ANDROID_RUNTIME) {
            return null;
        }
        return new Source[]{new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream(SCHEMA_RESOURCE))};
    }

    @Override // org.fourthline.cling.support.lastchange.LastChangeParser
    protected Set<Class<? extends EventedValue>> getEventedVariables() {
        return AVTransportVariable.ALL;
    }
}
