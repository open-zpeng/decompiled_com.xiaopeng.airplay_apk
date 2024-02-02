package org.eclipse.jetty.servlet;

import com.xpeng.airplay.service.NsdConstants;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class StatisticsServlet extends HttpServlet {
    private static final Logger LOG = Log.getLogger(StatisticsServlet.class);
    private Connector[] _connectors;
    private MemoryMXBean _memoryBean;
    boolean _restrictToLocalhost = true;
    private StatisticsHandler _statsHandler;

    @Override // javax.servlet.GenericServlet
    public void init() throws ServletException {
        ServletContext context = getServletContext();
        ContextHandler.Context scontext = (ContextHandler.Context) context;
        Server _server = scontext.getContextHandler().getServer();
        Handler handler = _server.getChildHandlerByClass(StatisticsHandler.class);
        if (handler != null) {
            this._statsHandler = (StatisticsHandler) handler;
            this._memoryBean = ManagementFactory.getMemoryMXBean();
            this._connectors = _server.getConnectors();
            if (getInitParameter("restrictToLocalhost") != null) {
                this._restrictToLocalhost = NsdConstants.AIRPLAY_TXT_VALUE_DA.equals(getInitParameter("restrictToLocalhost"));
                return;
            }
            return;
        }
        LOG.warn("Statistics Handler not installed!", new Object[0]);
    }

    @Override // javax.servlet.http.HttpServlet
    public void doPost(HttpServletRequest sreq, HttpServletResponse sres) throws ServletException, IOException {
        doGet(sreq, sres);
    }

    @Override // javax.servlet.http.HttpServlet
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (this._statsHandler == null) {
            LOG.warn("Statistics Handler not installed!", new Object[0]);
            resp.sendError(503);
        } else if (this._restrictToLocalhost && !isLoopbackAddress(req.getRemoteAddr())) {
            resp.sendError(503);
        } else {
            String wantXml = req.getParameter("xml");
            if (wantXml == null) {
                wantXml = req.getParameter("XML");
            }
            if (wantXml != null && NsdConstants.AIRPLAY_TXT_VALUE_DA.equalsIgnoreCase(wantXml)) {
                sendXmlResponse(resp);
            } else {
                sendTextResponse(resp);
            }
        }
    }

    private boolean isLoopbackAddress(String address) {
        try {
            InetAddress addr = InetAddress.getByName(address);
            return addr.isLoopbackAddress();
        } catch (UnknownHostException e) {
            Logger logger = LOG;
            logger.warn("Warning: attempt to access statistics servlet from " + address, e);
            return false;
        }
    }

    private void sendXmlResponse(HttpServletResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<statistics>\n");
        sb.append("  <requests>\n");
        sb.append("    <statsOnMs>");
        sb.append(this._statsHandler.getStatsOnMs());
        sb.append("</statsOnMs>\n");
        sb.append("    <requests>");
        sb.append(this._statsHandler.getRequests());
        sb.append("</requests>\n");
        sb.append("    <requestsActive>");
        sb.append(this._statsHandler.getRequestsActive());
        sb.append("</requestsActive>\n");
        sb.append("    <requestsActiveMax>");
        sb.append(this._statsHandler.getRequestsActiveMax());
        sb.append("</requestsActiveMax>\n");
        sb.append("    <requestsTimeTotal>");
        sb.append(this._statsHandler.getRequestTimeTotal());
        sb.append("</requestsTimeTotal>\n");
        sb.append("    <requestsTimeMean>");
        sb.append(this._statsHandler.getRequestTimeMean());
        sb.append("</requestsTimeMean>\n");
        sb.append("    <requestsTimeMax>");
        sb.append(this._statsHandler.getRequestTimeMax());
        sb.append("</requestsTimeMax>\n");
        sb.append("    <requestsTimeStdDev>");
        sb.append(this._statsHandler.getRequestTimeStdDev());
        sb.append("</requestsTimeStdDev>\n");
        sb.append("    <dispatched>");
        sb.append(this._statsHandler.getDispatched());
        sb.append("</dispatched>\n");
        sb.append("    <dispatchedActive>");
        sb.append(this._statsHandler.getDispatchedActive());
        sb.append("</dispatchedActive>\n");
        sb.append("    <dispatchedActiveMax>");
        sb.append(this._statsHandler.getDispatchedActiveMax());
        sb.append("</dispatchedActiveMax>\n");
        sb.append("    <dispatchedTimeTotal>");
        sb.append(this._statsHandler.getDispatchedTimeTotal());
        sb.append("</dispatchedTimeTotal>\n");
        sb.append("    <dispatchedTimeMean>");
        sb.append(this._statsHandler.getDispatchedTimeMean());
        sb.append("</dispatchedTimeMean>\n");
        sb.append("    <dispatchedTimeMax>");
        sb.append(this._statsHandler.getDispatchedTimeMax());
        sb.append("</dispatchedTimeMax>\n");
        sb.append("    <dispatchedTimeStdDev>");
        sb.append(this._statsHandler.getDispatchedTimeStdDev());
        sb.append("</dispatchedTimeStdDev>\n");
        sb.append("    <requestsSuspended>");
        sb.append(this._statsHandler.getSuspends());
        sb.append("</requestsSuspended>\n");
        sb.append("    <requestsExpired>");
        sb.append(this._statsHandler.getExpires());
        sb.append("</requestsExpired>\n");
        sb.append("    <requestsResumed>");
        sb.append(this._statsHandler.getResumes());
        sb.append("</requestsResumed>\n");
        sb.append("  </requests>\n");
        sb.append("  <responses>\n");
        sb.append("    <responses1xx>");
        sb.append(this._statsHandler.getResponses1xx());
        sb.append("</responses1xx>\n");
        sb.append("    <responses2xx>");
        sb.append(this._statsHandler.getResponses2xx());
        sb.append("</responses2xx>\n");
        sb.append("    <responses3xx>");
        sb.append(this._statsHandler.getResponses3xx());
        sb.append("</responses3xx>\n");
        sb.append("    <responses4xx>");
        sb.append(this._statsHandler.getResponses4xx());
        sb.append("</responses4xx>\n");
        sb.append("    <responses5xx>");
        sb.append(this._statsHandler.getResponses5xx());
        sb.append("</responses5xx>\n");
        sb.append("    <responsesBytesTotal>");
        sb.append(this._statsHandler.getResponsesBytesTotal());
        sb.append("</responsesBytesTotal>\n");
        sb.append("  </responses>\n");
        sb.append("  <connections>\n");
        Connector[] arr$ = this._connectors;
        for (Connector connector : arr$) {
            sb.append("    <connector>\n");
            sb.append("      <name>");
            sb.append(connector.getName());
            sb.append("</name>\n");
            sb.append("      <statsOn>");
            sb.append(connector.getStatsOn());
            sb.append("</statsOn>\n");
            if (connector.getStatsOn()) {
                sb.append("    <statsOnMs>");
                sb.append(connector.getStatsOnMs());
                sb.append("</statsOnMs>\n");
                sb.append("    <connections>");
                sb.append(connector.getConnections());
                sb.append("</connections>\n");
                sb.append("    <connectionsOpen>");
                sb.append(connector.getConnectionsOpen());
                sb.append("</connectionsOpen>\n");
                sb.append("    <connectionsOpenMax>");
                sb.append(connector.getConnectionsOpenMax());
                sb.append("</connectionsOpenMax>\n");
                sb.append("    <connectionsDurationTotal>");
                sb.append(connector.getConnectionsDurationTotal());
                sb.append("</connectionsDurationTotal>\n");
                sb.append("    <connectionsDurationMean>");
                sb.append(connector.getConnectionsDurationMean());
                sb.append("</connectionsDurationMean>\n");
                sb.append("    <connectionsDurationMax>");
                sb.append(connector.getConnectionsDurationMax());
                sb.append("</connectionsDurationMax>\n");
                sb.append("    <connectionsDurationStdDev>");
                sb.append(connector.getConnectionsDurationStdDev());
                sb.append("</connectionsDurationStdDev>\n");
                sb.append("    <requests>");
                sb.append(connector.getRequests());
                sb.append("</requests>\n");
                sb.append("    <connectionsRequestsMean>");
                sb.append(connector.getConnectionsRequestsMean());
                sb.append("</connectionsRequestsMean>\n");
                sb.append("    <connectionsRequestsMax>");
                sb.append(connector.getConnectionsRequestsMax());
                sb.append("</connectionsRequestsMax>\n");
                sb.append("    <connectionsRequestsStdDev>");
                sb.append(connector.getConnectionsRequestsStdDev());
                sb.append("</connectionsRequestsStdDev>\n");
            }
            sb.append("    </connector>\n");
        }
        sb.append("  </connections>\n");
        sb.append("  <memory>\n");
        sb.append("    <heapMemoryUsage>");
        sb.append(this._memoryBean.getHeapMemoryUsage().getUsed());
        sb.append("</heapMemoryUsage>\n");
        sb.append("    <nonHeapMemoryUsage>");
        sb.append(this._memoryBean.getNonHeapMemoryUsage().getUsed());
        sb.append("</nonHeapMemoryUsage>\n");
        sb.append("  </memory>\n");
        sb.append("</statistics>\n");
        response.setContentType(MimeTypes.TEXT_XML);
        PrintWriter pout = response.getWriter();
        pout.write(sb.toString());
    }

    private void sendTextResponse(HttpServletResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(this._statsHandler.toStatsHTML());
        sb.append("<h2>Connections:</h2>\n");
        Connector[] arr$ = this._connectors;
        for (Connector connector : arr$) {
            sb.append("<h3>");
            sb.append(connector.getName());
            sb.append("</h3>");
            if (connector.getStatsOn()) {
                sb.append("Statistics gathering started ");
                sb.append(connector.getStatsOnMs());
                sb.append("ms ago");
                sb.append("<br />\n");
                sb.append("Total connections: ");
                sb.append(connector.getConnections());
                sb.append("<br />\n");
                sb.append("Current connections open: ");
                sb.append(connector.getConnectionsOpen());
                sb.append("<br />\n");
                sb.append("Max concurrent connections open: ");
                sb.append(connector.getConnectionsOpenMax());
                sb.append("<br />\n");
                sb.append("Total connections duration: ");
                sb.append(connector.getConnectionsDurationTotal());
                sb.append("<br />\n");
                sb.append("Mean connection duration: ");
                sb.append(connector.getConnectionsDurationMean());
                sb.append("<br />\n");
                sb.append("Max connection duration: ");
                sb.append(connector.getConnectionsDurationMax());
                sb.append("<br />\n");
                sb.append("Connection duration standard deviation: ");
                sb.append(connector.getConnectionsDurationStdDev());
                sb.append("<br />\n");
                sb.append("Total requests: ");
                sb.append(connector.getRequests());
                sb.append("<br />\n");
                sb.append("Mean requests per connection: ");
                sb.append(connector.getConnectionsRequestsMean());
                sb.append("<br />\n");
                sb.append("Max requests per connection: ");
                sb.append(connector.getConnectionsRequestsMax());
                sb.append("<br />\n");
                sb.append("Requests per connection standard deviation: ");
                sb.append(connector.getConnectionsRequestsStdDev());
                sb.append("<br />\n");
            } else {
                sb.append("Statistics gathering off.\n");
            }
        }
        sb.append("<h2>Memory:</h2>\n");
        sb.append("Heap memory usage: ");
        sb.append(this._memoryBean.getHeapMemoryUsage().getUsed());
        sb.append(" bytes");
        sb.append("<br />\n");
        sb.append("Non-heap memory usage: ");
        sb.append(this._memoryBean.getNonHeapMemoryUsage().getUsed());
        sb.append(" bytes");
        sb.append("<br />\n");
        response.setContentType(MimeTypes.TEXT_HTML);
        PrintWriter pout = response.getWriter();
        pout.write(sb.toString());
    }
}
