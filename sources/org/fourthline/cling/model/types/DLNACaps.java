package org.fourthline.cling.model.types;

import java.util.Arrays;
import org.fourthline.cling.model.ModelUtil;
/* loaded from: classes.dex */
public class DLNACaps {
    final String[] caps;

    public DLNACaps(String[] caps) {
        this.caps = caps;
    }

    public String[] getCaps() {
        return this.caps;
    }

    public static DLNACaps valueOf(String s) throws InvalidValueException {
        if (s == null || s.length() == 0) {
            return new DLNACaps(new String[0]);
        }
        String[] caps = s.split(",");
        String[] trimmed = new String[caps.length];
        for (int i = 0; i < caps.length; i++) {
            trimmed[i] = caps[i].trim();
        }
        return new DLNACaps(trimmed);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DLNACaps dlnaCaps = (DLNACaps) o;
        if (Arrays.equals(this.caps, dlnaCaps.caps)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Arrays.hashCode(this.caps);
    }

    public String toString() {
        return ModelUtil.toCommaSeparatedList(getCaps());
    }
}
