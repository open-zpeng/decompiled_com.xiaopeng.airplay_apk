package org.fourthline.cling.binding.xml;

import com.xpeng.airplay.service.NsdConstants;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.fourthline.cling.binding.staging.MutableDevice;
import org.fourthline.cling.binding.staging.MutableIcon;
import org.fourthline.cling.binding.staging.MutableService;
import org.fourthline.cling.binding.xml.Descriptor;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.XMLUtil;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.profile.RemoteClientInfo;
import org.fourthline.cling.model.types.DLNACaps;
import org.fourthline.cling.model.types.DLNADoc;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;
import org.seamless.util.Exceptions;
import org.seamless.util.MimeType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
/* loaded from: classes.dex */
public class UDA10DeviceDescriptorBinderImpl implements DeviceDescriptorBinder, ErrorHandler {
    private static Logger log = Logger.getLogger(DeviceDescriptorBinder.class.getName());

    @Override // org.fourthline.cling.binding.xml.DeviceDescriptorBinder
    public <D extends Device> D describe(D undescribedDevice, String descriptorXml) throws DescriptorBindingException, ValidationException {
        if (descriptorXml == null || descriptorXml.length() == 0) {
            throw new DescriptorBindingException("Null or empty descriptor");
        }
        try {
            Logger logger = log;
            logger.fine("Populating device from XML descriptor: " + undescribedDevice);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            documentBuilder.setErrorHandler(this);
            Document d = documentBuilder.parse(new InputSource(new StringReader(descriptorXml.trim())));
            return (D) describe((UDA10DeviceDescriptorBinderImpl) undescribedDevice, d);
        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex2) {
            throw new DescriptorBindingException("Could not parse device descriptor: " + ex2.toString(), ex2);
        }
    }

    @Override // org.fourthline.cling.binding.xml.DeviceDescriptorBinder
    public <D extends Device> D describe(D undescribedDevice, Document dom) throws DescriptorBindingException, ValidationException {
        try {
            Logger logger = log;
            logger.fine("Populating device from DOM: " + undescribedDevice);
            MutableDevice descriptor = new MutableDevice();
            Element rootElement = dom.getDocumentElement();
            hydrateRoot(descriptor, rootElement);
            return (D) buildInstance(undescribedDevice, descriptor);
        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex2) {
            throw new DescriptorBindingException("Could not parse device DOM: " + ex2.toString(), ex2);
        }
    }

    public <D extends Device> D buildInstance(D undescribedDevice, MutableDevice descriptor) throws ValidationException {
        return (D) descriptor.build(undescribedDevice);
    }

    protected void hydrateRoot(MutableDevice descriptor, Element rootElement) throws DescriptorBindingException {
        if (rootElement.getNamespaceURI() == null || !rootElement.getNamespaceURI().equals(Descriptor.Device.NAMESPACE_URI)) {
            Logger logger = log;
            logger.warning("Wrong XML namespace declared on root element: " + rootElement.getNamespaceURI());
        }
        if (!rootElement.getNodeName().equals(Descriptor.Device.ELEMENT.root.name())) {
            throw new DescriptorBindingException("Root element name is not <root>: " + rootElement.getNodeName());
        }
        NodeList rootChildren = rootElement.getChildNodes();
        Node deviceNode = null;
        for (int i = 0; i < rootChildren.getLength(); i++) {
            Node rootChild = rootChildren.item(i);
            if (rootChild.getNodeType() == 1) {
                if (Descriptor.Device.ELEMENT.specVersion.equals(rootChild)) {
                    hydrateSpecVersion(descriptor, rootChild);
                } else if (Descriptor.Device.ELEMENT.URLBase.equals(rootChild)) {
                    try {
                        String urlString = XMLUtil.getTextContent(rootChild);
                        if (urlString != null && urlString.length() > 0) {
                            descriptor.baseURL = new URL(urlString);
                        }
                    } catch (Exception ex) {
                        throw new DescriptorBindingException("Invalid URLBase: " + ex.getMessage());
                    }
                } else if (Descriptor.Device.ELEMENT.device.equals(rootChild)) {
                    if (deviceNode != null) {
                        throw new DescriptorBindingException("Found multiple <device> elements in <root>");
                    }
                    deviceNode = rootChild;
                } else {
                    Logger logger2 = log;
                    logger2.finer("Ignoring unknown element: " + rootChild.getNodeName());
                }
            }
        }
        if (deviceNode == null) {
            throw new DescriptorBindingException("No <device> element in <root>");
        }
        hydrateDevice(descriptor, deviceNode);
    }

