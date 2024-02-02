package org.eclipse.jetty.server.handler;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationListener;
import org.eclipse.jetty.server.AsyncContinuation;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.statistic.CounterStatistic;
import org.eclipse.jetty.util.statistic.SampleStatistic;
/* loaded from: classes.dex */
public class StatisticsHandler extends HandlerWrapper {
    private final AtomicLong _statsStartedAt = new AtomicLong();
    private final CounterStatistic _requestStats = new CounterStatistic();
    private final SampleStatistic _requestTimeStats = new SampleStatistic();
    private final CounterStatistic _dispatchedStats = new CounterStatistic();
    private final SampleStatistic _dispatchedTimeStats = new SampleStatistic();
    private final CounterStatistic _suspendStats = new CounterStatistic();
    private final AtomicInteger _resumes = new AtomicInteger();
    private final AtomicInteger _expires = new AtomicInteger();
    private final AtomicInteger _responses1xx = new AtomicInteger();
    private final AtomicInteger _responses2xx = new AtomicInteger();
    private final AtomicInteger _responses3xx = new AtomicInteger();
    private final AtomicInteger _responses4xx = new AtomicInteger();
    private final AtomicInteger _responses5xx = new AtomicInteger();
    private final AtomicLong _responsesTotalBytes = new AtomicLong();
    private final ContinuationListener _onCompletion = new ContinuationListener() { // from class: org.eclipse.jetty.server.handler.StatisticsHandler.1
        @Override // org.eclipse.jetty.continuation.ContinuationListener
        public void onComplete(Continuation continuation) {
            Request request = ((AsyncContinuation) continuation).getBaseRequest();
            long elapsed = System.currentTimeMillis() - request.getTimeStamp();
            StatisticsHandler.this._requestStats.decrement();
            StatisticsHandler.this._requestTimeStats.set(elapsed);
            StatisticsHandler.this.updateResponse(request);
            if (!continuation.isResumed()) {
                StatisticsHandler.this._suspendStats.decrement();
            }
        }

        @Override // org.eclipse.jetty.continuation.ContinuationListener
        public void onTimeout(Continuation continuation) {
            StatisticsHandler.this._expires.incrementAndGet();
        }
    };

