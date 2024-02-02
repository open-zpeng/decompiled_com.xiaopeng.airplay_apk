package org.fourthline.cling.model.meta;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.fourthline.cling.model.Validatable;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.types.DLNACaps;
import org.fourthline.cling.model.types.DLNADoc;
/* loaded from: classes.dex */
public class DeviceDetails implements Validatable {
    private static final Logger log = Logger.getLogger(DeviceDetails.class.getName());
    private final URL baseURL;
    private final DLNACaps dlnaCaps;
    private final DLNADoc[] dlnaDocs;
    private final String friendlyName;
    private final ManufacturerDetails manufacturerDetails;
    private final ModelDetails modelDetails;
    private final URI presentationURI;
    private final DLNACaps secProductCaps;
    private final String serialNumber;
    private final String upc;

    public DeviceDetails(String friendlyName) {
        this((URL) null, friendlyName, (ManufacturerDetails) null, (ModelDetails) null, (String) null, (String) null, (URI) null);
    }

    public DeviceDetails(String friendlyName, DLNADoc[] dlnaDocs, DLNACaps dlnaCaps) {
        this(null, friendlyName, null, null, null, null, null, dlnaDocs, dlnaCaps);
    }

    public DeviceDetails(String friendlyName, ManufacturerDetails manufacturerDetails) {
        this((URL) null, friendlyName, manufacturerDetails, (ModelDetails) null, (String) null, (String) null, (URI) null);
    }

    public DeviceDetails(String friendlyName, ManufacturerDetails manufacturerDetails, DLNADoc[] dlnaDocs, DLNACaps dlnaCaps) {
        this(null, friendlyName, manufacturerDetails, null, null, null, null, dlnaDocs, dlnaCaps);
    }

    public DeviceDetails(String friendlyName, ManufacturerDetails manufacturerDetails, ModelDetails modelDetails) {
        this((URL) null, friendlyName, manufacturerDetails, modelDetails, (String) null, (String) null, (URI) null);
    }

    public DeviceDetails(String friendlyName, ManufacturerDetails manufacturerDetails, ModelDetails modelDetails, DLNADoc[] dlnaDocs, DLNACaps dlnaCaps) {
        this(null, friendlyName, manufacturerDetails, modelDetails, null, null, null, dlnaDocs, dlnaCaps);
    }

    public DeviceDetails(String friendlyName, ManufacturerDetails manufacturerDetails, ModelDetails modelDetails, DLNADoc[] dlnaDocs, DLNACaps dlnaCaps, DLNACaps secProductCaps) {
        this(null, friendlyName, manufacturerDetails, modelDetails, null, null, null, dlnaDocs, dlnaCaps, secProductCaps);
    }

    public DeviceDetails(String friendlyName, ManufacturerDetails manufacturerDetails, ModelDetails modelDetails, String serialNumber, String upc) {
        this((URL) null, friendlyName, manufacturerDetails, modelDetails, serialNumber, upc, (URI) null);
    }

    public DeviceDetails(String friendlyName, ManufacturerDetails manufacturerDetails, ModelDetails modelDetails, String serialNumber, String upc, DLNADoc[] dlnaDocs, DLNACaps dlnaCaps) {
        this(null, friendlyName, manufacturerDetails, modelDetails, serialNumber, upc, null, dlnaDocs, dlnaCaps);
    }

    public DeviceDetails(String friendlyName, URI presentationURI) {
        this((URL) null, friendlyName, (ManufacturerDetails) null, (ModelDetails) null, (String) null, (String) null, presentationURI);
    }

    public DeviceDetails(String friendlyName, URI presentationURI, DLNADoc[] dlnaDocs, DLNACaps dlnaCaps) {
        this(null, friendlyName, null, null, null, null, presentationURI, dlnaDocs, dlnaCaps);
    }

    public DeviceDetails(String friendlyName, ManufacturerDetails manufacturerDetails, ModelDetails modelDetails, URI presentationURI) {
        this((URL) null, friendlyName, manufacturerDetails, modelDetails, (String) null, (String) null, presentationURI);
    }

    public DeviceDetails(String friendlyName, ManufacturerDetails manufacturerDetails, ModelDetails modelDetails, URI presentationURI, DLNADoc[] dlnaDocs, DLNACaps dlnaCaps) {
        this(null, friendlyName, manufacturerDetails, modelDetails, null, null, presentationURI, dlnaDocs, dlnaCaps);
    }

