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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.application.mgt.common.ImageArtifact;
import io.entgra.application.mgt.common.exception.ApplicationStorageManagementException;
import io.entgra.application.mgt.common.exception.ResourceManagementException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * This is a util class that handles Storage Management related tasks.
 */
public class StorageManagementUtil {

    private static Log log = LogFactory.getLog(StorageManagementUtil.class);

    /**
     * This method is responsible for creating artifact parent directories in the given path.
     *
     * @param artifactDirectoryPath Path for the artifact directory.
     * @throws ResourceManagementException Resource Management Exception.
     */
    public static void createArtifactDirectory(String artifactDirectoryPath) throws ResourceManagementException {
        File artifactDirectory = new File(artifactDirectoryPath);

        if (!artifactDirectory.exists() && !artifactDirectory.mkdirs()) {
                throw new ResourceManagementException(
                        "Cannot create directories in the path to save the application related artifacts");
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
        try (OutputStream outStream = new FileOutputStream(new File(path))) {
            byte[] buffer = new byte[inputStream.available()];
            if (inputStream.read(buffer) != -1) {
                outStream.write(buffer);
            }
        } finally {
            inputStream.close();
        }
    }

    /**
     * To create {@link ImageArtifact}.
     *
     * @param imageFile         Image File.
     * @param imageArtifactPath Path of the image artifact file.
     * @return Image Artifact.
     * @throws IOException IO Exception.
     */
    public static ImageArtifact createImageArtifact(File imageFile, String imageArtifactPath) throws IOException {
        ImageArtifact imageArtifact = new ImageArtifact();
        imageArtifact.setName(imageFile.getName());
        imageArtifact.setType(Files.probeContentType(imageFile.toPath()));
        byte[] imageBytes = IOUtils.toByteArray(new FileInputStream(imageArtifactPath));
        imageArtifact.setEncodedImage(Base64.encodeBase64URLSafeString(imageBytes));
        return imageArtifact;
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

    public static String getMD5(InputStream binaryFile) throws ApplicationStorageManagementException {
        String md5;
        try {
            md5 = DigestUtils.md5Hex(binaryFile);
        } catch (IOException e) {
            String msg = "IO Exception occurred while trying to get the md5sum value of application";
            log.error(msg, e);
            throw new ApplicationStorageManagementException(msg, e);
        }
        return md5;
    }
}
