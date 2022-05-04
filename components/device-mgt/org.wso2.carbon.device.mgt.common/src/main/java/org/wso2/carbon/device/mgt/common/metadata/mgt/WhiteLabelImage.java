package org.wso2.carbon.device.mgt.common.metadata.mgt;

public class WhiteLabelImage {
    private ImageLocationType imageLocationType;
    private String imageLocation;

    public ImageLocationType getImageLocationType() {
        return imageLocationType;
    }

    public void setImageLocationType(ImageLocationType imageLocationType) {
        this.imageLocationType = imageLocationType;
    }

    public String getImageLocation() {
        return imageLocation;
    }

    public void setImageLocation(String imageLocation) {
        this.imageLocation = imageLocation;
    }

    public enum ImageName {
        FAVICON,
        LOGO;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public enum ImageLocationType {
        URL,
        CUSTOM_FILE,
        DEFAULT_FILE
    }
}
