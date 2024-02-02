package com.xpeng.airplay.service;

import android.os.Parcel;
import android.os.Parcelable;
/* loaded from: classes.dex */
public class MediaPlaybackInfo implements Parcelable {
    public static final Parcelable.Creator<MediaPlaybackInfo> CREATOR = new Parcelable.Creator<MediaPlaybackInfo>() { // from class: com.xpeng.airplay.service.MediaPlaybackInfo.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaPlaybackInfo createFromParcel(Parcel parcel) {
            MediaPlaybackInfo info = new MediaPlaybackInfo();
            info.setDuration(parcel.readDouble());
            info.setPosition(parcel.readDouble());
            info.setRate(parcel.readInt());
            info.setVolume(parcel.readFloat());
            return info;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaPlaybackInfo[] newArray(int size) {
            return new MediaPlaybackInfo[size];
        }
    };
    private double duration;
    private double position;
    private int rate;
    private float vol;

    public MediaPlaybackInfo() {
        reset();
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public void setVolume(float vol) {
        this.vol = vol;
    }

    public double getDuration() {
        return this.duration;
    }

    public double getPosition() {
        return this.position;
    }

    public int getRate() {
        return this.rate;
    }

    public float getVolume() {
        return this.vol;
    }

    public String toString() {
        return "MediaPlaybackInfo { duration = " + this.duration + ",rate = " + this.rate + ",position = " + this.position + ",volume = " + this.vol + "}";
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeDouble(this.duration);
        parcel.writeDouble(this.position);
        parcel.writeInt(this.rate);
        parcel.writeFloat(this.vol);
    }

    public void readFromParcel(Parcel parcel) {
        this.duration = parcel.readDouble();
        this.position = parcel.readDouble();
        this.rate = parcel.readInt();
        this.vol = parcel.readFloat();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public void copy(MediaPlaybackInfo info) {
        this.duration = info.duration;
        this.position = info.position;
        this.rate = info.rate;
        this.vol = info.vol;
    }

    public void reset() {
        this.duration = 0.0d;
        this.position = 0.0d;
        this.rate = 0;
        this.vol = 0.0f;
    }
}
