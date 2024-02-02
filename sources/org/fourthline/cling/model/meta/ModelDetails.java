package org.fourthline.cling.model.meta;

import java.net.URI;
/* loaded from: classes.dex */
public class ModelDetails {
    private String modelDescription;
    private String modelName;
    private String modelNumber;
    private URI modelURI;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ModelDetails() {
    }

    public ModelDetails(String modelName) {
        this.modelName = modelName;
    }

    public ModelDetails(String modelName, String modelDescription) {
        this.modelName = modelName;
        this.modelDescription = modelDescription;
    }

    public ModelDetails(String modelName, String modelDescription, String modelNumber) {
        this.modelName = modelName;
        this.modelDescription = modelDescription;
        this.modelNumber = modelNumber;
    }

    public ModelDetails(String modelName, String modelDescription, String modelNumber, URI modelURI) {
        this.modelName = modelName;
        this.modelDescription = modelDescription;
        this.modelNumber = modelNumber;
        this.modelURI = modelURI;
    }

    public ModelDetails(String modelName, String modelDescription, String modelNumber, String modelURI) throws IllegalArgumentException {
        this.modelName = modelName;
        this.modelDescription = modelDescription;
        this.modelNumber = modelNumber;
        this.modelURI = URI.create(modelURI);
    }

    public String getModelName() {
        return this.modelName;
    }

    public String getModelDescription() {
        return this.modelDescription;
    }

    public String getModelNumber() {
        return this.modelNumber;
    }

    public URI getModelURI() {
        return this.modelURI;
    }
}
