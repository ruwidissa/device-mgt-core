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

package io.entgra.application.mgt.core;

import org.junit.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import io.entgra.application.mgt.common.exception.ResourceManagementException;
import io.entgra.application.mgt.core.util.StorageManagementUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StorageManagementUtilTest {
    private static final String TEMP_FOLDER = "src/test/resources/util/temp";
    private static final String APK_FILE = "src/test/resources/util/app-debug.apk";
    private static final String APK_FILE_NAME = "/app-debug.apk";

    @BeforeMethod
    public void before() throws IOException {
        File file = new File(TEMP_FOLDER);
        if (file.exists()) {
            StorageManagementUtil.delete(file);
        }
    }

    @Test
    public void testCreateArtifactDirectory() {
        try {
            StorageManagementUtil.createArtifactDirectory(TEMP_FOLDER);
        } catch (ResourceManagementException e) {
            e.printStackTrace();
            Assert.fail("Directory creation failed.");
        }
    }

    @Test
    public void testSaveFile() throws IOException, ResourceManagementException {
        StorageManagementUtil.createArtifactDirectory(TEMP_FOLDER);
        InputStream apk = new FileInputStream(APK_FILE);
        StorageManagementUtil.saveFile(apk, TEMP_FOLDER + APK_FILE_NAME);
        File file = new File(TEMP_FOLDER + APK_FILE_NAME);
        if (!file.exists()) {
            Assert.fail("File saving failed.");
        }
    }

    @AfterMethod
    public void deleteFileTest() throws IOException, ResourceManagementException {
        File file = new File(TEMP_FOLDER);
        StorageManagementUtil.delete(file);
        if (file.exists()) {
            Assert.fail("File deleting failed.");
        }
    }
}
