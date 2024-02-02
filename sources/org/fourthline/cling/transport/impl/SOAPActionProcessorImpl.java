package org.fourthline.cling.transport.impl;

import android.util.Log;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import org.fourthline.cling.model.Constants;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.XMLUtil;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.control.ActionMessage;
import org.fourthline.cling.model.message.control.ActionRequestMessage;
import org.fourthline.cling.model.message.control.ActionResponseMessage;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.transport.spi.SOAPActionProcessor;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
/* loaded from: classes.dex */
public class SOAPActionProcessorImpl implements SOAPActionProcessor, ErrorHandler {
    private static Logger log = Logger.getLogger(SOAPActionProcessor.class.getName());
    private DocumentBuilder dBuilder;
    private final String TAG = "SOAPActionProcessorImpl";
    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    protected DocumentBuilderFactory createDocumentBuilderFactory() throws FactoryConfigurationError {
        return DocumentBuilderFactory.newInstance();
    }

    @Override // org.fourthline.cling.transport.spi.SOAPActionProcessor
    public void writeBody(ActionRequestMessage requestMessage, ActionInvocation actionInvocation) throws UnsupportedDataException {
        Logger logger = log;
        logger.fine("Writing body of " + requestMessage + " for: " + actionInvocation);
        try {
            this.factory.setNamespaceAware(true);
            Document d = this.factory.newDocumentBuilder().newDocument();
            Element body = writeBodyElement(d);
            writeBodyRequest(d, body, requestMessage, actionInvocation);
            if (log.isLoggable(Level.FINER)) {
                log.finer("===================================== SOAP BODY BEGIN ============================================");
                log.finer(requestMessage.getBodyString());
                log.finer("-===================================== SOAP BODY END ============================================");
            }
        } catch (Exception ex) {
            throw new UnsupportedDataException("Can't transform message payload: " + ex, ex);
        }
    }

    @Override // org.fourthline.cling.transport.spi.SOAPActionProcessor
    public void writeBody(ActionResponseMessage responseMessage, ActionInvocation actionInvocation) throws UnsupportedDataException {
        Log.d("SOAPActionProcessorImpl", "Writing body of " + responseMessage + " for: " + actionInvocation);
        try {
            this.factory.setNamespaceAware(true);
            if (this.dBuilder == null) {
                this.dBuilder = this.factory.newDocumentBuilder();
            }
            Document d = this.dBuilder.newDocument();
            Element body = writeBodyElement(d);
            if (actionInvocation.getFailure() != null) {
                writeBodyFailure(d, body, responseMessage, actionInvocation);
            } else {
                writeBodyResponse(d, body, responseMessage, actionInvocation);
            }
            if (log.isLoggable(Level.FINER)) {
                log.finer("===================================== SOAP BODY BEGIN ============================================");
                log.finer(responseMessage.getBodyString());
                log.finer("-===================================== SOAP BODY END ============================================");
            }
        } catch (Exception ex) {
            throw new UnsupportedDataException("Can't transform message payload: " + ex, ex);
        }
    }

    @Override // org.fourthline.cling.transport.spi.SOAPActionProcessor
    public void readBody(ActionRequestMessage requestMessage, ActionInvocation actionInvocation) throws UnsupportedDataException {
        Logger logger = log;
        logger.fine("Reading body of " + requestMessage + " for: " + actionInvocation);
        if (log.isLoggable(Level.FINER)) {
            log.finer("===================================== SOAP BODY BEGIN ============================================");
            log.finer(requestMessage.getBodyString());
            log.finer("-===================================== SOAP BODY END ============================================");
        }
        String body = getMessageBody(requestMessage);
        try {
            this.factory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = this.factory.newDocumentBuilder();
            documentBuilder.setErrorHandler(this);
            Document d = documentBuilder.parse(new InputSource(new StringReader(body)));
            Element bodyElement = readBodyElement(d);
            readBodyRequest(d, bodyElement, requestMessage, actionInvocation);
        } catch (Exception ex) {
            throw new UnsupportedDataException("Can't transform message payload: " + ex, ex, body);
        }
    }

