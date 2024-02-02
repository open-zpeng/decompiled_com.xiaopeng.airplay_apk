package org.fourthline.cling.binding.xml;

import com.xpeng.airplay.service.NsdConstants;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.eclipse.jetty.http.HttpHeaders;
import org.fourthline.cling.binding.staging.MutableDevice;
import org.fourthline.cling.binding.staging.MutableIcon;
import org.fourthline.cling.binding.staging.MutableService;
import org.fourthline.cling.binding.staging.MutableUDAVersion;
import org.fourthline.cling.binding.xml.Descriptor;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.types.DLNACaps;
import org.fourthline.cling.model.types.DLNADoc;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;
import org.seamless.util.MimeType;
import org.seamless.xml.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/* loaded from: classes.dex */
public class UDA10DeviceDescriptorBinderSAXImpl extends UDA10DeviceDescriptorBinderImpl {
    private static Logger log = Logger.getLogger(DeviceDescriptorBinder.class.getName());

    @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderImpl, org.fourthline.cling.binding.xml.DeviceDescriptorBinder
    public <D extends Device> D describe(D undescribedDevice, String descriptorXml) throws DescriptorBindingException, ValidationException {
        if (descriptorXml == null || descriptorXml.length() == 0) {
            throw new DescriptorBindingException("Null or empty descriptor");
        }
        try {
            Logger logger = log;
            logger.fine("Populating device from XML descriptor: " + undescribedDevice);
            SAXParser parser = new SAXParser();
            MutableDevice descriptor = new MutableDevice();
            new RootHandler(descriptor, parser);
            parser.parse(new InputSource(new StringReader(descriptorXml.trim())));
            return (D) descriptor.build(undescribedDevice);
        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex2) {
            throw new DescriptorBindingException("Could not parse device descriptor: " + ex2.toString(), ex2);
        }
    }

    /* loaded from: classes.dex */
    protected static class RootHandler extends DeviceDescriptorHandler<MutableDevice> {
        public RootHandler(MutableDevice instance, SAXParser parser) {
            super(instance, parser);
        }

        @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler
        public void startElement(Descriptor.Device.ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(SpecVersionHandler.EL)) {
                MutableUDAVersion udaVersion = new MutableUDAVersion();
                getInstance().udaVersion = udaVersion;
                new SpecVersionHandler(udaVersion, this);
            }
            if (element.equals(DeviceHandler.EL)) {
                new DeviceHandler(getInstance(), this);
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler
        public void endElement(Descriptor.Device.ELEMENT element) throws SAXException {
            if (AnonymousClass1.$SwitchMap$org$fourthline$cling$binding$xml$Descriptor$Device$ELEMENT[element.ordinal()] == 1) {
                try {
                    String urlString = getCharacters();
                    if (urlString != null && urlString.length() > 0) {
                        getInstance().baseURL = new URL(urlString);
                    }
                } catch (Exception ex) {
                    throw new SAXException("Invalid URLBase: " + ex.toString());
                }
            }
        }
    }

    /* loaded from: classes.dex */
    protected static class SpecVersionHandler extends DeviceDescriptorHandler<MutableUDAVersion> {
        public static final Descriptor.Device.ELEMENT EL = Descriptor.Device.ELEMENT.specVersion;

        public SpecVersionHandler(MutableUDAVersion instance, DeviceDescriptorHandler parent) {
            super(instance, parent);
        }

