package org.fourthline.cling.transport.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Alternative;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.message.gena.IncomingEventRequestMessage;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.transport.spi.GENAEventProcessor;
import org.seamless.xml.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
@Alternative
/* loaded from: classes.dex */
public class PullGENAEventProcessorImpl extends GENAEventProcessorImpl {
    private static Logger log = Logger.getLogger(GENAEventProcessor.class.getName());

    @Override // org.fourthline.cling.transport.impl.GENAEventProcessorImpl, org.fourthline.cling.transport.spi.GENAEventProcessor
    public void readBody(IncomingEventRequestMessage requestMessage) throws UnsupportedDataException {
        Logger logger = log;
        logger.fine("Reading body of: " + requestMessage);
        if (log.isLoggable(Level.FINER)) {
            log.finer("===================================== GENA BODY BEGIN ============================================");
            log.finer(requestMessage.getBody() != null ? requestMessage.getBody().toString() : null);
            log.finer("-===================================== GENA BODY END ============================================");
        }
        String body = getMessageBody(requestMessage);
        try {
            XmlPullParser xpp = XmlPullParserUtils.createParser(body);
            readProperties(xpp, requestMessage);
        } catch (Exception ex) {
            throw new UnsupportedDataException("Can't transform message payload: " + ex.getMessage(), ex, body);
        }
    }

    protected void readProperties(XmlPullParser xpp, IncomingEventRequestMessage message) throws Exception {
        StateVariable[] stateVariables = message.getService().getStateVariables();
        while (true) {
            int event = xpp.next();
            if (event != 1) {
                if (event == 2 && xpp.getName().equals("property")) {
                    readProperty(xpp, message, stateVariables);
                }
            } else {
                return;
            }
        }
    }

    protected void readProperty(XmlPullParser xpp, IncomingEventRequestMessage message, StateVariable[] stateVariables) throws Exception {
        while (true) {
            int event = xpp.next();
            if (event == 2) {
                String stateVariableName = xpp.getName();
                int length = stateVariables.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    StateVariable stateVariable = stateVariables[i];
                    if (!stateVariable.getName().equals(stateVariableName)) {
                        i++;
                    } else {
                        Logger logger = log;
                        logger.fine("Reading state variable value: " + stateVariableName);
                        String value = xpp.nextText();
                        message.getStateVariableValues().add(new StateVariableValue(stateVariable, value));
                        break;
                    }
                }
            }
            if (event == 1) {
                return;
            }
            if (event == 3 && xpp.getName().equals("property")) {
                return;
            }
        }
    }
}