    @Override // org.fourthline.cling.transport.spi.SOAPActionProcessor
    public void readBody(ActionResponseMessage responseMsg, ActionInvocation actionInvocation) throws UnsupportedDataException {
        Logger logger = log;
        logger.fine("Reading body of " + responseMsg + " for: " + actionInvocation);
        if (log.isLoggable(Level.FINER)) {
            log.finer("===================================== SOAP BODY BEGIN ============================================");
            log.finer(responseMsg.getBodyString());
            log.finer("-===================================== SOAP BODY END ============================================");
        }
        String body = getMessageBody(responseMsg);
        try {
            this.factory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = this.factory.newDocumentBuilder();
            documentBuilder.setErrorHandler(this);
            Document d = documentBuilder.parse(new InputSource(new StringReader(body)));
            Element bodyElement = readBodyElement(d);
            ActionException failure = readBodyFailure(d, bodyElement);
            if (failure == null) {
                readBodyResponse(d, bodyElement, responseMsg, actionInvocation);
            } else {
                actionInvocation.setFailure(failure);
            }
        } catch (Exception ex) {
            throw new UnsupportedDataException("Can't transform message payload: " + ex, ex, body);
        }
    }

    protected void writeBodyFailure(Document d, Element bodyElement, ActionResponseMessage message, ActionInvocation actionInvocation) throws Exception {
        writeFaultElement(d, bodyElement, actionInvocation);
        message.setBody(toString(d));
    }

    protected void writeBodyRequest(Document d, Element bodyElement, ActionRequestMessage message, ActionInvocation actionInvocation) throws Exception {
        Element actionRequestElement = writeActionRequestElement(d, bodyElement, message, actionInvocation);
        writeActionInputArguments(d, actionRequestElement, actionInvocation);
        message.setBody(toString(d));
    }

    protected void writeBodyResponse(Document d, Element bodyElement, ActionResponseMessage message, ActionInvocation actionInvocation) throws Exception {
        Element actionResponseElement = writeActionResponseElement(d, bodyElement, message, actionInvocation);
        writeActionOutputArguments(d, actionResponseElement, actionInvocation);
        message.setBody(toString(d));
    }

    protected ActionException readBodyFailure(Document d, Element bodyElement) throws Exception {
        return readFaultElement(bodyElement);
    }

    protected void readBodyRequest(Document d, Element bodyElement, ActionRequestMessage message, ActionInvocation actionInvocation) throws Exception {
        Element actionRequestElement = readActionRequestElement(bodyElement, message, actionInvocation);
        readActionInputArguments(actionRequestElement, actionInvocation);
    }

    protected void readBodyResponse(Document d, Element bodyElement, ActionResponseMessage message, ActionInvocation actionInvocation) throws Exception {
        Element actionResponse = readActionResponseElement(bodyElement, actionInvocation);
        readActionOutputArguments(actionResponse, actionInvocation);
    }

    protected Element writeBodyElement(Document d) {
        Element envelopeElement = d.createElementNS(Constants.SOAP_NS_ENVELOPE, "s:Envelope");
        Attr encodingStyleAttr = d.createAttributeNS(Constants.SOAP_NS_ENVELOPE, "s:encodingStyle");
        encodingStyleAttr.setValue(Constants.SOAP_URI_ENCODING_STYLE);
        envelopeElement.setAttributeNode(encodingStyleAttr);
        d.appendChild(envelopeElement);
        Element bodyElement = d.createElementNS(Constants.SOAP_NS_ENVELOPE, "s:Body");
        envelopeElement.appendChild(bodyElement);
        return bodyElement;
    }

    protected Element readBodyElement(Document d) {
        Element envelopeElement = d.getDocumentElement();
        if (envelopeElement == null || !getUnprefixedNodeName(envelopeElement).equals("Envelope")) {
            throw new RuntimeException("Response root element was not 'Envelope'");
        }
        NodeList envelopeElementChildren = envelopeElement.getChildNodes();
        for (int i = 0; i < envelopeElementChildren.getLength(); i++) {
            Node envelopeChild = envelopeElementChildren.item(i);
            if (envelopeChild.getNodeType() == 1 && getUnprefixedNodeName(envelopeChild).equals("Body")) {
                return (Element) envelopeChild;
            }
        }
        throw new RuntimeException("Response envelope did not contain 'Body' child element");
    }

    protected Element writeActionRequestElement(Document d, Element bodyElement, ActionRequestMessage message, ActionInvocation actionInvocation) {
        Logger logger = log;
        logger.fine("Writing action request element: " + actionInvocation.getAction().getName());
        String actionNamespace = message.getActionNamespace();
        Element actionRequestElement = d.createElementNS(actionNamespace, "u:" + actionInvocation.getAction().getName());
        bodyElement.appendChild(actionRequestElement);
        return actionRequestElement;
    }

