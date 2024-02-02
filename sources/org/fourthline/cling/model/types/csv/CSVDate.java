package org.fourthline.cling.model.types.csv;

import java.util.Date;
import org.fourthline.cling.model.types.InvalidValueException;
/* loaded from: classes.dex */
public class CSVDate extends CSV<Date> {
    public CSVDate() {
    }

    public CSVDate(String s) throws InvalidValueException {
        super(s);
    }
}
