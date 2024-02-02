package org.fourthline.cling.model.types.csv;

import org.fourthline.cling.model.types.InvalidValueException;
/* loaded from: classes.dex */
public class CSVBytes extends CSV<byte[]> {
    public CSVBytes() {
    }

    public CSVBytes(String s) throws InvalidValueException {
        super(s);
    }
}
