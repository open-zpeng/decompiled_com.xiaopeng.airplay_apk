package org.seamless.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jetty.http.HttpHeaderValues;
/* loaded from: classes.dex */
public class CacheControl {
    private int maxAge = -1;
    private int sharedMaxAge = -1;
    private boolean noCache = false;
    private List<String> noCacheFields = new ArrayList();
    private boolean privateFlag = false;
    private List<String> privateFields = new ArrayList();
    private boolean noStore = false;
    private boolean noTransform = true;
    private boolean mustRevalidate = false;
    private boolean proxyRevalidate = false;
    private Map<String, String> cacheExtensions = new HashMap();

    public int getMaxAge() {
        return this.maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public int getSharedMaxAge() {
        return this.sharedMaxAge;
    }

    public void setSharedMaxAge(int sharedMaxAge) {
        this.sharedMaxAge = sharedMaxAge;
    }

    public boolean isNoCache() {
        return this.noCache;
    }

    public void setNoCache(boolean noCache) {
        this.noCache = noCache;
    }

    public List<String> getNoCacheFields() {
        return this.noCacheFields;
    }

    public void setNoCacheFields(List<String> noCacheFields) {
        this.noCacheFields = noCacheFields;
    }

    public boolean isPrivateFlag() {
        return this.privateFlag;
    }

    public void setPrivateFlag(boolean privateFlag) {
        this.privateFlag = privateFlag;
    }

    public List<String> getPrivateFields() {
        return this.privateFields;
    }

    public void setPrivateFields(List<String> privateFields) {
        this.privateFields = privateFields;
    }

    public boolean isNoStore() {
        return this.noStore;
    }

    public void setNoStore(boolean noStore) {
        this.noStore = noStore;
    }

    public boolean isNoTransform() {
        return this.noTransform;
    }

    public void setNoTransform(boolean noTransform) {
        this.noTransform = noTransform;
    }

    public boolean isMustRevalidate() {
        return this.mustRevalidate;
    }

    public void setMustRevalidate(boolean mustRevalidate) {
        this.mustRevalidate = mustRevalidate;
    }

    public boolean isProxyRevalidate() {
        return this.proxyRevalidate;
    }

    public void setProxyRevalidate(boolean proxyRevalidate) {
        this.proxyRevalidate = proxyRevalidate;
    }

    public Map<String, String> getCacheExtensions() {
        return this.cacheExtensions;
    }

    public void setCacheExtensions(Map<String, String> cacheExtensions) {
        this.cacheExtensions = cacheExtensions;
    }

    public static CacheControl valueOf(String s) throws IllegalArgumentException {
        if (s == null) {
            return null;
        }
        CacheControl result = new CacheControl();
        String[] directives = s.split(",");
        for (String directive : directives) {
            String[] nameValue = directive.trim().split("=");
            String name = nameValue[0].trim();
            String value = null;
            if (nameValue.length > 1) {
                value = nameValue[1].trim();
                if (value.startsWith("\"")) {
                    value = value.substring(1);
                }
                if (value.endsWith("\"")) {
                    value = value.substring(0, value.length() - 1);
                }
            }
            String lowercase = name.toLowerCase();
            if (HttpHeaderValues.NO_CACHE.equals(lowercase)) {
                result.setNoCache(true);
                if (value != null && !"".equals(value)) {
                    result.getNoCacheFields().add(value);
                }
            } else if ("private".equals(lowercase)) {
                result.setPrivateFlag(true);
                if (value != null && !"".equals(value)) {
                    result.getPrivateFields().add(value);
                }
            } else if ("no-store".equals(lowercase)) {
                result.setNoStore(true);
            } else if ("max-age".equals(lowercase)) {
                if (value == null) {
                    throw new IllegalArgumentException("CacheControl max-age header does not have a value: " + value);
                }
                result.setMaxAge(Integer.valueOf(value).intValue());
            } else if ("s-maxage".equals(lowercase)) {
                if (value == null) {
                    throw new IllegalArgumentException("CacheControl s-maxage header does not have a value: " + value);
                }
                result.setSharedMaxAge(Integer.valueOf(value).intValue());
            } else if ("no-transform".equals(lowercase)) {
                result.setNoTransform(true);
            } else if ("must-revalidate".equals(lowercase)) {
                result.setMustRevalidate(true);
            } else if ("proxy-revalidate".equals(lowercase)) {
                result.setProxyRevalidate(true);
            } else if (!"public".equals(lowercase)) {
                if (value == null) {
                    value = "";
                }
                result.getCacheExtensions().put(name, value);
            }
        }
        return result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!isPrivateFlag()) {
            sb.append("public");
        }
        if (isMustRevalidate()) {
            append("must-revalidate", sb);
        }
        if (isNoTransform()) {
            append("no-transform", sb);
        }
        if (isNoStore()) {
            append("no-store", sb);
        }
        if (isProxyRevalidate()) {
            append("proxy-revalidate", sb);
        }
        if (getSharedMaxAge() > -1) {
            StringBuilder append = append("s-maxage", sb);
            append.append("=");
            append.append(getSharedMaxAge());
        }
        if (getMaxAge() > -1) {
            StringBuilder append2 = append("max-age", sb);
            append2.append("=");
            append2.append(getMaxAge());
        }
        if (isNoCache()) {
            List<String> fields = getNoCacheFields();
            if (fields.size() < 1) {
                append(HttpHeaderValues.NO_CACHE, sb);
            } else {
                for (String field : getNoCacheFields()) {
                    StringBuilder append3 = append(HttpHeaderValues.NO_CACHE, sb);
                    append3.append("=\"");
                    append3.append(field);
                    append3.append("\"");
                }
            }
        }
        if (isPrivateFlag()) {
            List<String> fields2 = getPrivateFields();
            if (fields2.size() < 1) {
                append("private", sb);
            } else {
                for (String field2 : getPrivateFields()) {
                    StringBuilder append4 = append("private", sb);
                    append4.append("=\"");
                    append4.append(field2);
                    append4.append("\"");
                }
            }
        }
        for (String key : getCacheExtensions().keySet()) {
            String val = getCacheExtensions().get(key);
            append(key, sb);
            if (val != null && !"".equals(val)) {
                sb.append("=\"");
                sb.append(val);
                sb.append("\"");
            }
        }
        return sb.toString();
    }

    private StringBuilder append(String s, StringBuilder sb) {
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(s);
        return sb;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CacheControl that = (CacheControl) o;
        if (this.maxAge == that.maxAge && this.mustRevalidate == that.mustRevalidate && this.noCache == that.noCache && this.noStore == that.noStore && this.noTransform == that.noTransform && this.privateFlag == that.privateFlag && this.proxyRevalidate == that.proxyRevalidate && this.sharedMaxAge == that.sharedMaxAge && this.cacheExtensions.equals(that.cacheExtensions) && this.noCacheFields.equals(that.noCacheFields) && this.privateFields.equals(that.privateFields)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = this.maxAge;
        return (31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * result) + this.sharedMaxAge)) + (this.noCache ? 1 : 0))) + this.noCacheFields.hashCode())) + (this.privateFlag ? 1 : 0))) + this.privateFields.hashCode())) + (this.noStore ? 1 : 0))) + (this.noTransform ? 1 : 0))) + (this.mustRevalidate ? 1 : 0))) + (this.proxyRevalidate ? 1 : 0))) + this.cacheExtensions.hashCode();
    }
}