    public void statsReset() {
        this._statsStartedAt.set(System.currentTimeMillis());
        this._requestStats.reset();
        this._requestTimeStats.reset();
        this._dispatchedStats.reset();
        this._dispatchedTimeStats.reset();
        this._suspendStats.reset();
        this._resumes.set(0);
        this._expires.set(0);
        this._responses1xx.set(0);
        this._responses2xx.set(0);
        this._responses3xx.set(0);
        this._responses4xx.set(0);
        this._responses5xx.set(0);
        this._responsesTotalBytes.set(0L);
    }

    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.Handler
    public void handle(String path, Request request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException {
        long start;
        this._dispatchedStats.increment();
        AsyncContinuation continuation = request.getAsyncContinuation();
        if (continuation.isInitial()) {
            this._requestStats.increment();
            start = request.getTimeStamp();
        } else {
            start = System.currentTimeMillis();
            this._suspendStats.decrement();
            if (continuation.isResumed()) {
                this._resumes.incrementAndGet();
            }
        }
        try {
            super.handle(path, request, httpRequest, httpResponse);
            long now = System.currentTimeMillis();
            long dispatched = now - start;
            this._dispatchedStats.decrement();
            this._dispatchedTimeStats.set(dispatched);
            if (continuation.isSuspended()) {
                if (continuation.isInitial()) {
                    continuation.addContinuationListener(this._onCompletion);
                }
                this._suspendStats.increment();
            } else if (continuation.isInitial()) {
                this._requestStats.decrement();
                this._requestTimeStats.set(dispatched);
                updateResponse(request);
            }
        } catch (Throwable th) {
            long now2 = System.currentTimeMillis();
            long dispatched2 = now2 - start;
            this._dispatchedStats.decrement();
            this._dispatchedTimeStats.set(dispatched2);
            if (continuation.isSuspended()) {
                if (continuation.isInitial()) {
                    continuation.addContinuationListener(this._onCompletion);
                }
                this._suspendStats.increment();
            } else if (continuation.isInitial()) {
                this._requestStats.decrement();
                this._requestTimeStats.set(dispatched2);
                updateResponse(request);
            }
            throw th;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateResponse(Request request) {
        Response response = request.getResponse();
        switch (response.getStatus() / 100) {
            case 1:
                this._responses1xx.incrementAndGet();
                break;
            case 2:
                this._responses2xx.incrementAndGet();
                break;
            case 3:
                this._responses3xx.incrementAndGet();
                break;
            case 4:
                this._responses4xx.incrementAndGet();
                break;
            case 5:
                this._responses5xx.incrementAndGet();
                break;
        }
        this._responsesTotalBytes.addAndGet(response.getContentCount());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        super.doStart();
        statsReset();
    }

    public int getRequests() {
        return (int) this._requestStats.getTotal();
    }

    public int getRequestsActive() {
        return (int) this._requestStats.getCurrent();
    }

    public int getRequestsActiveMax() {
        return (int) this._requestStats.getMax();
    }

    public long getRequestTimeMax() {
        return this._requestTimeStats.getMax();
    }

    public long getRequestTimeTotal() {
        return this._requestTimeStats.getTotal();
    }

    public double getRequestTimeMean() {
        return this._requestTimeStats.getMean();
    }

    public double getRequestTimeStdDev() {
        return this._requestTimeStats.getStdDev();
    }

    public int getDispatched() {
        return (int) this._dispatchedStats.getTotal();
    }

    public int getDispatchedActive() {
        return (int) this._dispatchedStats.getCurrent();
    }

    public int getDispatchedActiveMax() {
        return (int) this._dispatchedStats.getMax();
    }

    public long getDispatchedTimeMax() {
        return this._dispatchedTimeStats.getMax();
    }

    public long getDispatchedTimeTotal() {
        return this._dispatchedTimeStats.getTotal();
    }

    public double getDispatchedTimeMean() {
        return this._dispatchedTimeStats.getMean();
    }

    public double getDispatchedTimeStdDev() {
        return this._dispatchedTimeStats.getStdDev();
    }

    public int getSuspends() {
        return (int) this._suspendStats.getTotal();
    }

    public int getSuspendsActive() {
        return (int) this._suspendStats.getCurrent();
    }

    public int getSuspendsActiveMax() {
        return (int) this._suspendStats.getMax();
    }

    public int getResumes() {
        return this._resumes.get();
    }

    public int getExpires() {
        return this._expires.get();
    }

    public int getResponses1xx() {
        return this._responses1xx.get();
    }

    public int getResponses2xx() {
        return this._responses2xx.get();
    }

    public int getResponses3xx() {
        return this._responses3xx.get();
    }

    public int getResponses4xx() {
        return this._responses4xx.get();
    }

    public int getResponses5xx() {
        return this._responses5xx.get();
    }

    public long getStatsOnMs() {
        return System.currentTimeMillis() - this._statsStartedAt.get();
    }

    public long getResponsesBytesTotal() {
        return this._responsesTotalBytes.get();
    }

    public String toStatsHTML() {
        return "<h1>Statistics:</h1>\nStatistics gathering started " + getStatsOnMs() + "ms ago<br />\n<h2>Requests:</h2>\nTotal requests: " + getRequests() + "<br />\nActive requests: " + getRequestsActive() + "<br />\nMax active requests: " + getRequestsActiveMax() + "<br />\nTotal requests time: " + getRequestTimeTotal() + "<br />\nMean request time: " + getRequestTimeMean() + "<br />\nMax request time: " + getRequestTimeMax() + "<br />\nRequest time standard deviation: " + getRequestTimeStdDev() + "<br />\n<h2>Dispatches:</h2>\nTotal dispatched: " + getDispatched() + "<br />\nActive dispatched: " + getDispatchedActive() + "<br />\nMax active dispatched: " + getDispatchedActiveMax() + "<br />\nTotal dispatched time: " + getDispatchedTimeTotal() + "<br />\nMean dispatched time: " + getDispatchedTimeMean() + "<br />\nMax dispatched time: " + getDispatchedTimeMax() + "<br />\nDispatched time standard deviation: " + getDispatchedTimeStdDev() + "<br />\nTotal requests suspended: " + getSuspends() + "<br />\nTotal requests expired: " + getExpires() + "<br />\nTotal requests resumed: " + getResumes() + "<br />\n<h2>Responses:</h2>\n1xx responses: " + getResponses1xx() + "<br />\n2xx responses: " + getResponses2xx() + "<br />\n3xx responses: " + getResponses3xx() + "<br />\n4xx responses: " + getResponses4xx() + "<br />\n5xx responses: " + getResponses5xx() + "<br />\nBytes sent total: " + getResponsesBytesTotal() + "<br />\n";
    }
}
