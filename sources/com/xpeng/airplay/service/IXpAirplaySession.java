package com.xpeng.airplay.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.Surface;
import com.xpeng.airplay.service.IXpAirplayCallbacks;
/* loaded from: classes.dex */
public interface IXpAirplaySession extends IInterface {
    String getServerName() throws RemoteException;

    boolean hasActiveConnection() throws RemoteException;

    void registerAirplayCallbacks(IXpAirplayCallbacks iXpAirplayCallbacks) throws RemoteException;

    void saveSystemVolume(int i) throws RemoteException;

    void setMediaPlaybackInfo(MediaPlaybackInfo mediaPlaybackInfo) throws RemoteException;

    void setMirrorSurface(Surface surface) throws RemoteException;

    void setVideoPlaybackState(int i) throws RemoteException;

    void unregisterAirplayCallbacks(IXpAirplayCallbacks iXpAirplayCallbacks) throws RemoteException;

    /* loaded from: classes.dex */
    public static abstract class Stub extends Binder implements IXpAirplaySession {
        private static final String DESCRIPTOR = "com.xpeng.airplay.service.IXpAirplaySession";
        static final int TRANSACTION_getServerName = 1;
        static final int TRANSACTION_hasActiveConnection = 2;
        static final int TRANSACTION_registerAirplayCallbacks = 3;
        static final int TRANSACTION_saveSystemVolume = 8;
        static final int TRANSACTION_setMediaPlaybackInfo = 7;
        static final int TRANSACTION_setMirrorSurface = 5;
        static final int TRANSACTION_setVideoPlaybackState = 6;
        static final int TRANSACTION_unregisterAirplayCallbacks = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IXpAirplaySession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IXpAirplaySession)) {
                return (IXpAirplaySession) iin;
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
                    String _result = getServerName();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    boolean hasActiveConnection = hasActiveConnection();
                    reply.writeNoException();
                    reply.writeInt(hasActiveConnection ? 1 : 0);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    IXpAirplayCallbacks _arg0 = IXpAirplayCallbacks.Stub.asInterface(data.readStrongBinder());
                    registerAirplayCallbacks(_arg0);
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    IXpAirplayCallbacks _arg02 = IXpAirplayCallbacks.Stub.asInterface(data.readStrongBinder());
                    unregisterAirplayCallbacks(_arg02);
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    Surface _arg03 = data.readInt() != 0 ? (Surface) Surface.CREATOR.createFromParcel(data) : null;
                    setMirrorSurface(_arg03);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg04 = data.readInt();
                    setVideoPlaybackState(_arg04);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    MediaPlaybackInfo _arg05 = data.readInt() != 0 ? MediaPlaybackInfo.CREATOR.createFromParcel(data) : null;
                    setMediaPlaybackInfo(_arg05);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg06 = data.readInt();
                    saveSystemVolume(_arg06);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        /* loaded from: classes.dex */
        private static class Proxy implements IXpAirplaySession {
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

            @Override // com.xpeng.airplay.service.IXpAirplaySession
            public String getServerName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplaySession
            public boolean hasActiveConnection() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplaySession
            public void registerAirplayCallbacks(IXpAirplayCallbacks callbacks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplaySession
            public void unregisterAirplayCallbacks(IXpAirplayCallbacks callbacks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplaySession
            public void setMirrorSurface(Surface surface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (surface != null) {
                        _data.writeInt(1);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplaySession
            public void setVideoPlaybackState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplaySession
            public void setMediaPlaybackInfo(MediaPlaybackInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xpeng.airplay.service.IXpAirplaySession
            public void saveSystemVolume(int volume) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(volume);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}
