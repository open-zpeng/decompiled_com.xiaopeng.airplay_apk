package org.fourthline.cling.support.renderingcontrol;

import java.beans.PropertyChangeSupport;
import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.binding.annotations.UpnpOutputArgument;
import org.fourthline.cling.binding.annotations.UpnpService;
import org.fourthline.cling.binding.annotations.UpnpServiceId;
import org.fourthline.cling.binding.annotations.UpnpServiceType;
import org.fourthline.cling.binding.annotations.UpnpStateVariable;
import org.fourthline.cling.binding.annotations.UpnpStateVariables;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.lastchange.LastChangeDelegator;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.model.PresetName;
import org.fourthline.cling.support.model.VolumeDBRange;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelLoudness;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelMute;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolumeDB;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable;
@UpnpService(serviceId = @UpnpServiceId("RenderingControl"), serviceType = @UpnpServiceType(value = "RenderingControl", version = 1), stringConvertibleTypes = {LastChange.class})
@UpnpStateVariables({@UpnpStateVariable(datatype = "string", name = "PresetNameList", sendEvents = false), @UpnpStateVariable(datatype = "boolean", name = "Mute", sendEvents = false), @UpnpStateVariable(allowedValueMaximum = 100, allowedValueMinimum = 0, datatype = "ui2", name = "Volume", sendEvents = false), @UpnpStateVariable(allowedValueMaximum = 100, allowedValueMinimum = 0, datatype = "ui2", name = "Brightness", sendEvents = false), @UpnpStateVariable(allowedValueMaximum = 32767, allowedValueMinimum = -36864, datatype = "i2", name = "VolumeDB", sendEvents = false), @UpnpStateVariable(datatype = "boolean", name = "Loudness", sendEvents = false), @UpnpStateVariable(allowedValuesEnum = Channel.class, name = "A_ARG_TYPE_Channel", sendEvents = false), @UpnpStateVariable(allowedValuesEnum = PresetName.class, name = "A_ARG_TYPE_PresetName", sendEvents = false), @UpnpStateVariable(datatype = "ui4", name = "A_ARG_TYPE_InstanceID", sendEvents = false)})
/* loaded from: classes.dex */
public abstract class AbstractAudioRenderingControl implements LastChangeDelegator {
    @UpnpStateVariable(eventMaximumRateMilliseconds = 200)
    private final LastChange lastChange;
    protected final PropertyChangeSupport propertyChangeSupport;

