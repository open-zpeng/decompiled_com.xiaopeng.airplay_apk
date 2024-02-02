package org.fourthline.cling.support.model.item;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Res;
/* loaded from: classes.dex */
public class AudioBroadcast extends AudioItem {
    public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.item.audioItem.audioBroadcast");

    public AudioBroadcast() {
        setClazz(CLASS);
    }

    public AudioBroadcast(Item other) {
        super(other);
    }

    public AudioBroadcast(String id, String parentID, String title, String creator, Res... resource) {
        super(id, parentID, title, creator, resource);
        setClazz(CLASS);
    }

    public String getRegion() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.REGION.class);
    }

    public AudioBroadcast setRegion(String region) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.REGION(region));
        return this;
    }

    public String getRadioCallSign() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.RADIO_CALL_SIGN.class);
    }

    public AudioBroadcast setRadioCallSign(String radioCallSign) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.RADIO_CALL_SIGN(radioCallSign));
        return this;
    }

    public String getRadioStationID() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.RADIO_STATION_ID.class);
    }

    public AudioBroadcast setRadioStationID(String radioStationID) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.RADIO_STATION_ID(radioStationID));
        return this;
    }

    public String getRadioBand() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.RADIO_BAND.class);
    }

    public AudioBroadcast setRadioBand(String radioBand) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.RADIO_BAND(radioBand));
        return this;
    }

    public Integer getChannelNr() {
        return (Integer) getFirstPropertyValue(DIDLObject.Property.UPNP.CHANNEL_NR.class);
    }

    public AudioBroadcast setChannelNr(Integer channelNr) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.CHANNEL_NR(channelNr));
        return this;
    }
}
