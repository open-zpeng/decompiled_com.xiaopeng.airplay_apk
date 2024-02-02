package org.fourthline.cling.controlpoint;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.gena.LocalGENASubscription;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.protocol.ProtocolCreationException;
import org.fourthline.cling.protocol.sync.SendingSubscribe;
import org.seamless.util.Exceptions;
/* loaded from: classes.dex */
public abstract class SubscriptionCallback implements Runnable {
    protected static Logger log = Logger.getLogger(SubscriptionCallback.class.getName());
    private ControlPoint controlPoint;
    protected final Integer requestedDurationSeconds;
    protected final Service service;
    private GENASubscription subscription;

    protected abstract void ended(GENASubscription gENASubscription, CancelReason cancelReason, UpnpResponse upnpResponse);

    protected abstract void established(GENASubscription gENASubscription);

    protected abstract void eventReceived(GENASubscription gENASubscription);

    protected abstract void eventsMissed(GENASubscription gENASubscription, int i);

    protected abstract void failed(GENASubscription gENASubscription, UpnpResponse upnpResponse, Exception exc, String str);

    protected SubscriptionCallback(Service service) {
        this.service = service;
        this.requestedDurationSeconds = 1800;
    }

    protected SubscriptionCallback(Service service, int requestedDurationSeconds) {
        this.service = service;
        this.requestedDurationSeconds = Integer.valueOf(requestedDurationSeconds);
    }

    public Service getService() {
        return this.service;
    }

    public synchronized ControlPoint getControlPoint() {
        return this.controlPoint;
    }

    public synchronized void setControlPoint(ControlPoint controlPoint) {
        this.controlPoint = controlPoint;
    }

    public synchronized GENASubscription getSubscription() {
        return this.subscription;
    }

    public synchronized void setSubscription(GENASubscription subscription) {
        this.subscription = subscription;
    }

    @Override // java.lang.Runnable
    public synchronized void run() {
        if (getControlPoint() == null) {
            throw new IllegalStateException("Callback must be executed through ControlPoint");
        }
        if (getService() instanceof LocalService) {
            establishLocalSubscription((LocalService) this.service);
        } else if (getService() instanceof RemoteService) {
            establishRemoteSubscription((RemoteService) this.service);
        }
    }

    private void establishLocalSubscription(LocalService service) {
        if (getControlPoint().getRegistry().getLocalDevice(service.getDevice().getIdentity().getUdn(), false) == null) {
            log.fine("Local device service is currently not registered, failing subscription immediately");
            failed(null, null, new IllegalStateException("Local device is not registered"));
            return;
        }
        LocalGENASubscription localSubscription = null;
        try {
            localSubscription = new LocalGENASubscription(service, Integer.MAX_VALUE, Collections.EMPTY_LIST) { // from class: org.fourthline.cling.controlpoint.SubscriptionCallback.1
                public void failed(Exception ex) {
                    synchronized (SubscriptionCallback.this) {
                        SubscriptionCallback.this.setSubscription(null);
                        SubscriptionCallback.this.failed(null, null, ex);
                    }
                }

                @Override // org.fourthline.cling.model.gena.GENASubscription
                public void established() {
                    synchronized (SubscriptionCallback.this) {
                        SubscriptionCallback.this.setSubscription(this);
                        SubscriptionCallback.this.established(this);
                    }
                }

                @Override // org.fourthline.cling.model.gena.LocalGENASubscription
                public void ended(CancelReason reason) {
                    synchronized (SubscriptionCallback.this) {
                        SubscriptionCallback.this.setSubscription(null);
                        SubscriptionCallback.this.ended(this, reason, null);
                    }
                }

                @Override // org.fourthline.cling.model.gena.GENASubscription
                public void eventReceived() {
                    synchronized (SubscriptionCallback.this) {
                        Logger logger = SubscriptionCallback.log;
                        logger.fine("Local service state updated, notifying callback, sequence is: " + getCurrentSequence());
                        SubscriptionCallback.this.eventReceived(this);
                        incrementSequence();
                    }
                }
            };
            log.fine("Local device service is currently registered, also registering subscription");
            getControlPoint().getRegistry().addLocalSubscription(localSubscription);
            log.fine("Notifying subscription callback of local subscription availablity");
            localSubscription.establish();
            log.fine("Simulating first initial event for local subscription callback, sequence: " + localSubscription.getCurrentSequence());
            eventReceived(localSubscription);
            localSubscription.incrementSequence();
            log.fine("Starting to monitor state changes of local service");
            localSubscription.registerOnService();
        } catch (Exception ex) {
            log.fine("Local callback creation failed: " + ex.toString());
            log.log(Level.FINE, "Exception root cause: ", Exceptions.unwrap(ex));
            if (localSubscription != null) {
                getControlPoint().getRegistry().removeLocalSubscription(localSubscription);
            }
            failed(localSubscription, null, ex);
        }
    }

