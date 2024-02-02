package org.fourthline.cling.model.message.header;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
/* loaded from: classes.dex */
public class CallbackHeader extends UpnpHeader<List<URL>> {
    private static final Logger log = Logger.getLogger(CallbackHeader.class.getName());

    public CallbackHeader() {
        setValue(new ArrayList());
    }

    public CallbackHeader(List<URL> urls) {
        this();
        getValue().addAll(urls);
    }

    public CallbackHeader(URL url) {
        this();
        getValue().add(url);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (s.length() == 0) {
            return;
        }
        if (!s.contains("<") || !s.contains(">")) {
            throw new InvalidHeaderException("URLs not in brackets: " + s);
        }
        String s2 = s.replaceAll("<", "");
        String[] split = s2.split(">");
        try {
            ArrayList arrayList = new ArrayList();
            for (String sp : split) {
                String sp2 = sp.trim();
                if (!sp2.startsWith("http://")) {
                    log.warning("Discarding non-http callback URL: " + sp2);
                } else {
                    URL url = new URL(sp2);
                    try {
                        url.toURI();
                        arrayList.add(url);
                    } catch (URISyntaxException ex) {
                        log.log(Level.WARNING, "Discarding callback URL, not a valid URI on this platform: " + url, (Throwable) ex);
                    }
                }
            }
            setValue(arrayList);
        } catch (MalformedURLException ex2) {
            throw new InvalidHeaderException("Can't parse callback URLs from '" + s2 + "': " + ex2);
        }
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        StringBuilder s = new StringBuilder();
        for (URL url : getValue()) {
            s.append("<");
            s.append(url.toString());
            s.append(">");
        }
        return s.toString();
    }
}
