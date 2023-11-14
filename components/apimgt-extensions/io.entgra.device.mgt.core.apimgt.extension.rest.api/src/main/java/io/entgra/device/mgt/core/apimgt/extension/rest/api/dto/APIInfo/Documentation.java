/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo;

import java.util.Date;

public class Documentation {

    private static final long serialVersionUID = 1L;
    private String id;
    private String documentId;
    private String name;
    private DocumentationType type;
    private String summary;
    private DocumentSourceType sourceType;
    private String sourceUrl;
    private String fileName;
    private String filePath;
    private String inlineContent;
    private String otherTypeName;
    private DocumentVisibility visibility;
    private String createdTime;
    private Date createdDate;
    private String createdBy;
    private Date lastUpdatedTime;
    private String lastUpdatedBy;

    public String getOtherTypeName() {
        return this.otherTypeName;
    }

    public void setOtherTypeName(String otherTypeName) {
        this.otherTypeName = otherTypeName;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSourceUrl() {
        return this.sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Documentation(DocumentationType type, String name) {
        this.type = type;
        this.name = name;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            Documentation that = (Documentation)o;
            return this.name.equals(that.name) && this.type == that.type;
        } else {
            return false;
        }
    }

    public DocumentationType getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public DocumentVisibility getVisibility() {
        return this.visibility;
    }

    public void setVisibility(DocumentVisibility visibility) {
        this.visibility = visibility;
    }

    public DocumentSourceType getSourceType() {
        return this.sourceType;
    }

    public void setSourceType(DocumentSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public int hashCode() {
        int result = this.type.hashCode();
        result = 31 * result + this.name.hashCode();
        return result;
    }

    public Date getLastUpdatedTime() {
        return this.lastUpdatedTime;
    }

    public void setLastUpdatedTime(Date lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Date getCreatedDate() {
        return this.createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getInlineContent() {
        return inlineContent;
    }

    public void setInlineContent(String inlineContent) {
        this.inlineContent = inlineContent;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public static enum DocumentVisibility {
        OWNER_ONLY("owner_only"),
        PRIVATE("private"),
        API_LEVEL("api_level");

        private String visibility;

        private DocumentVisibility(String visibility) {
            this.visibility = visibility;
        }
    }

    public static enum DocumentSourceType {
        INLINE("In line"),
        MARKDOWN("Markdown"),
        URL("URL"),
        FILE("File");

        private String type;

        private DocumentSourceType(String type) {
            this.type = type;
        }
    }

    public static enum DocumentationType {
        HOWTO("How To"),
        SAMPLES("Samples"),
        PUBLIC_FORUM("Public Forum"),
        SUPPORT_FORUM("Support Forum"),
        API_MESSAGE_FORMAT("API Message Format"),
        SWAGGER_DOC("Swagger API Definition"),
        OTHER("Other");

        private String type;

        private DocumentationType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
