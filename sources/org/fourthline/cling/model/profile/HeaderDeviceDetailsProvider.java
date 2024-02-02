package org.fourthline.cling.model.profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.fourthline.cling.model.meta.DeviceDetails;
/* loaded from: classes.dex */
public class HeaderDeviceDetailsProvider implements DeviceDetailsProvider {
    private final DeviceDetails defaultDeviceDetails;
    private final Map<Key, DeviceDetails> headerDetails;

    /* loaded from: classes.dex */
    public static class Key {
        final String headerName;
        final Pattern pattern;
        final String valuePattern;

        public Key(String headerName, String valuePattern) {
            this.headerName = headerName;
            this.valuePattern = valuePattern;
            this.pattern = Pattern.compile(valuePattern, 2);
        }

        public String getHeaderName() {
            return this.headerName;
        }

        public String getValuePattern() {
            return this.valuePattern;
        }

        public boolean isValuePatternMatch(String value) {
            return this.pattern.matcher(value).matches();
        }
    }

    public HeaderDeviceDetailsProvider(DeviceDetails defaultDeviceDetails) {
        this(defaultDeviceDetails, null);
    }

    public HeaderDeviceDetailsProvider(DeviceDetails defaultDeviceDetails, Map<Key, DeviceDetails> headerDetails) {
        this.defaultDeviceDetails = defaultDeviceDetails;
        this.headerDetails = headerDetails != null ? headerDetails : new HashMap<>();
    }

    public DeviceDetails getDefaultDeviceDetails() {
        return this.defaultDeviceDetails;
    }

    public Map<Key, DeviceDetails> getHeaderDetails() {
        return this.headerDetails;
    }

    @Override // org.fourthline.cling.model.profile.DeviceDetailsProvider
    public DeviceDetails provide(RemoteClientInfo info) {
        if (info == null || info.getRequestHeaders().isEmpty()) {
            return getDefaultDeviceDetails();
        }
        for (Key key : getHeaderDetails().keySet()) {
            List<String> headerValues = info.getRequestHeaders().get((Object) key.getHeaderName());
            if (headerValues != null) {
                for (String headerValue : headerValues) {
                    if (key.isValuePatternMatch(headerValue)) {
                        return getHeaderDetails().get(key);
                    }
                }
                continue;
            }
        }
        return getDefaultDeviceDetails();
    }
}
