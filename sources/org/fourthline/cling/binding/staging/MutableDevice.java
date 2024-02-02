package org.fourthline.cling.binding.staging;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.meta.UDAVersion;
import org.fourthline.cling.model.types.DLNACaps;
import org.fourthline.cling.model.types.DLNADoc;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDN;
/* loaded from: classes.dex */
public class MutableDevice {
    public URL baseURL;
    public String deviceType;
    public DLNACaps dlnaCaps;
    public String friendlyName;
    public String manufacturer;
    public URI manufacturerURI;
    public String modelDescription;
    public String modelName;
    public String modelNumber;
    public URI modelURI;
    public MutableDevice parentDevice;
    public URI presentationURI;
    public String serialNumber;
    public UDN udn;
    public String upc;
    public MutableUDAVersion udaVersion = new MutableUDAVersion();
    public List<DLNADoc> dlnaDocs = new ArrayList();
    public List<MutableIcon> icons = new ArrayList();
    public List<MutableService> services = new ArrayList();
    public List<MutableDevice> embeddedDevices = new ArrayList();

    public Device build(Device prototype) throws ValidationException {
        return build(prototype, createDeviceVersion(), this.baseURL);
    }

    public Device build(Device prototype, UDAVersion deviceVersion, URL baseURL) throws ValidationException {
        ArrayList arrayList = new ArrayList();
        for (MutableDevice embeddedDevice : this.embeddedDevices) {
            arrayList.add(embeddedDevice.build(prototype, deviceVersion, baseURL));
        }
        return prototype.newInstance(this.udn, deviceVersion, createDeviceType(), createDeviceDetails(baseURL), createIcons(), createServices(prototype), arrayList);
    }

    public UDAVersion createDeviceVersion() {
        return new UDAVersion(this.udaVersion.major, this.udaVersion.minor);
    }

    public DeviceType createDeviceType() {
        return DeviceType.valueOf(this.deviceType);
    }

    public DeviceDetails createDeviceDetails(URL baseURL) {
        return new DeviceDetails(baseURL, this.friendlyName, new ManufacturerDetails(this.manufacturer, this.manufacturerURI), new ModelDetails(this.modelName, this.modelDescription, this.modelNumber, this.modelURI), this.serialNumber, this.upc, this.presentationURI, (DLNADoc[]) this.dlnaDocs.toArray(new DLNADoc[this.dlnaDocs.size()]), this.dlnaCaps);
    }

    public Icon[] createIcons() {
        Icon[] iconArray = new Icon[this.icons.size()];
        int i = 0;
        for (MutableIcon icon : this.icons) {
            iconArray[i] = icon.build();
            i++;
        }
        return iconArray;
    }

    public Service[] createServices(Device prototype) throws ValidationException {
        Service[] services = prototype.newServiceArray(this.services.size());
        int i = 0;
        for (MutableService service : this.services) {
            services[i] = service.build(prototype);
            i++;
        }
        return services;
    }
}
