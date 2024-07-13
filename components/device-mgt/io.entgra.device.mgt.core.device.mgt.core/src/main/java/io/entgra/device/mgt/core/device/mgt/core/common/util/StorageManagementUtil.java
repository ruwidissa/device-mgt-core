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

package io.entgra.device.mgt.core.device.mgt.core.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.common.Base64File;
import io.entgra.device.mgt.core.device.mgt.core.common.exception.StorageManagementException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This is a util class that handles Storage Management related tasks.
 */
public class StorageManagementUtil {

    private static Log log = LogFactory.getLog(StorageManagementUtil.class);

    /**
     * This method is responsible for creating artifact parent directories in the given path.
     *
     * @param artifactDirectoryPath Path for the artifact directory.
     */
    public static void createArtifactDirectory(String artifactDirectoryPath) throws StorageManagementException {
        File artifactDirectory = new File(artifactDirectoryPath);

        if (!artifactDirectory.exists() && !artifactDirectory.mkdirs()) {
                throw new StorageManagementException(
                        "Cannot create directories in the path: " + artifactDirectoryPath);
        }
    }

    /**
     * To delete a directory recursively
     *
     * @param artifactDirectory Artifact Directory that need to be deleted.
     */
    public static void delete(File artifactDirectory) throws IOException {
        File[] contents = artifactDirectory.listFiles();
        if (contents != null) {
            for (File file : contents) {
                delete(file);
            }
        }
        Files.delete(artifactDirectory.toPath());
    }

    public static void copy(String source, String destination) throws IOException {
        File sourceFile = new File(source);
        File destinationFile = new File(destination);
        if (sourceFile.exists() && destinationFile.exists()) {
            Files.copy(sourceFile.toPath(), destinationFile.toPath());
        } else {
            String msg = "Source file " + source + " or destination file " + destination + " doesn't exist";
            log.error(msg);
            throw new IOException(msg);
        }
    }

    /**
     * To save a file in a given location.
     *
     * @param inputStream Stream of the file.
     * @param path        Path the file need to be saved in.
     */
    public static void saveFile(InputStream inputStream, String path) throws IOException {
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(Files.newOutputStream(Paths.get(path)));
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            byte []buffer = new byte[8192];
            int n;
            while ((n = bufferedInputStream.read(buffer)) != -1) {
                bufferedOutputStream.write(buffer, 0, n);
            }
            bufferedOutputStream.flush();
        }
    }

    /**
     * To save a bas64 string of a file in a given location.
     *
     * @param base64File {@link Base64File} of the file.
     */
    public static void saveFile(Base64File base64File, String path) throws IOException {
        InputStream inputStream = FileUtil.base64ToInputStream(base64File.getBase64String());
        saveFile(inputStream, path);
    }

    /***
     * Get the fine input stream
     * @param filePath File path
     * @return {@link InputStream}
     * @throws IOException throws if error occured when reading file or if couldn't find a file in the filePath
     */
    public static InputStream getInputStream (String filePath) throws IOException {
        File sourceFile = new File(filePath);
        if (!sourceFile.exists()){
            return null;
        }
        try {
            return new FileInputStream(sourceFile);
        } catch (FileNotFoundException e) {
            String msg = "Couldn't file the file in file path: " + filePath;
            log.error(msg, e);
            throw new IOException(msg, e);
        }
    }
}
