/*
 * Copyright (c) 2023, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
