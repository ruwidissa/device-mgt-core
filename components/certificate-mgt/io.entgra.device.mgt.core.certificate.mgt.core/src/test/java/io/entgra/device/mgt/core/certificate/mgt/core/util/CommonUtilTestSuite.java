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

package io.entgra.device.mgt.core.certificate.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;

public class CommonUtilTestSuite {

    private static Log log = LogFactory.getLog(CommonUtilTestSuite.class);
    private final CommonUtil commonUtil = new CommonUtil();

    @Test
    public void testValidityStartDate() {
        Date validityStartDate = commonUtil.getValidityStartDate();

        if(validityStartDate == null) {
            Assert.fail("Validity start date is empty");
        }

        Date todayDate = new Date();
        Assert.assertTrue(validityStartDate.before(todayDate), "Validity start date is valid");
    }

    @Test
    public void testValidityEndDate() {
        Date validityEndDate = commonUtil.getValidityEndDate();

        if(validityEndDate == null) {
            Assert.fail("Validity end date is empty");
        }

        Date todayDate = new Date();
        Assert.assertTrue(validityEndDate.after(todayDate), "Validity end date is valid");
    }
}
