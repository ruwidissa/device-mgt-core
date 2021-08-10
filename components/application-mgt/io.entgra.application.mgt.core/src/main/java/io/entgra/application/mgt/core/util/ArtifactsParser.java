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

package io.entgra.application.mgt.core.util;

import com.dd.plist.BinaryPropertyListParser;
import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.io.IOUtils;
import io.entgra.application.mgt.core.exception.ParsingException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ArtifactsParser {

    //ios CF Bundle keys
    public static final String IPA_BUNDLE_VERSION_KEY = "CFBundleVersion";
    public static final String IPA_BUNDLE_NAME_KEY = "CFBundleName";
    public static final String IPA_BUNDLE_IDENTIFIER_KEY = "CFBundleIdentifier";

    private static final Log log = LogFactory.getLog(ArtifactsParser.class);

    public static ApkMeta readAndroidManifestFile(InputStream inputStream) throws ParsingException {
        File tempFile = null;
        ApkMeta apkMeta;
        try {
            tempFile = File.createTempFile("temp" + UUID.randomUUID(), ".apk");
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                IOUtils.copy(inputStream, out);
                try (ApkFile apkFile = new ApkFile(tempFile)) {
                    apkMeta = apkFile.getApkMeta();
                }
            }
        } catch (IOException e) {
            throw new ParsingException("Error while parsing the apk.", e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.delete(tempFile.toPath());
                } catch (IOException e) {
                    log.error("Error occured while deleting the temp file", e);
                }
            }
        }
        return apkMeta;
    }

    public static NSDictionary readiOSManifestFile(InputStream inputStream) throws ParsingException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        NSDictionary rootDict;
        File tempFile = null;
        try {
            tempFile = File.createTempFile("temp" + UUID.randomUUID(), ".ipa");
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                IOUtils.copy(inputStream, out);
                try (ZipInputStream stream = new ZipInputStream(new FileInputStream(tempFile))) {
                    ZipEntry entry;
                    while ((entry = stream.getNextEntry()) != null) {
                        if (entry.getName().matches("^(Payload/)(.)+(.app/Info.plist)$")) {
                            InputStream is = stream;
                            int nRead;
                            byte[] data = new byte[16384];

                            while ((nRead = is.read(data, 0, data.length)) != -1) {
                                buffer.write(data, 0, nRead);
                            }
                            buffer.flush();
                            break;
                        }
                    }
                    try {
                        rootDict = (NSDictionary) BinaryPropertyListParser.parse(buffer.toByteArray());
                    } catch (IllegalArgumentException e) {
                        log.debug("Uploaded file didn't have a Binary Plist");
                        try {
                            rootDict = (NSDictionary) PropertyListParser.parse(buffer.toByteArray());
                        } catch (Exception e1) {
                            throw new ParsingException("Error while parsing the non binary plist.", e1);
                        }
                    }
                }
            }
        } catch (PropertyListFormatException e1) {
            throw new ParsingException("Error while parsing the plist.", e1);
        } catch (FileNotFoundException e) {
            throw new ParsingException("Error while creating temporary file.", e);
        } catch (IOException e) {
            throw new ParsingException("Error while parsing the file.", e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.delete(tempFile.toPath());
                } catch (IOException e) {
                    log.error("Error occured while deleting the temp file", e);
                }
            }
        }
        return rootDict;
    }
}
