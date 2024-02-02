package org.fourthline.cling.support.model;

import java.net.URI;
import org.seamless.util.MimeType;
/* loaded from: classes.dex */
public class Res {
    protected Long bitrate;
    protected Long bitsPerSample;
    protected Long colorDepth;
    protected String duration;
    protected URI importUri;
    protected Long nrAudioChannels;
    protected String protection;
    protected ProtocolInfo protocolInfo;
    protected String resolution;
    protected Long sampleFrequency;
    protected Long size;
    protected String value;

    public Res() {
    }

    public Res(String httpGetMimeType, Long size, String duration, Long bitrate, String value) {
        this(new ProtocolInfo(Protocol.HTTP_GET, "*", httpGetMimeType, "*"), size, duration, bitrate, value);
    }

    public Res(MimeType httpGetMimeType, Long size, String duration, Long bitrate, String value) {
        this(new ProtocolInfo(httpGetMimeType), size, duration, bitrate, value);
    }

    public Res(MimeType httpGetMimeType, Long size, String value) {
        this(new ProtocolInfo(httpGetMimeType), size, value);
    }

    public Res(ProtocolInfo protocolInfo, Long size, String value) {
        this.protocolInfo = protocolInfo;
        this.size = size;
        this.value = value;
    }

    public Res(ProtocolInfo protocolInfo, Long size, String duration, Long bitrate, String value) {
        this.protocolInfo = protocolInfo;
        this.size = size;
        this.duration = duration;
        this.bitrate = bitrate;
        this.value = value;
    }

    public Res(URI importUri, ProtocolInfo protocolInfo, Long size, String duration, Long bitrate, Long sampleFrequency, Long bitsPerSample, Long nrAudioChannels, Long colorDepth, String protection, String resolution, String value) {
        this.importUri = importUri;
        this.protocolInfo = protocolInfo;
        this.size = size;
        this.duration = duration;
        this.bitrate = bitrate;
        this.sampleFrequency = sampleFrequency;
        this.bitsPerSample = bitsPerSample;
        this.nrAudioChannels = nrAudioChannels;
        this.colorDepth = colorDepth;
        this.protection = protection;
        this.resolution = resolution;
        this.value = value;
    }

    public URI getImportUri() {
        return this.importUri;
    }

    public void setImportUri(URI importUri) {
        this.importUri = importUri;
    }

    public ProtocolInfo getProtocolInfo() {
        return this.protocolInfo;
    }

    public void setProtocolInfo(ProtocolInfo protocolInfo) {
        this.protocolInfo = protocolInfo;
    }

    public Long getSize() {
        return this.size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getDuration() {
        return this.duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Long getBitrate() {
        return this.bitrate;
    }

    public void setBitrate(Long bitrate) {
        this.bitrate = bitrate;
    }

    public Long getSampleFrequency() {
        return this.sampleFrequency;
    }

    public void setSampleFrequency(Long sampleFrequency) {
        this.sampleFrequency = sampleFrequency;
    }

    public Long getBitsPerSample() {
        return this.bitsPerSample;
    }

    public void setBitsPerSample(Long bitsPerSample) {
        this.bitsPerSample = bitsPerSample;
    }

    public Long getNrAudioChannels() {
        return this.nrAudioChannels;
    }

    public void setNrAudioChannels(Long nrAudioChannels) {
        this.nrAudioChannels = nrAudioChannels;
    }

    public Long getColorDepth() {
        return this.colorDepth;
    }

    public void setColorDepth(Long colorDepth) {
        this.colorDepth = colorDepth;
    }

    public String getProtection() {
        return this.protection;
    }

    public void setProtection(String protection) {
        this.protection = protection;
    }

    public String getResolution() {
        return this.resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public void setResolution(int x, int y) {
        this.resolution = x + "x" + y;
    }

    public int getResolutionX() {
        if (getResolution() == null || getResolution().split("x").length != 2) {
            return 0;
        }
        return Integer.valueOf(getResolution().split("x")[0]).intValue();
    }

    public int getResolutionY() {
        if (getResolution() != null && getResolution().split("x").length == 2) {
            return Integer.valueOf(getResolution().split("x")[1]).intValue();
        }
        return 0;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
