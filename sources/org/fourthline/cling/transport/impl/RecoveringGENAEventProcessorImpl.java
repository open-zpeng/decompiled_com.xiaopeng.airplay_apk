package org.fourthline.cling.transport.impl;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.inject.Alternative;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.XMLUtil;
import org.fourthline.cling.model.message.gena.IncomingEventRequestMessage;
import org.fourthline.cling.transport.spi.GENAEventProcessor;
import org.seamless.xml.XmlPullParserUtils;
@Alternative
/* loaded from: classes.dex */
public class RecoveringGENAEventProcessorImpl extends PullGENAEventProcessorImpl {
    private static Logger log = Logger.getLogger(GENAEventProcessor.class.getName());

    @Override // org.fourthline.cling.transport.impl.PullGENAEventProcessorImpl, org.fourthline.cling.transport.impl.GENAEventProcessorImpl, org.fourthline.cling.transport.spi.GENAEventProcessor
    public void readBody(IncomingEventRequestMessage requestMessage) throws UnsupportedDataException {
        try {
            super.readBody(requestMessage);
        } catch (UnsupportedDataException ex) {
            if (!requestMessage.isBodyNonEmptyString()) {
                throw ex;
            }
            Logger logger = log;
            logger.warning("Trying to recover from invalid GENA XML event: " + ex);
            requestMessage.getStateVariableValues().clear();
            String body = getMessageBody(requestMessage);
            String fixedBody = fixXMLEncodedLastChange(XmlPullParserUtils.fixXMLEntities(body));
            try {
                requestMessage.setBody(fixedBody);
                super.readBody(requestMessage);
            } catch (UnsupportedDataException e) {
                if (requestMessage.getStateVariableValues().isEmpty()) {
                    throw ex;
                }
                log.warning("Partial read of GENA event properties (probably due to truncated XML)");
            }
        }
    }

    protected String fixXMLEncodedLastChange(String xml) {
        Pattern pattern = Pattern.compile("<LastChange>(.*)</LastChange>", 32);
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find() && matcher.groupCount() == 1) {
            String lastChange = matcher.group(1);
            if (XmlPullParserUtils.isNullOrEmpty(lastChange)) {
                return xml;
            }
            String lastChange2 = lastChange.trim();
            String fixedLastChange = lastChange2;
            if (lastChange2.charAt(0) == '<') {
                fixedLastChange = XMLUtil.encodeText(fixedLastChange);
            }
            if (fixedLastChange.equals(lastChange2)) {
                return xml;
            }
            return "<?xml version=\"1.0\" encoding=\"utf-8\"?><e:propertyset xmlns:e=\"urn:schemas-upnp-org:event-1-0\"><e:property><LastChange>" + fixedLastChange + "</LastChange></e:property></e:propertyset>";
        }
        return xml;
    }
}
