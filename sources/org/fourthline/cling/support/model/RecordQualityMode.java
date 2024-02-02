package org.fourthline.cling.support.model;

import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.model.ModelUtil;
/* loaded from: classes.dex */
public enum RecordQualityMode {
    EP("0:EP"),
    LP("1:LP"),
    SP("2:SP"),
    BASIC("0:BASIC"),
    MEDIUM("1:MEDIUM"),
    HIGH("2:HIGH"),
    NOT_IMPLEMENTED("NOT_IMPLEMENTED");
    
    private String protocolString;

    RecordQualityMode(String protocolString) {
        this.protocolString = protocolString;
    }

    @Override // java.lang.Enum
    public String toString() {
        return this.protocolString;
    }

    public static RecordQualityMode valueOrExceptionOf(String s) throws IllegalArgumentException {
        RecordQualityMode[] values;
        for (RecordQualityMode recordQualityMode : values()) {
            if (recordQualityMode.protocolString.equals(s)) {
                return recordQualityMode;
            }
        }
        throw new IllegalArgumentException("Invalid record quality mode string: " + s);
    }

    public static RecordQualityMode[] valueOfCommaSeparatedList(String s) {
        RecordQualityMode[] values;
        String[] strings = ModelUtil.fromCommaSeparatedList(s);
        if (strings == null) {
            return new RecordQualityMode[0];
        }
        List<RecordQualityMode> result = new ArrayList<>();
        for (String rqm : strings) {
            for (RecordQualityMode recordQualityMode : values()) {
                if (recordQualityMode.protocolString.equals(rqm)) {
                    result.add(recordQualityMode);
                }
            }
        }
        return (RecordQualityMode[]) result.toArray(new RecordQualityMode[result.size()]);
    }
}