    public DeviceDetails(String friendlyName, ManufacturerDetails manufacturerDetails, ModelDetails modelDetails, String serialNumber, String upc, URI presentationURI) {
        this((URL) null, friendlyName, manufacturerDetails, modelDetails, serialNumber, upc, presentationURI);
    }

    public DeviceDetails(String friendlyName, ManufacturerDetails manufacturerDetails, ModelDetails modelDetails, String serialNumber, String upc, URI presentationURI, DLNADoc[] dlnaDocs, DLNACaps dlnaCaps) {
        this(null, friendlyName, manufacturerDetails, modelDetails, serialNumber, upc, presentationURI, dlnaDocs, dlnaCaps);
    }

    public DeviceDetails(String friendlyName, ManufacturerDetails manufacturerDetails, ModelDetails modelDetails, String serialNumber, String upc, String presentationURI) throws IllegalArgumentException {
        this((URL) null, friendlyName, manufacturerDetails, modelDetails, serialNumber, upc, URI.create(presentationURI));
    }

    public DeviceDetails(String friendlyName, ManufacturerDetails manufacturerDetails, ModelDetails modelDetails, String serialNumber, String upc, String presentationURI, DLNADoc[] dlnaDocs, DLNACaps dlnaCaps) throws IllegalArgumentException {
        this(null, friendlyName, manufacturerDetails, modelDetails, serialNumber, upc, URI.create(presentationURI), dlnaDocs, dlnaCaps);
    }

    public DeviceDetails(URL baseURL, String friendlyName, ManufacturerDetails manufacturerDetails, ModelDetails modelDetails, String serialNumber, String upc, URI presentationURI) {
        this(baseURL, friendlyName, manufacturerDetails, modelDetails, serialNumber, upc, presentationURI, null, null);
    }

    public DeviceDetails(URL baseURL, String friendlyName, ManufacturerDetails manufacturerDetails, ModelDetails modelDetails, String serialNumber, String upc, URI presentationURI, DLNADoc[] dlnaDocs, DLNACaps dlnaCaps) {
        this(baseURL, friendlyName, manufacturerDetails, modelDetails, serialNumber, upc, presentationURI, dlnaDocs, dlnaCaps, null);
    }

    public DeviceDetails(URL baseURL, String friendlyName, ManufacturerDetails manufacturerDetails, ModelDetails modelDetails, String serialNumber, String upc, URI presentationURI, DLNADoc[] dlnaDocs, DLNACaps dlnaCaps, DLNACaps secProductCaps) {
        this.baseURL = baseURL;
        this.friendlyName = friendlyName;
        this.manufacturerDetails = manufacturerDetails == null ? new ManufacturerDetails() : manufacturerDetails;
        this.modelDetails = modelDetails == null ? new ModelDetails() : modelDetails;
        this.serialNumber = serialNumber;
        this.upc = upc;
        this.presentationURI = presentationURI;
        this.dlnaDocs = dlnaDocs != null ? dlnaDocs : new DLNADoc[0];
        this.dlnaCaps = dlnaCaps;
        this.secProductCaps = secProductCaps;
    }

    public URL getBaseURL() {
        return this.baseURL;
    }

    public String getFriendlyName() {
        return this.friendlyName;
    }

    public ManufacturerDetails getManufacturerDetails() {
        return this.manufacturerDetails;
    }

    public ModelDetails getModelDetails() {
        return this.modelDetails;
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }

    public String getUpc() {
        return this.upc;
    }

    public URI getPresentationURI() {
        return this.presentationURI;
    }

    public DLNADoc[] getDlnaDocs() {
        return this.dlnaDocs;
    }

    public DLNACaps getDlnaCaps() {
        return this.dlnaCaps;
    }

    public DLNACaps getSecProductCaps() {
        return this.secProductCaps;
    }

    @Override // org.fourthline.cling.model.Validatable
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();
        if (getUpc() != null) {
            if (getUpc().length() != 12) {
                Logger logger = log;
                logger.fine("UPnP specification violation, UPC must be 12 digits: " + getUpc());
            } else {
                try {
                    Long.parseLong(getUpc());
                } catch (NumberFormatException e) {
                    Logger logger2 = log;
                    logger2.fine("UPnP specification violation, UPC must be 12 digits all-numeric: " + getUpc());
                }
            }
        }
        return errors;
    }
}
