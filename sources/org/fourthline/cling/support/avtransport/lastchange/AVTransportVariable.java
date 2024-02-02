package org.fourthline.cling.support.avtransport.lastchange;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.lastchange.EventedValueEnum;
import org.fourthline.cling.support.lastchange.EventedValueEnumArray;
import org.fourthline.cling.support.lastchange.EventedValueString;
import org.fourthline.cling.support.lastchange.EventedValueURI;
import org.fourthline.cling.support.lastchange.EventedValueUnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.PlayMode;
import org.fourthline.cling.support.model.RecordQualityMode;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportAction;
/* loaded from: classes.dex */
public class AVTransportVariable {
    public static Set<Class<? extends EventedValue>> ALL = new HashSet<Class<? extends EventedValue>>() { // from class: org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.1
        {
            add(TransportState.class);
            add(TransportStatus.class);
            add(RecordStorageMedium.class);
            add(PossibleRecordStorageMedia.class);
            add(PossiblePlaybackStorageMedia.class);
            add(CurrentPlayMode.class);
            add(TransportPlaySpeed.class);
            add(RecordMediumWriteStatus.class);
            add(CurrentRecordQualityMode.class);
            add(PossibleRecordQualityModes.class);
            add(NumberOfTracks.class);
            add(CurrentTrack.class);
            add(CurrentTrackDuration.class);
            add(CurrentMediaDuration.class);
            add(CurrentTrackMetaData.class);
            add(CurrentTrackURI.class);
            add(AVTransportURI.class);
            add(NextAVTransportURI.class);
            add(AVTransportURIMetaData.class);
            add(NextAVTransportURIMetaData.class);
            add(CurrentTransportActions.class);
            add(RelativeTimePosition.class);
            add(AbsoluteTimePosition.class);
            add(RelativeCounterPosition.class);
            add(AbsoluteCounterPosition.class);
        }
    };

    /* loaded from: classes.dex */
    public static class TransportState extends EventedValueEnum<org.fourthline.cling.support.model.TransportState> {
        public TransportState(org.fourthline.cling.support.model.TransportState avTransportState) {
            super(avTransportState);
        }

        public TransportState(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.fourthline.cling.support.lastchange.EventedValueEnum
        public org.fourthline.cling.support.model.TransportState enumValueOf(String s) {
            return org.fourthline.cling.support.model.TransportState.valueOf(s);
        }
    }

    /* loaded from: classes.dex */
    public static class TransportStatus extends EventedValueEnum<org.fourthline.cling.support.model.TransportStatus> {
        public TransportStatus(org.fourthline.cling.support.model.TransportStatus transportStatus) {
            super(transportStatus);
        }

        public TransportStatus(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.fourthline.cling.support.lastchange.EventedValueEnum
        public org.fourthline.cling.support.model.TransportStatus enumValueOf(String s) {
            return org.fourthline.cling.support.model.TransportStatus.valueOf(s);
        }
    }

    /* loaded from: classes.dex */
    public static class RecordStorageMedium extends EventedValueEnum<StorageMedium> {
        public RecordStorageMedium(StorageMedium storageMedium) {
            super(storageMedium);
        }

        public RecordStorageMedium(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.fourthline.cling.support.lastchange.EventedValueEnum
        public StorageMedium enumValueOf(String s) {
            return StorageMedium.valueOf(s);
        }
    }

    /* loaded from: classes.dex */
    public static class PossibleRecordStorageMedia extends EventedValueEnumArray<StorageMedium> {
        public PossibleRecordStorageMedia(StorageMedium[] e) {
            super(e);
        }

        public PossibleRecordStorageMedia(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.fourthline.cling.support.lastchange.EventedValueEnumArray
        public StorageMedium[] enumValueOf(String[] names) {
            List<StorageMedium> list = new ArrayList<>();
            for (String s : names) {
                list.add(StorageMedium.valueOf(s));
            }
            return (StorageMedium[]) list.toArray(new StorageMedium[list.size()]);
        }
    }

    /* loaded from: classes.dex */
    public static class PossiblePlaybackStorageMedia extends PossibleRecordStorageMedia {
        public PossiblePlaybackStorageMedia(StorageMedium[] e) {
            super(e);
        }

