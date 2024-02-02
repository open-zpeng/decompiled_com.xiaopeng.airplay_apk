package org.fourthline.cling.model.meta;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.ServiceType;
/* loaded from: classes.dex */
public class RemoteService extends Service<RemoteDevice, RemoteService> {
    private final URI controlURI;
    private final URI descriptorURI;
    private final URI eventSubscriptionURI;

    public RemoteService(ServiceType serviceType, ServiceId serviceId, URI descriptorURI, URI controlURI, URI eventSubscriptionURI) throws ValidationException {
        this(serviceType, serviceId, descriptorURI, controlURI, eventSubscriptionURI, null, null);
    }

    public RemoteService(ServiceType serviceType, ServiceId serviceId, URI descriptorURI, URI controlURI, URI eventSubscriptionURI, Action<RemoteService>[] actions, StateVariable<RemoteService>[] stateVariables) throws ValidationException {
        super(serviceType, serviceId, actions, stateVariables);
        this.descriptorURI = descriptorURI;
        this.controlURI = controlURI;
        this.eventSubscriptionURI = eventSubscriptionURI;
        List<ValidationError> errors = validateThis();
        if (errors.size() > 0) {
            throw new ValidationException("Validation of device graph failed, call getErrors() on exception", errors);
        }
    }

    @Override // org.fourthline.cling.model.meta.Service
    public Action getQueryStateVariableAction() {
        return new QueryStateVariableAction(this);
    }

    public URI getDescriptorURI() {
        return this.descriptorURI;
    }

    public URI getControlURI() {
        return this.controlURI;
    }

    public URI getEventSubscriptionURI() {
        return this.eventSubscriptionURI;
    }

    public List<ValidationError> validateThis() {
        List<ValidationError> errors = new ArrayList<>();
        if (getDescriptorURI() == null) {
            errors.add(new ValidationError(getClass(), "descriptorURI", "Descriptor location (SCPDURL) is required"));
        }
        if (getControlURI() == null) {
            errors.add(new ValidationError(getClass(), "controlURI", "Control URL is required"));
        }
        if (getEventSubscriptionURI() == null) {
            errors.add(new ValidationError(getClass(), "eventSubscriptionURI", "Event subscription URL is required"));
        }
        return errors;
    }

    @Override // org.fourthline.cling.model.meta.Service
    public String toString() {
        return "(" + getClass().getSimpleName() + ") Descriptor: " + getDescriptorURI();
    }
}
