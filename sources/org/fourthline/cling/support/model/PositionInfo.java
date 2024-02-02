package org.fourthline.cling.support.model;

import java.util.Map;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
/* loaded from: classes.dex */
public class PositionInfo {
    private int absCount;
    private String absTime;
    private int relCount;
    private String relTime;
    private UnsignedIntegerFourBytes track;
    private String trackDuration;
    private String trackMetaData;
    private String trackURI;

    public PositionInfo() {
        this.track = new UnsignedIntegerFourBytes(0L);
        this.trackDuration = "00:00:00";
        this.trackMetaData = "NOT_IMPLEMENTED";
        this.trackURI = "";
        this.relTime = "00:00:00";
        this.absTime = "00:00:00";
        this.relCount = Integer.MAX_VALUE;
        this.absCount = Integer.MAX_VALUE;
    }

    public PositionInfo(Map<String, ActionArgumentValue> args) {
        this(((UnsignedIntegerFourBytes) args.get("Track").getValue()).getValue().longValue(), (String) args.get("TrackDuration").getValue(), (String) args.get("TrackMetaData").getValue(), (String) args.get("TrackURI").getValue(), (String) args.get("RelTime").getValue(), (String) args.get("AbsTime").getValue(), ((Integer) args.get("RelCount").getValue()).intValue(), ((Integer) args.get("AbsCount").getValue()).intValue());
    }

    public PositionInfo(PositionInfo copy, String relTime, String absTime) {
        this.track = new UnsignedIntegerFourBytes(0L);
        this.trackDuration = "00:00:00";
        this.trackMetaData = "NOT_IMPLEMENTED";
        this.trackURI = "";
        this.relTime = "00:00:00";
        this.absTime = "00:00:00";
        this.relCount = Integer.MAX_VALUE;
        this.absCount = Integer.MAX_VALUE;
        this.track = copy.track;
        this.trackDuration = copy.trackDuration;
        this.trackMetaData = copy.trackMetaData;
        this.trackURI = copy.trackURI;
        this.relTime = relTime;
        this.absTime = absTime;
        this.relCount = copy.relCount;
        this.absCount = copy.absCount;
    }

    public PositionInfo(PositionInfo copy, long positionSeconds, long durationSeconds) {
        this.track = new UnsignedIntegerFourBytes(0L);
        this.trackDuration = "00:00:00";
        this.trackMetaData = "NOT_IMPLEMENTED";
        this.trackURI = "";
        this.relTime = "00:00:00";
        this.absTime = "00:00:00";
        this.relCount = Integer.MAX_VALUE;
        this.absCount = Integer.MAX_VALUE;
        this.track = new UnsignedIntegerFourBytes(1L);
        this.trackDuration = ModelUtil.toTimeString(durationSeconds);
        this.trackMetaData = copy.trackMetaData;
        this.trackURI = copy.trackURI;
        this.relTime = ModelUtil.toTimeString(positionSeconds);
        this.absTime = ModelUtil.toTimeString(positionSeconds);
        this.relCount = copy.relCount;
        this.absCount = copy.absCount;
    }

    public PositionInfo(long track, String trackDuration, String trackURI, String relTime, String absTime) {
        this.track = new UnsignedIntegerFourBytes(0L);
        this.trackDuration = "00:00:00";
        this.trackMetaData = "NOT_IMPLEMENTED";
        this.trackURI = "";
        this.relTime = "00:00:00";
        this.absTime = "00:00:00";
        this.relCount = Integer.MAX_VALUE;
        this.absCount = Integer.MAX_VALUE;
        this.track = new UnsignedIntegerFourBytes(track);
        this.trackDuration = trackDuration;
        this.trackURI = trackURI;
        this.relTime = relTime;
        this.absTime = absTime;
    }

    public PositionInfo(long track, String trackDuration, String trackMetaData, String trackURI, String relTime, String absTime, int relCount, int absCount) {
        this.track = new UnsignedIntegerFourBytes(0L);
        this.trackDuration = "00:00:00";
        this.trackMetaData = "NOT_IMPLEMENTED";
        this.trackURI = "";
        this.relTime = "00:00:00";
        this.absTime = "00:00:00";
        this.relCount = Integer.MAX_VALUE;
        this.absCount = Integer.MAX_VALUE;
        this.track = new UnsignedIntegerFourBytes(track);
        this.trackDuration = trackDuration;
        this.trackMetaData = trackMetaData;
        this.trackURI = trackURI;
        this.relTime = relTime;
        this.absTime = absTime;
        this.relCount = relCount;
        this.absCount = absCount;
    }

    public PositionInfo(long track, String trackMetaData, String trackURI) {
        this.track = new UnsignedIntegerFourBytes(0L);
        this.trackDuration = "00:00:00";
        this.trackMetaData = "NOT_IMPLEMENTED";
        this.trackURI = "";
        this.relTime = "00:00:00";
        this.absTime = "00:00:00";
        this.relCount = Integer.MAX_VALUE;
        this.absCount = Integer.MAX_VALUE;
        this.track = new UnsignedIntegerFourBytes(track);
        this.trackMetaData = trackMetaData;
        this.trackURI = trackURI;
    }

    public void setTrack(long track) {
        this.track = new UnsignedIntegerFourBytes(track);
    }

    public UnsignedIntegerFourBytes getTrack() {
        return this.track;
    }

    public String getTrackDuration() {
        return this.trackDuration;
    }

    public void setTrackMetaData(String metaData) {
        this.trackMetaData = metaData;
    }

    public String getTrackMetaData() {
        return this.trackMetaData;
    }

    public void setTrackURI(String uri) {
        this.trackURI = uri;
    }

    public String getTrackURI() {
        return this.trackURI;
    }

    public void setRelTime(long time) {
        this.relTime = ModelUtil.toTimeString(time);
    }

    public String getRelTime() {
        return this.relTime;
    }

    public void setAbsTime(long time) {
        this.absTime = ModelUtil.toTimeString(time);
    }

    public String getAbsTime() {
        return this.absTime;
    }

    public int getRelCount() {
        return this.relCount;
    }

    public int getAbsCount() {
        return this.absCount;
    }

    public void setTrackDuration(long trackDuration) {
        this.trackDuration = ModelUtil.toTimeString(trackDuration);
    }

    public void setTrackDuration(String trackDuration) {
        this.trackDuration = trackDuration;
    }

    public void setRelTime(String relTime) {
        this.relTime = relTime;
    }

    public long getTrackDurationSeconds() {
        if (getTrackDuration() == null) {
            return 0L;
        }
        return ModelUtil.fromTimeString(getTrackDuration());
    }

    public long getTrackElapsedSeconds() {
        if (getRelTime() == null || getRelTime().equals("NOT_IMPLEMENTED")) {
            return 0L;
        }
        return ModelUtil.fromTimeString(getRelTime());
    }

    public long getTrackRemainingSeconds() {
        return getTrackDurationSeconds() - getTrackElapsedSeconds();
    }

    public int getElapsedPercent() {
        long elapsed = getTrackElapsedSeconds();
        long total = getTrackDurationSeconds();
        if (elapsed == 0 || total == 0) {
            return 0;
        }
        return new Double(elapsed / (total / 100.0d)).intValue();
    }

    public String toString() {
        return "(PositionInfo) Track: " + getTrack() + " RelTime: " + getRelTime() + " Duration: " + getTrackDuration() + " Percent: " + getElapsedPercent();
    }
}
