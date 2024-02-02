package org.eclipse.jetty.servlet;

import com.xpeng.airplay.service.NsdConstants;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpContent;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.io.WriterOutputStream;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpOutput;
import org.eclipse.jetty.server.InclusiveByteRange;
import org.eclipse.jetty.server.ResourceCache;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.nio.NIOConnector;
import org.eclipse.jetty.server.ssl.SslConnector;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.MultiPartOutputStream;
import org.eclipse.jetty.util.QuotedStringTokenizer;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.util.resource.ResourceFactory;
/* loaded from: classes.dex */
public class DefaultServlet extends HttpServlet implements ResourceFactory {
    private static final Logger LOG = Log.getLogger(DefaultServlet.class);
    private static final long serialVersionUID = 4930458713846881193L;
    private ResourceCache _cache;
    private ByteArrayBuffer _cacheControl;
    private ContextHandler _contextHandler;
    private ServletHolder _defaultHolder;
    private MimeTypes _mimeTypes;
    private String _relativeResourceBase;
    private Resource _resourceBase;
    private ServletContext _servletContext;
    private ServletHandler _servletHandler;
    private Resource _stylesheet;
    private String[] _welcomes;
    private boolean _acceptRanges = true;
    private boolean _dirAllowed = true;
    private boolean _welcomeServlets = false;
    private boolean _welcomeExactServlets = false;
    private boolean _redirectWelcome = false;
    private boolean _gzip = true;
    private boolean _pathInfoOnly = false;
    private boolean _etags = false;
    private boolean _useFileMappedBuffer = false;

