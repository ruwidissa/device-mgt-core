/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
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
package io.entgra.application.mgt.store.api.services;

import io.entgra.application.mgt.common.response.Review;

/**
 * Helper class for Review Management API test cases.
 */

public class CommentMgtTestHelper {

    private static final String COMMENT_TEXT = "Dummy Review";
    private static final String CREATED_BY = "TEST_CREATED_BY";
    private static final String MODIFIED_BY = "TEST_MODIFIED_BY";
    private static final int PARENT_ID = 123;
    private static final int COMMENT_ID = 1;

    /**
     * Creates a Review with given text and given uuid.
     * If the text is null, the COMMENT_TEXT will be used as the Dummy Review.
     *
     * @param commentText : Text of the Review
     * @return Review
     */
    public static Review getDummyComment(String commentText, String uuid) {
        Review reviewTmp = new Review();
        reviewTmp.setId(COMMENT_ID);
        reviewTmp.setUsername(CREATED_BY);
        reviewTmp.setContent(commentText != null ? commentText : COMMENT_TEXT);

        return reviewTmp;
    }
}


