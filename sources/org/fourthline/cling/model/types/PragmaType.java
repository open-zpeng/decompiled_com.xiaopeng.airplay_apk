package org.fourthline.cling.model.types;
/* loaded from: classes.dex */
public class PragmaType {
    private boolean quote;
    private String token;
    private String value;

    public PragmaType(String token, String value, boolean quote) {
        this.token = token;
        this.value = value;
        this.quote = quote;
    }

    public PragmaType(String token, String value) {
        this.token = token;
        this.value = value;
    }

    public PragmaType(String value) {
        this.token = null;
        this.value = value;
    }

    public String getToken() {
        return this.token;
    }

    public String getValue() {
        return this.value;
    }

    public String getString() {
        String str;
        String s = "";
        if (this.token != null) {
            s = "" + this.token + "=";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(s);
        if (this.quote) {
            str = "\"" + this.value + "\"";
        } else {
            str = this.value;
        }
        sb.append(str);
        String s2 = sb.toString();
        return s2;
    }

    public static PragmaType valueOf(String s) throws InvalidValueException {
        String value;
        if (s.length() != 0) {
            String token = null;
            boolean quote = false;
            String[] params = s.split("=");
            if (params.length > 1) {
                token = params[0];
                value = params[1];
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    quote = true;
                    value = value.substring(1, value.length() - 1);
                }
            } else {
                value = s;
            }
            return new PragmaType(token, value, quote);
        }
        throw new InvalidValueException("Can't parse Bytes Range: " + s);
    }
}
