package com.redhat.operators;

public class VisitorStatus {
    private String backendImage;
    
    private String frontendImage;
    
    public VisitorStatus() {
    }
    
    public VisitorStatus(String backendImage, String frontendImage) {
        this.backendImage = backendImage;
        this.frontendImage = frontendImage;
    }
    public String getBackendImage() {
        return backendImage;
    }
    public void setBackendImage(String backendImage) {
        this.backendImage = backendImage;
    }
    public String getFrontendImage() {
        return frontendImage;
    }
    public void setFrontendImage(String frontendImage) {
        this.frontendImage = frontendImage;
    }
}
