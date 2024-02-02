package org.fourthline.cling.binding.xml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.fourthline.cling.binding.staging.MutableAction;
import org.fourthline.cling.binding.staging.MutableActionArgument;
import org.fourthline.cling.binding.staging.MutableAllowedValueRange;
import org.fourthline.cling.binding.staging.MutableService;
import org.fourthline.cling.binding.staging.MutableStateVariable;
import org.fourthline.cling.binding.xml.Descriptor;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.XMLUtil;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.QueryStateVariableAction;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.meta.StateVariableEventDetails;
import org.fourthline.cling.model.types.CustomDatatype;
import org.fourthline.cling.model.types.Datatype;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
/* loaded from: classes.dex */
public class UDA10ServiceDescriptorBinderImpl implements ServiceDescriptorBinder, ErrorHandler {
    private static Logger log = Logger.getLogger(ServiceDescriptorBinder.class.getName());

    @Override // org.fourthline.cling.binding.xml.ServiceDescriptorBinder
    public <S extends Service> S describe(S undescribedService, String descriptorXml) throws DescriptorBindingException, ValidationException {
        if (descriptorXml == null || descriptorXml.length() == 0) {
            throw new DescriptorBindingException("Null or empty descriptor");
        }
        try {
            Logger logger = log;
            logger.fine("Populating service from XML descriptor: " + undescribedService);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            documentBuilder.setErrorHandler(this);
            Document d = documentBuilder.parse(new InputSource(new StringReader(descriptorXml.trim())));
            return (S) describe((UDA10ServiceDescriptorBinderImpl) undescribedService, d);
        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex2) {
            throw new DescriptorBindingException("Could not parse service descriptor: " + ex2.toString(), ex2);
        }
    }

    @Override // org.fourthline.cling.binding.xml.ServiceDescriptorBinder
    public <S extends Service> S describe(S undescribedService, Document dom) throws DescriptorBindingException, ValidationException {
        try {
            Logger logger = log;
            logger.fine("Populating service from DOM: " + undescribedService);
            MutableService descriptor = new MutableService();
            hydrateBasic(descriptor, undescribedService);
            Element rootElement = dom.getDocumentElement();
            hydrateRoot(descriptor, rootElement);
            return (S) buildInstance(undescribedService, descriptor);
        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex2) {
            throw new DescriptorBindingException("Could not parse service DOM: " + ex2.toString(), ex2);
        }
    }