        public PossiblePlaybackStorageMedia(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    /* loaded from: classes.dex */
    public static class CurrentPlayMode extends EventedValueEnum<PlayMode> {
        public CurrentPlayMode(PlayMode playMode) {
            super(playMode);
        }

        public CurrentPlayMode(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.fourthline.cling.support.lastchange.EventedValueEnum
        public PlayMode enumValueOf(String s) {
            return PlayMode.valueOf(s);
        }
    }

    /* loaded from: classes.dex */
    public static class TransportPlaySpeed extends EventedValueString {
        static final Pattern pattern = Pattern.compile("^-?\\d+(/\\d+)?$", 2);

        public TransportPlaySpeed(String value) {
            super(value);
            if (!pattern.matcher(value).matches()) {
                throw new InvalidValueException("Can't parse TransportPlaySpeed speeds.");
            }
        }

        public TransportPlaySpeed(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    /* loaded from: classes.dex */
    public static class RecordMediumWriteStatus extends EventedValueEnum<org.fourthline.cling.support.model.RecordMediumWriteStatus> {
        public RecordMediumWriteStatus(org.fourthline.cling.support.model.RecordMediumWriteStatus recordMediumWriteStatus) {
            super(recordMediumWriteStatus);
        }

        public RecordMediumWriteStatus(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.fourthline.cling.support.lastchange.EventedValueEnum
        public org.fourthline.cling.support.model.RecordMediumWriteStatus enumValueOf(String s) {
            return org.fourthline.cling.support.model.RecordMediumWriteStatus.valueOf(s);
        }
    }

    /* loaded from: classes.dex */
    public static class CurrentRecordQualityMode extends EventedValueEnum<RecordQualityMode> {
        public CurrentRecordQualityMode(RecordQualityMode recordQualityMode) {
            super(recordQualityMode);
        }

        public CurrentRecordQualityMode(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.fourthline.cling.support.lastchange.EventedValueEnum
        public RecordQualityMode enumValueOf(String s) {
            return RecordQualityMode.valueOf(s);
        }
    }

    /* loaded from: classes.dex */
    public static class PossibleRecordQualityModes extends EventedValueEnumArray<RecordQualityMode> {
        public PossibleRecordQualityModes(RecordQualityMode[] e) {
            super(e);
        }

        public PossibleRecordQualityModes(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.fourthline.cling.support.lastchange.EventedValueEnumArray
        public RecordQualityMode[] enumValueOf(String[] names) {
            List<RecordQualityMode> list = new ArrayList<>();
            for (String s : names) {
                list.add(RecordQualityMode.valueOf(s));
            }
            return (RecordQualityMode[]) list.toArray(new RecordQualityMode[list.size()]);
        }
    }

    /* loaded from: classes.dex */
    public static class NumberOfTracks extends EventedValueUnsignedIntegerFourBytes {
        public NumberOfTracks(UnsignedIntegerFourBytes value) {
            super(value);
        }

        public NumberOfTracks(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    /* loaded from: classes.dex */
    public static class CurrentTrack extends EventedValueUnsignedIntegerFourBytes {
        public CurrentTrack(UnsignedIntegerFourBytes value) {
            super(value);
        }

        public CurrentTrack(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    /* loaded from: classes.dex */
    public static class CurrentTrackDuration extends EventedValueString {
        public CurrentTrackDuration(String value) {
            super(value);
        }

        public CurrentTrackDuration(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    /* loaded from: classes.dex */
    public static class CurrentMediaDuration extends EventedValueString {
        public CurrentMediaDuration(String value) {
            super(value);
        }

        public CurrentMediaDuration(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    /* loaded from: classes.dex */
    public static class CurrentTrackMetaData extends EventedValueString {
        public CurrentTrackMetaData(String value) {
            super(value);
        }

        public CurrentTrackMetaData(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    /* loaded from: classes.dex */
    public static class CurrentTrackURI extends EventedValueURI {
        public CurrentTrackURI(URI value) {
            super(value);
        }

        public CurrentTrackURI(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    /* loaded from: classes.dex */
    public static class AVTransportURI extends EventedValueURI {
        public AVTransportURI(URI value) {
            super(value);
        }

        public AVTransportURI(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    /* loaded from: classes.dex */
    public static class NextAVTransportURI extends EventedValueURI {
        public NextAVTransportURI(URI value) {
            super(value);
        }

        public NextAVTransportURI(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    /* loaded from: classes.dex */
    public static class AVTransportURIMetaData extends EventedValueString {
        public AVTransportURIMetaData(String value) {
            super(value);
        }

        public AVTransportURIMetaData(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    /* loaded from: classes.dex */
    public static class NextAVTransportURIMetaData extends EventedValueString {
        public NextAVTransportURIMetaData(String value) {
            super(value);
        }

        public NextAVTransportURIMetaData(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    /* loaded from: classes.dex */
    public static class CurrentTransportActions extends EventedValueEnumArray<TransportAction> {
        public CurrentTransportActions(TransportAction[] e) {
            super(e);
        }

        public CurrentTransportActions(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.fourthline.cling.support.lastchange.EventedValueEnumArray
        public TransportAction[] enumValueOf(String[] names) {
            if (names == null) {
                return new TransportAction[0];
            }
            List<TransportAction> list = new ArrayList<>();
            for (String s : names) {
                list.add(TransportAction.valueOf(s));
            }
            return (TransportAction[]) list.toArray(new TransportAction[list.size()]);
        }
    }

    /* loaded from: classes.dex */
    public static class RelativeTimePosition extends EventedValueString {
        public RelativeTimePosition(String value) {
            super(value);
        }

        public RelativeTimePosition(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    /* loaded from: classes.dex */
    public static class AbsoluteTimePosition extends EventedValueString {
        public AbsoluteTimePosition(String value) {
            super(value);
        }

        public AbsoluteTimePosition(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    /* loaded from: classes.dex */
    public static class RelativeCounterPosition extends EventedValueString {
        public RelativeCounterPosition(String value) {
            super(value);
        }

        public RelativeCounterPosition(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }

    /* loaded from: classes.dex */
    public static class AbsoluteCounterPosition extends EventedValueString {
        public AbsoluteCounterPosition(String value) {
            super(value);
        }

        public AbsoluteCounterPosition(Map.Entry<String, String>[] attributes) {
            super(attributes);
        }
    }
}
