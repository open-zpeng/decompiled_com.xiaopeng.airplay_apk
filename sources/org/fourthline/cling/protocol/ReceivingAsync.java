package org.fourthline.cling.protocol;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.message.UpnpMessage;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.transport.RouterException;
import org.seamless.util.Exceptions;
/* loaded from: classes.dex */
public abstract class ReceivingAsync<M extends UpnpMessage> implements Runnable {
    private static final Logger log = Logger.getLogger(UpnpService.class.getName());
    private M inputMessage;
    private final UpnpService upnpService;

    protected abstract void execute() throws RouterException;

    /* JADX INFO: Access modifiers changed from: protected */
    public ReceivingAsync(UpnpService upnpService, M inputMessage) {
        this.upnpService = upnpService;
        this.inputMessage = inputMessage;
    }

    public UpnpService getUpnpService() {
        return this.upnpService;
    }

    public void setInputMessage(M inputMessage) {
        this.inputMessage = inputMessage;
    }

    public M getInputMessage() {
        return this.inputMessage;
    }

    @Override // java.lang.Runnable
    public void run() {
        boolean proceed;
        try {
            proceed = waitBeforeExecution();
        } catch (InterruptedException e) {
            Logger logger = log;
            logger.info("Protocol wait before execution interrupted (on shutdown?): " + getClass().getSimpleName());
            proceed = false;
        }
        if (proceed) {
            try {
                execute();
            } catch (Exception ex) {
                Throwable cause = Exceptions.unwrap(ex);
                if (cause instanceof InterruptedException) {
                    Logger logger2 = log;
                    Level level = Level.INFO;
                    logger2.log(level, "Interrupted protocol '" + getClass().getSimpleName() + "': " + ex, cause);
                } else if (cause instanceof RouterException) {
                    Logger logger3 = log;
                    Level level2 = Level.INFO;
                    logger3.log(level2, "Router exception: " + getClass().getSimpleName());
                } else {
                    throw new RuntimeException("Fatal error while executing protocol '" + getClass().getSimpleName() + "': " + ex, ex);
                }
            }
        }
    }

    protected boolean waitBeforeExecution() throws InterruptedException {
        return true;
    }

    protected <H extends UpnpHeader> H getFirstHeader(UpnpHeader.Type headerType, Class<H> subtype) {
        return (H) getInputMessage().getHeaders().getFirstHeader(headerType, subtype);
    }

    public String toString() {
        return "(" + getClass().getSimpleName() + ")";
    }
}
