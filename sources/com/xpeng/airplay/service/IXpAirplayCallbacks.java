package com.xpeng.airplay.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
/* loaded from: classes.dex */
public interface IXpAirplayCallbacks extends IInterface {
    void onAudioProgressUpdated(MediaPlaybackInfo mediaPlaybackInfo) throws RemoteException;

    void onClientConnected(int i) throws RemoteException;

    void onClientDisconnected(int i, int i2) throws RemoteException;

    void onMetaDataUpdated(MediaMetaData mediaMetaData) throws RemoteException;

    void onMirrorSizeChanged(int i, int i2) throws RemoteException;

    void onMirrorStarted() throws RemoteException;

    void onMirrorStopped() throws RemoteException;

    void onServerNameUpdated(String str) throws RemoteException;

    void onVideoPlayStarted(MediaPlayInfo mediaPlayInfo) throws RemoteException;

    void onVideoPlayStopped() throws RemoteException;

    void onVideoRateChanged(int i) throws RemoteException;

    void onVideoScrubbed(int i) throws RemoteException;

    void onVolumeChanged(float f) throws RemoteException;

    /* loaded from: classes.dex */
    public static abstract class Stub extends Binder implements IXpAirplayCallbacks {
        private static final String DESCRIPTOR = "com.xpeng.airplay.service.IXpAirplayCallbacks";
        static final int TRANSACTION_onAudioProgressUpdated = 13;
        static final int TRANSACTION_onClientConnected = 1;
        static final int TRANSACTION_onClientDisconnected = 2;
        static final int TRANSACTION_onMetaDataUpdated = 11;
        static final int TRANSACTION_onMirrorSizeChanged = 5;
        static final int TRANSACTION_onMirrorStarted = 3;
        static final int TRANSACTION_onMirrorStopped = 4;
        static final int TRANSACTION_onServerNameUpdated = 12;
        static final int TRANSACTION_onVideoPlayStarted = 6;
        static final int TRANSACTION_onVideoPlayStopped = 7;
        static final int TRANSACTION_onVideoRateChanged = 8;
        static final int TRANSACTION_onVideoScrubbed = 9;
        static final int TRANSACTION_onVolumeChanged = 10;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IXpAirplayCallbacks asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IXpAirplayCallbacks)) {
                return (IXpAirplayCallbacks) iin;
            }
            return new Proxy(obj);
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1598968902) {
                reply.writeString(DESCRIPTOR);
                return true;
            }
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    onClientConnected(_arg0);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg02 = data.readInt();
                    int _arg1 = data.readInt();
                    onClientDisconnected(_arg02, _arg1);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    onMirrorStarted();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    onMirrorStopped();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg03 = data.readInt();
                    int _arg12 = data.readInt();
                    onMirrorSizeChanged(_arg03, _arg12);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    MediaPlayInfo _arg04 = data.readInt() != 0 ? MediaPlayInfo.CREATOR.createFromParcel(data) : null;
                    onVideoPlayStarted(_arg04);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    onVideoPlayStopped();
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg05 = data.readInt();
                    onVideoRateChanged(_arg05);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg06 = data.readInt();
                    onVideoScrubbed(_arg06);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    float _arg07 = data.readFloat();
                    onVolumeChanged(_arg07);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    MediaMetaData _arg08 = data.readInt() != 0 ? MediaMetaData.CREATOR.createFromParcel(data) : null;
                    onMetaDataUpdated(_arg08);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg09 = data.readString();
                    onServerNameUpdated(_arg09);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    MediaPlaybackInfo _arg010 = data.readInt() != 0 ? MediaPlaybackInfo.CREATOR.createFromParcel(data) : null;
                    onAudioProgressUpdated(_arg010);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        /* loaded from: classes.dex */
        private static class Proxy implements IXpAirplayCallbacks {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
            public void onClientConnected(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
            public void onClientDisconnected(int type, int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(reason);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
            public void onMirrorStarted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
            public void onMirrorStopped() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
            public void onMirrorSizeChanged(int w, int h) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(w);
                    _data.writeInt(h);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
            public void onVideoPlayStarted(MediaPlayInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
            public void onVideoPlayStopped() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
            public void onVideoRateChanged(int rate) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rate);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
            public void onVideoScrubbed(int pos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pos);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
            public void onVolumeChanged(float vol) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(vol);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
            public void onMetaDataUpdated(MediaMetaData metaData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (metaData != null) {
                        _data.writeInt(1);
                        metaData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
            public void onServerNameUpdated(String serverName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(serverName);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
            public void onAudioProgressUpdated(MediaPlaybackInfo playbackInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (playbackInfo != null) {
                        _data.writeInt(1);
                        playbackInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}
