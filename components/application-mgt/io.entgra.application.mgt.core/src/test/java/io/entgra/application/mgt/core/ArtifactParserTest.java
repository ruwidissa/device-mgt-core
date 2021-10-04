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

import com.dd.plist.NSDictionary;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.testng.Assert;
import org.testng.annotations.Test;
import io.entgra.application.mgt.core.exception.ParsingException;
import io.entgra.application.mgt.core.util.ArtifactsParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ArtifactParserTest {
    private static final String APK_FILE = "src/test/resources/util/app-debug.apk";
    private static final String APK_FILE_INVALID = "src/test/resources/util/app-debug1.apk";
    private static final String IPA_FILE = "src/test/resources/util/iOSMDMAgent.ipa";
    private static final String IPA_FILE_INVALID = "src/test/resources/util/iOSMDMAgent1.ipa";

    @Test
    public void testReadAndroidManifestFile() throws FileNotFoundException, ParsingException {
        InputStream apk = new FileInputStream(APK_FILE);
        ApkMeta apkMeta = ArtifactsParser.readAndroidManifestFile(apk);
        Assert.assertEquals(apkMeta.getVersionName(), "1.0", "APK version name extraction failed.");
        Assert.assertEquals(apkMeta.getPackageName(), "com.games.inosh.myapplication",
                "APK package name extraction failed.");
    }

    @Test(expectedExceptions = java.io.FileNotFoundException.class)
    public void testReadAndroidManifestInvalidFile() throws FileNotFoundException, ParsingException {
        InputStream apk = new FileInputStream(APK_FILE_INVALID);
        ArtifactsParser.readAndroidManifestFile(apk);
    }

    @Test
    public void testReadiOSManifestFile() throws FileNotFoundException, ParsingException {
        InputStream ipa = new FileInputStream(IPA_FILE);
        NSDictionary ipaDictionary = ArtifactsParser.readiOSManifestFile(ipa);
        Assert.assertEquals(ipaDictionary.objectForKey(ArtifactsParser.IPA_BUNDLE_IDENTIFIER_KEY).toString(),
                "org.wso2.carbon.emm.ios.agent.inosh", "IPA bundle ID extraction failed.");
        Assert.assertEquals(ipaDictionary.objectForKey(ArtifactsParser.IPA_BUNDLE_VERSION_KEY).toString(),
                "GA", "IPA file version name extraction failed.");
    }

    @Test(expectedExceptions = java.io.FileNotFoundException.class)
    public void testReadiOSManifestInvalidFile() throws FileNotFoundException, ParsingException {
        InputStream ipa = new FileInputStream(IPA_FILE_INVALID);
        ArtifactsParser.readiOSManifestFile(ipa);
    }
}
