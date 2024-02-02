package org.fourthline.cling.model.types.csv;

import org.fourthline.cling.model.types.InvalidValueException;
/* loaded from: classes.dex */
public class CSVFloat extends CSV<Float> {
    public CSVFloat() {
    }

    public CSVFloat(String s) throws InvalidValueException {
        super(s);
    }
}