        @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler
        public void endElement(Descriptor.Device.ELEMENT element) throws SAXException {
            switch (element) {
                case major:
                    String majorVersion = getCharacters().trim();
                    if (!majorVersion.equals(NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS)) {
                        Logger logger = UDA10DeviceDescriptorBinderSAXImpl.log;
                        logger.warning("Unsupported UDA major version, ignoring: " + majorVersion);
                        majorVersion = NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS;
                    }
                    getInstance().major = Integer.valueOf(majorVersion).intValue();
                    return;
                case minor:
                    String minorVersion = getCharacters().trim();
                    if (!minorVersion.equals("0")) {
                        Logger logger2 = UDA10DeviceDescriptorBinderSAXImpl.log;
                        logger2.warning("Unsupported UDA minor version, ignoring: " + minorVersion);
                        minorVersion = "0";
                    }
                    getInstance().minor = Integer.valueOf(minorVersion).intValue();
                    return;
                default:
                    return;
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler
        public boolean isLastElement(Descriptor.Device.ELEMENT element) {
            return element.equals(EL);
        }
    }

    /* loaded from: classes.dex */
    protected static class DeviceHandler extends DeviceDescriptorHandler<MutableDevice> {
        public static final Descriptor.Device.ELEMENT EL = Descriptor.Device.ELEMENT.device;

        public DeviceHandler(MutableDevice instance, DeviceDescriptorHandler parent) {
            super(instance, parent);
        }

        @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler
        public void startElement(Descriptor.Device.ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(IconListHandler.EL)) {
                List<MutableIcon> icons = new ArrayList<>();
                getInstance().icons = icons;
                new IconListHandler(icons, this);
            }
            if (element.equals(ServiceListHandler.EL)) {
                List<MutableService> services = new ArrayList<>();
                getInstance().services = services;
                new ServiceListHandler(services, this);
            }
            if (element.equals(DeviceListHandler.EL)) {
                List<MutableDevice> devices = new ArrayList<>();
                getInstance().embeddedDevices = devices;
                new DeviceListHandler(devices, this);
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler
        public void endElement(Descriptor.Device.ELEMENT element) throws SAXException {
            switch (AnonymousClass1.$SwitchMap$org$fourthline$cling$binding$xml$Descriptor$Device$ELEMENT[element.ordinal()]) {
                case 4:
                    getInstance().deviceType = getCharacters();
                    return;
                case 5:
                    getInstance().friendlyName = getCharacters();
                    return;
                case 6:
                    getInstance().manufacturer = getCharacters();
                    return;
                case 7:
                    getInstance().manufacturerURI = UDA10DeviceDescriptorBinderImpl.parseURI(getCharacters());
                    return;
                case 8:
                    getInstance().modelDescription = getCharacters();
                    return;
                case 9:
                    getInstance().modelName = getCharacters();
                    return;
                case 10:
                    getInstance().modelNumber = getCharacters();
                    return;
                case 11:
                    getInstance().modelURI = UDA10DeviceDescriptorBinderImpl.parseURI(getCharacters());
                    return;
                case 12:
                    getInstance().presentationURI = UDA10DeviceDescriptorBinderImpl.parseURI(getCharacters());
                    return;
                case 13:
                    getInstance().upc = getCharacters();
                    return;
                case 14:
                    getInstance().serialNumber = getCharacters();
                    return;
                case 15:
                    getInstance().udn = UDN.valueOf(getCharacters());
                    return;
                case 16:
                    String txt = getCharacters();
                    try {
                        getInstance().dlnaDocs.add(DLNADoc.valueOf(txt));
                        return;
                    } catch (InvalidValueException e) {
                        Logger logger = UDA10DeviceDescriptorBinderSAXImpl.log;
                        logger.info("Invalid X_DLNADOC value, ignoring value: " + txt);
                        return;
                    }
                case HttpHeaders.EXPIRES_ORDINAL /* 17 */:
                    getInstance().dlnaCaps = DLNACaps.valueOf(getCharacters());
                    return;
                default:
                    return;
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler
        public boolean isLastElement(Descriptor.Device.ELEMENT element) {
            return element.equals(EL);
        }
    }

    /* loaded from: classes.dex */
    protected static class IconListHandler extends DeviceDescriptorHandler<List<MutableIcon>> {
        public static final Descriptor.Device.ELEMENT EL = Descriptor.Device.ELEMENT.iconList;

        public IconListHandler(List<MutableIcon> instance, DeviceDescriptorHandler parent) {
            super(instance, parent);
        }

        @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler
        public void startElement(Descriptor.Device.ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(IconHandler.EL)) {
                MutableIcon icon = new MutableIcon();
                getInstance().add(icon);
                new IconHandler(icon, this);
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler
        public boolean isLastElement(Descriptor.Device.ELEMENT element) {
            return element.equals(EL);
        }
    }

    /* loaded from: classes.dex */
    protected static class IconHandler extends DeviceDescriptorHandler<MutableIcon> {
        public static final Descriptor.Device.ELEMENT EL = Descriptor.Device.ELEMENT.icon;

        public IconHandler(MutableIcon instance, DeviceDescriptorHandler parent) {
            super(instance, parent);
        }

        @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler
        public void endElement(Descriptor.Device.ELEMENT element) throws SAXException {
            switch (AnonymousClass1.$SwitchMap$org$fourthline$cling$binding$xml$Descriptor$Device$ELEMENT[element.ordinal()]) {
                case HttpHeaders.LAST_MODIFIED_ORDINAL /* 18 */:
                    getInstance().width = Integer.valueOf(getCharacters()).intValue();
                    return;
                case HttpHeaders.ACCEPT_ORDINAL /* 19 */:
                    getInstance().height = Integer.valueOf(getCharacters()).intValue();
                    return;
                case HttpHeaders.ACCEPT_CHARSET_ORDINAL /* 20 */:
                    try {
                        getInstance().depth = Integer.valueOf(getCharacters()).intValue();
                        return;
                    } catch (NumberFormatException ex) {
                        Logger logger = UDA10DeviceDescriptorBinderSAXImpl.log;
                        logger.warning("Invalid icon depth '" + getCharacters() + "', using 16 as default: " + ex);
                        getInstance().depth = 16;
                        return;
                    }
                case HttpHeaders.ACCEPT_ENCODING_ORDINAL /* 21 */:
                    getInstance().uri = UDA10DeviceDescriptorBinderImpl.parseURI(getCharacters());
                    return;
                case HttpHeaders.ACCEPT_LANGUAGE_ORDINAL /* 22 */:
                    try {
                        getInstance().mimeType = getCharacters();
                        MimeType.valueOf(getInstance().mimeType);
                        return;
                    } catch (IllegalArgumentException e) {
                        Logger logger2 = UDA10DeviceDescriptorBinderSAXImpl.log;
                        logger2.warning("Ignoring invalid icon mime type: " + getInstance().mimeType);
                        getInstance().mimeType = "";
                        return;
                    }
                default:
                    return;
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler
        public boolean isLastElement(Descriptor.Device.ELEMENT element) {
            return element.equals(EL);
        }
    }

    /* loaded from: classes.dex */
    protected static class ServiceListHandler extends DeviceDescriptorHandler<List<MutableService>> {
        public static final Descriptor.Device.ELEMENT EL = Descriptor.Device.ELEMENT.serviceList;

        public ServiceListHandler(List<MutableService> instance, DeviceDescriptorHandler parent) {
            super(instance, parent);
        }

        @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler
        public void startElement(Descriptor.Device.ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(ServiceHandler.EL)) {
                MutableService service = new MutableService();
                getInstance().add(service);
                new ServiceHandler(service, this);
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler
        public boolean isLastElement(Descriptor.Device.ELEMENT element) {
            boolean last = element.equals(EL);
            if (last) {
                Iterator<MutableService> it = getInstance().iterator();
                while (it.hasNext()) {
                    MutableService service = it.next();
                    if (service.serviceType == null || service.serviceId == null) {
                        it.remove();
                    }
                }
            }
            return last;
        }
    }

    /* loaded from: classes.dex */
    protected static class ServiceHandler extends DeviceDescriptorHandler<MutableService> {
        public static final Descriptor.Device.ELEMENT EL = Descriptor.Device.ELEMENT.service;

        public ServiceHandler(MutableService instance, DeviceDescriptorHandler parent) {
            super(instance, parent);
        }

        @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler
        public void endElement(Descriptor.Device.ELEMENT element) throws SAXException {
            try {
                switch (AnonymousClass1.$SwitchMap$org$fourthline$cling$binding$xml$Descriptor$Device$ELEMENT[element.ordinal()]) {
                    case HttpHeaders.AUTHORIZATION_ORDINAL /* 23 */:
                        getInstance().serviceType = ServiceType.valueOf(getCharacters());
                        break;
                    case HttpHeaders.EXPECT_ORDINAL /* 24 */:
                        getInstance().serviceId = ServiceId.valueOf(getCharacters());
                        break;
                    case HttpHeaders.FORWARDED_ORDINAL /* 25 */:
                        getInstance().descriptorURI = UDA10DeviceDescriptorBinderImpl.parseURI(getCharacters());
                        break;
                    case HttpHeaders.FROM_ORDINAL /* 26 */:
                        getInstance().controlURI = UDA10DeviceDescriptorBinderImpl.parseURI(getCharacters());
                        break;
                    case HttpHeaders.HOST_ORDINAL /* 27 */:
                        getInstance().eventSubscriptionURI = UDA10DeviceDescriptorBinderImpl.parseURI(getCharacters());
                        break;
                }
            } catch (InvalidValueException ex) {
                Logger logger = UDA10DeviceDescriptorBinderSAXImpl.log;
                logger.warning("UPnP specification violation, skipping invalid service declaration. " + ex.getMessage());
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler
        public boolean isLastElement(Descriptor.Device.ELEMENT element) {
            return element.equals(EL);
        }
    }

    /* loaded from: classes.dex */
    protected static class DeviceListHandler extends DeviceDescriptorHandler<List<MutableDevice>> {
        public static final Descriptor.Device.ELEMENT EL = Descriptor.Device.ELEMENT.deviceList;

        public DeviceListHandler(List<MutableDevice> instance, DeviceDescriptorHandler parent) {
            super(instance, parent);
        }

        @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler
        public void startElement(Descriptor.Device.ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(DeviceHandler.EL)) {
                MutableDevice device = new MutableDevice();
                getInstance().add(device);
                new DeviceHandler(device, this);
            }
        }

        @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler
        public boolean isLastElement(Descriptor.Device.ELEMENT element) {
            return element.equals(EL);
        }
    }

    /* loaded from: classes.dex */
    protected static class DeviceDescriptorHandler<I> extends SAXParser.Handler<I> {
        public DeviceDescriptorHandler(I instance) {
            super(instance);
        }

        public DeviceDescriptorHandler(I instance, SAXParser parser) {
            super(instance, parser);
        }

        public DeviceDescriptorHandler(I instance, DeviceDescriptorHandler parent) {
            super(instance, parent);
        }

        public DeviceDescriptorHandler(I instance, SAXParser parser, DeviceDescriptorHandler parent) {
            super(instance, parser, parent);
        }

        @Override // org.seamless.xml.SAXParser.Handler, org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            Descriptor.Device.ELEMENT el = Descriptor.Device.ELEMENT.valueOrNullOf(localName);
            if (el == null) {
                return;
            }
            startElement(el, attributes);
        }

        @Override // org.seamless.xml.SAXParser.Handler, org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            Descriptor.Device.ELEMENT el = Descriptor.Device.ELEMENT.valueOrNullOf(localName);
            if (el == null) {
                return;
            }
            endElement(el);
        }

        @Override // org.seamless.xml.SAXParser.Handler
        protected boolean isLastElement(String uri, String localName, String qName) {
            Descriptor.Device.ELEMENT el = Descriptor.Device.ELEMENT.valueOrNullOf(localName);
            return el != null && isLastElement(el);
        }

        public void startElement(Descriptor.Device.ELEMENT element, Attributes attributes) throws SAXException {
        }

        public void endElement(Descriptor.Device.ELEMENT element) throws SAXException {
        }

        public boolean isLastElement(Descriptor.Device.ELEMENT element) {
            return false;
        }
    }
}
