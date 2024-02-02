package org.fourthline.cling.binding.xml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import org.fourthline.cling.binding.staging.MutableAction;
import org.fourthline.cling.binding.staging.MutableActionArgument;
import org.fourthline.cling.binding.staging.MutableAllowedValueRange;
import org.fourthline.cling.binding.staging.MutableService;
import org.fourthline.cling.binding.staging.MutableStateVariable;
import org.fourthline.cling.binding.xml.Descriptor;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.meta.StateVariableEventDetails;
import org.fourthline.cling.model.types.CustomDatatype;
import org.fourthline.cling.model.types.Datatype;
import org.seamless.xml.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/* loaded from: classes.dex */
public class UDA10ServiceDescriptorBinderSAXImpl extends UDA10ServiceDescriptorBinderImpl {
    private static Logger log = Logger.getLogger(ServiceDescriptorBinder.class.getName());

    @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderImpl, org.fourthline.cling.binding.xml.ServiceDescriptorBinder
    public <S extends Service> S describe(S undescribedService, String descriptorXml) throws DescriptorBindingException, ValidationException {
        if (descriptorXml == null || descriptorXml.length() == 0) {
            throw new DescriptorBindingException("Null or empty descriptor");
        }
        try {
            log.fine("Reading service from XML descriptor");
            SAXParser parser = new SAXParser();
            MutableService descriptor = new MutableService();
            hydrateBasic(descriptor, undescribedService);
            new RootHandler(descriptor, parser);
            parser.parse(new InputSource(new StringReader(descriptorXml.trim())));
            return (S) descriptor.build(undescribedService.getDevice());
        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex2) {
            throw new DescriptorBindingException("Could not parse service descriptor: " + ex2.toString(), ex2);
        }
    }

    /* loaded from: classes.dex */
    protected static class RootHandler extends ServiceDescriptorHandler<MutableService> {
        public RootHandler(MutableService instance, SAXParser parser) {
            super(instance, parser);
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public void startElement(Descriptor.Service.ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(ActionListHandler.EL)) {
                List<MutableAction> actions = new ArrayList<>();
                getInstance().actions = actions;
                new ActionListHandler(actions, this);
            }
            if (element.equals(StateVariableListHandler.EL)) {
                List<MutableStateVariable> stateVariables = new ArrayList<>();
                getInstance().stateVariables = stateVariables;
                new StateVariableListHandler(stateVariables, this);
            }
        }
    }

    /* loaded from: classes.dex */
    protected static class ActionListHandler extends ServiceDescriptorHandler<List<MutableAction>> {
        public static final Descriptor.Service.ELEMENT EL = Descriptor.Service.ELEMENT.actionList;

