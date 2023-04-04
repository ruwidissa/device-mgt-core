/*
 *  Copyright (c) 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.device.mgt.common;

public class FileResponse {
    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    private byte[] fileContent;
    private String mimeType;

    private String name;

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public enum ImageExtension {
        SVG() {
            @Override
            public String mimeType() {
                return "image/svg+xml";
            }
        },
        PNG,
        JPG,
        JPEG,
        GIF;

        public String mimeType() {
            return DEFAULT_MIME_TYPE;
        }

        public static String mimeTypeOf(String extension) {
            if (extension.isEmpty()) {
                return DEFAULT_MIME_TYPE;
            }
            ImageExtension imageExtension = ImageExtension.valueOf(extension.toUpperCase());
            return imageExtension.mimeType();
        }

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }

    }
}
