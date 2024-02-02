package org.fourthline.cling.transport.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import javax.enterprise.inject.Alternative;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.control.ActionRequestMessage;
import org.fourthline.cling.model.message.control.ActionResponseMessage;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.transport.spi.SOAPActionProcessor;
import org.seamless.xml.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
@Alternative
/* loaded from: classes.dex */
public class PullSOAPActionProcessorImpl extends SOAPActionProcessorImpl {
    protected static Logger log = Logger.getLogger(SOAPActionProcessor.class.getName());

    @Override // org.fourthline.cling.transport.impl.SOAPActionProcessorImpl, org.fourthline.cling.transport.spi.SOAPActionProcessor
    public void readBody(ActionRequestMessage requestMessage, ActionInvocation actionInvocation) throws UnsupportedDataException {
        String body = getMessageBody(requestMessage);
        try {
            XmlPullParser xpp = XmlPullParserUtils.createParser(body);
            readBodyRequest(xpp, requestMessage, actionInvocation);
        } catch (Exception ex) {
            throw new UnsupportedDataException("Can't transform message payload: " + ex, ex, body);
        }
    }

    @Override // org.fourthline.cling.transport.impl.SOAPActionProcessorImpl, org.fourthline.cling.transport.spi.SOAPActionProcessor
    public void readBody(ActionResponseMessage responseMsg, ActionInvocation actionInvocation) throws UnsupportedDataException {
        String body = getMessageBody(responseMsg);
        try {
            XmlPullParser xpp = XmlPullParserUtils.createParser(body);
            readBodyElement(xpp);
            readBodyResponse(xpp, actionInvocation);
        } catch (Exception ex) {
            throw new UnsupportedDataException("Can't transform message payload: " + ex, ex, body);
        }
    }

    protected void readBodyElement(XmlPullParser xpp) throws Exception {
        XmlPullParserUtils.searchTag(xpp, "Body");
    }

    protected void readBodyRequest(XmlPullParser xpp, ActionRequestMessage requestMessage, ActionInvocation actionInvocation) throws Exception {
        XmlPullParserUtils.searchTag(xpp, actionInvocation.getAction().getName());
        readActionInputArguments(xpp, actionInvocation);
    }

