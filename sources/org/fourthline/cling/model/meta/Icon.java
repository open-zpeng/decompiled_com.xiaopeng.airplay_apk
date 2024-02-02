package org.fourthline.cling.model.meta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.fourthline.cling.model.Validatable;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.types.BinHexDatatype;
import org.seamless.util.MimeType;
import org.seamless.util.URIUtil;
import org.seamless.util.io.IO;
/* loaded from: classes.dex */
public class Icon implements Validatable {
    private static final Logger log = Logger.getLogger(StateVariable.class.getName());
    private final byte[] data;
    private final int depth;
    private Device device;
    private final int height;
    private final MimeType mimeType;
    private final URI uri;
    private final int width;

    public Icon(String mimeType, int width, int height, int depth, URI uri) {
        this((mimeType == null || mimeType.length() <= 0) ? null : MimeType.valueOf(mimeType), width, height, depth, uri, (byte[]) null);
    }

    public Icon(String mimeType, int width, int height, int depth, URL url) throws IOException {
        this(mimeType, width, height, depth, new File(URIUtil.toURI(url)));
    }

    public Icon(String mimeType, int width, int height, int depth, File file) throws IOException {
        this(mimeType, width, height, depth, file.getName(), IO.readBytes(file));
    }

    public Icon(String mimeType, int width, int height, int depth, String uniqueName, InputStream is) throws IOException {
        this(mimeType, width, height, depth, uniqueName, IO.readBytes(is));
    }

    public Icon(String mimeType, int width, int height, int depth, String uniqueName, byte[] data) {
        this((mimeType == null || mimeType.length() <= 0) ? null : MimeType.valueOf(mimeType), width, height, depth, URI.create(uniqueName), data);
    }

    public Icon(String mimeType, int width, int height, int depth, String uniqueName, String binHexEncoded) {
        this(mimeType, width, height, depth, uniqueName, (binHexEncoded == null || binHexEncoded.equals("")) ? null : new BinHexDatatype().valueOf(binHexEncoded));
    }

    protected Icon(MimeType mimeType, int width, int height, int depth, URI uri, byte[] data) {
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.uri = uri;
        this.data = data;
    }

    public MimeType getMimeType() {
        return this.mimeType;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getDepth() {
        return this.depth;
    }

    public URI getUri() {
        return this.uri;
    }

    public byte[] getData() {
        return this.data;
    }

    public Device getDevice() {
        return this.device;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setDevice(Device device) {
        if (this.device != null) {
            throw new IllegalStateException("Final value has been set already, model is immutable");
        }
        this.device = device;
    }

    @Override // org.fourthline.cling.model.Validatable
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();
        if (getMimeType() == null) {
            Logger logger = log;
            logger.warning("UPnP specification violation of: " + getDevice());
            Logger logger2 = log;
            logger2.warning("Invalid icon, missing mime type: " + this);
        }
        if (getWidth() == 0) {
            Logger logger3 = log;
            logger3.warning("UPnP specification violation of: " + getDevice());
            Logger logger4 = log;
            logger4.warning("Invalid icon, missing width: " + this);
        }
        if (getHeight() == 0) {
            Logger logger5 = log;
            logger5.warning("UPnP specification violation of: " + getDevice());
            Logger logger6 = log;
            logger6.warning("Invalid icon, missing height: " + this);
        }
        if (getDepth() == 0) {
            Logger logger7 = log;
            logger7.warning("UPnP specification violation of: " + getDevice());
            Logger logger8 = log;
            logger8.warning("Invalid icon, missing bitmap depth: " + this);
        }
        if (getUri() == null) {
            errors.add(new ValidationError(getClass(), "uri", "URL is required"));
        } else {
            try {
                URL testURI = getUri().toURL();
                if (testURI == null) {
                    throw new MalformedURLException();
                }
            } catch (IllegalArgumentException e) {
            } catch (MalformedURLException ex) {
                Class<?> cls = getClass();
                errors.add(new ValidationError(cls, "uri", "URL must be valid: " + ex.getMessage()));
            }
        }
        return errors;
    }

    public Icon deepCopy() {
        return new Icon(getMimeType(), getWidth(), getHeight(), getDepth(), getUri(), getData());
    }

    public String toString() {
        return "Icon(" + getWidth() + "x" + getHeight() + ", MIME: " + getMimeType() + ") " + getUri();
    }
}
