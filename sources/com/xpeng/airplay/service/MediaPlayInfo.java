package com.xpeng.airplay.service;

import android.os.Parcel;
import android.os.Parcelable;
/* loaded from: classes.dex */
public class MediaPlayInfo implements Parcelable {
    public static final Parcelable.Creator<MediaPlayInfo> CREATOR = new Parcelable.Creator<MediaPlayInfo>() { // from class: com.xpeng.airplay.service.MediaPlayInfo.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaPlayInfo createFromParcel(Parcel parcel) {
            MediaPlayInfo info = new MediaPlayInfo();
            info.setType(parcel.readInt());
            info.setUrl(parcel.readString());
            info.setTitle(parcel.readString());
            info.setVolume(parcel.readFloat());
            info.setPosition(parcel.readInt());
            info.setStreamType(parcel.readInt());
            return info;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaPlayInfo[] newArray(int size) {
            return new MediaPlayInfo[size];
        }
    };
    private int pos;
    private int streamType;
    private String title;
    private int type;
    private String url;
    private float volume;

    public MediaPlayInfo() {
        this.url = "";
        this.title = "";
        this.volume = 0.0f;
        this.pos = 0;
        this.streamType = 0;
    }

    public MediaPlayInfo(String url, String title, float vol, int pos) {
        this.url = url;
        this.title = title;
        this.volume = vol;
        this.pos = pos;
    }

    public void copy(MediaPlayInfo info) {
        this.url = info.url;
        this.title = info.title;
        this.volume = info.volume;
        this.pos = info.pos;
        this.streamType = info.streamType;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVolume(float vol) {
        this.volume = vol;
    }

    public void setPosition(int pos) {
        this.pos = pos;
    }

    public void setStreamType(int type) {
        this.streamType = type;
    }

    public String getUrl() {
        return this.url;
    }

    public String getTitle() {
        return this.title;
    }

    public float getVolume() {
        return this.volume;
    }

    public int getPosition() {
        return this.pos;
    }

    public int getStreamType() {
        return this.streamType;
    }

    public String toString() {
        return "MediaPlayInfo { url = " + this.url + ",, type = " + this.type + ",title = " + this.title + ", volume = " + this.volume + ", position = " + this.pos + ",streamType = " + this.streamType + "}";
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.type);
        parcel.writeString(this.url);
        parcel.writeString(this.title);
        parcel.writeFloat(this.volume);
        parcel.writeInt(this.pos);
        parcel.writeInt(this.streamType);
    }

    public void readFromParcel(Parcel parcel) {
        this.type = parcel.readInt();
        this.url = parcel.readString();
        this.title = parcel.readString();
        this.volume = parcel.readFloat();
        this.pos = parcel.readInt();
        this.streamType = parcel.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
