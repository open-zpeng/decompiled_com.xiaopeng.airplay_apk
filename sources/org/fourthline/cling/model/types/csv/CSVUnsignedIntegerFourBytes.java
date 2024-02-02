package org.fourthline.cling.model.types.csv;

import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
/* loaded from: classes.dex */
public class CSVUnsignedIntegerFourBytes extends CSV<UnsignedIntegerFourBytes> {
    public CSVUnsignedIntegerFourBytes() {
    }

    public CSVUnsignedIntegerFourBytes(String s) throws InvalidValueException {
        super(s);
    }
}
