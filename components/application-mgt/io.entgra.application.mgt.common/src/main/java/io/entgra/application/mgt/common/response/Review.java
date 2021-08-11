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

package io.entgra.application.mgt.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.sql.Timestamp;
import java.util.List;

@ApiModel(value = "Review", description = "Review represents the user's review for an application release")
public class Review {

    @ApiModelProperty(name = "id", value = "Review ID.")
    private int id;

    @ApiModelProperty(name = "content", value = "Review message.")
    private String content;

    @ApiModelProperty(name = "username", value = "Username odf the Review creator")
    private String username;

    @ApiModelProperty(name = "createdAt", value = "Review created timestamp.")
    private Timestamp createdAt;

    @ApiModelProperty(name = "createdAt", value = "Review modified timestamp.")
    private Timestamp modifiedAt;

    @ApiModelProperty(name = "rating", value = "Rating value of the application release")
    private int rating;

    @ApiModelProperty(name = "releaseUuid", value = "UUID of the review associated application")
    private String releaseUuid;

    @ApiModelProperty(name = "releaseVersion", value = "Version of the review associated application")
    private String releaseVersion;

    @ApiModelProperty(name = "replies", value = "Replying reviews")
    private List<Review> replies;

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

    public List<Review> getReplies() { return replies; }

    public void setReplies(List<Review> replies) { this.replies = replies; }

    public String getReleaseUuid() { return releaseUuid; }

    public void setReleaseUuid(String releaseUuid) { this.releaseUuid = releaseUuid; }

    public String getReleaseVersion() { return releaseVersion; }

    public void setReleaseVersion(String releaseVersion) { this.releaseVersion = releaseVersion; }
}