        public ActionListHandler(List<MutableAction> instance, ServiceDescriptorHandler parent) {
            super(instance, parent);
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public void startElement(Descriptor.Service.ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(ActionHandler.EL)) {
                MutableAction action = new MutableAction();
                getInstance().add(action);
                new ActionHandler(action, this);
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public boolean isLastElement(Descriptor.Service.ELEMENT element) {
            return element.equals(EL);
        }
    }

    /* loaded from: classes.dex */
    protected static class ActionHandler extends ServiceDescriptorHandler<MutableAction> {
        public static final Descriptor.Service.ELEMENT EL = Descriptor.Service.ELEMENT.action;

        public ActionHandler(MutableAction instance, ServiceDescriptorHandler parent) {
            super(instance, parent);
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public void startElement(Descriptor.Service.ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(ActionArgumentListHandler.EL)) {
                List<MutableActionArgument> arguments = new ArrayList<>();
                getInstance().arguments = arguments;
                new ActionArgumentListHandler(arguments, this);
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public void endElement(Descriptor.Service.ELEMENT element) throws SAXException {
            if (AnonymousClass1.$SwitchMap$org$fourthline$cling$binding$xml$Descriptor$Service$ELEMENT[element.ordinal()] == 1) {
                getInstance().name = getCharacters();
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public boolean isLastElement(Descriptor.Service.ELEMENT element) {
            return element.equals(EL);
        }
    }

    /* loaded from: classes.dex */
    protected static class ActionArgumentListHandler extends ServiceDescriptorHandler<List<MutableActionArgument>> {
        public static final Descriptor.Service.ELEMENT EL = Descriptor.Service.ELEMENT.argumentList;

        public ActionArgumentListHandler(List<MutableActionArgument> instance, ServiceDescriptorHandler parent) {
            super(instance, parent);
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public void startElement(Descriptor.Service.ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(ActionArgumentHandler.EL)) {
                MutableActionArgument argument = new MutableActionArgument();
                getInstance().add(argument);
                new ActionArgumentHandler(argument, this);
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public boolean isLastElement(Descriptor.Service.ELEMENT element) {
            return element.equals(EL);
        }
    }

    /* loaded from: classes.dex */
    protected static class ActionArgumentHandler extends ServiceDescriptorHandler<MutableActionArgument> {
        public static final Descriptor.Service.ELEMENT EL = Descriptor.Service.ELEMENT.argument;

        public ActionArgumentHandler(MutableActionArgument instance, ServiceDescriptorHandler parent) {
            super(instance, parent);
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public void endElement(Descriptor.Service.ELEMENT element) throws SAXException {
            switch (element) {
                case name:
                    getInstance().name = getCharacters();
                    return;
                case direction:
                    String directionString = getCharacters();
                    try {
                        getInstance().direction = ActionArgument.Direction.valueOf(directionString.toUpperCase(Locale.ROOT));
                        return;
                    } catch (IllegalArgumentException e) {
                        Logger logger = UDA10ServiceDescriptorBinderSAXImpl.log;
                        logger.warning("UPnP specification violation: Invalid action argument direction, assuming 'IN': " + directionString);
                        getInstance().direction = ActionArgument.Direction.IN;
                        return;
                    }
                case relatedStateVariable:
                    getInstance().relatedStateVariable = getCharacters();
                    return;
                case retval:
                    getInstance().retval = true;
                    return;
                default:
                    return;
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public boolean isLastElement(Descriptor.Service.ELEMENT element) {
            return element.equals(EL);
        }
    }

    /* loaded from: classes.dex */
    protected static class StateVariableListHandler extends ServiceDescriptorHandler<List<MutableStateVariable>> {
        public static final Descriptor.Service.ELEMENT EL = Descriptor.Service.ELEMENT.serviceStateTable;

        public StateVariableListHandler(List<MutableStateVariable> instance, ServiceDescriptorHandler parent) {
            super(instance, parent);
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public void startElement(Descriptor.Service.ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(StateVariableHandler.EL)) {
                MutableStateVariable stateVariable = new MutableStateVariable();
                String sendEventsAttributeValue = attributes.getValue(Descriptor.Service.ATTRIBUTE.sendEvents.toString());
                stateVariable.eventDetails = new StateVariableEventDetails(sendEventsAttributeValue != null && sendEventsAttributeValue.toUpperCase(Locale.ROOT).equals("YES"));
                getInstance().add(stateVariable);
                new StateVariableHandler(stateVariable, this);
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public boolean isLastElement(Descriptor.Service.ELEMENT element) {
            return element.equals(EL);
        }
    }

    /* loaded from: classes.dex */
    protected static class StateVariableHandler extends ServiceDescriptorHandler<MutableStateVariable> {
        public static final Descriptor.Service.ELEMENT EL = Descriptor.Service.ELEMENT.stateVariable;

        public StateVariableHandler(MutableStateVariable instance, ServiceDescriptorHandler parent) {
            super(instance, parent);
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public void startElement(Descriptor.Service.ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(AllowedValueListHandler.EL)) {
                List<String> allowedValues = new ArrayList<>();
                getInstance().allowedValues = allowedValues;
                new AllowedValueListHandler(allowedValues, this);
            }
            if (element.equals(AllowedValueRangeHandler.EL)) {
                MutableAllowedValueRange allowedValueRange = new MutableAllowedValueRange();
                getInstance().allowedValueRange = allowedValueRange;
                new AllowedValueRangeHandler(allowedValueRange, this);
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public void endElement(Descriptor.Service.ELEMENT element) throws SAXException {
            int i = AnonymousClass1.$SwitchMap$org$fourthline$cling$binding$xml$Descriptor$Service$ELEMENT[element.ordinal()];
            if (i == 1) {
                getInstance().name = getCharacters();
                return;
            }
            switch (i) {
                case 5:
                    String dtName = getCharacters();
                    Datatype.Builtin builtin = Datatype.Builtin.getByDescriptorName(dtName);
                    getInstance().dataType = builtin != null ? builtin.getDatatype() : new CustomDatatype(dtName);
                    return;
                case 6:
                    getInstance().defaultValue = getCharacters();
                    return;
                default:
                    return;
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public boolean isLastElement(Descriptor.Service.ELEMENT element) {
            return element.equals(EL);
        }
    }

    /* loaded from: classes.dex */
    protected static class AllowedValueListHandler extends ServiceDescriptorHandler<List<String>> {
        public static final Descriptor.Service.ELEMENT EL = Descriptor.Service.ELEMENT.allowedValueList;

        public AllowedValueListHandler(List<String> instance, ServiceDescriptorHandler parent) {
            super(instance, parent);
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public void endElement(Descriptor.Service.ELEMENT element) throws SAXException {
            if (AnonymousClass1.$SwitchMap$org$fourthline$cling$binding$xml$Descriptor$Service$ELEMENT[element.ordinal()] == 7) {
                getInstance().add(getCharacters());
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public boolean isLastElement(Descriptor.Service.ELEMENT element) {
            return element.equals(EL);
        }
    }

    /* loaded from: classes.dex */
    protected static class AllowedValueRangeHandler extends ServiceDescriptorHandler<MutableAllowedValueRange> {
        public static final Descriptor.Service.ELEMENT EL = Descriptor.Service.ELEMENT.allowedValueRange;

        public AllowedValueRangeHandler(MutableAllowedValueRange instance, ServiceDescriptorHandler parent) {
            super(instance, parent);
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public void endElement(Descriptor.Service.ELEMENT element) throws SAXException {
            try {
                switch (element) {
                    case minimum:
                        getInstance().minimum = Long.valueOf(getCharacters());
                        break;
                    case maximum:
                        getInstance().maximum = Long.valueOf(getCharacters());
                        break;
                    case step:
                        getInstance().step = Long.valueOf(getCharacters());
                        break;
                }
            } catch (Exception e) {
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler
        public boolean isLastElement(Descriptor.Service.ELEMENT element) {
            return element.equals(EL);
        }
    }

    /* loaded from: classes.dex */
    protected static class ServiceDescriptorHandler<I> extends SAXParser.Handler<I> {
        public ServiceDescriptorHandler(I instance) {
            super(instance);
        }

        public ServiceDescriptorHandler(I instance, SAXParser parser) {
            super(instance, parser);
        }

        public ServiceDescriptorHandler(I instance, ServiceDescriptorHandler parent) {
            super(instance, parent);
        }

        public ServiceDescriptorHandler(I instance, SAXParser parser, ServiceDescriptorHandler parent) {
            super(instance, parser, parent);
        }

        @Override // org.seamless.xml.SAXParser.Handler, org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            Descriptor.Service.ELEMENT el = Descriptor.Service.ELEMENT.valueOrNullOf(localName);
            if (el == null) {
                return;
            }
            startElement(el, attributes);
        }

        @Override // org.seamless.xml.SAXParser.Handler, org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            Descriptor.Service.ELEMENT el = Descriptor.Service.ELEMENT.valueOrNullOf(localName);
            if (el == null) {
                return;
            }
            endElement(el);
        }

        @Override // org.seamless.xml.SAXParser.Handler
        protected boolean isLastElement(String uri, String localName, String qName) {
            Descriptor.Service.ELEMENT el = Descriptor.Service.ELEMENT.valueOrNullOf(localName);
            return el != null && isLastElement(el);
        }

        public void startElement(Descriptor.Service.ELEMENT element, Attributes attributes) throws SAXException {
        }

        public void endElement(Descriptor.Service.ELEMENT element) throws SAXException {
        }

        public boolean isLastElement(Descriptor.Service.ELEMENT element) {
            return false;
        }
    }
}
