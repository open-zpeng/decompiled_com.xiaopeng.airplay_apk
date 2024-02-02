package org.fourthline.cling.support.model;

import android.util.Log;
import com.xpeng.airplay.service.NsdConstants;
import java.util.Map;
import org.fourthline.cling.model.action.ActionArgumentValue;
/* loaded from: classes.dex */
public class TransportInfo {
    private final String TAG;
    private String currentSpeed;
    private TransportState currentTransportState;
    private TransportStatus currentTransportStatus;

    public TransportInfo() {
        this.TAG = "TransportInfo";
        this.currentTransportState = TransportState.NO_MEDIA_PRESENT;
        this.currentTransportStatus = TransportStatus.OK;
        this.currentSpeed = NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS;
    }

    public TransportInfo(Map<String, ActionArgumentValue> args) {
        this(TransportState.valueOrCustomOf((String) args.get("CurrentTransportState").getValue()), TransportStatus.valueOrCustomOf((String) args.get("CurrentTransportStatus").getValue()), (String) args.get("CurrentSpeed").getValue());
    }

    public TransportInfo(TransportState currentTransportState) {
        this.TAG = "TransportInfo";
        this.currentTransportState = TransportState.NO_MEDIA_PRESENT;
        this.currentTransportStatus = TransportStatus.OK;
        this.currentSpeed = NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS;
        this.currentTransportState = currentTransportState;
    }

    public TransportInfo(TransportState currentTransportState, String currentSpeed) {
        this.TAG = "TransportInfo";
        this.currentTransportState = TransportState.NO_MEDIA_PRESENT;
        this.currentTransportStatus = TransportStatus.OK;
        this.currentSpeed = NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS;
        this.currentTransportState = currentTransportState;
        this.currentSpeed = currentSpeed;
    }

    public TransportInfo(TransportState currentTransportState, TransportStatus currentTransportStatus) {
        this.TAG = "TransportInfo";
        this.currentTransportState = TransportState.NO_MEDIA_PRESENT;
        this.currentTransportStatus = TransportStatus.OK;
        this.currentSpeed = NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS;
        this.currentTransportState = currentTransportState;
        this.currentTransportStatus = currentTransportStatus;
    }

    public TransportInfo(TransportState currentTransportState, TransportStatus currentTransportStatus, String currentSpeed) {
        this.TAG = "TransportInfo";
        this.currentTransportState = TransportState.NO_MEDIA_PRESENT;
        this.currentTransportStatus = TransportStatus.OK;
        this.currentSpeed = NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS;
        this.currentTransportState = currentTransportState;
        this.currentTransportStatus = currentTransportStatus;
        this.currentSpeed = currentSpeed;
    }

    public TransportState getCurrentTransportState() {
        return this.currentTransportState;
    }

    public void setCurrentTransportState(TransportState state) {
        Log.i("TransportInfo", "setCurrentTransportState state=" + state.getValue());
        this.currentTransportState = state;
    }

    public TransportStatus getCurrentTransportStatus() {
        return this.currentTransportStatus;
    }

    public void setCurrentTransportStatus(TransportStatus status) {
        Log.i("TransportInfo", "setCurrentTransportStatus state=" + status.getValue());
        this.currentTransportStatus = status;
    }

    public String getCurrentSpeed() {
        return this.currentSpeed;
    }

    public void setCurrentSpeed(String speed) {
        Log.i("TransportInfo", "setCurrentSpeed speed=" + speed);
        this.currentSpeed = speed;
    }

    public String toString() {
        return "TransportInfo { State = " + this.currentTransportState.getValue() + ",Status = " + this.currentTransportStatus.getValue() + "}";
    }
}