    public void hydrateSpecVersion(MutableDevice descriptor, Node specVersionNode) throws DescriptorBindingException {
        NodeList specVersionChildren = specVersionNode.getChildNodes();
        for (int i = 0; i < specVersionChildren.getLength(); i++) {
            Node specVersionChild = specVersionChildren.item(i);
            if (specVersionChild.getNodeType() == 1) {
                if (Descriptor.Device.ELEMENT.major.equals(specVersionChild)) {
                    String version = XMLUtil.getTextContent(specVersionChild).trim();
                    if (!version.equals(NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS)) {
                        Logger logger = log;
                        logger.warning("Unsupported UDA major version, ignoring: " + version);
                        version = NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS;
                    }
                    descriptor.udaVersion.major = Integer.valueOf(version).intValue();
                } else if (Descriptor.Device.ELEMENT.minor.equals(specVersionChild)) {
                    String version2 = XMLUtil.getTextContent(specVersionChild).trim();
                    if (!version2.equals("0")) {
                        Logger logger2 = log;
                        logger2.warning("Unsupported UDA minor version, ignoring: " + version2);
                        version2 = "0";
                    }
                    descriptor.udaVersion.minor = Integer.valueOf(version2).intValue();
                }
            }
        }
    }

    public void hydrateDevice(MutableDevice descriptor, Node deviceNode) throws DescriptorBindingException {
        NodeList deviceNodeChildren = deviceNode.getChildNodes();
        for (int i = 0; i < deviceNodeChildren.getLength(); i++) {
            Node deviceNodeChild = deviceNodeChildren.item(i);
            if (deviceNodeChild.getNodeType() == 1) {
                if (Descriptor.Device.ELEMENT.deviceType.equals(deviceNodeChild)) {
                    descriptor.deviceType = XMLUtil.getTextContent(deviceNodeChild);
                } else if (Descriptor.Device.ELEMENT.friendlyName.equals(deviceNodeChild)) {
                    descriptor.friendlyName = XMLUtil.getTextContent(deviceNodeChild);
                } else if (Descriptor.Device.ELEMENT.manufacturer.equals(deviceNodeChild)) {
                    descriptor.manufacturer = XMLUtil.getTextContent(deviceNodeChild);
                } else if (Descriptor.Device.ELEMENT.manufacturerURL.equals(deviceNodeChild)) {
                    descriptor.manufacturerURI = parseURI(XMLUtil.getTextContent(deviceNodeChild));
                } else if (Descriptor.Device.ELEMENT.modelDescription.equals(deviceNodeChild)) {
                    descriptor.modelDescription = XMLUtil.getTextContent(deviceNodeChild);
                } else if (Descriptor.Device.ELEMENT.modelName.equals(deviceNodeChild)) {
                    descriptor.modelName = XMLUtil.getTextContent(deviceNodeChild);
                } else if (Descriptor.Device.ELEMENT.modelNumber.equals(deviceNodeChild)) {
                    descriptor.modelNumber = XMLUtil.getTextContent(deviceNodeChild);
                } else if (Descriptor.Device.ELEMENT.modelURL.equals(deviceNodeChild)) {
                    descriptor.modelURI = parseURI(XMLUtil.getTextContent(deviceNodeChild));
                } else if (Descriptor.Device.ELEMENT.presentationURL.equals(deviceNodeChild)) {
                    descriptor.presentationURI = parseURI(XMLUtil.getTextContent(deviceNodeChild));
                } else if (Descriptor.Device.ELEMENT.UPC.equals(deviceNodeChild)) {
                    descriptor.upc = XMLUtil.getTextContent(deviceNodeChild);
                } else if (Descriptor.Device.ELEMENT.serialNumber.equals(deviceNodeChild)) {
                    descriptor.serialNumber = XMLUtil.getTextContent(deviceNodeChild);
                } else if (Descriptor.Device.ELEMENT.UDN.equals(deviceNodeChild)) {
                    descriptor.udn = UDN.valueOf(XMLUtil.getTextContent(deviceNodeChild));
                } else if (Descriptor.Device.ELEMENT.iconList.equals(deviceNodeChild)) {
                    hydrateIconList(descriptor, deviceNodeChild);
                } else if (Descriptor.Device.ELEMENT.serviceList.equals(deviceNodeChild)) {
                    hydrateServiceList(descriptor, deviceNodeChild);
                } else if (Descriptor.Device.ELEMENT.deviceList.equals(deviceNodeChild)) {
                    hydrateDeviceList(descriptor, deviceNodeChild);
                } else if (Descriptor.Device.ELEMENT.X_DLNADOC.equals(deviceNodeChild) && Descriptor.Device.DLNA_PREFIX.equals(deviceNodeChild.getPrefix())) {
                    String txt = XMLUtil.getTextContent(deviceNodeChild);
                    try {
                        descriptor.dlnaDocs.add(DLNADoc.valueOf(txt));
                    } catch (InvalidValueException e) {
                        Logger logger = log;
                        logger.info("Invalid X_DLNADOC value, ignoring value: " + txt);
                    }
                } else if (Descriptor.Device.ELEMENT.X_DLNACAP.equals(deviceNodeChild) && Descriptor.Device.DLNA_PREFIX.equals(deviceNodeChild.getPrefix())) {
                    descriptor.dlnaCaps = DLNACaps.valueOf(XMLUtil.getTextContent(deviceNodeChild));
                }
            }
        }
    }

