package org.fourthline.cling.model.meta;

import java.net.URI;
/* loaded from: classes.dex */
public class ManufacturerDetails {
    private String manufacturer;
    private URI manufacturerURI;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ManufacturerDetails() {
    }

    public ManufacturerDetails(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public ManufacturerDetails(URI manufacturerURI) {
        this.manufacturerURI = manufacturerURI;
    }

    public ManufacturerDetails(String manufacturer, URI manufacturerURI) {
        this.manufacturer = manufacturer;
        this.manufacturerURI = manufacturerURI;
    }

    public ManufacturerDetails(String manufacturer, String manufacturerURI) throws IllegalArgumentException {
        this.manufacturer = manufacturer;
        this.manufacturerURI = URI.create(manufacturerURI);
    }

    public String getManufacturer() {
        return this.manufacturer;
    }

    public URI getManufacturerURI() {
        return this.manufacturerURI;
    }
}
