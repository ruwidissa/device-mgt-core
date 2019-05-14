/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.application.mgt.store.api.services.util;

import org.wso2.carbon.device.application.mgt.common.ReviewTmp;

/**
 * Helper class for ReviewTmp Management API test cases.
 */

public class CommentMgtTestHelper {

    private static final String COMMENT_TEXT = "Dummy ReviewTmp";
    private static final String CREATED_BY = "TEST_CREATED_BY";
    private static final String MODIFIED_BY = "TEST_MODIFIED_BY";
    private static final int PARENT_ID = 123;
    private static final int COMMENT_ID = 1;

    /**
     * Creates a ReviewTmp with given text and given uuid.
     * If the text is null, the COMMENT_TEXT will be used as the Dummy ReviewTmp.
     *
     * @param commentText : Text of the ReviewTmp
     * @return ReviewTmp
     */
    public static ReviewTmp getDummyComment(String commentText, String uuid) {
        ReviewTmp reviewTmp = new ReviewTmp();
        reviewTmp.setId(COMMENT_ID);
        reviewTmp.setUsername(CREATED_BY);
        reviewTmp.setComment(commentText != null ? commentText : COMMENT_TEXT);

        return reviewTmp;
    }
}


