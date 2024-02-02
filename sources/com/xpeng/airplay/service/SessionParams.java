package com.xpeng.airplay.service;

import android.os.Parcel;
import android.os.Parcelable;
/* loaded from: classes.dex */
public class SessionParams implements Parcelable {
    public static final Parcelable.Creator<SessionParams> CREATOR = new Parcelable.Creator<SessionParams>() { // from class: com.xpeng.airplay.service.SessionParams.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public SessionParams createFromParcel(Parcel in) {
            return new SessionParams(in);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public SessionParams[] newArray(int size) {
            return new SessionParams[size];
        }
    };
    private String mPkgName;
    private int mScreenId;
    private String mServerName;

    public SessionParams(String pkgName, String serverName) {
        this.mPkgName = pkgName;
        this.mServerName = serverName;
    }

    public SessionParams(String pkgName, String serverName, int screenId) {
        this.mPkgName = pkgName;
        this.mServerName = serverName;
        this.mScreenId = screenId;
    }

    private SessionParams(Parcel in) {
        this.mPkgName = in.readString();
        this.mServerName = in.readString();
        this.mScreenId = in.readInt();
    }

    public void setPackgeName(String pkgName) {
        this.mPkgName = pkgName;
    }

    public String getPackageName() {
        return this.mPkgName;
    }

    public void setServerName(String serverName) {
        this.mServerName = serverName;
    }

    public String getServerName() {
        return this.mServerName;
    }

    public int getScreenId() {
        return this.mScreenId;
    }

    public void setScreenId(int mScreenId) {
        this.mScreenId = mScreenId;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPkgName);
        dest.writeString(this.mServerName);
        dest.writeInt(this.mScreenId);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public int hashCode() {
        return (131 * this.mPkgName.hashCode()) + (31 * this.mServerName.hashCode()) + this.mScreenId;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        SessionParams sp = (SessionParams) obj;
        if (sp.mPkgName.equals(this.mPkgName) && sp.mServerName.equals(this.mServerName) && sp.mScreenId == this.mScreenId) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "SessionParams { PkgName: " + this.mPkgName + ", ServerName: " + this.mServerName + ", ScreenId: " + this.mScreenId + "}";
    }
}