    public void hydrateIconList(MutableDevice descriptor, Node iconListNode) throws DescriptorBindingException {
        NodeList iconListNodeChildren = iconListNode.getChildNodes();
        int i = 0;
        while (true) {
            int i2 = i;
            int i3 = iconListNodeChildren.getLength();
            if (i2 >= i3) {
                return;
            }
            Node iconListNodeChild = iconListNodeChildren.item(i2);
            if (iconListNodeChild.getNodeType() == 1 && Descriptor.Device.ELEMENT.icon.equals(iconListNodeChild)) {
                MutableIcon icon = new MutableIcon();
                NodeList iconChildren = iconListNodeChild.getChildNodes();
                int x = 0;
                while (true) {
                    int x2 = x;
                    int x3 = iconChildren.getLength();
                    if (x2 >= x3) {
                        break;
                    }
                    Node iconChild = iconChildren.item(x2);
                    if (iconChild.getNodeType() == 1) {
                        if (Descriptor.Device.ELEMENT.width.equals(iconChild)) {
                            icon.width = Integer.valueOf(XMLUtil.getTextContent(iconChild)).intValue();
                        } else if (Descriptor.Device.ELEMENT.height.equals(iconChild)) {
                            icon.height = Integer.valueOf(XMLUtil.getTextContent(iconChild)).intValue();
                        } else if (Descriptor.Device.ELEMENT.depth.equals(iconChild)) {
                            String depth = XMLUtil.getTextContent(iconChild);
                            try {
                                icon.depth = Integer.valueOf(depth).intValue();
                            } catch (NumberFormatException ex) {
                                log.warning("Invalid icon depth '" + depth + "', using 16 as default: " + ex);
                                icon.depth = 16;
                            }
                        } else if (Descriptor.Device.ELEMENT.url.equals(iconChild)) {
                            icon.uri = parseURI(XMLUtil.getTextContent(iconChild));
                        } else if (Descriptor.Device.ELEMENT.mimetype.equals(iconChild)) {
                            try {
                                icon.mimeType = XMLUtil.getTextContent(iconChild);
                                MimeType.valueOf(icon.mimeType);
                            } catch (IllegalArgumentException e) {
                                log.warning("Ignoring invalid icon mime type: " + icon.mimeType);
                                icon.mimeType = "";
                            }
                        }
                    }
                    x = x2 + 1;
                }
                descriptor.icons.add(icon);
            }
            i = i2 + 1;
        }
    }

