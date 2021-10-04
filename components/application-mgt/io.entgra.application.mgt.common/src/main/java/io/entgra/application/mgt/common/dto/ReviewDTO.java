/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.application.mgt.common.dto;

import java.sql.Timestamp;

public class ReviewDTO {
    private int id;
    private String content;
    private String username;
    private Timestamp createdAt;
    private Timestamp modifiedAt;
    private int rating;
    private int rootParentId;
    private int immediateParentId;
    private String releaseUuid;
    private String releaseVersion;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Timestamp modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getRootParentId() { return rootParentId; }

    public void setRootParentId(int rootParentId) { this.rootParentId = rootParentId; }

    public int getImmediateParentId() { return immediateParentId; }

    public void setImmediateParentId(int immediateParentId) { this.immediateParentId = immediateParentId; }

    public String getReleaseUuid() { return releaseUuid; }

    public void setReleaseUuid(String releaseUuid) { this.releaseUuid = releaseUuid; }

    public String getReleaseVersion() { return releaseVersion; }

    public void setReleaseVersion(String releaseVersion) { this.releaseVersion = releaseVersion; }
}
