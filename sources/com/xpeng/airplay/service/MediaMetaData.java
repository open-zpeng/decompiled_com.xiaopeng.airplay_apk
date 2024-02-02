package com.xpeng.airplay.service;

import android.os.Parcel;
import android.os.Parcelable;
/* loaded from: classes.dex */
public class MediaMetaData implements Parcelable {
    public static final Parcelable.Creator<MediaMetaData> CREATOR = new Parcelable.Creator<MediaMetaData>() { // from class: com.xpeng.airplay.service.MediaMetaData.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaMetaData createFromParcel(Parcel in) {
            return new MediaMetaData(in);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaMetaData[] newArray(int size) {
            return new MediaMetaData[size];
        }
    };
    private String mAlbum;
    private String mArtist;
    private int mMediaType;
    private String mTitle;

    public MediaMetaData(String title, String artist, String album, int type) {
        this.mTitle = title;
        this.mArtist = artist;
        this.mAlbum = album;
        this.mMediaType = type;
    }

    protected MediaMetaData(Parcel in) {
        this.mTitle = in.readString();
        this.mArtist = in.readString();
        this.mAlbum = in.readString();
        this.mMediaType = in.readInt();
    }

    public String getTitle() {
        return this.mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getArtist() {
        return this.mArtist;
    }

    public void setArtist(String artist) {
        this.mArtist = artist;
    }

    public String getAlbum() {
        return this.mAlbum;
    }

    public void setAlbum(String album) {
        this.mAlbum = album;
    }

    public int getMediaType() {
        return this.mMediaType;
    }

    public void setMediaType(int type) {
        this.mMediaType = type;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mTitle);
        dest.writeString(this.mArtist);
        dest.writeString(this.mAlbum);
        dest.writeInt(this.mMediaType);
    }

    public String toString() {
        return "MediaMetaData {title = " + this.mTitle + ", artist = " + this.mArtist + ",album = " + this.mAlbum + ", media type = " + this.mMediaType + "}";
    }
}
