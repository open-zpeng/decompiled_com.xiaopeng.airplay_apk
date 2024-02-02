package org.fourthline.cling.support.model.dlna.message.header;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.seamless.util.Exceptions;
/* loaded from: classes.dex */
public abstract class DLNAHeader<T> extends UpnpHeader<T> {
    private static final Logger log = Logger.getLogger(DLNAHeader.class.getName());

    /* loaded from: classes.dex */
    public enum Type {
        TimeSeekRange("TimeSeekRange.dlna.org", TimeSeekRangeHeader.class),
        XSeekRange("X-Seek-Range", TimeSeekRangeHeader.class),
        PlaySpeed("PlaySpeed.dlna.org", PlaySpeedHeader.class),
        AvailableSeekRange("availableSeekRange.dlna.org", AvailableSeekRangeHeader.class),
        GetAvailableSeekRange("getAvailableSeekRange.dlna.org", GetAvailableSeekRangeHeader.class),
        GetContentFeatures("getcontentFeatures.dlna.org", GetContentFeaturesHeader.class),
        ContentFeatures("contentFeatures.dlna.org", ContentFeaturesHeader.class),
        TransferMode("transferMode.dlna.org", TransferModeHeader.class),
        FriendlyName("friendlyName.dlna.org", FriendlyNameHeader.class),
        PeerManager("peerManager.dlna.org", PeerManagerHeader.class),
        AvailableRange("Available-Range.dlna.org", AvailableRangeHeader.class),
        SCID("scid.dlna.org", SCIDHeader.class),
        RealTimeInfo("realTimeInfo.dlna.org", RealTimeInfoHeader.class),
        ScmsFlag("scmsFlag.dlna.org", ScmsFlagHeader.class),
        WCT("WCT.dlna.org", WCTHeader.class),
        MaxPrate("Max-Prate.dlna.org", MaxPrateHeader.class),
        EventType("Event-Type.dlna.org", EventTypeHeader.class),
        Supported("Supported", SupportedHeader.class),
        BufferInfo("Buffer-Info.dlna.org", BufferInfoHeader.class),
        RTPH264DeInterleaving("rtp-h264-deint-buf-cap.dlna.org", BufferBytesHeader.class),
        RTPAACDeInterleaving("rtp-aac-deint-buf-cap.dlna.org", BufferBytesHeader.class),
        RTPAMRDeInterleaving("rtp-amr-deint-buf-cap.dlna.org", BufferBytesHeader.class),
        RTPAMRWBPlusDeInterleaving("rtp-amrwbplus-deint-buf-cap.dlna.org", BufferBytesHeader.class),
        PRAGMA("PRAGMA", PragmaHeader.class);
        
        private static Map<String, Type> byName = new HashMap<String, Type>() { // from class: org.fourthline.cling.support.model.dlna.message.header.DLNAHeader.Type.1
            {
                Type[] values;
                for (Type t : Type.values()) {
                    put(t.getHttpName(), t);
                }
            }
        };
        private Class<? extends DLNAHeader>[] headerTypes;
        private String httpName;

        @SafeVarargs
        Type(String httpName, Class... clsArr) {
            this.httpName = httpName;
            this.headerTypes = clsArr;
        }

        public String getHttpName() {
            return this.httpName;
        }

        public Class<? extends DLNAHeader>[] getHeaderTypes() {
            return this.headerTypes;
        }

        public boolean isValidHeaderType(Class<? extends DLNAHeader> clazz) {
            Class<? extends DLNAHeader>[] headerTypes;
            for (Class<? extends DLNAHeader> permissibleType : getHeaderTypes()) {
                if (permissibleType.isAssignableFrom(clazz)) {
                    return true;
                }
            }
            return false;
        }

        public static Type getByHttpName(String httpName) {
            if (httpName == null) {
                return null;
            }
            return byName.get(httpName);
        }
    }

    public static DLNAHeader newInstance(Type type, String headerValue) {
        DLNAHeader upnpHeader = null;
        for (int i = 0; i < type.getHeaderTypes().length && upnpHeader == null; i++) {
            Class<? extends DLNAHeader> headerClass = type.getHeaderTypes()[i];
            try {
                Logger logger = log;
                logger.finest("Trying to parse '" + type + "' with class: " + headerClass.getSimpleName());
                upnpHeader = headerClass.newInstance();
                if (headerValue != null) {
                    upnpHeader.setString(headerValue);
                }
            } catch (InvalidHeaderException ex) {
                Logger logger2 = log;
                logger2.finest("Invalid header value for tested type: " + headerClass.getSimpleName() + " - " + ex.getMessage());
                upnpHeader = null;
            } catch (Exception ex2) {
                Logger logger3 = log;
                logger3.severe("Error instantiating header of type '" + type + "' with value: " + headerValue);
                log.log(Level.SEVERE, "Exception root cause: ", Exceptions.unwrap(ex2));
            }
        }
        return upnpHeader;
    }
}
