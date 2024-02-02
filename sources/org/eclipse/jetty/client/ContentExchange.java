package org.eclipse.jetty.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.BufferUtil;
import org.eclipse.jetty.util.StringUtil;
/* loaded from: classes.dex */
public class ContentExchange extends CachedExchange {
    private int _bufferSize;
    private String _encoding;
    private File _fileForUpload;
    private ByteArrayOutputStream _responseContent;

    public ContentExchange() {
        super(false);
        this._bufferSize = 4096;
        this._encoding = "utf-8";
    }

    public ContentExchange(boolean cacheFields) {
        super(cacheFields);
        this._bufferSize = 4096;
        this._encoding = "utf-8";
    }

    public synchronized String getResponseContent() throws UnsupportedEncodingException {
        if (this._responseContent != null) {
            return this._responseContent.toString(this._encoding);
        }
        return null;
    }

    public synchronized byte[] getResponseContentBytes() {
        if (this._responseContent != null) {
            return this._responseContent.toByteArray();
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.client.CachedExchange, org.eclipse.jetty.client.HttpExchange
    public synchronized void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException {
        if (this._responseContent != null) {
            this._responseContent.reset();
        }
        super.onResponseStatus(version, status, reason);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.client.CachedExchange, org.eclipse.jetty.client.HttpExchange
    public synchronized void onResponseHeader(Buffer name, Buffer value) throws IOException {
        String mime;
        int i;
        super.onResponseHeader(name, value);
        int header = HttpHeaders.CACHE.getOrdinal(name);
        if (header == 12) {
            this._bufferSize = BufferUtil.toInt(value);
        } else if (header == 16 && (i = (mime = StringUtil.asciiToLowerCase(value.toString())).indexOf("charset=")) > 0) {
            this._encoding = mime.substring(i + 8);
            int i2 = this._encoding.indexOf(59);
            if (i2 > 0) {
                this._encoding = this._encoding.substring(0, i2);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.client.HttpExchange
    public synchronized void onResponseContent(Buffer content) throws IOException {
        super.onResponseContent(content);
        if (this._responseContent == null) {
            this._responseContent = new ByteArrayOutputStream(this._bufferSize);
        }
        content.writeTo(this._responseContent);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.client.HttpExchange
    public synchronized void onRetry() throws IOException {
        if (this._fileForUpload != null) {
            setRequestContent(null);
            setRequestContentSource(getInputStream());
        } else {
            super.onRetry();
        }
    }

    private synchronized InputStream getInputStream() throws IOException {
        return new FileInputStream(this._fileForUpload);
    }

    public synchronized File getFileForUpload() {
        return this._fileForUpload;
    }

    public synchronized void setFileForUpload(File fileForUpload) throws IOException {
        this._fileForUpload = fileForUpload;
        setRequestContentSource(getInputStream());
    }
}
