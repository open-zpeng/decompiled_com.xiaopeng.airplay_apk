package org.fourthline.cling.model;

import org.eclipse.jetty.http.gzip.CompressedResponseWrapper;
/* loaded from: classes.dex */
public class ServerClientTokens {
    public static final String UNKNOWN_PLACEHOLDER = "UNKNOWN";
    private int majorVersion;
    private int minorVersion;
    private String osName;
    private String osVersion;
    private String productName;
    private String productVersion;

    public ServerClientTokens() {
        this.majorVersion = 1;
        this.minorVersion = 0;
        this.osName = System.getProperty("os.name").replaceAll("[^a-zA-Z0-9\\.\\-_]", "");
        this.osVersion = System.getProperty("os.version").replaceAll("[^a-zA-Z0-9\\.\\-_]", "");
        this.productName = UserConstants.PRODUCT_TOKEN_NAME;
        this.productVersion = UserConstants.PRODUCT_TOKEN_VERSION;
    }

    public ServerClientTokens(int majorVersion, int minorVersion) {
        this.majorVersion = 1;
        this.minorVersion = 0;
        this.osName = System.getProperty("os.name").replaceAll("[^a-zA-Z0-9\\.\\-_]", "");
        this.osVersion = System.getProperty("os.version").replaceAll("[^a-zA-Z0-9\\.\\-_]", "");
        this.productName = UserConstants.PRODUCT_TOKEN_NAME;
        this.productVersion = UserConstants.PRODUCT_TOKEN_VERSION;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    public ServerClientTokens(String productName, String productVersion) {
        this.majorVersion = 1;
        this.minorVersion = 0;
        this.osName = System.getProperty("os.name").replaceAll("[^a-zA-Z0-9\\.\\-_]", "");
        this.osVersion = System.getProperty("os.version").replaceAll("[^a-zA-Z0-9\\.\\-_]", "");
        this.productName = UserConstants.PRODUCT_TOKEN_NAME;
        this.productVersion = UserConstants.PRODUCT_TOKEN_VERSION;
        this.productName = productName;
        this.productVersion = productVersion;
    }

    public ServerClientTokens(int majorVersion, int minorVersion, String osName, String osVersion, String productName, String productVersion) {
        this.majorVersion = 1;
        this.minorVersion = 0;
        this.osName = System.getProperty("os.name").replaceAll("[^a-zA-Z0-9\\.\\-_]", "");
        this.osVersion = System.getProperty("os.version").replaceAll("[^a-zA-Z0-9\\.\\-_]", "");
        this.productName = UserConstants.PRODUCT_TOKEN_NAME;
        this.productVersion = UserConstants.PRODUCT_TOKEN_VERSION;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.osName = osName;
        this.osVersion = osVersion;
        this.productName = productName;
        this.productVersion = productVersion;
    }

    public int getMajorVersion() {
        return this.majorVersion;
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public int getMinorVersion() {
        return this.minorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    public String getOsName() {
        return this.osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return this.osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getProductName() {
        return this.productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductVersion() {
        return this.productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    public String toString() {
        return getOsName() + "/" + getOsVersion() + " UPnP/" + getMajorVersion() + "." + getMinorVersion() + " " + getProductName() + "/" + getProductVersion();
    }

    public String getHttpToken() {
        StringBuilder sb = new StringBuilder((int) CompressedResponseWrapper.DEFAULT_MIN_COMPRESS_SIZE);
        sb.append(this.osName.indexOf(32) != -1 ? this.osName.replace(' ', '_') : this.osName);
        sb.append('/');
        sb.append(this.osVersion.indexOf(32) != -1 ? this.osVersion.replace(' ', '_') : this.osVersion);
        sb.append(" UPnP/");
        sb.append(this.majorVersion);
        sb.append('.');
        sb.append(this.minorVersion);
        sb.append(' ');
        sb.append(this.productName.indexOf(32) != -1 ? this.productName.replace(' ', '_') : this.productName);
        sb.append('/');
        sb.append(this.productVersion.indexOf(32) != -1 ? this.productVersion.replace(' ', '_') : this.productVersion);
        return sb.toString();
    }

    public String getOsToken() {
        return getOsName().replaceAll(" ", "_") + "/" + getOsVersion().replaceAll(" ", "_");
    }

    public String getProductToken() {
        return getProductName().replaceAll(" ", "_") + "/" + getProductVersion().replaceAll(" ", "_");
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerClientTokens that = (ServerClientTokens) o;
        if (this.majorVersion == that.majorVersion && this.minorVersion == that.minorVersion && this.osName.equals(that.osName) && this.osVersion.equals(that.osVersion) && this.productName.equals(that.productName) && this.productVersion.equals(that.productVersion)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = this.majorVersion;
        return (31 * ((31 * ((31 * ((31 * ((31 * result) + this.minorVersion)) + this.osName.hashCode())) + this.osVersion.hashCode())) + this.productName.hashCode())) + this.productVersion.hashCode();
    }
}
