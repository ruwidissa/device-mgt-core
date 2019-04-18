/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/

package org.wso2.carbon.device.application.mgt.core.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.ImageArtifact;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;

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
    public static void deleteDir(File artifactDirectory) throws IOException {
        File[] contents = artifactDirectory.listFiles();
        if (contents != null) {
            for (File file : contents) {
                deleteDir(file);
            }
        }
        Files.delete(artifactDirectory.toPath());
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
        try (InputStream inputStream = new FileInputStream(filePath)){
            return inputStream;
        } catch (FileNotFoundException e) {
            String msg = "Couldn't file the file in file path: " + filePath;
            log.error(msg);
            throw new IOException(msg);
        }
    }
}
