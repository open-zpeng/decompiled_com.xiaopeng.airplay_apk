package org.fourthline.cling.protocol.sync;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.binding.xml.DescriptorBindingException;
import org.fourthline.cling.binding.xml.DeviceDescriptorBinder;
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.ContentTypeHeader;
import org.fourthline.cling.model.message.header.ServerHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.resource.DeviceDescriptorResource;
import org.fourthline.cling.model.resource.IconResource;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.resource.ServiceDescriptorResource;
import org.fourthline.cling.protocol.ReceivingSync;
import org.fourthline.cling.transport.RouterException;
import org.seamless.util.Exceptions;
/* loaded from: classes.dex */
public class ReceivingRetrieval extends ReceivingSync<StreamRequestMessage, StreamResponseMessage> {
    private static final Logger log = Logger.getLogger(ReceivingRetrieval.class.getName());

    public ReceivingRetrieval(UpnpService upnpService, StreamRequestMessage inputMessage) {
        super(upnpService, inputMessage);
    }

    @Override // org.fourthline.cling.protocol.ReceivingSync
    protected StreamResponseMessage executeSync() throws RouterException {
        if (!((StreamRequestMessage) getInputMessage()).hasHostHeader()) {
            Logger logger = log;
            logger.fine("Ignoring message, missing HOST header: " + getInputMessage());
            return new StreamResponseMessage(new UpnpResponse(UpnpResponse.Status.PRECONDITION_FAILED));
        }
        URI requestedURI = ((StreamRequestMessage) getInputMessage()).getOperation().getURI();
        Resource foundResource = getUpnpService().getRegistry().getResource(requestedURI);
        if (foundResource == null && (foundResource = onResourceNotFound(requestedURI)) == null) {
            Logger logger2 = log;
            logger2.fine("No local resource found: " + getInputMessage());
            return null;
        }
        return createResponse(requestedURI, foundResource);
    }

    protected StreamResponseMessage createResponse(URI requestedURI, Resource resource) {
        StreamResponseMessage response;
        try {
            if (DeviceDescriptorResource.class.isAssignableFrom(resource.getClass())) {
                Logger logger = log;
                logger.fine("Found local device matching relative request URI: " + requestedURI);
                LocalDevice device = (LocalDevice) resource.getModel();
                DeviceDescriptorBinder deviceDescriptorBinder = getUpnpService().getConfiguration().getDeviceDescriptorBinderUDA10();
                String deviceDescriptor = deviceDescriptorBinder.generate(device, getRemoteClientInfo(), getUpnpService().getConfiguration().getNamespace());
                response = new StreamResponseMessage(deviceDescriptor, new ContentTypeHeader(ContentTypeHeader.DEFAULT_CONTENT_TYPE));
            } else if (ServiceDescriptorResource.class.isAssignableFrom(resource.getClass())) {
                Logger logger2 = log;
                logger2.fine("Found local service matching relative request URI: " + requestedURI);
                LocalService service = (LocalService) resource.getModel();
                ServiceDescriptorBinder serviceDescriptorBinder = getUpnpService().getConfiguration().getServiceDescriptorBinderUDA10();
                String serviceDescriptor = serviceDescriptorBinder.generate(service);
                response = new StreamResponseMessage(serviceDescriptor, new ContentTypeHeader(ContentTypeHeader.DEFAULT_CONTENT_TYPE));
            } else if (IconResource.class.isAssignableFrom(resource.getClass())) {
                Logger logger3 = log;
                logger3.fine("Found local icon matching relative request URI: " + requestedURI);
                Icon icon = (Icon) resource.getModel();
                response = new StreamResponseMessage(icon.getData(), icon.getMimeType());
            } else {
                Logger logger4 = log;
                logger4.fine("Ignoring GET for found local resource: " + resource);
                return null;
            }
        } catch (DescriptorBindingException ex) {
            Logger logger5 = log;
            logger5.warning("Error generating requested device/service descriptor: " + ex.toString());
            log.log(Level.WARNING, "Exception root cause: ", Exceptions.unwrap(ex));
            response = new StreamResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR);
        }
        response.getHeaders().add(UpnpHeader.Type.SERVER, new ServerHeader());
        return response;
    }

    protected Resource onResourceNotFound(URI requestedURIPath) {
        return null;
    }
}
