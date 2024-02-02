package org.seamless.xhtml;

import java.net.URI;
/* loaded from: classes.dex */
public class Href {
    private URI uri;

    public Href(URI uri) {
        this.uri = uri;
    }

    public URI getURI() {
        return this.uri;
    }

    public static Href fromString(String string) {
        if (string == null) {
            return null;
        }
        return new Href(URI.create(string.replaceAll(" ", "%20")));
    }

    public String toString() {
        return getURI().toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Href href = (Href) o;
        if (this.uri.equals(href.uri)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.uri.hashCode();
    }
}
