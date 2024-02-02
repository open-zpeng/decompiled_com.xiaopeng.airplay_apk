package org.seamless.xml;

import org.xml.sax.SAXParseException;
/* loaded from: classes.dex */
public class ParserException extends Exception {
    public ParserException() {
    }

    public ParserException(String s) {
        super(s);
    }

    public ParserException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ParserException(Throwable throwable) {
        super(throwable);
    }

    public ParserException(SAXParseException ex) {
        super("(Line/Column: " + ex.getLineNumber() + ":" + ex.getColumnNumber() + ") " + ex.getMessage());
    }
}
