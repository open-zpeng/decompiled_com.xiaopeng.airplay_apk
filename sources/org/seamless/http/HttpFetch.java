package org.seamless.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import org.eclipse.jetty.http.HttpMethods;
import org.seamless.util.io.IO;
/* loaded from: classes.dex */
public class HttpFetch {

    /* loaded from: classes.dex */
    public interface RepresentationFactory<E> {
        Representation<E> createRepresentation(URLConnection uRLConnection, InputStream inputStream) throws IOException;
    }

    public static Representation<byte[]> fetchBinary(URL url) throws IOException {
        return fetchBinary(url, 500, 500);
    }

    public static Representation<byte[]> fetchBinary(URL url, int connectTimeoutMillis, int readTimeoutMillis) throws IOException {
        return fetch(url, connectTimeoutMillis, readTimeoutMillis, new RepresentationFactory<byte[]>() { // from class: org.seamless.http.HttpFetch.1
            @Override // org.seamless.http.HttpFetch.RepresentationFactory
            public Representation<byte[]> createRepresentation(URLConnection urlConnection, InputStream is) throws IOException {
                return new Representation<>(urlConnection, IO.readBytes(is));
            }
        });
    }

    public static Representation<String> fetchString(URL url, int connectTimeoutMillis, int readTimeoutMillis) throws IOException {
        return fetch(url, connectTimeoutMillis, readTimeoutMillis, new RepresentationFactory<String>() { // from class: org.seamless.http.HttpFetch.2
            @Override // org.seamless.http.HttpFetch.RepresentationFactory
            public Representation<String> createRepresentation(URLConnection urlConnection, InputStream is) throws IOException {
                return new Representation<>(urlConnection, IO.readLines(is));
            }
        });
    }

    public static <E> Representation<E> fetch(URL url, int connectTimeoutMillis, int readTimeoutMillis, RepresentationFactory<E> factory) throws IOException {
        return fetch(url, HttpMethods.GET, connectTimeoutMillis, readTimeoutMillis, factory);
    }

    public static <E> Representation<E> fetch(URL url, String method, int connectTimeoutMillis, int readTimeoutMillis, RepresentationFactory<E> factory) throws IOException {
        HttpURLConnection urlConnection = null;
        InputStream is = null;
        try {
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod(method);
                urlConnection.setConnectTimeout(connectTimeoutMillis);
                urlConnection.setReadTimeout(readTimeoutMillis);
                is = urlConnection.getInputStream();
                return factory.createRepresentation(urlConnection, is);
            } catch (IOException ex) {
                if (urlConnection != null) {
                    int responseCode = urlConnection.getResponseCode();
                    throw new IOException("Fetching resource failed, returned status code: " + responseCode);
                }
                throw ex;
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public static void validate(URL url) throws IOException {
        fetch(url, HttpMethods.HEAD, 500, 500, new RepresentationFactory() { // from class: org.seamless.http.HttpFetch.3
            @Override // org.seamless.http.HttpFetch.RepresentationFactory
            public Representation createRepresentation(URLConnection urlConnection, InputStream is) throws IOException {
                return new Representation(urlConnection, null);
            }
        });
    }
}