    protected Element readActionRequestElement(Element bodyElement, ActionRequestMessage message, ActionInvocation actionInvocation) {
        NodeList bodyChildren = bodyElement.getChildNodes();
        Logger logger = log;
        logger.fine("Looking for action request element matching namespace:" + message.getActionNamespace());
        for (int i = 0; i < bodyChildren.getLength(); i++) {
            Node bodyChild = bodyChildren.item(i);
            if (bodyChild.getNodeType() == 1) {
                String unprefixedName = getUnprefixedNodeName(bodyChild);
                if (unprefixedName.equals(actionInvocation.getAction().getName())) {
                    if (bodyChild.getNamespaceURI() == null || !bodyChild.getNamespaceURI().equals(message.getActionNamespace())) {
                        throw new UnsupportedDataException("Illegal or missing namespace on action request element: " + bodyChild);
                    }
                    Logger logger2 = log;
                    logger2.fine("Reading action request element: " + unprefixedName);
                    return (Element) bodyChild;
                }
            }
        }
        throw new UnsupportedDataException("Could not read action request element matching namespace: " + message.getActionNamespace());
    }

    protected Element writeActionResponseElement(Document d, Element bodyElement, ActionResponseMessage message, ActionInvocation actionInvocation) {
        Logger logger = log;
        logger.fine("Writing action response element: " + actionInvocation.getAction().getName());
        String actionNamespace = message.getActionNamespace();
        Element actionResponseElement = d.createElementNS(actionNamespace, "u:" + actionInvocation.getAction().getName() + "Response");
        bodyElement.appendChild(actionResponseElement);
        return actionResponseElement;
    }

    protected Element readActionResponseElement(Element bodyElement, ActionInvocation actionInvocation) {
        NodeList bodyChildren = bodyElement.getChildNodes();
        for (int i = 0; i < bodyChildren.getLength(); i++) {
            Node bodyChild = bodyChildren.item(i);
            if (bodyChild.getNodeType() == 1) {
                String unprefixedNodeName = getUnprefixedNodeName(bodyChild);
                if (unprefixedNodeName.equals(actionInvocation.getAction().getName() + "Response")) {
                    Logger logger = log;
                    logger.fine("Reading action response element: " + getUnprefixedNodeName(bodyChild));
                    return (Element) bodyChild;
                }
            }
        }
        log.fine("Could not read action response element");
        return null;
    }

    protected void writeActionInputArguments(Document d, Element actionRequestElement, ActionInvocation actionInvocation) {
        ActionArgument[] inputArguments;
        for (ActionArgument argument : actionInvocation.getAction().getInputArguments()) {
            log.fine("Writing action input argument: " + argument.getName());
            String value = actionInvocation.getInput(argument) != null ? actionInvocation.getInput(argument).toString() : "";
            XMLUtil.appendNewElement(d, actionRequestElement, argument.getName(), value);
        }
    }

    public void readActionInputArguments(Element actionRequestElement, ActionInvocation actionInvocation) throws ActionException {
        actionInvocation.setInput(readArgumentValues(actionRequestElement.getChildNodes(), actionInvocation.getAction().getInputArguments()));
    }

    protected void writeActionOutputArguments(Document d, Element actionResponseElement, ActionInvocation actionInvocation) {
        ActionArgument[] outputArguments;
        for (ActionArgument argument : actionInvocation.getAction().getOutputArguments()) {
            log.fine("Writing action output argument: " + argument.getName());
            String value = actionInvocation.getOutput(argument) != null ? actionInvocation.getOutput(argument).toString() : "";
            XMLUtil.appendNewElement(d, actionResponseElement, argument.getName(), value);
        }
    }

    protected void readActionOutputArguments(Element actionResponseElement, ActionInvocation actionInvocation) throws ActionException {
        actionInvocation.setOutput(readArgumentValues(actionResponseElement.getChildNodes(), actionInvocation.getAction().getOutputArguments()));
    }

