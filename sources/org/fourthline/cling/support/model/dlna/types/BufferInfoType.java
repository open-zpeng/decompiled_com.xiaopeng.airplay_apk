package org.fourthline.cling.support.model.dlna.types;

import com.xpeng.airplay.service.NsdConstants;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.support.model.dlna.types.CodedDataBuffer;
/* loaded from: classes.dex */
public class BufferInfoType {
    static final Pattern pattern = Pattern.compile("^dejitter=(\\d{1,10})(;CDB=(\\d{1,10});BTM=(0|1|2))?(;TD=(\\d{1,10}))?(;BFR=(0|1))?$", 2);
    private CodedDataBuffer cdb;
    private Long dejitterSize;
    private Boolean fullnessReports;
    private Long targetDuration;

    public BufferInfoType(Long dejitterSize) {
        this.dejitterSize = dejitterSize;
    }

    public BufferInfoType(Long dejitterSize, CodedDataBuffer cdb, Long targetDuration, Boolean fullnessReports) {
        this.dejitterSize = dejitterSize;
        this.cdb = cdb;
        this.targetDuration = targetDuration;
        this.fullnessReports = fullnessReports;
    }

    public static BufferInfoType valueOf(String s) throws InvalidValueException {
        Matcher matcher = pattern.matcher(s);
        if (matcher.matches()) {
            try {
                Long dejitterSize = Long.valueOf(Long.parseLong(matcher.group(1)));
                CodedDataBuffer cdb = null;
                Long targetDuration = null;
                Boolean fullnessReports = null;
                if (matcher.group(2) != null) {
                    cdb = new CodedDataBuffer(Long.valueOf(Long.parseLong(matcher.group(3))), CodedDataBuffer.TransferMechanism.values()[Integer.parseInt(matcher.group(4))]);
                }
                if (matcher.group(5) != null) {
                    targetDuration = Long.valueOf(Long.parseLong(matcher.group(6)));
                }
                if (matcher.group(7) != null) {
                    fullnessReports = Boolean.valueOf(matcher.group(8).equals(NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS));
                }
                return new BufferInfoType(dejitterSize, cdb, targetDuration, fullnessReports);
            } catch (NumberFormatException e) {
            }
        }
        throw new InvalidValueException("Can't parse BufferInfoType: " + s);
    }

    public String getString() {
        String s = "dejitter=" + this.dejitterSize.toString();
        if (this.cdb != null) {
            s = s + ";CDB=" + this.cdb.getSize().toString() + ";BTM=" + this.cdb.getTranfer().ordinal();
        }
        if (this.targetDuration != null) {
            s = s + ";TD=" + this.targetDuration.toString();
        }
        if (this.fullnessReports != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(s);
            sb.append(";BFR=");
            sb.append(this.fullnessReports.booleanValue() ? NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS : "0");
            return sb.toString();
        }
        return s;
    }

    public Long getDejitterSize() {
        return this.dejitterSize;
    }

    public CodedDataBuffer getCdb() {
        return this.cdb;
    }

    public Long getTargetDuration() {
        return this.targetDuration;
    }

    public Boolean isFullnessReports() {
        return this.fullnessReports;
    }
}
