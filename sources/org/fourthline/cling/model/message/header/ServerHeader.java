package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.ServerClientTokens;
/* loaded from: classes.dex */
public class ServerHeader extends UpnpHeader<ServerClientTokens> {
    public ServerHeader() {
        setValue(new ServerClientTokens());
    }

    public ServerHeader(ServerClientTokens tokens) {
        setValue(tokens);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        String[] osNameVersion;
        String[] productTokens;
        ServerClientTokens serverClientTokens = new ServerClientTokens();
        serverClientTokens.setOsName("UNKNOWN");
        serverClientTokens.setOsVersion("UNKNOWN");
        serverClientTokens.setProductName("UNKNOWN");
        serverClientTokens.setProductVersion("UNKNOWN");
        if (s.contains("UPnP/1.1")) {
            serverClientTokens.setMinorVersion(1);
        } else if (!s.contains("UPnP/1.")) {
            throw new InvalidHeaderException("Missing 'UPnP/1.' in server information: " + s);
        }
        int numberOfSpaces = 0;
        for (int numberOfSpaces2 = 0; numberOfSpaces2 < s.length(); numberOfSpaces2++) {
            try {
                if (s.charAt(numberOfSpaces2) == ' ') {
                    numberOfSpaces++;
                }
            } catch (Exception e) {
                serverClientTokens.setOsName("UNKNOWN");
                serverClientTokens.setOsVersion("UNKNOWN");
                serverClientTokens.setProductName("UNKNOWN");
                serverClientTokens.setProductVersion("UNKNOWN");
            }
        }
        if (s.contains(",")) {
            String[] productTokens2 = s.split(",");
            osNameVersion = productTokens2[0].split("/");
            productTokens = productTokens2[2].split("/");
        } else if (numberOfSpaces > 2) {
            String beforeUpnpToken = s.substring(0, s.indexOf("UPnP/1.")).trim();
            String afterUpnpToken = s.substring(s.indexOf("UPnP/1.") + 8).trim();
            osNameVersion = beforeUpnpToken.split("/");
            productTokens = afterUpnpToken.split("/");
        } else {
            String[] productTokens3 = s.split(" ");
            osNameVersion = productTokens3[0].split("/");
            productTokens = productTokens3[2].split("/");
        }
        serverClientTokens.setOsName(osNameVersion[0].trim());
        if (osNameVersion.length > 1) {
            serverClientTokens.setOsVersion(osNameVersion[1].trim());
        }
        serverClientTokens.setProductName(productTokens[0].trim());
        if (productTokens.length > 1) {
            serverClientTokens.setProductVersion(productTokens[1].trim());
        }
        setValue(serverClientTokens);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue().getHttpToken();
    }
}
