package org.eclipse.jetty.server.handler;

import java.io.IOException;
import java.net.MalformedURLException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.io.WriterOutputStream;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;
/* loaded from: classes.dex */
public class ResourceHandler extends HandlerWrapper {
    private static final Logger LOG = Log.getLogger(ResourceHandler.class);
    boolean _aliases;
    Resource _baseResource;
    ByteArrayBuffer _cacheControl;
    ContextHandler _context;
    Resource _defaultStylesheet;
    boolean _directory;
    boolean _etags;
    Resource _stylesheet;
    String[] _welcomeFiles = {"index.html"};
    MimeTypes _mimeTypes = new MimeTypes();

    public MimeTypes getMimeTypes() {
        return this._mimeTypes;
    }

    public void setMimeTypes(MimeTypes mimeTypes) {
        this._mimeTypes = mimeTypes;
    }

    public boolean isAliases() {
        return this._aliases;
    }

    public void setAliases(boolean aliases) {
        this._aliases = aliases;
    }

    public boolean isDirectoriesListed() {
        return this._directory;
    }

    public void setDirectoriesListed(boolean directory) {
        this._directory = directory;
    }

    public boolean isEtags() {
        return this._etags;
    }

    public void setEtags(boolean etags) {
        this._etags = etags;
    }

    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        ContextHandler.Context scontext = ContextHandler.getCurrentContext();
        this._context = scontext == null ? null : scontext.getContextHandler();
        if (this._context != null) {
            this._aliases = this._context.isAliases();
        }
        if (!this._aliases && !FileResource.getCheckAliases()) {
            throw new IllegalStateException("Alias checking disabled");
        }
        super.doStart();
    }

    public Resource getBaseResource() {
        if (this._baseResource == null) {
            return null;
        }
        return this._baseResource;
    }

    public String getResourceBase() {
        if (this._baseResource == null) {
            return null;
        }
        return this._baseResource.toString();
    }

    public void setBaseResource(Resource base) {
        this._baseResource = base;
    }

    public void setResourceBase(String resourceBase) {
        try {
            setBaseResource(Resource.newResource(resourceBase));
        } catch (Exception e) {
            LOG.warn(e.toString(), new Object[0]);
            LOG.debug(e);
            throw new IllegalArgumentException(resourceBase);
        }
    }

    public Resource getStylesheet() {
        if (this._stylesheet != null) {
            return this._stylesheet;
        }
        if (this._defaultStylesheet == null) {
            try {
                this._defaultStylesheet = Resource.newResource(getClass().getResource("/jetty-dir.css"));
            } catch (IOException e) {
                LOG.warn(e.toString(), new Object[0]);
                LOG.debug(e);
            }
        }
        return this._defaultStylesheet;
    }

    public void setStylesheet(String stylesheet) {
        try {
            this._stylesheet = Resource.newResource(stylesheet);
            if (!this._stylesheet.exists()) {
                Logger logger = LOG;
                logger.warn("unable to find custom stylesheet: " + stylesheet, new Object[0]);
                this._stylesheet = null;
            }
        } catch (Exception e) {
            LOG.warn(e.toString(), new Object[0]);
            LOG.debug(e);
            throw new IllegalArgumentException(stylesheet.toString());
        }
    }

    public String getCacheControl() {
        return this._cacheControl.toString();
    }

    public void setCacheControl(String cacheControl) {
        this._cacheControl = cacheControl == null ? null : new ByteArrayBuffer(cacheControl);
    }

    public Resource getResource(String path) throws MalformedURLException {
        if (path == null || !path.startsWith("/")) {
            throw new MalformedURLException(path);
        }
        Resource base = this._baseResource;
        if (base == null && (this._context == null || (base = this._context.getBaseResource()) == null)) {
            return null;
        }
        try {
            return base.addPath(URIUtil.canonicalPath(path));
        } catch (Exception e) {
            LOG.ignore(e);
            return null;
        }
    }

    protected Resource getResource(HttpServletRequest request) throws MalformedURLException {
        String servletPath;
        String pathInfo;
        Boolean included = Boolean.valueOf(request.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI) != null);
        if (included != null && included.booleanValue()) {
            servletPath = (String) request.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
            pathInfo = (String) request.getAttribute(RequestDispatcher.INCLUDE_PATH_INFO);
            if (servletPath == null && pathInfo == null) {
                servletPath = request.getServletPath();
                pathInfo = request.getPathInfo();
            }
        } else {
            servletPath = request.getServletPath();
            pathInfo = request.getPathInfo();
        }
        String pathInContext = URIUtil.addPaths(servletPath, pathInfo);
        return getResource(pathInContext);
    }

    public String[] getWelcomeFiles() {
        return this._welcomeFiles;
    }

    public void setWelcomeFiles(String[] welcomeFiles) {
        this._welcomeFiles = welcomeFiles;
    }

    protected Resource getWelcome(Resource directory) throws MalformedURLException, IOException {
        for (int i = 0; i < this._welcomeFiles.length; i++) {
            Resource welcome = directory.addPath(this._welcomeFiles[i]);
            if (welcome.exists() && !welcome.isDirectory()) {
                return welcome;
            }
        }
        return null;
    }

    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.Handler
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ServletOutputStream out;
        if (baseRequest.isHandled()) {
            return;
        }
        boolean skipContentBody = false;
        if (!HttpMethods.GET.equals(request.getMethod())) {
            if (!HttpMethods.HEAD.equals(request.getMethod())) {
                super.handle(target, baseRequest, request, response);
                return;
            }
            skipContentBody = true;
        }
        boolean skipContentBody2 = skipContentBody;
        Resource resource = getResource(request);
        if (resource == null || !resource.exists()) {
            if (target.endsWith("/jetty-dir.css")) {
                resource = getStylesheet();
                if (resource != null) {
                    response.setContentType("text/css");
                } else {
                    return;
                }
            } else {
                super.handle(target, baseRequest, request, response);
                return;
            }
        }
        if (!this._aliases && resource.getAlias() != null) {
            Logger logger = LOG;
            logger.info(resource + " aliased to " + resource.getAlias(), new Object[0]);
            return;
        }
        baseRequest.setHandled(true);
        if (resource.isDirectory()) {
            if (!request.getPathInfo().endsWith("/")) {
                response.sendRedirect(response.encodeRedirectURL(URIUtil.addPaths(request.getRequestURI(), "/")));
                return;
            }
            Resource welcome = getWelcome(resource);
            if (welcome != null && welcome.exists()) {
                resource = welcome;
            } else {
                doDirectory(request, response, resource);
                baseRequest.setHandled(true);
                return;
            }
        }
        Resource resource2 = resource;
        long last_modified = resource2.lastModified();
        String etag = null;
        if (this._etags) {
            String ifnm = request.getHeader(HttpHeaders.IF_NONE_MATCH);
            etag = resource2.getWeakETag();
            if (ifnm != null && resource2 != null && ifnm.equals(etag)) {
                response.setStatus(304);
                baseRequest.getResponse().getHttpFields().put(HttpHeaders.ETAG_BUFFER, etag);
                return;
            }
        }
        String ifnm2 = etag;
        if (last_modified > 0) {
            long if_modified = request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
            if (if_modified > 0 && last_modified / 1000 <= if_modified / 1000) {
                response.setStatus(304);
                return;
            }
        }
        Buffer mime = this._mimeTypes.getMimeByExtension(resource2.toString());
        if (mime == null) {
            mime = this._mimeTypes.getMimeByExtension(request.getPathInfo());
        }
        Buffer mime2 = mime;
        doResponseHeaders(response, resource2, mime2 != null ? mime2.toString() : null);
        response.setDateHeader(HttpHeaders.LAST_MODIFIED, last_modified);
        if (this._etags) {
            baseRequest.getResponse().getHttpFields().put(HttpHeaders.ETAG_BUFFER, ifnm2);
        }
        if (skipContentBody2) {
            return;
        }
        try {
            out = response.getOutputStream();
        } catch (IllegalStateException e) {
            out = new WriterOutputStream(response.getWriter());
        }
        if (out instanceof AbstractHttpConnection.Output) {
            ((AbstractHttpConnection.Output) out).sendContent(resource2.getInputStream());
        } else {
            resource2.writeTo(out, 0L, resource2.length());
        }
    }

    protected void doDirectory(HttpServletRequest request, HttpServletResponse response, Resource resource) throws IOException {
        if (this._directory) {
            String listing = resource.getListHTML(request.getRequestURI(), request.getPathInfo().lastIndexOf("/") > 0);
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().println(listing);
            return;
        }
        response.sendError(403);
    }

    protected void doResponseHeaders(HttpServletResponse response, Resource resource, String mimeType) {
        if (mimeType != null) {
            response.setContentType(mimeType);
        }
        long length = resource.length();
        if (response instanceof Response) {
            HttpFields fields = ((Response) response).getHttpFields();
            if (length > 0) {
                fields.putLongField(HttpHeaders.CONTENT_LENGTH_BUFFER, length);
            }
            if (this._cacheControl != null) {
                fields.put(HttpHeaders.CACHE_CONTROL_BUFFER, this._cacheControl);
                return;
            }
            return;
        }
        if (length > 0) {
            response.setHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(length));
        }
        if (this._cacheControl != null) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, this._cacheControl.toString());
        }
    }
}