    /* JADX WARN: Code restructure failed: missing block: B:19:0x0055, code lost:
        r3 = org.fourthline.cling.model.types.ErrorCode.ACTION_FAILED;
     */
    /* JADX WARN: Code restructure failed: missing block: B:20:0x0080, code lost:
        throw new org.fourthline.cling.model.action.ActionException(r3, java.lang.String.format("Action SOAP response do not contain %s element", r9.getAction().getName() + "Response"));
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    protected void readBodyResponse(org.xmlpull.v1.XmlPullParser r8, org.fourthline.cling.model.action.ActionInvocation r9) throws java.lang.Exception {
        /*
            r7 = this;
        L0:
            int r0 = r8.next()
            r1 = 2
            if (r0 != r1) goto L42
            java.lang.String r1 = r8.getName()
            java.lang.String r2 = "Fault"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L1b
            org.fourthline.cling.model.action.ActionException r1 = r7.readFaultElement(r8)
            r9.setFailure(r1)
            return
        L1b:
            java.lang.String r1 = r8.getName()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            org.fourthline.cling.model.meta.Action r3 = r9.getAction()
            java.lang.String r3 = r3.getName()
            r2.append(r3)
            java.lang.String r3 = "Response"
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L42
            r7.readActionOutputArguments(r8, r9)
            return
        L42:
            r1 = 1
            if (r0 == r1) goto L55
            r2 = 3
            if (r0 != r2) goto L0
            java.lang.String r2 = r8.getName()
            java.lang.String r3 = "Body"
            boolean r2 = r2.equals(r3)
            if (r2 != 0) goto L55
            goto L0
        L55:
            org.fourthline.cling.model.action.ActionException r2 = new org.fourthline.cling.model.action.ActionException
            org.fourthline.cling.model.types.ErrorCode r3 = org.fourthline.cling.model.types.ErrorCode.ACTION_FAILED
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r4 = 0
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            org.fourthline.cling.model.meta.Action r6 = r9.getAction()
            java.lang.String r6 = r6.getName()
            r5.append(r6)
            java.lang.String r6 = "Response"
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            r1[r4] = r5
            java.lang.String r4 = "Action SOAP response do not contain %s element"
            java.lang.String r1 = java.lang.String.format(r4, r1)
            r2.<init>(r3, r1)
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: org.fourthline.cling.transport.impl.PullSOAPActionProcessorImpl.readBodyResponse(org.xmlpull.v1.XmlPullParser, org.fourthline.cling.model.action.ActionInvocation):void");
    }

    protected void readActionInputArguments(XmlPullParser xpp, ActionInvocation actionInvocation) throws Exception {
        actionInvocation.setInput(readArgumentValues(xpp, actionInvocation.getAction().getInputArguments()));
    }

    protected void readActionOutputArguments(XmlPullParser xpp, ActionInvocation actionInvocation) throws Exception {
        actionInvocation.setOutput(readArgumentValues(xpp, actionInvocation.getAction().getOutputArguments()));
    }

    protected Map<String, String> getMatchingNodes(XmlPullParser xpp, ActionArgument[] args) throws Exception {
        List<String> names = new ArrayList<>();
        for (ActionArgument argument : args) {
            names.add(argument.getName().toUpperCase(Locale.ROOT));
            for (String alias : Arrays.asList(argument.getAliases())) {
                names.add(alias.toUpperCase(Locale.ROOT));
            }
        }
        Map<String, String> matches = new HashMap<>();
        String enclosingTag = xpp.getName();
        while (true) {
            int event = xpp.next();
            if (event == 2 && names.contains(xpp.getName().toUpperCase(Locale.ROOT))) {
                matches.put(xpp.getName(), xpp.nextText());
            }
            if (event == 1 || (event == 3 && xpp.getName().equals(enclosingTag))) {
                break;
            }
        }
        if (matches.size() < args.length) {
            throw new ActionException(ErrorCode.ARGUMENT_VALUE_INVALID, "Invalid number of input or output arguments in XML message, expected " + args.length + " but found " + matches.size());
        }
        return matches;
    }

    protected ActionArgumentValue[] readArgumentValues(XmlPullParser xpp, ActionArgument[] args) throws Exception {
        Map<String, String> matches = getMatchingNodes(xpp, args);
        ActionArgumentValue[] values = new ActionArgumentValue[args.length];
        for (int i = 0; i < args.length; i++) {
            ActionArgument arg = args[i];
            String value = findActionArgumentValue(matches, arg);
            if (value == null) {
                ErrorCode errorCode = ErrorCode.ARGUMENT_VALUE_INVALID;
                throw new ActionException(errorCode, "Could not find argument '" + arg.getName() + "' node");
            }
            Logger logger = log;
            logger.fine("Reading action argument: " + arg.getName());
            values[i] = createValue(arg, value);
        }
        return values;
    }

    protected String findActionArgumentValue(Map<String, String> entries, ActionArgument arg) {
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            if (arg.isNameOrAlias(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    protected ActionException readFaultElement(XmlPullParser xpp) throws Exception {
        String errorCode = null;
        String errorDescription = null;
        XmlPullParserUtils.searchTag(xpp, "UPnPError");
        while (true) {
            int event = xpp.next();
            if (event == 2) {
                String tag = xpp.getName();
                if (tag.equals("errorCode")) {
                    errorCode = xpp.nextText();
                } else if (tag.equals("errorDescription")) {
                    errorDescription = xpp.nextText();
                }
            }
            if (event == 1 || (event == 3 && xpp.getName().equals("UPnPError"))) {
                break;
            }
        }
        if (errorCode != null) {
            try {
                int numericCode = Integer.valueOf(errorCode).intValue();
                ErrorCode standardErrorCode = ErrorCode.getByCode(numericCode);
                if (standardErrorCode != null) {
                    Logger logger = log;
                    logger.fine("Reading fault element: " + standardErrorCode.getCode() + " - " + errorDescription);
                    return new ActionException(standardErrorCode, errorDescription, false);
                }
                Logger logger2 = log;
                logger2.fine("Reading fault element: " + numericCode + " - " + errorDescription);
                return new ActionException(numericCode, errorDescription);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Error code was not a number");
            }
        }
        throw new RuntimeException("Received fault element but no error code");
    }
}
