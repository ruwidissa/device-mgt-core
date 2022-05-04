package org.wso2.carbon.device.mgt.common.metadata.mgt;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.wso2.carbon.device.mgt.common.Base64File;

public class WhiteLabelImageRequestPayload {
    private ImageType imageType;
    private JsonElement image;

    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    public JsonElement getImage() {
        return image;
    }

    public Base64File getImageAsBase64File() {
        if (imageType != ImageType.BASE64) {
            throw new IllegalStateException("Cannot convert image with Image type of " + imageType + " to base64.");
        }
        return new Gson().fromJson(image, Base64File.class);
    }

    public String getImageAsUrl() {
        if (imageType != ImageType.URL) {
            throw new IllegalStateException("Cannot convert image with Image type of " + imageType + " to image url string.");
        }
        return new Gson().fromJson(image, String.class);
    }

    public void setImage(JsonElement image) {
        this.image = image;
    }

    public enum ImageType {
        URL,
        BASE64;

        public WhiteLabelImage.ImageLocationType getDTOImageLocationType() {
            if (this == URL) {
                return WhiteLabelImage.ImageLocationType.URL;
            }
            return WhiteLabelImage.ImageLocationType.CUSTOM_FILE;
        }
    }

}
