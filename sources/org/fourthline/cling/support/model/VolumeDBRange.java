package org.fourthline.cling.support.model;
/* loaded from: classes.dex */
public class VolumeDBRange {
    private Integer maxValue;
    private Integer minValue;

    public VolumeDBRange(Integer minValue, Integer maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public Integer getMinValue() {
        return this.minValue;
    }

    public Integer getMaxValue() {
        return this.maxValue;
    }
}