    @UpnpAction(out = {@UpnpOutputArgument(name = "CurrentBrightness", stateVariable = "Brightness")})
    public abstract UnsignedIntegerTwoBytes getBrightness(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws RenderingControlException;

    protected abstract Channel[] getCurrentChannels();

    @UpnpAction(out = {@UpnpOutputArgument(name = "CurrentMute", stateVariable = "Mute")})
    public abstract boolean getMute(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes, @UpnpInputArgument(name = "Channel") String str) throws RenderingControlException;

    @UpnpAction(out = {@UpnpOutputArgument(name = "CurrentVolume", stateVariable = "Volume")})
    public abstract UnsignedIntegerTwoBytes getVolume(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes, @UpnpInputArgument(name = "Channel") String str) throws RenderingControlException;

    @UpnpAction
    public abstract void setBrightness(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes, @UpnpInputArgument(name = "DesiredBrightness", stateVariable = "Brightness") UnsignedIntegerTwoBytes unsignedIntegerTwoBytes) throws RenderingControlException;

    @UpnpAction
    public abstract void setMute(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes, @UpnpInputArgument(name = "Channel") String str, @UpnpInputArgument(name = "DesiredMute", stateVariable = "Mute") boolean z) throws RenderingControlException;

    @UpnpAction
    public abstract void setVolume(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes unsignedIntegerFourBytes, @UpnpInputArgument(name = "Channel") String str, @UpnpInputArgument(name = "DesiredVolume", stateVariable = "Volume") UnsignedIntegerTwoBytes unsignedIntegerTwoBytes) throws RenderingControlException;

    protected AbstractAudioRenderingControl() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.lastChange = new LastChange(new RenderingControlLastChangeParser());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractAudioRenderingControl(LastChange lastChange) {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.lastChange = lastChange;
    }

    protected AbstractAudioRenderingControl(PropertyChangeSupport propertyChangeSupport) {
        this.propertyChangeSupport = propertyChangeSupport;
        this.lastChange = new LastChange(new RenderingControlLastChangeParser());
    }

    protected AbstractAudioRenderingControl(PropertyChangeSupport propertyChangeSupport, LastChange lastChange) {
        this.propertyChangeSupport = propertyChangeSupport;
        this.lastChange = lastChange;
    }

    @Override // org.fourthline.cling.support.lastchange.LastChangeDelegator
    public LastChange getLastChange() {
        return this.lastChange;
    }

    @Override // org.fourthline.cling.support.lastchange.LastChangeDelegator
    public void appendCurrentState(LastChange lc, UnsignedIntegerFourBytes instanceId) throws Exception {
        Channel[] currentChannels;
        for (Channel channel : getCurrentChannels()) {
            String channelString = channel.name();
            lc.setEventedValue(instanceId, new RenderingControlVariable.Mute(new ChannelMute(channel, Boolean.valueOf(getMute(instanceId, channelString)))), new RenderingControlVariable.Loudness(new ChannelLoudness(channel, Boolean.valueOf(getLoudness(instanceId, channelString)))), new RenderingControlVariable.Volume(new ChannelVolume(channel, Integer.valueOf(getVolume(instanceId, channelString).getValue().intValue()))), new RenderingControlVariable.VolumeDB(new ChannelVolumeDB(channel, getVolumeDB(instanceId, channelString))), new RenderingControlVariable.PresetNameList(PresetName.FactoryDefaults.name()), new RenderingControlVariable.Brightness(getBrightness(instanceId)));
        }
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return this.propertyChangeSupport;
    }

    public static UnsignedIntegerFourBytes getDefaultInstanceID() {
        return new UnsignedIntegerFourBytes(0L);
    }

    @UpnpAction(out = {@UpnpOutputArgument(name = "CurrentPresetNameList", stateVariable = "PresetNameList")})
    public String listPresets(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId) throws RenderingControlException {
        return PresetName.FactoryDefaults.toString();
    }

    @UpnpAction
    public void selectPreset(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId, @UpnpInputArgument(name = "PresetName") String presetName) throws RenderingControlException {
    }

    @UpnpAction(out = {@UpnpOutputArgument(name = "CurrentVolume", stateVariable = "VolumeDB")})
    public Integer getVolumeDB(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId, @UpnpInputArgument(name = "Channel") String channelName) throws RenderingControlException {
        return 0;
    }

    @UpnpAction
    public void setVolumeDB(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId, @UpnpInputArgument(name = "Channel") String channelName, @UpnpInputArgument(name = "DesiredVolume", stateVariable = "VolumeDB") Integer desiredVolumeDB) throws RenderingControlException {
    }

    @UpnpAction(out = {@UpnpOutputArgument(getterName = "getMinValue", name = "MinValue", stateVariable = "VolumeDB"), @UpnpOutputArgument(getterName = "getMaxValue", name = "MaxValue", stateVariable = "VolumeDB")})
    public VolumeDBRange getVolumeDBRange(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId, @UpnpInputArgument(name = "Channel") String channelName) throws RenderingControlException {
        return new VolumeDBRange(0, 0);
    }

    @UpnpAction(out = {@UpnpOutputArgument(name = "CurrentLoudness", stateVariable = "Loudness")})
    public boolean getLoudness(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId, @UpnpInputArgument(name = "Channel") String channelName) throws RenderingControlException {
        return false;
    }

    @UpnpAction
    public void setLoudness(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId, @UpnpInputArgument(name = "Channel") String channelName, @UpnpInputArgument(name = "DesiredLoudness", stateVariable = "Loudness") boolean desiredLoudness) throws RenderingControlException {
    }

    protected Channel getChannel(String channelName) throws RenderingControlException {
        try {
            return Channel.valueOf(channelName);
        } catch (IllegalArgumentException e) {
            ErrorCode errorCode = ErrorCode.ARGUMENT_VALUE_INVALID;
            throw new RenderingControlException(errorCode, "Unsupported audio channel: " + channelName);
        }
    }
}