    public void hydrateServiceList(MutableDevice descriptor, Node serviceListNode) throws DescriptorBindingException {
        NodeList serviceListNodeChildren = serviceListNode.getChildNodes();
        for (int i = 0; i < serviceListNodeChildren.getLength(); i++) {
            Node serviceListNodeChild = serviceListNodeChildren.item(i);
            if (serviceListNodeChild.getNodeType() == 1 && Descriptor.Device.ELEMENT.service.equals(serviceListNodeChild)) {
                NodeList serviceChildren = serviceListNodeChild.getChildNodes();
                try {
                    MutableService service = new MutableService();
                    for (int x = 0; x < serviceChildren.getLength(); x++) {
                        Node serviceChild = serviceChildren.item(x);
                        if (serviceChild.getNodeType() == 1) {
                            if (Descriptor.Device.ELEMENT.serviceType.equals(serviceChild)) {
                                service.serviceType = ServiceType.valueOf(XMLUtil.getTextContent(serviceChild));
                            } else if (Descriptor.Device.ELEMENT.serviceId.equals(serviceChild)) {
                                service.serviceId = ServiceId.valueOf(XMLUtil.getTextContent(serviceChild));
                            } else if (Descriptor.Device.ELEMENT.SCPDURL.equals(serviceChild)) {
                                service.descriptorURI = parseURI(XMLUtil.getTextContent(serviceChild));
                            } else if (Descriptor.Device.ELEMENT.controlURL.equals(serviceChild)) {
                                service.controlURI = parseURI(XMLUtil.getTextContent(serviceChild));
                            } else if (Descriptor.Device.ELEMENT.eventSubURL.equals(serviceChild)) {
                                service.eventSubscriptionURI = parseURI(XMLUtil.getTextContent(serviceChild));
                            }
                        }
                    }
                    descriptor.services.add(service);
                } catch (InvalidValueException ex) {
                    log.warning("UPnP specification violation, skipping invalid service declaration. " + ex.getMessage());
                }
            }
        }
    }

    public void hydrateDeviceList(MutableDevice descriptor, Node deviceListNode) throws DescriptorBindingException {
        NodeList deviceListNodeChildren = deviceListNode.getChildNodes();
        for (int i = 0; i < deviceListNodeChildren.getLength(); i++) {
            Node deviceListNodeChild = deviceListNodeChildren.item(i);
            if (deviceListNodeChild.getNodeType() == 1 && Descriptor.Device.ELEMENT.device.equals(deviceListNodeChild)) {
                MutableDevice embeddedDevice = new MutableDevice();
                embeddedDevice.parentDevice = descriptor;
                descriptor.embeddedDevices.add(embeddedDevice);
                hydrateDevice(embeddedDevice, deviceListNodeChild);
            }
        }
    }

    @Override // org.fourthline.cling.binding.xml.DeviceDescriptorBinder
    public String generate(Device deviceModel, RemoteClientInfo info, Namespace namespace) throws DescriptorBindingException {
        try {
            Logger logger = log;
            logger.fine("Generating XML descriptor from device model: " + deviceModel);
            return XMLUtil.documentToString(buildDOM(deviceModel, info, namespace));
        } catch (Exception ex) {
            throw new DescriptorBindingException("Could not build DOM: " + ex.getMessage(), ex);
        }
    }

    @Override // org.fourthline.cling.binding.xml.DeviceDescriptorBinder
    public Document buildDOM(Device deviceModel, RemoteClientInfo info, Namespace namespace) throws DescriptorBindingException {
        try {
            Logger logger = log;
            logger.fine("Generating DOM from device model: " + deviceModel);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document d = factory.newDocumentBuilder().newDocument();
            generateRoot(namespace, deviceModel, d, info);
            return d;
        } catch (Exception ex) {
            throw new DescriptorBindingException("Could not generate device descriptor: " + ex.getMessage(), ex);
        }
    }

    protected void generateRoot(Namespace namespace, Device deviceModel, Document descriptor, RemoteClientInfo info) {
        Element rootElement = descriptor.createElementNS(Descriptor.Device.NAMESPACE_URI, Descriptor.Device.ELEMENT.root.toString());
        descriptor.appendChild(rootElement);
        generateSpecVersion(namespace, deviceModel, descriptor, rootElement);
        generateDevice(namespace, deviceModel, descriptor, rootElement, info);
    }

    protected void generateSpecVersion(Namespace namespace, Device deviceModel, Document descriptor, Element rootElement) {
        Element specVersionElement = XMLUtil.appendNewElement(descriptor, rootElement, Descriptor.Device.ELEMENT.specVersion);
        XMLUtil.appendNewElementIfNotNull(descriptor, specVersionElement, Descriptor.Device.ELEMENT.major, Integer.valueOf(deviceModel.getVersion().getMajor()));
        XMLUtil.appendNewElementIfNotNull(descriptor, specVersionElement, Descriptor.Device.ELEMENT.minor, Integer.valueOf(deviceModel.getVersion().getMinor()));
    }