    private void establishRemoteSubscription(RemoteService service) {
        RemoteGENASubscription remoteSubscription = new RemoteGENASubscription(service, this.requestedDurationSeconds.intValue()) { // from class: org.fourthline.cling.controlpoint.SubscriptionCallback.2
            @Override // org.fourthline.cling.model.gena.RemoteGENASubscription
            public void failed(UpnpResponse responseStatus) {
                synchronized (SubscriptionCallback.this) {
                    SubscriptionCallback.this.setSubscription(null);
                    SubscriptionCallback.this.failed(this, responseStatus, null);
                }
            }

            @Override // org.fourthline.cling.model.gena.GENASubscription
            public void established() {
                synchronized (SubscriptionCallback.this) {
                    SubscriptionCallback.this.setSubscription(this);
                    SubscriptionCallback.this.established(this);
                }
            }

            @Override // org.fourthline.cling.model.gena.RemoteGENASubscription
            public void ended(CancelReason reason, UpnpResponse responseStatus) {
                synchronized (SubscriptionCallback.this) {
                    SubscriptionCallback.this.setSubscription(null);
                    SubscriptionCallback.this.ended(this, reason, responseStatus);
                }
            }

            @Override // org.fourthline.cling.model.gena.GENASubscription
            public void eventReceived() {
                synchronized (SubscriptionCallback.this) {
                    SubscriptionCallback.this.eventReceived(this);
                }
            }

            @Override // org.fourthline.cling.model.gena.RemoteGENASubscription
            public void eventsMissed(int numberOfMissedEvents) {
                synchronized (SubscriptionCallback.this) {
                    SubscriptionCallback.this.eventsMissed(this, numberOfMissedEvents);
                }
            }

            @Override // org.fourthline.cling.model.gena.RemoteGENASubscription
            public void invalidMessage(UnsupportedDataException ex) {
                synchronized (SubscriptionCallback.this) {
                    SubscriptionCallback.this.invalidMessage(this, ex);
                }
            }
        };
        try {
            SendingSubscribe protocol = getControlPoint().getProtocolFactory().createSendingSubscribe(remoteSubscription);
            protocol.run();
        } catch (ProtocolCreationException ex) {
            failed(this.subscription, null, ex);
        }
    }

    public synchronized void end() {
        if (this.subscription == null) {
            return;
        }
        if (this.subscription instanceof LocalGENASubscription) {
            endLocalSubscription((LocalGENASubscription) this.subscription);
        } else if (this.subscription instanceof RemoteGENASubscription) {
            endRemoteSubscription((RemoteGENASubscription) this.subscription);
        }
    }

    private void endLocalSubscription(LocalGENASubscription subscription) {
        Logger logger = log;
        logger.fine("Removing local subscription and ending it in callback: " + subscription);
        getControlPoint().getRegistry().removeLocalSubscription(subscription);
        subscription.end(null);
    }

    private void endRemoteSubscription(RemoteGENASubscription subscription) {
        Logger logger = log;
        logger.fine("Ending remote subscription: " + subscription);
        getControlPoint().getConfiguration().getSyncProtocolExecutorService().execute(getControlPoint().getProtocolFactory().createSendingUnsubscribe(subscription));
    }

    protected void failed(GENASubscription subscription, UpnpResponse responseStatus, Exception exception) {
        failed(subscription, responseStatus, exception, createDefaultFailureMessage(responseStatus, exception));
    }

    public static String createDefaultFailureMessage(UpnpResponse responseStatus, Exception exception) {
        if (responseStatus != null) {
            String message = "Subscription failed:  HTTP response was: " + responseStatus.getResponseDetails();
            return message;
        } else if (exception != null) {
            String message2 = "Subscription failed:  Exception occured: " + exception;
            return message2;
        } else {
            String message3 = "Subscription failed:  No response received.";
            return message3;
        }
    }

    protected void invalidMessage(RemoteGENASubscription remoteGENASubscription, UnsupportedDataException ex) {
        Logger logger = log;
        logger.info("Invalid event message received, causing: " + ex);
        if (log.isLoggable(Level.FINE)) {
            log.fine("------------------------------------------------------------------------------");
            log.fine(ex.getData() != null ? ex.getData().toString() : "null");
            log.fine("------------------------------------------------------------------------------");
        }
    }

    public String toString() {
        return "(SubscriptionCallback) " + getService();
    }
}
