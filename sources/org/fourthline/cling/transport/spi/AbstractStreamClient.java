package org.fourthline.cling.transport.spi;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.transport.spi.StreamClientConfiguration;
import org.seamless.util.Exceptions;
/* loaded from: classes.dex */
public abstract class AbstractStreamClient<C extends StreamClientConfiguration, REQUEST> implements StreamClient<C> {
    private static final Logger log = Logger.getLogger(StreamClient.class.getName());

    protected abstract void abort(REQUEST request);

    protected abstract Callable<StreamResponseMessage> createCallable(StreamRequestMessage streamRequestMessage, REQUEST request);

    protected abstract REQUEST createRequest(StreamRequestMessage streamRequestMessage);

    protected abstract boolean logExecutionException(Throwable th);

    @Override // org.fourthline.cling.transport.spi.StreamClient
    public StreamResponseMessage sendRequest(StreamRequestMessage requestMessage) throws InterruptedException {
        if (log.isLoggable(Level.FINE)) {
            Logger logger = log;
            logger.fine("Preparing HTTP request: " + requestMessage);
        }
        REQUEST request = createRequest(requestMessage);
        if (request == null) {
            return null;
        }
        Callable<StreamResponseMessage> callable = createCallable(requestMessage, request);
        long start = System.currentTimeMillis();
        Future<StreamResponseMessage> future = getConfiguration().getRequestExecutorService().submit(callable);
        try {
            try {
                if (log.isLoggable(Level.FINE)) {
                    Logger logger2 = log;
                    logger2.fine("Waiting " + getConfiguration().getTimeoutSeconds() + " seconds for HTTP request to complete: " + requestMessage);
                }
                StreamResponseMessage response = future.get(getConfiguration().getTimeoutSeconds(), TimeUnit.SECONDS);
                long elapsed = System.currentTimeMillis() - start;
                if (log.isLoggable(Level.FINEST)) {
                    Logger logger3 = log;
                    logger3.finest("Got HTTP response in " + elapsed + "ms: " + requestMessage);
                }
                if (getConfiguration().getLogWarningSeconds() > 0 && elapsed > getConfiguration().getLogWarningSeconds() * 1000) {
                    Logger logger4 = log;
                    logger4.warning("HTTP request took a long time (" + elapsed + "ms): " + requestMessage);
                }
                onFinally(request);
                return response;
            } catch (InterruptedException e) {
                if (log.isLoggable(Level.FINE)) {
                    Logger logger5 = log;
                    logger5.fine("Interruption, aborting request: " + requestMessage);
                }
                abort(request);
                throw new InterruptedException("HTTP request interrupted and aborted");
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause();
                if (!logExecutionException(cause)) {
                    Logger logger6 = log;
                    Level level = Level.WARNING;
                    logger6.log(level, "HTTP request failed: " + requestMessage, Exceptions.unwrap(cause));
                }
                onFinally(request);
                return null;
            } catch (TimeoutException e2) {
                Logger logger7 = log;
                logger7.info("Timeout of " + getConfiguration().getTimeoutSeconds() + " seconds while waiting for HTTP request to complete, aborting: " + requestMessage);
                abort(request);
                onFinally(request);
                return null;
            }
        } catch (Throwable th) {
            onFinally(request);
            throw th;
        }
    }

    protected void onFinally(REQUEST request) {
    }
}