    @Override // javax.servlet.GenericServlet
    public void init() throws UnavailableException {
        this._servletContext = getServletContext();
        this._contextHandler = initContextHandler(this._servletContext);
        this._mimeTypes = this._contextHandler.getMimeTypes();
        this._welcomes = this._contextHandler.getWelcomeFiles();
        if (this._welcomes == null) {
            this._welcomes = new String[]{"index.html", "index.jsp"};
        }
        this._acceptRanges = getInitBoolean("acceptRanges", this._acceptRanges);
        this._dirAllowed = getInitBoolean("dirAllowed", this._dirAllowed);
        this._redirectWelcome = getInitBoolean("redirectWelcome", this._redirectWelcome);
        this._gzip = getInitBoolean(HttpHeaderValues.GZIP, this._gzip);
        this._pathInfoOnly = getInitBoolean("pathInfoOnly", this._pathInfoOnly);
        if (!"exact".equals(getInitParameter("welcomeServlets"))) {
            this._welcomeServlets = getInitBoolean("welcomeServlets", this._welcomeServlets);
        } else {
            this._welcomeExactServlets = true;
            this._welcomeServlets = false;
        }
        if (getInitParameter("aliases") != null) {
            this._contextHandler.setAliases(getInitBoolean("aliases", false));
        }
        boolean aliases = this._contextHandler.isAliases();
        if (!aliases && !FileResource.getCheckAliases()) {
            throw new IllegalStateException("Alias checking disabled");
        }
        if (aliases) {
            this._servletContext.log("Aliases are enabled! Security constraints may be bypassed!!!");
        }
        this._useFileMappedBuffer = getInitBoolean("useFileMappedBuffer", this._useFileMappedBuffer);
        this._relativeResourceBase = getInitParameter("relativeResourceBase");
        String rb = getInitParameter("resourceBase");
        if (rb != null) {
            if (this._relativeResourceBase == null) {
                try {
                    this._resourceBase = this._contextHandler.newResource(rb);
                } catch (Exception e) {
                    LOG.warn(Log.EXCEPTION, e);
                    throw new UnavailableException(e.toString());
                }
            } else {
                throw new UnavailableException("resourceBase & relativeResourceBase");
            }
        }
        String css = getInitParameter("stylesheet");
        if (css != null) {
            try {
                this._stylesheet = Resource.newResource(css);
                if (!this._stylesheet.exists()) {
                    LOG.warn("!" + css, new Object[0]);
                    this._stylesheet = null;
                }
            } catch (Exception e2) {
                LOG.warn(e2.toString(), new Object[0]);
                LOG.debug(e2);
            }
        }
        if (this._stylesheet == null) {
            this._stylesheet = Resource.newResource(getClass().getResource("/jetty-dir.css"));
        }
        String t = getInitParameter("cacheControl");
        if (t != null) {
            this._cacheControl = new ByteArrayBuffer(t);
        }
        String resourceCache = getInitParameter("resourceCache");
        int max_cache_size = getInitInt("maxCacheSize", -2);
        int max_cached_file_size = getInitInt("maxCachedFileSize", -2);
        int max_cached_files = getInitInt("maxCachedFiles", -2);
        if (resourceCache != null) {
            if (max_cache_size != -1 || max_cached_file_size != -2 || max_cached_files != -2) {
                LOG.debug("ignoring resource cache configuration, using resourceCache attribute", new Object[0]);
            }
            if (this._relativeResourceBase == null && this._resourceBase == null) {
                this._cache = (ResourceCache) this._servletContext.getAttribute(resourceCache);
                LOG.debug("Cache {}={}", resourceCache, this._cache);
            } else {
                throw new UnavailableException("resourceCache specified with resource bases");
            }
        }
        this._etags = getInitBoolean("etags", this._etags);
        try {
            if (this._cache == null && max_cached_files > 0) {
                try {
                    this._cache = new ResourceCache(null, this, this._mimeTypes, this._useFileMappedBuffer, this._etags);
                    if (max_cache_size > 0) {
                        this._cache.setMaxCacheSize(max_cache_size);
                    }
                    if (max_cached_file_size >= -1) {
                        this._cache.setMaxCachedFileSize(max_cached_file_size);
                    }
                    if (max_cached_files >= -1) {
                        this._cache.setMaxCachedFiles(max_cached_files);
                    }
                } catch (Exception e3) {
                    e = e3;
                    LOG.warn(Log.EXCEPTION, e);
                    throw new UnavailableException(e.toString());
                }
            }
            this._servletHandler = (ServletHandler) this._contextHandler.getChildHandlerByClass(ServletHandler.class);
            ServletHolder[] arr$ = this._servletHandler.getServlets();
            for (ServletHolder h : arr$) {
                if (h.getServletInstance() == this) {
                    this._defaultHolder = h;
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("resource base = " + this._resourceBase, new Object[0]);
            }
        } catch (Exception e4) {
            e = e4;
        }
    }

    protected ContextHandler initContextHandler(ServletContext servletContext) {
        ContextHandler.Context scontext = ContextHandler.getCurrentContext();
        if (scontext == null) {
            if (servletContext instanceof ContextHandler.Context) {
                return ((ContextHandler.Context) servletContext).getContextHandler();
            }
            throw new IllegalArgumentException("The servletContext " + servletContext + " " + servletContext.getClass().getName() + " is not " + ContextHandler.Context.class.getName());
        }
        return ContextHandler.getCurrentContext().getContextHandler();
    }

    @Override // javax.servlet.GenericServlet, javax.servlet.ServletConfig
    public String getInitParameter(String name) {
        ServletContext servletContext = getServletContext();
        String value = servletContext.getInitParameter("org.eclipse.jetty.servlet.Default." + name);
        if (value == null) {
            return super.getInitParameter(name);
        }
        return value;
    }

    private boolean getInitBoolean(String name, boolean dft) {
        String value = getInitParameter(name);
        if (value == null || value.length() == 0) {
            return dft;
        }
        return value.startsWith("t") || value.startsWith("T") || value.startsWith("y") || value.startsWith("Y") || value.startsWith(NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS);
    }

    private int getInitInt(String name, int dft) {
        String value = getInitParameter(name);
        if (value == null) {
            value = getInitParameter(name);
        }
        if (value != null && value.length() > 0) {
            return Integer.parseInt(value);
        }
        return dft;
    }

    @Override // org.eclipse.jetty.util.resource.ResourceFactory
    public Resource getResource(String pathInContext) {
        Resource r = null;
        if (this._relativeResourceBase != null) {
            pathInContext = URIUtil.addPaths(this._relativeResourceBase, pathInContext);
        }
        try {
            if (this._resourceBase != null) {
                r = this._resourceBase.addPath(pathInContext);
                if (!this._contextHandler.checkAlias(pathInContext, r)) {
                    r = null;
                }
            } else if (this._servletContext instanceof ContextHandler.Context) {
                r = this._contextHandler.getResource(pathInContext);
            } else {
                URL u = this._servletContext.getResource(pathInContext);
                r = this._contextHandler.newResource(u);
            }
            if (LOG.isDebugEnabled()) {
                Logger logger = LOG;
                logger.debug("Resource " + pathInContext + "=" + r, new Object[0]);
            }
        } catch (IOException e) {
            LOG.ignore(e);
        }
        if ((r == null || !r.exists()) && pathInContext.endsWith("/jetty-dir.css")) {
            Resource r2 = this._stylesheet;
            return r2;
        }
        return r;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:178:0x0327  */
    /* JADX WARN: Removed duplicated region for block: B:179:0x032b  */
    /* JADX WARN: Removed duplicated region for block: B:197:0x036d A[Catch: all -> 0x0383, TRY_LEAVE, TryCatch #1 {all -> 0x0383, blocks: (B:195:0x0360, B:197:0x036d), top: B:211:0x0360 }] */
    /* JADX WARN: Removed duplicated region for block: B:199:0x0378  */
    /* JADX WARN: Removed duplicated region for block: B:200:0x037c  */
    /* JADX WARN: Removed duplicated region for block: B:207:0x0387  */
    /* JADX WARN: Removed duplicated region for block: B:209:0x038d  */
    /* JADX WARN: Type inference failed for: r11v0, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r11v1 */
    /* JADX WARN: Type inference failed for: r11v12 */
    /* JADX WARN: Type inference failed for: r11v16 */
    /* JADX WARN: Type inference failed for: r11v2, types: [org.eclipse.jetty.util.resource.Resource] */
    /* JADX WARN: Type inference failed for: r11v3 */
    /* JADX WARN: Type inference failed for: r11v5 */
    /* JADX WARN: Type inference failed for: r11v7 */
    @Override // javax.servlet.http.HttpServlet
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    protected void doGet(javax.servlet.http.HttpServletRequest r22, javax.servlet.http.HttpServletResponse r23) throws javax.servlet.ServletException, java.io.IOException {
        /*
            Method dump skipped, instructions count: 913
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.servlet.DefaultServlet.doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse):void");
    }

    private boolean hasDefinedRange(Enumeration<String> reqRanges) {
        return reqRanges != null && reqRanges.hasMoreElements();
    }

    @Override // javax.servlet.http.HttpServlet
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override // javax.servlet.http.HttpServlet
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(405);
    }

    @Override // javax.servlet.http.HttpServlet
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader(HttpHeaders.ALLOW, "GET,HEAD,POST,OPTIONS");
    }

    private String getWelcomeFile(String pathInContext) throws MalformedURLException, IOException {
        Map.Entry entry;
        if (this._welcomes == null) {
            return null;
        }
        String welcome_servlet = null;
        for (int i = 0; i < this._welcomes.length; i++) {
            String welcome_in_context = URIUtil.addPaths(pathInContext, this._welcomes[i]);
            Resource welcome = getResource(welcome_in_context);
            if (welcome != null && welcome.exists()) {
                return this._welcomes[i];
            }
            if ((this._welcomeServlets || this._welcomeExactServlets) && welcome_servlet == null && (entry = this._servletHandler.getHolderEntry(welcome_in_context)) != null && entry.getValue() != this._defaultHolder && (this._welcomeServlets || (this._welcomeExactServlets && entry.getKey().equals(welcome_in_context)))) {
                welcome_servlet = welcome_in_context;
            }
        }
        return welcome_servlet;
    }

    protected boolean passConditionalHeaders(HttpServletRequest request, HttpServletResponse response, Resource resource, HttpContent content) throws IOException {
        Buffer mdlm;
        try {
            if (!request.getMethod().equals(HttpMethods.HEAD)) {
                if (this._etags) {
                    String ifm = request.getHeader(HttpHeaders.IF_MATCH);
                    if (ifm != null) {
                        boolean match = false;
                        if (content != null && content.getETag() != null) {
                            QuotedStringTokenizer quoted = new QuotedStringTokenizer(ifm, ", ", false, true);
                            while (!match && quoted.hasMoreTokens()) {
                                String tag = quoted.nextToken();
                                if (content.getETag().toString().equals(tag)) {
                                    match = true;
                                }
                            }
                        }
                        if (!match) {
                            Response r = Response.getResponse(response);
                            r.reset(true);
                            r.setStatus(412);
                            return false;
                        }
                    }
                    String ifnm = request.getHeader(HttpHeaders.IF_NONE_MATCH);
                    if (ifnm != null && content != null && content.getETag() != null) {
                        if (content.getETag().toString().equals(request.getAttribute("o.e.j.s.GzipFilter.ETag"))) {
                            Response r2 = Response.getResponse(response);
                            r2.reset(true);
                            r2.setStatus(304);
                            r2.getHttpFields().put(HttpHeaders.ETAG_BUFFER, ifnm);
                            return false;
                        } else if (content.getETag().toString().equals(ifnm)) {
                            Response r3 = Response.getResponse(response);
                            r3.reset(true);
                            r3.setStatus(304);
                            r3.getHttpFields().put(HttpHeaders.ETAG_BUFFER, content.getETag());
                            return false;
                        } else {
                            QuotedStringTokenizer quoted2 = new QuotedStringTokenizer(ifnm, ", ", false, true);
                            while (quoted2.hasMoreTokens()) {
                                String tag2 = quoted2.nextToken();
                                if (content.getETag().toString().equals(tag2)) {
                                    Response r4 = Response.getResponse(response);
                                    r4.reset(true);
                                    r4.setStatus(304);
                                    r4.getHttpFields().put(HttpHeaders.ETAG_BUFFER, content.getETag());
                                    return false;
                                }
                            }
                            return true;
                        }
                    }
                }
                String ifms = request.getHeader(HttpHeaders.IF_MODIFIED_SINCE);
                if (ifms != null) {
                    Response r5 = Response.getResponse(response);
                    if (content != null && (mdlm = content.getLastModified()) != null && ifms.equals(mdlm.toString())) {
                        r5.reset(true);
                        r5.setStatus(304);
                        if (this._etags) {
                            r5.getHttpFields().add(HttpHeaders.ETAG_BUFFER, content.getETag());
                        }
                        r5.flushBuffer();
                        return false;
                    }
                    long ifmsl = request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
                    if (ifmsl != -1 && resource.lastModified() / 1000 <= ifmsl / 1000) {
                        r5.reset(true);
                        r5.setStatus(304);
                        if (this._etags) {
                            r5.getHttpFields().add(HttpHeaders.ETAG_BUFFER, content.getETag());
                        }
                        r5.flushBuffer();
                        return false;
                    }
                }
                long date = request.getDateHeader(HttpHeaders.IF_UNMODIFIED_SINCE);
                if (date != -1 && resource.lastModified() / 1000 > date / 1000) {
                    response.sendError(412);
                    return false;
                }
            }
            return true;
        } catch (IllegalArgumentException iae) {
            if (!response.isCommitted()) {
                response.sendError(400, iae.getMessage());
            }
            throw iae;
        }
    }

    protected void sendDirectory(HttpServletRequest request, HttpServletResponse response, Resource resource, String pathInContext) throws IOException {
        if (!this._dirAllowed) {
            response.sendError(403);
            return;
        }
        String base = URIUtil.addPaths(request.getRequestURI(), "/");
        if (this._resourceBase != null) {
            if (this._resourceBase instanceof ResourceCollection) {
                resource = this._resourceBase.addPath(pathInContext);
            }
        } else if (this._contextHandler.getBaseResource() instanceof ResourceCollection) {
            resource = this._contextHandler.getBaseResource().addPath(pathInContext);
        }
        String dir = resource.getListHTML(base, pathInContext.length() > 1);
        if (dir == null) {
            response.sendError(403, "No directory");
            return;
        }
        byte[] data = dir.getBytes(StringUtil.__UTF8);
        response.setContentType("text/html; charset=UTF-8");
        response.setContentLength(data.length);
        response.getOutputStream().write(data);
    }

    protected void sendData(HttpServletRequest request, HttpServletResponse response, boolean include, Resource resource, HttpContent content, Enumeration reqRanges) throws IOException {
        boolean direct;
        long content_length;
        OutputStream out;
        boolean z;
        OutputStream out2;
        boolean direct2;
        OutputStream out3;
        OutputStream out4;
        List ranges;
        String ctp;
        InputStream in;
        List ranges2;
        if (content == null) {
            content_length = resource.length();
            direct = false;
        } else {
            Connector connector = AbstractHttpConnection.getCurrentConnection().getConnector();
            boolean direct3 = (connector instanceof NIOConnector) && ((NIOConnector) connector).getUseDirectBuffers() && !(connector instanceof SslConnector);
            direct = direct3;
            content_length = content.getContentLength();
        }
        try {
            out = response.getOutputStream();
            z = out instanceof HttpOutput ? ((HttpOutput) out).isWritten() : AbstractHttpConnection.getCurrentConnection().getGenerator().isWritten();
        } catch (IllegalStateException e) {
            out = new WriterOutputStream(response.getWriter());
            z = true;
        }
        boolean written = z;
        OutputStream out5 = out;
        if (reqRanges == null || !reqRanges.hasMoreElements()) {
            out2 = out5;
            boolean direct4 = direct ? 1 : 0;
            direct2 = direct4;
        } else if (content_length < 0) {
            out2 = out5;
            boolean direct5 = direct ? 1 : 0;
            direct2 = direct5;
        } else {
            List ranges3 = InclusiveByteRange.satisfiableRanges(reqRanges, content_length);
            if (ranges3 == null) {
                out4 = out5;
                boolean direct6 = direct ? 1 : 0;
                ranges = ranges3;
            } else if (ranges3.size() != 0) {
                if (ranges3.size() == 1) {
                    InclusiveByteRange singleSatisfiableRange = (InclusiveByteRange) ranges3.get(0);
                    long singleLength = singleSatisfiableRange.getSize(content_length);
                    writeHeaders(response, content, singleLength);
                    response.setStatus(206);
                    response.setHeader(HttpHeaders.CONTENT_RANGE, singleSatisfiableRange.toHeaderRangeString(content_length));
                    resource.writeTo(out5, singleSatisfiableRange.getFirst(content_length), singleLength);
                    return;
                }
                writeHeaders(response, content, -1L);
                String mimetype = content.getContentType() != null ? content.getContentType().toString() : null;
                if (mimetype == null) {
                    LOG.warn("Unknown mimetype for " + request.getRequestURI(), new Object[0]);
                }
                MultiPartOutputStream multi = new MultiPartOutputStream(out5);
                response.setStatus(206);
                if (request.getHeader(HttpHeaders.REQUEST_RANGE) != null) {
                    ctp = "multipart/x-byteranges; boundary=";
                } else {
                    ctp = "multipart/byteranges; boundary=";
                }
                response.setContentType(ctp + multi.getBoundary());
                InputStream in2 = resource.getInputStream();
                long pos = 0;
                String[] header = new String[ranges3.size()];
                int length = 0;
                int length2 = 0;
                OutputStream out6 = out5;
                boolean direct7 = direct;
                while (true) {
                    in = in2;
                    if (length2 >= ranges3.size()) {
                        break;
                    }
                    InclusiveByteRange ibr = (InclusiveByteRange) ranges3.get(length2);
                    header[length2] = ibr.toHeaderRangeString(content_length);
                    OutputStream out7 = out6;
                    long j = length;
                    int i = length2 > 0 ? 2 : 0;
                    String ctp2 = ctp;
                    String ctp3 = multi.getBoundary();
                    length = (int) (j + i + 2 + ctp3.length() + 2 + (mimetype == null ? 0 : HttpHeaders.CONTENT_TYPE.length() + 2 + mimetype.length()) + 2 + HttpHeaders.CONTENT_RANGE.length() + 2 + header[length2].length() + 2 + 2 + (ibr.getLast(content_length) - ibr.getFirst(content_length)) + 1);
                    length2++;
                    in2 = in;
                    out6 = out7;
                    ctp = ctp2;
                    boolean direct8 = direct7 ? 1 : 0;
                    direct7 = direct8;
                    mimetype = mimetype;
                }
                String mimetype2 = mimetype;
                response.setContentLength(length + 4 + multi.getBoundary().length() + 2 + 2);
                InputStream in3 = in;
                int i2 = 0;
                while (i2 < ranges3.size()) {
                    InclusiveByteRange ibr2 = (InclusiveByteRange) ranges3.get(i2);
                    String mimetype3 = mimetype2;
                    multi.startPart(mimetype3, new String[]{"Content-Range: " + header[i2]});
                    long start = ibr2.getFirst(content_length);
                    long size = ibr2.getSize(content_length);
                    if (in3 != null) {
                        if (start < pos) {
                            in3.close();
                            in3 = resource.getInputStream();
                            pos = 0;
                        }
                        if (pos < start) {
                            ranges2 = ranges3;
                            in3.skip(start - pos);
                            pos = start;
                        } else {
                            ranges2 = ranges3;
                        }
                        IO.copy(in3, multi, size);
                        pos += size;
                    } else {
                        ranges2 = ranges3;
                        resource.writeTo(multi, start, size);
                    }
                    i2++;
                    mimetype2 = mimetype3;
                    ranges3 = ranges2;
                }
                if (in3 != null) {
                    in3.close();
                }
                multi.close();
                return;
            } else {
                out4 = out5;
                boolean direct9 = direct ? 1 : 0;
                ranges = ranges3;
            }
            writeHeaders(response, content, content_length);
            response.setStatus(416);
            response.setHeader(HttpHeaders.CONTENT_RANGE, InclusiveByteRange.to416HeaderRangeString(content_length));
            resource.writeTo(out4, 0L, content_length);
            return;
        }
        if (include) {
            resource.writeTo(out2, 0L, content_length);
            return;
        }
        if (content == null || written) {
            out3 = out2;
        } else {
            out3 = out2;
            if (out3 instanceof HttpOutput) {
                if (response instanceof Response) {
                    writeOptionHeaders(((Response) response).getHttpFields());
                    ((AbstractHttpConnection.Output) out3).sendContent(content);
                    return;
                }
                Buffer buffer = direct2 ? content.getDirectBuffer() : content.getIndirectBuffer();
                if (buffer != null) {
                    writeHeaders(response, content, content_length);
                    ((AbstractHttpConnection.Output) out3).sendContent(buffer);
                    return;
                }
                writeHeaders(response, content, content_length);
                resource.writeTo(out3, 0L, content_length);
                return;
            }
        }
        writeHeaders(response, content, written ? -1L : content_length);
        Buffer buffer2 = content != null ? content.getIndirectBuffer() : null;
        if (buffer2 != null) {
            buffer2.writeTo(out3);
        } else {
            resource.writeTo(out3, 0L, content_length);
        }
    }

    protected void writeHeaders(HttpServletResponse response, HttpContent content, long count) throws IOException {
        if (content.getContentType() != null && response.getContentType() == null) {
            response.setContentType(content.getContentType().toString());
        }
        if (response instanceof Response) {
            Response r = (Response) response;
            HttpFields fields = r.getHttpFields();
            if (content.getLastModified() != null) {
                fields.put(HttpHeaders.LAST_MODIFIED_BUFFER, content.getLastModified());
            } else if (content.getResource() != null) {
                long lml = content.getResource().lastModified();
                if (lml != -1) {
                    fields.putDateField(HttpHeaders.LAST_MODIFIED_BUFFER, lml);
                }
            }
            if (count != -1) {
                r.setLongContentLength(count);
            }
            writeOptionHeaders(fields);
            if (this._etags) {
                fields.put(HttpHeaders.ETAG_BUFFER, content.getETag());
                return;
            }
            return;
        }
        long lml2 = content.getResource().lastModified();
        if (lml2 >= 0) {
            response.setDateHeader(HttpHeaders.LAST_MODIFIED, lml2);
        }
        if (count != -1) {
            if (count < 2147483647L) {
                response.setContentLength((int) count);
            } else {
                response.setHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(count));
            }
        }
        writeOptionHeaders(response);
        if (this._etags) {
            response.setHeader(HttpHeaders.ETAG, content.getETag().toString());
        }
    }

    protected void writeOptionHeaders(HttpFields fields) throws IOException {
        if (this._acceptRanges) {
            fields.put(HttpHeaders.ACCEPT_RANGES_BUFFER, HttpHeaderValues.BYTES_BUFFER);
        }
        if (this._cacheControl != null) {
            fields.put(HttpHeaders.CACHE_CONTROL_BUFFER, this._cacheControl);
        }
    }

    protected void writeOptionHeaders(HttpServletResponse response) throws IOException {
        if (this._acceptRanges) {
            response.setHeader(HttpHeaders.ACCEPT_RANGES, HttpHeaderValues.BYTES);
        }
        if (this._cacheControl != null) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, this._cacheControl.toString());
        }
    }

    @Override // javax.servlet.GenericServlet, javax.servlet.Servlet
    public void destroy() {
        if (this._cache != null) {
            this._cache.flushCache();
        }
        super.destroy();
    }
}