    protected void generateDevice(Namespace namespace, Device deviceModel, Document descriptor, Element rootElement, RemoteClientInfo info) {
        DLNADoc[] dlnaDocs;
        Element deviceElement = XMLUtil.appendNewElement(descriptor, rootElement, Descriptor.Device.ELEMENT.device);
        XMLUtil.appendNewElementIfNotNull(descriptor, deviceElement, Descriptor.Device.ELEMENT.deviceType, deviceModel.getType());
        DeviceDetails deviceModelDetails = deviceModel.getDetails(info);
        XMLUtil.appendNewElementIfNotNull(descriptor, deviceElement, Descriptor.Device.ELEMENT.friendlyName, deviceModelDetails.getFriendlyName());
        if (deviceModelDetails.getManufacturerDetails() != null) {
            XMLUtil.appendNewElementIfNotNull(descriptor, deviceElement, Descriptor.Device.ELEMENT.manufacturer, deviceModelDetails.getManufacturerDetails().getManufacturer());
            XMLUtil.appendNewElementIfNotNull(descriptor, deviceElement, Descriptor.Device.ELEMENT.manufacturerURL, deviceModelDetails.getManufacturerDetails().getManufacturerURI());
        }
        if (deviceModelDetails.getModelDetails() != null) {
            XMLUtil.appendNewElementIfNotNull(descriptor, deviceElement, Descriptor.Device.ELEMENT.modelDescription, deviceModelDetails.getModelDetails().getModelDescription());
            XMLUtil.appendNewElementIfNotNull(descriptor, deviceElement, Descriptor.Device.ELEMENT.modelName, deviceModelDetails.getModelDetails().getModelName());
            XMLUtil.appendNewElementIfNotNull(descriptor, deviceElement, Descriptor.Device.ELEMENT.modelNumber, deviceModelDetails.getModelDetails().getModelNumber());
            XMLUtil.appendNewElementIfNotNull(descriptor, deviceElement, Descriptor.Device.ELEMENT.modelURL, deviceModelDetails.getModelDetails().getModelURI());
        }
        XMLUtil.appendNewElementIfNotNull(descriptor, deviceElement, Descriptor.Device.ELEMENT.serialNumber, deviceModelDetails.getSerialNumber());
        XMLUtil.appendNewElementIfNotNull(descriptor, deviceElement, Descriptor.Device.ELEMENT.UDN, deviceModel.getIdentity().getUdn());
        XMLUtil.appendNewElementIfNotNull(descriptor, deviceElement, Descriptor.Device.ELEMENT.presentationURL, deviceModelDetails.getPresentationURI());
        XMLUtil.appendNewElementIfNotNull(descriptor, deviceElement, Descriptor.Device.ELEMENT.UPC, deviceModelDetails.getUpc());
        if (deviceModelDetails.getDlnaDocs() != null) {
            for (DLNADoc dlnaDoc : deviceModelDetails.getDlnaDocs()) {
                XMLUtil.appendNewElementIfNotNull(descriptor, deviceElement, "dlna:" + Descriptor.Device.ELEMENT.X_DLNADOC, dlnaDoc, Descriptor.Device.DLNA_NAMESPACE_URI);
            }
        }
        XMLUtil.appendNewElementIfNotNull(descriptor, deviceElement, "dlna:" + Descriptor.Device.ELEMENT.X_DLNACAP, deviceModelDetails.getDlnaCaps(), Descriptor.Device.DLNA_NAMESPACE_URI);
        XMLUtil.appendNewElementIfNotNull(descriptor, deviceElement, "sec:" + Descriptor.Device.ELEMENT.ProductCap, deviceModelDetails.getSecProductCaps(), Descriptor.Device.SEC_NAMESPACE_URI);
        XMLUtil.appendNewElementIfNotNull(descriptor, deviceElement, "sec:" + Descriptor.Device.ELEMENT.X_ProductCap, deviceModelDetails.getSecProductCaps(), Descriptor.Device.SEC_NAMESPACE_URI);
        generateIconList(namespace, deviceModel, descriptor, deviceElement);
        generateServiceList(namespace, deviceModel, descriptor, deviceElement);
        generateDeviceList(namespace, deviceModel, descriptor, deviceElement, info);
    }