    protected <S extends Service> S buildInstance(S undescribedService, MutableService descriptor) throws ValidationException {
        return (S) descriptor.build(undescribedService.getDevice());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void hydrateBasic(MutableService descriptor, Service undescribedService) {
        descriptor.serviceId = undescribedService.getServiceId();
        descriptor.serviceType = undescribedService.getServiceType();
        if (undescribedService instanceof RemoteService) {
            RemoteService rs = (RemoteService) undescribedService;
            descriptor.controlURI = rs.getControlURI();
            descriptor.eventSubscriptionURI = rs.getEventSubscriptionURI();
            descriptor.descriptorURI = rs.getDescriptorURI();
        }
    }

    protected void hydrateRoot(MutableService descriptor, Element rootElement) throws DescriptorBindingException {
        if (!Descriptor.Service.ELEMENT.scpd.equals((Node) rootElement)) {
            throw new DescriptorBindingException("Root element name is not <scpd>: " + rootElement.getNodeName());
        }
        NodeList rootChildren = rootElement.getChildNodes();
        for (int i = 0; i < rootChildren.getLength(); i++) {
            Node rootChild = rootChildren.item(i);
            if (rootChild.getNodeType() == 1 && !Descriptor.Service.ELEMENT.specVersion.equals(rootChild)) {
                if (Descriptor.Service.ELEMENT.actionList.equals(rootChild)) {
                    hydrateActionList(descriptor, rootChild);
                } else if (Descriptor.Service.ELEMENT.serviceStateTable.equals(rootChild)) {
                    hydrateServiceStateTableList(descriptor, rootChild);
                } else {
                    Logger logger = log;
                    logger.finer("Ignoring unknown element: " + rootChild.getNodeName());
                }
            }
        }
    }

    public void hydrateActionList(MutableService descriptor, Node actionListNode) throws DescriptorBindingException {
        NodeList actionListChildren = actionListNode.getChildNodes();
        for (int i = 0; i < actionListChildren.getLength(); i++) {
            Node actionListChild = actionListChildren.item(i);
            if (actionListChild.getNodeType() == 1 && Descriptor.Service.ELEMENT.action.equals(actionListChild)) {
                MutableAction action = new MutableAction();
                hydrateAction(action, actionListChild);
                descriptor.actions.add(action);
            }
        }
    }

    public void hydrateAction(MutableAction action, Node actionNode) {
        NodeList actionNodeChildren = actionNode.getChildNodes();
        for (int i = 0; i < actionNodeChildren.getLength(); i++) {
            Node actionNodeChild = actionNodeChildren.item(i);
            if (actionNodeChild.getNodeType() == 1) {
                if (Descriptor.Service.ELEMENT.name.equals(actionNodeChild)) {
                    action.name = XMLUtil.getTextContent(actionNodeChild);
                } else if (Descriptor.Service.ELEMENT.argumentList.equals(actionNodeChild)) {
                    NodeList argumentChildren = actionNodeChild.getChildNodes();
                    for (int j = 0; j < argumentChildren.getLength(); j++) {
                        Node argumentChild = argumentChildren.item(j);
                        if (argumentChild.getNodeType() == 1) {
                            MutableActionArgument actionArgument = new MutableActionArgument();
                            hydrateActionArgument(actionArgument, argumentChild);
                            action.arguments.add(actionArgument);
                        }
                    }
                }
            }
        }
    }

    public void hydrateActionArgument(MutableActionArgument actionArgument, Node actionArgumentNode) {
        NodeList argumentNodeChildren = actionArgumentNode.getChildNodes();
        for (int i = 0; i < argumentNodeChildren.getLength(); i++) {
            Node argumentNodeChild = argumentNodeChildren.item(i);
            if (argumentNodeChild.getNodeType() == 1) {
                if (Descriptor.Service.ELEMENT.name.equals(argumentNodeChild)) {
                    actionArgument.name = XMLUtil.getTextContent(argumentNodeChild);
                } else if (Descriptor.Service.ELEMENT.direction.equals(argumentNodeChild)) {
                    String directionString = XMLUtil.getTextContent(argumentNodeChild);
                    try {
                        actionArgument.direction = ActionArgument.Direction.valueOf(directionString.toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException e) {
                        Logger logger = log;
                        logger.warning("UPnP specification violation: Invalid action argument direction, assuming 'IN': " + directionString);
                        actionArgument.direction = ActionArgument.Direction.IN;
                    }
                } else if (Descriptor.Service.ELEMENT.relatedStateVariable.equals(argumentNodeChild)) {
                    actionArgument.relatedStateVariable = XMLUtil.getTextContent(argumentNodeChild);
                } else if (Descriptor.Service.ELEMENT.retval.equals(argumentNodeChild)) {
                    actionArgument.retval = true;
                }
            }
        }
    }

    public void hydrateServiceStateTableList(MutableService descriptor, Node serviceStateTableNode) {
        NodeList serviceStateTableChildren = serviceStateTableNode.getChildNodes();
        for (int i = 0; i < serviceStateTableChildren.getLength(); i++) {
            Node serviceStateTableChild = serviceStateTableChildren.item(i);
            if (serviceStateTableChild.getNodeType() == 1 && Descriptor.Service.ELEMENT.stateVariable.equals(serviceStateTableChild)) {
                MutableStateVariable stateVariable = new MutableStateVariable();
                hydrateStateVariable(stateVariable, (Element) serviceStateTableChild);
                descriptor.stateVariables.add(stateVariable);
            }
        }
    }

    public void hydrateStateVariable(MutableStateVariable stateVariable, Element stateVariableElement) {
        stateVariable.eventDetails = new StateVariableEventDetails(stateVariableElement.getAttribute("sendEvents") != null && stateVariableElement.getAttribute(Descriptor.Service.ATTRIBUTE.sendEvents.toString()).toUpperCase(Locale.ROOT).equals("YES"));
        NodeList stateVariableChildren = stateVariableElement.getChildNodes();
        for (int i = 0; i < stateVariableChildren.getLength(); i++) {
            Node stateVariableChild = stateVariableChildren.item(i);
            if (stateVariableChild.getNodeType() == 1) {
                if (Descriptor.Service.ELEMENT.name.equals(stateVariableChild)) {
                    stateVariable.name = XMLUtil.getTextContent(stateVariableChild);
                } else if (Descriptor.Service.ELEMENT.dataType.equals(stateVariableChild)) {
                    String dtName = XMLUtil.getTextContent(stateVariableChild);
                    Datatype.Builtin builtin = Datatype.Builtin.getByDescriptorName(dtName);
                    stateVariable.dataType = builtin != null ? builtin.getDatatype() : new CustomDatatype(dtName);
                } else if (Descriptor.Service.ELEMENT.defaultValue.equals(stateVariableChild)) {
                    stateVariable.defaultValue = XMLUtil.getTextContent(stateVariableChild);
                } else if (Descriptor.Service.ELEMENT.allowedValueList.equals(stateVariableChild)) {
                    List<String> allowedValues = new ArrayList<>();
                    NodeList allowedValueListChildren = stateVariableChild.getChildNodes();
                    for (int j = 0; j < allowedValueListChildren.getLength(); j++) {
                        Node allowedValueListChild = allowedValueListChildren.item(j);
                        if (allowedValueListChild.getNodeType() == 1 && Descriptor.Service.ELEMENT.allowedValue.equals(allowedValueListChild)) {
                            allowedValues.add(XMLUtil.getTextContent(allowedValueListChild));
                        }
                    }
                    stateVariable.allowedValues = allowedValues;
                } else if (Descriptor.Service.ELEMENT.allowedValueRange.equals(stateVariableChild)) {
                    MutableAllowedValueRange range = new MutableAllowedValueRange();
                    NodeList allowedValueRangeChildren = stateVariableChild.getChildNodes();
                    for (int j2 = 0; j2 < allowedValueRangeChildren.getLength(); j2++) {
                        Node allowedValueRangeChild = allowedValueRangeChildren.item(j2);
                        if (allowedValueRangeChild.getNodeType() == 1) {
                            if (Descriptor.Service.ELEMENT.minimum.equals(allowedValueRangeChild)) {
                                try {
                                    range.minimum = Long.valueOf(XMLUtil.getTextContent(allowedValueRangeChild));
                                } catch (Exception e) {
                                }
                            } else if (Descriptor.Service.ELEMENT.maximum.equals(allowedValueRangeChild)) {
                                try {
                                    range.maximum = Long.valueOf(XMLUtil.getTextContent(allowedValueRangeChild));
                                } catch (Exception e2) {
                                }
                            } else if (Descriptor.Service.ELEMENT.step.equals(allowedValueRangeChild)) {
                                try {
                                    range.step = Long.valueOf(XMLUtil.getTextContent(allowedValueRangeChild));
                                } catch (Exception e3) {
                                }
                            }
                        }
                    }
                    stateVariable.allowedValueRange = range;
                }
            }
        }
    }

    @Override // org.fourthline.cling.binding.xml.ServiceDescriptorBinder
    public String generate(Service service) throws DescriptorBindingException {
        try {
            Logger logger = log;
            logger.fine("Generating XML descriptor from service model: " + service);
            return XMLUtil.documentToString(buildDOM(service));
        } catch (Exception ex) {
            throw new DescriptorBindingException("Could not build DOM: " + ex.getMessage(), ex);
        }
    }

    @Override // org.fourthline.cling.binding.xml.ServiceDescriptorBinder
    public Document buildDOM(Service service) throws DescriptorBindingException {
        try {
            Logger logger = log;
            logger.fine("Generating XML descriptor from service model: " + service);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document d = factory.newDocumentBuilder().newDocument();
            generateScpd(service, d);
            return d;
        } catch (Exception ex) {
            throw new DescriptorBindingException("Could not generate service descriptor: " + ex.getMessage(), ex);
        }
    }

    private void generateScpd(Service serviceModel, Document descriptor) {
        Element scpdElement = descriptor.createElementNS(Descriptor.Service.NAMESPACE_URI, Descriptor.Service.ELEMENT.scpd.toString());
        descriptor.appendChild(scpdElement);
        generateSpecVersion(serviceModel, descriptor, scpdElement);
        if (serviceModel.hasActions()) {
            generateActionList(serviceModel, descriptor, scpdElement);
        }
        generateServiceStateTable(serviceModel, descriptor, scpdElement);
    }

    private void generateSpecVersion(Service serviceModel, Document descriptor, Element rootElement) {
        Element specVersionElement = XMLUtil.appendNewElement(descriptor, rootElement, Descriptor.Service.ELEMENT.specVersion);
        XMLUtil.appendNewElementIfNotNull(descriptor, specVersionElement, Descriptor.Service.ELEMENT.major, Integer.valueOf(serviceModel.getDevice().getVersion().getMajor()));
        XMLUtil.appendNewElementIfNotNull(descriptor, specVersionElement, Descriptor.Service.ELEMENT.minor, Integer.valueOf(serviceModel.getDevice().getVersion().getMinor()));
    }

    private void generateActionList(Service serviceModel, Document descriptor, Element scpdElement) {
        Action[] actions;
        Element actionListElement = XMLUtil.appendNewElement(descriptor, scpdElement, Descriptor.Service.ELEMENT.actionList);
        for (Action action : serviceModel.getActions()) {
            if (!action.getName().equals(QueryStateVariableAction.ACTION_NAME)) {
                generateAction(action, descriptor, actionListElement);
            }
        }
    }

    private void generateAction(Action action, Document descriptor, Element actionListElement) {
        ActionArgument[] arguments;
        Element actionElement = XMLUtil.appendNewElement(descriptor, actionListElement, Descriptor.Service.ELEMENT.action);
        XMLUtil.appendNewElementIfNotNull(descriptor, actionElement, Descriptor.Service.ELEMENT.name, action.getName());
        if (action.hasArguments()) {
            Element argumentListElement = XMLUtil.appendNewElement(descriptor, actionElement, Descriptor.Service.ELEMENT.argumentList);
            for (ActionArgument actionArgument : action.getArguments()) {
                generateActionArgument(actionArgument, descriptor, argumentListElement);
            }
        }
    }

    private void generateActionArgument(ActionArgument actionArgument, Document descriptor, Element actionElement) {
        Element actionArgumentElement = XMLUtil.appendNewElement(descriptor, actionElement, Descriptor.Service.ELEMENT.argument);
        XMLUtil.appendNewElementIfNotNull(descriptor, actionArgumentElement, Descriptor.Service.ELEMENT.name, actionArgument.getName());
        XMLUtil.appendNewElementIfNotNull(descriptor, actionArgumentElement, Descriptor.Service.ELEMENT.direction, actionArgument.getDirection().toString().toLowerCase(Locale.ROOT));
        actionArgument.isReturnValue();
        XMLUtil.appendNewElementIfNotNull(descriptor, actionArgumentElement, Descriptor.Service.ELEMENT.relatedStateVariable, actionArgument.getRelatedStateVariableName());
    }

    private void generateServiceStateTable(Service serviceModel, Document descriptor, Element scpdElement) {
        StateVariable[] stateVariables;
        Element serviceStateTableElement = XMLUtil.appendNewElement(descriptor, scpdElement, Descriptor.Service.ELEMENT.serviceStateTable);
        for (StateVariable stateVariable : serviceModel.getStateVariables()) {
            generateStateVariable(stateVariable, descriptor, serviceStateTableElement);
        }
    }

    private void generateStateVariable(StateVariable stateVariable, Document descriptor, Element serviveStateTableElement) {
        String[] allowedValues;
        Element stateVariableElement = XMLUtil.appendNewElement(descriptor, serviveStateTableElement, Descriptor.Service.ELEMENT.stateVariable);
        XMLUtil.appendNewElementIfNotNull(descriptor, stateVariableElement, Descriptor.Service.ELEMENT.name, stateVariable.getName());
        if (stateVariable.getTypeDetails().getDatatype() instanceof CustomDatatype) {
            XMLUtil.appendNewElementIfNotNull(descriptor, stateVariableElement, Descriptor.Service.ELEMENT.dataType, ((CustomDatatype) stateVariable.getTypeDetails().getDatatype()).getName());
        } else {
            XMLUtil.appendNewElementIfNotNull(descriptor, stateVariableElement, Descriptor.Service.ELEMENT.dataType, stateVariable.getTypeDetails().getDatatype().getBuiltin().getDescriptorName());
        }
        XMLUtil.appendNewElementIfNotNull(descriptor, stateVariableElement, Descriptor.Service.ELEMENT.defaultValue, stateVariable.getTypeDetails().getDefaultValue());
        if (stateVariable.getEventDetails().isSendEvents()) {
            stateVariableElement.setAttribute(Descriptor.Service.ATTRIBUTE.sendEvents.toString(), "yes");
        } else {
            stateVariableElement.setAttribute(Descriptor.Service.ATTRIBUTE.sendEvents.toString(), "no");
        }
        if (stateVariable.getTypeDetails().getAllowedValues() != null) {
            Element allowedValueListElement = XMLUtil.appendNewElement(descriptor, stateVariableElement, Descriptor.Service.ELEMENT.allowedValueList);
            for (String allowedValue : stateVariable.getTypeDetails().getAllowedValues()) {
                XMLUtil.appendNewElementIfNotNull(descriptor, allowedValueListElement, Descriptor.Service.ELEMENT.allowedValue, allowedValue);
            }
        }
        if (stateVariable.getTypeDetails().getAllowedValueRange() != null) {
            Element allowedValueRangeElement = XMLUtil.appendNewElement(descriptor, stateVariableElement, Descriptor.Service.ELEMENT.allowedValueRange);
            XMLUtil.appendNewElementIfNotNull(descriptor, allowedValueRangeElement, Descriptor.Service.ELEMENT.minimum, Long.valueOf(stateVariable.getTypeDetails().getAllowedValueRange().getMinimum()));
            XMLUtil.appendNewElementIfNotNull(descriptor, allowedValueRangeElement, Descriptor.Service.ELEMENT.maximum, Long.valueOf(stateVariable.getTypeDetails().getAllowedValueRange().getMaximum()));
            if (stateVariable.getTypeDetails().getAllowedValueRange().getStep() >= 1) {
                XMLUtil.appendNewElementIfNotNull(descriptor, allowedValueRangeElement, Descriptor.Service.ELEMENT.step, Long.valueOf(stateVariable.getTypeDetails().getAllowedValueRange().getStep()));
            }
        }
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
