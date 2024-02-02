package com.xpeng.airplay.dlna;

import android.util.Log;
import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.model.Protocol;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.dlna.DLNAProfiles;
/* loaded from: classes.dex */
public class ConnectionManagerServiceImpl extends ConnectionManagerService {
    private static final String TAG = "ConnectionManagerServiceImpl";

    public ConnectionManagerServiceImpl() {
        try {
            this.sinkProtocolInfo.add(new ProtocolInfo(Protocol.HTTP_GET, "*", DLNAProfiles.DLNAMimeTypes.MIME_AUDIO_MPEG, "DLNA.ORG_PN=MP3;DLNA.ORG_OP=01"));
            this.sinkProtocolInfo.add(new ProtocolInfo(Protocol.HTTP_GET, "*", DLNAProfiles.DLNAMimeTypes.MIME_VIDEO_MPEG, "DLNA.ORG_PN=MPEG1;DLNA.ORG_OP=01;DLNA.ORG_CI=0"));
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "fail to add sink protocol");
            ex.printStackTrace();
        }
    }
}