    protected void writeFaultElement(Document d, Element bodyElement, ActionInvocation actionInvocation) {
        Element faultElement = d.createElementNS(Constants.SOAP_NS_ENVELOPE, "s:Fault");
        bodyElement.appendChild(faultElement);
        XMLUtil.appendNewElement(d, faultElement, "faultcode", "s:Client");
        XMLUtil.appendNewElement(d, faultElement, "faultstring", "UPnPError");
        Element detailElement = d.createElement("detail");
        faultElement.appendChild(detailElement);
        Element upnpErrorElement = d.createElementNS(Constants.NS_UPNP_CONTROL_10, "UPnPError");
        detailElement.appendChild(upnpErrorElement);
        int errorCode = actionInvocation.getFailure().getErrorCode();
        String errorDescription = actionInvocation.getFailure().getMessage();
        Logger logger = log;
        logger.fine("Writing fault element: " + errorCode + " - " + errorDescription);
        XMLUtil.appendNewElement(d, upnpErrorElement, "errorCode", Integer.toString(errorCode));
        XMLUtil.appendNewElement(d, upnpErrorElement, "errorDescription", errorDescription);
    }

    protected ActionException readFaultElement(Element bodyElement) {
        NodeList bodyChildren;
        boolean receivedFaultElement;
        NodeList bodyChildren2;
        boolean receivedFaultElement2;
        NodeList bodyChildren3;
        String errorCode = null;
        NodeList bodyChildren4 = bodyElement.getChildNodes();
        String errorDescription = null;
        boolean receivedFaultElement3 = false;
        int i = 0;
        while (i < bodyChildren4.getLength()) {
            Node bodyChild = bodyChildren4.item(i);
            short s = 1;
            if (bodyChild.getNodeType() != 1) {
                bodyChildren = bodyChildren4;
            } else if (getUnprefixedNodeName(bodyChild).equals("Fault")) {
                receivedFaultElement3 = true;
                NodeList faultChildren = bodyChild.getChildNodes();
                String errorDescription2 = errorDescription;
                String errorDescription3 = errorCode;
                int j = 0;
                while (j < faultChildren.getLength()) {
                    Node faultChild = faultChildren.item(j);
                    if (faultChild.getNodeType() != s) {
                        receivedFaultElement = receivedFaultElement3;
                        bodyChildren2 = bodyChildren4;
                    } else if (getUnprefixedNodeName(faultChild).equals("detail")) {
                        NodeList detailChildren = faultChild.getChildNodes();
                        String errorDescription4 = errorDescription2;
                        String errorDescription5 = errorDescription3;
                        int x = 0;
                        while (x < detailChildren.getLength()) {
                            Node detailChild = detailChildren.item(x);
                            if (detailChild.getNodeType() != s) {
                                receivedFaultElement2 = receivedFaultElement3;
                                bodyChildren3 = bodyChildren4;
                            } else if (getUnprefixedNodeName(detailChild).equals("UPnPError")) {
                                NodeList errorChildren = detailChild.getChildNodes();
                                String errorDescription6 = errorDescription4;
                                String errorCode2 = errorDescription5;
                                int y = 0;
                                while (y < errorChildren.getLength()) {
                                    Node errorChild = errorChildren.item(y);
                                    boolean receivedFaultElement4 = receivedFaultElement3;
                                    NodeList bodyChildren5 = bodyChildren4;
                                    if (errorChild.getNodeType() == 1) {
                                        if (getUnprefixedNodeName(errorChild).equals("errorCode")) {
                                            errorCode2 = XMLUtil.getTextContent(errorChild);
                                        }
                                        if (getUnprefixedNodeName(errorChild).equals("errorDescription")) {
                                            String errorDescription7 = XMLUtil.getTextContent(errorChild);
                                            errorDescription6 = errorDescription7;
                                        }
                                    }
                                    y++;
                                    receivedFaultElement3 = receivedFaultElement4;
                                    bodyChildren4 = bodyChildren5;
                                }
                                receivedFaultElement2 = receivedFaultElement3;
                                bodyChildren3 = bodyChildren4;
                                errorDescription5 = errorCode2;
                                errorDescription4 = errorDescription6;
                            } else {
                                receivedFaultElement2 = receivedFaultElement3;
                                bodyChildren3 = bodyChildren4;
                            }
                            x++;
                            receivedFaultElement3 = receivedFaultElement2;
                            bodyChildren4 = bodyChildren3;
                            s = 1;
                        }
                        receivedFaultElement = receivedFaultElement3;
                        bodyChildren2 = bodyChildren4;
                        errorDescription3 = errorDescription5;
                        errorDescription2 = errorDescription4;
                    } else {
                        receivedFaultElement = receivedFaultElement3;
                        bodyChildren2 = bodyChildren4;
                    }
                    j++;
                    receivedFaultElement3 = receivedFaultElement;
                    bodyChildren4 = bodyChildren2;
                    s = 1;
                }
                bodyChildren = bodyChildren4;
                errorCode = errorDescription3;
                errorDescription = errorDescription2;
            } else {
                bodyChildren = bodyChildren4;
            }
            i++;
            bodyChildren4 = bodyChildren;
        }
        if (errorCode != null) {
            try {
                int numericCode = Integer.valueOf(errorCode).intValue();
                ErrorCode standardErrorCode = ErrorCode.getByCode(numericCode);
                if (standardErrorCode != null) {
                    log.fine("Reading fault element: " + standardErrorCode.getCode() + " - " + errorDescription);
                    return new ActionException(standardErrorCode, errorDescription, false);
                }
                log.fine("Reading fault element: " + numericCode + " - " + errorDescription);
                return new ActionException(numericCode, errorDescription);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Error code was not a number");
            }
        } else if (receivedFaultElement3) {
            throw new RuntimeException("Received fault element but no error code");
        } else {
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String getMessageBody(ActionMessage message) throws UnsupportedDataException {
        if (!message.isBodyNonEmptyString()) {
            throw new UnsupportedDataException("Can't transform null or non-string/zero-length body of: " + message);
        }
        return message.getBodyString().trim();
    }

    protected String toString(Document d) throws Exception {
        String output = XMLUtil.documentToString(d);
        while (true) {
            if (output.endsWith("\n") || output.endsWith("\r")) {
                output = output.substring(0, output.length() - 1);
            } else {
                return output;
            }
        }
    }

    protected String getUnprefixedNodeName(Node node) {
        if (node.getPrefix() != null) {
            return node.getNodeName().substring(node.getPrefix().length() + 1);
        }
        return node.getNodeName();
    }

    protected ActionArgumentValue[] readArgumentValues(NodeList nodeList, ActionArgument[] args) throws ActionException {
        List<Node> nodes = getMatchingNodes(nodeList, args);
        ActionArgumentValue[] values = new ActionArgumentValue[args.length];
        for (int i = 0; i < args.length; i++) {
            ActionArgument arg = args[i];
            Node node = findActionArgumentNode(nodes, arg);
            if (node == null) {
                ErrorCode errorCode = ErrorCode.ARGUMENT_VALUE_INVALID;
                throw new ActionException(errorCode, "Could not find argument '" + arg.getName() + "' node");
            }
            Logger logger = log;
            logger.fine("Reading action argument: " + arg.getName());
            String value = XMLUtil.getTextContent(node);
            values[i] = createValue(arg, value);
        }
        return values;
    }

    protected List<Node> getMatchingNodes(NodeList nodeList, ActionArgument[] args) throws ActionException {
        List<String> names = new ArrayList<>();
        for (ActionArgument argument : args) {
            names.add(argument.getName());
            names.addAll(Arrays.asList(argument.getAliases()));
        }
        List<Node> matches = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child.getNodeType() == 1 && names.contains(getUnprefixedNodeName(child))) {
                matches.add(child);
            }
        }
        int i2 = matches.size();
        if (i2 < args.length) {
            throw new ActionException(ErrorCode.ARGUMENT_VALUE_INVALID, "Invalid number of input or output arguments in XML message, expected " + args.length + " but found " + matches.size());
        }
        return matches;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public ActionArgumentValue createValue(ActionArgument arg, String value) throws ActionException {
        try {
            return new ActionArgumentValue(arg, value);
        } catch (InvalidValueException ex) {
            ErrorCode errorCode = ErrorCode.ARGUMENT_VALUE_INVALID;
            throw new ActionException(errorCode, "Wrong type or invalid value for '" + arg.getName() + "': " + ex.getMessage(), ex);
        }
    }

    protected Node findActionArgumentNode(List<Node> nodes, ActionArgument arg) {
        for (Node node : nodes) {
            if (arg.isNameOrAlias(getUnprefixedNodeName(node))) {
                return node;
            }
        }
        return null;
    }

    @Override // org.xml.sax.ErrorHandler
    public void warning(SAXParseException e) throws SAXException {
        log.warning(e.toString());
    }

    @Override // org.xml.sax.ErrorHandler
    public void error(SAXParseException e) throws SAXException {
        throw e;
    }

    @Override // org.xml.sax.ErrorHandler
    public void fatalError(SAXParseException e) throws SAXException {
        throw e;
    }
}