    protected void generateIconList(Namespace namespace, Device deviceModel, Document descriptor, Element deviceElement) {
        Icon[] icons;
        if (deviceModel.hasIcons()) {
            Element iconListElement = XMLUtil.appendNewElement(descriptor, deviceElement, Descriptor.Device.ELEMENT.iconList);
            for (Icon icon : deviceModel.getIcons()) {
                Element iconElement = XMLUtil.appendNewElement(descriptor, iconListElement, Descriptor.Device.ELEMENT.icon);
                XMLUtil.appendNewElementIfNotNull(descriptor, iconElement, Descriptor.Device.ELEMENT.mimetype, icon.getMimeType());
                XMLUtil.appendNewElementIfNotNull(descriptor, iconElement, Descriptor.Device.ELEMENT.width, Integer.valueOf(icon.getWidth()));
                XMLUtil.appendNewElementIfNotNull(descriptor, iconElement, Descriptor.Device.ELEMENT.height, Integer.valueOf(icon.getHeight()));
                XMLUtil.appendNewElementIfNotNull(descriptor, iconElement, Descriptor.Device.ELEMENT.depth, Integer.valueOf(icon.getDepth()));
                if (deviceModel instanceof RemoteDevice) {
                    XMLUtil.appendNewElementIfNotNull(descriptor, iconElement, Descriptor.Device.ELEMENT.url, icon.getUri());
                } else if (deviceModel instanceof LocalDevice) {
                    XMLUtil.appendNewElementIfNotNull(descriptor, iconElement, Descriptor.Device.ELEMENT.url, namespace.getIconPath(icon));
                }
            }
        }
    }

    protected void generateServiceList(Namespace namespace, Device deviceModel, Document descriptor, Element deviceElement) {
        Service[] services;
        if (deviceModel.hasServices()) {
            Element serviceListElement = XMLUtil.appendNewElement(descriptor, deviceElement, Descriptor.Device.ELEMENT.serviceList);
            for (Service service : deviceModel.getServices()) {
                Element serviceElement = XMLUtil.appendNewElement(descriptor, serviceListElement, Descriptor.Device.ELEMENT.service);
                XMLUtil.appendNewElementIfNotNull(descriptor, serviceElement, Descriptor.Device.ELEMENT.serviceType, service.getServiceType());
                XMLUtil.appendNewElementIfNotNull(descriptor, serviceElement, Descriptor.Device.ELEMENT.serviceId, service.getServiceId());
                if (service instanceof RemoteService) {
                    RemoteService rs = (RemoteService) service;
                    XMLUtil.appendNewElementIfNotNull(descriptor, serviceElement, Descriptor.Device.ELEMENT.SCPDURL, rs.getDescriptorURI());
                    XMLUtil.appendNewElementIfNotNull(descriptor, serviceElement, Descriptor.Device.ELEMENT.controlURL, rs.getControlURI());
                    XMLUtil.appendNewElementIfNotNull(descriptor, serviceElement, Descriptor.Device.ELEMENT.eventSubURL, rs.getEventSubscriptionURI());
                } else if (service instanceof LocalService) {
                    LocalService ls = (LocalService) service;
                    XMLUtil.appendNewElementIfNotNull(descriptor, serviceElement, Descriptor.Device.ELEMENT.SCPDURL, namespace.getDescriptorPath(ls));
                    XMLUtil.appendNewElementIfNotNull(descriptor, serviceElement, Descriptor.Device.ELEMENT.controlURL, namespace.getControlPath(ls));
                    XMLUtil.appendNewElementIfNotNull(descriptor, serviceElement, Descriptor.Device.ELEMENT.eventSubURL, namespace.getEventSubscriptionPath(ls));
                }
            }
        }
    }

    protected void generateDeviceList(Namespace namespace, Device deviceModel, Document descriptor, Element deviceElement, RemoteClientInfo info) {
        Device[] embeddedDevices;
        if (deviceModel.hasEmbeddedDevices()) {
            Element deviceListElement = XMLUtil.appendNewElement(descriptor, deviceElement, Descriptor.Device.ELEMENT.deviceList);
            for (Device device : deviceModel.getEmbeddedDevices()) {
                generateDevice(namespace, device, descriptor, deviceListElement, info);
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

    /* JADX INFO: Access modifiers changed from: protected */
    public static URI parseURI(String uri) {
        if (uri.startsWith("www.")) {
            uri = "http://" + uri;
        }
        if (uri.contains(" ")) {
            uri = uri.replaceAll(" ", "%20");
        }
        try {
            return URI.create(uri);
        } catch (Throwable ex) {
            log.fine("Illegal URI, trying with ./ prefix: " + Exceptions.unwrap(ex));
            try {
                return URI.create("./" + uri);
            } catch (IllegalArgumentException ex2) {
                log.warning("Illegal URI '" + uri + "', ignoring value: " + Exceptions.unwrap(ex2));
                return null;
            }
        }
    }
}
