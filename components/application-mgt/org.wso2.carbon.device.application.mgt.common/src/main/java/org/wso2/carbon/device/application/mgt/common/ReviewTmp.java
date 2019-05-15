/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.sql.Timestamp;

@ApiModel(value = "ReviewTmp", description = "ReviewTmp represents the user's review for an application release")
public class ReviewTmp {

    @ApiModelProperty(name = "id",
            value = "The Id given to the comment when it store to the App manager")
    private int id;

    @ApiModelProperty(name = "comment",
            value = "Comment of the review")
    private String comment;

    @ApiModelProperty(name = "parentId",
            value = "Parent id of the review")
    private int parentId;

    @ApiModelProperty(name = "username",
            value = "Username odf the Review creator",
            required = true)
    private String username;

    @ApiModelProperty(name = "createdAt",
            value = "Timestamp fo the review is created")
    private Timestamp createdAt;

    @ApiModelProperty(name = "modifiedAt",
            value = "Timestamp of the review is modified")
    private Timestamp modifiedAt;

    @ApiModelProperty(name = "rating",
            value = "Rating value of the application release")
    private int rating;

    @ApiModelProperty(name = "replyReviewTmp",
            value = "Replying review")
    private ReviewTmp replyReviewTmp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public ReviewTmp getReplyReviewTmp() {
        return replyReviewTmp;
    }

    public void setReplyReviewTmp(ReviewTmp replyReviewTmp) {
        this.replyReviewTmp = replyReviewTmp;
    }
}

