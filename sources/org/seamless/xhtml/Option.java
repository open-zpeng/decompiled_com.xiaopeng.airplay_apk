package org.seamless.xhtml;

import com.xpeng.airplay.service.NsdConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
/* loaded from: classes.dex */
public class Option {
    private String key;
    private String[] values;

    public Option(String key, String[] values) {
        this.key = key;
        this.values = values;
    }

    public static Option[] fromString(String string) {
        int i = 0;
        if (string == null || string.length() == 0) {
            return new Option[0];
        }
        List<Option> options = new ArrayList<>();
        try {
            String[] fields = string.split(";");
            int len$ = fields.length;
            int i$ = 0;
            while (i$ < len$) {
                String field = fields[i$].trim();
                if (field.contains(":")) {
                    String[] keyValues = field.split(":");
                    if (keyValues.length == 2) {
                        String key = keyValues[i].trim();
                        String[] values = keyValues[1].split(",");
                        List<String> cleanValues = new ArrayList<>();
                        int len$2 = values.length;
                        for (int i$2 = i; i$2 < len$2; i$2++) {
                            String s = values[i$2];
                            String value = s.trim();
                            if (value.length() > 0) {
                                cleanValues.add(value);
                            }
                        }
                        options.add(new Option(key, (String[]) cleanValues.toArray(new String[cleanValues.size()])));
                    }
                }
                i$++;
                i = 0;
            }
            return (Option[]) options.toArray(new Option[options.size()]);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Can't parse options string: " + string, ex);
        }
    }

    public String getKey() {
        return this.key;
    }

    public String[] getValues() {
        return this.values;
    }

    public boolean isTrue() {
        return getValues().length == 1 && getValues()[0].toLowerCase().equals(NsdConstants.AIRPLAY_TXT_VALUE_DA);
    }

    public boolean isFalse() {
        return getValues().length == 1 && getValues()[0].toLowerCase().equals("false");
    }

    public String getFirstValue() {
        return getValues()[0];
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getKey());
        sb.append(": ");
        Iterator<String> it = Arrays.asList(getValues()).iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Option that = (Option) o;
        if (this.key.equals(that.key) && Arrays.equals(this.values, that.values)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = this.key.hashCode();
        return (31 * result) + Arrays.hashCode(this.values);
    }
}
