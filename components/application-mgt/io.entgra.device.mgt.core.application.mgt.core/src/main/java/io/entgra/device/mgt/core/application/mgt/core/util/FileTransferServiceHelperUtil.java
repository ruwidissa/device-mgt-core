/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.application.mgt.core.util;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.application.mgt.common.ChunkDescriptor;
import io.entgra.device.mgt.core.application.mgt.common.FileDescriptor;
import io.entgra.device.mgt.core.application.mgt.common.FileMetaEntry;
import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationStorageManagementException;
import io.entgra.device.mgt.core.application.mgt.core.exception.FileTransferServiceHelperUtilException;
import io.entgra.device.mgt.core.application.mgt.core.internal.DataHolder;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.NotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.UUID;

public class FileTransferServiceHelperUtil {
    private static final Log log = LogFactory.getLog(FileTransferServiceHelperUtil.class);
    private static final String ROOT = "iot-artifact-holder";
    private static final String SYSTEM_PROPERTY_TEMP_DIR = "java.io.tmpdir";
    private static final String META_ENTRY_FILE_NAME = ".meta.json";
    private static final Gson gson = new Gson();
    public static void createDefaultRootStructure() throws FileTransferServiceHelperUtilException {
        try {
            Path root = Paths.get(System.getProperty(SYSTEM_PROPERTY_TEMP_DIR), ROOT);
            if (Files.notExists(root)) {
                setMinimumPermissions(Files.createDirectory(root));
            }

            if (!Files.isDirectory(root)) {
                throw new FileTransferServiceHelperUtilException(root.toAbsolutePath() + " is not a directory");
            }
            setMinimumPermissions(root);
        } catch (IOException e) {
            String msg = "Error encountered while creating default artifact root structure";
            log.error(msg, e);
            throw new FileTransferServiceHelperUtilException(msg, e);
        }
    }

    public static Path createNewArtifactHolder(FileMetaEntry fileMetaEntry) throws FileTransferServiceHelperUtilException {
        try {
            Path artifactHolder = Paths.get(System.getProperty(SYSTEM_PROPERTY_TEMP_DIR), ROOT, UUID.randomUUID().toString());
            if (Files.exists(artifactHolder)) {
                throw new FileTransferServiceHelperUtilException("Artifact holder already exists in " + artifactHolder);
            }
            setMinimumPermissions(Files.createDirectory(artifactHolder));
            createMetaEntry(fileMetaEntry, artifactHolder);
            createArtifactFile(fileMetaEntry, artifactHolder);
            return artifactHolder;
        } catch (IOException e) {
            String msg = "Error occurred while creating artifact holder";
            log.error(msg, e);
            throw new FileTransferServiceHelperUtilException(msg, e);
        }
    }

    public static void populateChunkDescriptor(String artifactHolder, InputStream chunk, ChunkDescriptor chunkDescriptor)
            throws FileTransferServiceHelperUtilException, NotFoundException {
        Path holder = locateArtifactHolder(artifactHolder);
        Path metaEntry = locateMetaEntry(holder);
        chunkDescriptor.setChunk(chunk);
        FileDescriptor fileDescriptor = new FileDescriptor();
        populateFileDescriptor(metaEntry, holder, fileDescriptor);
        chunkDescriptor.setAssociateFileDescriptor(fileDescriptor);
    }

    public static void populateFileDescriptor(String artifactHolder, FileDescriptor fileDescriptor)
            throws FileTransferServiceHelperUtilException, NotFoundException {
        Path holder = locateArtifactHolder(artifactHolder);
        Path metaEntry = locateMetaEntry(holder);
        populateFileDescriptor(metaEntry, holder, fileDescriptor);
    }

    public static void populateFileDescriptor(Path metaEntry, Path artifactHolder, FileDescriptor fileDescriptor) throws FileTransferServiceHelperUtilException {
        try {
            byte []metaEntryByteContent = Files.readAllBytes(metaEntry);
            FileMetaEntry fileMetaEntry = gson.fromJson(new String(metaEntryByteContent, StandardCharsets.UTF_8), FileMetaEntry.class);
            fileDescriptor.setFileName(fileMetaEntry.getFileName());
            fileDescriptor.setActualFileSize(fileMetaEntry.getSize());
            fileDescriptor.setFullQualifiedName(fileMetaEntry.getFileName() + "." + fileMetaEntry.getExtension());
            Path artifact = artifactHolder.resolve(fileDescriptor.getFullQualifiedName());
            fileDescriptor.setAbsolutePath(artifact.toAbsolutePath().toString());
            fileDescriptor.setExtension(fileMetaEntry.getExtension());
            fileDescriptor.setFile(Files.newInputStream(artifact));
        } catch (IOException e) {
            String msg = "Error encountered while populating chuck descriptor";
            log.error(msg, e);
            throw new FileTransferServiceHelperUtilException(msg, e);
        }
    }

    private static Path locateArtifactHolder(String artifactHolder) throws FileTransferServiceHelperUtilException, NotFoundException {
        Path holder = Paths.get(System.getProperty(SYSTEM_PROPERTY_TEMP_DIR), ROOT, artifactHolder);
        if (Files.notExists(holder)) {
            throw new NotFoundException(holder.toAbsolutePath() + " is not exists");
        }

        if (!Files.isDirectory(holder)) {
            throw new FileTransferServiceHelperUtilException(holder.toFile().getAbsolutePath() + " is not a directory");
        }
        return holder;
    }

    private static Path locateMetaEntry(Path artifactHolder) throws FileTransferServiceHelperUtilException {
        Path metaEntry = artifactHolder.resolve(META_ENTRY_FILE_NAME);
        if (Files.notExists(metaEntry) || Files.isDirectory(metaEntry)) {
            throw new FileTransferServiceHelperUtilException("Can't locate " + META_ENTRY_FILE_NAME);
        }

        if (!Files.isReadable(metaEntry)) {
            throw new FileTransferServiceHelperUtilException("Unreadable " + META_ENTRY_FILE_NAME);
        }
        return metaEntry;
    }

    public static void writeChunk(ChunkDescriptor chunkDescriptor) throws FileTransferServiceHelperUtilException {
        if (chunkDescriptor == null) {
            throw new FileTransferServiceHelperUtilException("Received null for chuck descriptor");
        }
        FileDescriptor fileDescriptor = chunkDescriptor.getAssociateFileDescriptor();
        if (fileDescriptor == null) {
            throw new FileTransferServiceHelperUtilException("Target file descriptor is missing for retrieved chunk");
        }
        Path artifact = Paths.get(fileDescriptor.getAbsolutePath());
        try {
            InputStream chuckStream = chunkDescriptor.getChunk();
            byte []chunk = new byte[chuckStream.available()];
            chuckStream.read(chunk);
            Files.write(artifact, chunk, StandardOpenOption.CREATE, StandardOpenOption.SYNC, StandardOpenOption.APPEND);
        } catch (IOException e) {
            String msg = "Error encountered while writing to the " + artifact;
            log.error(msg, e);
            throw new FileTransferServiceHelperUtilException(msg, e);
        }
    }

    public static FileDescriptor resolve(URL downloadUrl) throws FileTransferServiceHelperUtilException {
        if (downloadUrl == null) {
            throw new FileTransferServiceHelperUtilException("Received null for download url");
        }

        if (!Objects.equals(System.getProperty("iot.gateway.host"), downloadUrl.getHost()) &&
                !Objects.equals(System.getProperty("iot.core.host"), downloadUrl.getHost())) {
            if (log.isDebugEnabled()) {
                log.debug("Download URL " + downloadUrl + " contains not matching host");
            }
            return null;
        }

        String []urlPathSegments = downloadUrl.getPath().split("/");

        FileDescriptor fileDescriptorResolvedFromRelease = resolve(urlPathSegments);
        if (fileDescriptorResolvedFromRelease != null) {
            return fileDescriptorResolvedFromRelease;
        }

        if (urlPathSegments.length < 2) {
            if (log.isDebugEnabled()) {
                log.debug("URL patch segments contain less than 2 segments");
            }
            return null;
        }

        String file = urlPathSegments[urlPathSegments.length - 1];
        String artifactHolder = urlPathSegments[urlPathSegments.length - 2];
        try {
            FileDescriptor fileDescriptor = new FileDescriptor();
            populateFileDescriptor(artifactHolder, fileDescriptor);
            if (!Objects.equals(file, fileDescriptor.getFullQualifiedName())) {
                if (log.isDebugEnabled()) {
                    log.debug("File name not equal to the file exists in the local");
                }
                return null;
            }
            return fileDescriptor;
        } catch (NotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("Local URL not found in the system");
            }
            return null;
        }
    }

    private static void setMinimumPermissions(Path path) throws FileTransferServiceHelperUtilException {
        File file = path.toFile();
        if (!file.setReadable(true, true)) {
            throw new FileTransferServiceHelperUtilException("Failed to set read permission for " + file.getAbsolutePath());
        }

        if (!file.setWritable(true, true)) {
            throw new FileTransferServiceHelperUtilException("Failed to set write permission for " + file.getAbsolutePath());
        }
    }

    private static void createMetaEntry(FileMetaEntry fileMetaEntry, Path artifactHolder) throws FileTransferServiceHelperUtilException {
        try {
            Path metaEntry = artifactHolder.resolve(META_ENTRY_FILE_NAME);
            String fileMetaJsonContent = gson.toJson(fileMetaEntry);
            Files.write(metaEntry, fileMetaJsonContent.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.SYNC);
        } catch (IOException e) {
            throw new FileTransferServiceHelperUtilException("Error encountered while creating meta entry", e);
        }
    }

    private static void createArtifactFile(FileMetaEntry fileMetaEntry, Path artifactHolder) throws FileTransferServiceHelperUtilException {
        try {
            Path artifactFile = artifactHolder.resolve(fileMetaEntry.getFileName() + "." + fileMetaEntry.getExtension());
            fileMetaEntry.setAbsolutePath(artifactFile.toAbsolutePath().toString());
            Files.createFile(artifactFile);
            setMinimumPermissions(artifactFile);
        } catch (IOException e) {
            throw new FileTransferServiceHelperUtilException("Error encountered while creating artifact file", e);
        }
    }

    private static FileDescriptor resolve(String []urlSegments) throws FileTransferServiceHelperUtilException {
        // check the possibility of url is pointing to a file resides in  the default storage path
        if (urlSegments.length < 4) {
            if (log.isDebugEnabled()) {
                log.debug("URL path segments contain less than 4 segments");
            }
            return null;
        }

        int tenantId;
        try {
            tenantId = Integer.parseInt(urlSegments[urlSegments.length - 4]);
        } catch (NumberFormatException e) {
            if (log.isDebugEnabled()) {
                log.debug("URL isn't pointing to a file resides in  the default storage path");
            }
            return null;
        }

        String fileName = urlSegments[urlSegments.length - 1];
        String folderName = urlSegments[urlSegments.length - 2];
        String appHash = urlSegments[urlSegments.length - 3];

        try {
            InputStream fileStream = DataHolder.getInstance().
                    getApplicationStorageManager().getFileStream(appHash, folderName, fileName, tenantId);
            if (fileStream == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not found the file " + fileName);
                }
                return null;
            }

            String []fileNameSegments = fileName.split("\\.(?=[^.]+$)");
            if (fileNameSegments.length < 2) {
                throw new FileTransferServiceHelperUtilException("Invalid full qualified name encountered :" + fileName);
            }
            FileDescriptor fileDescriptor = new FileDescriptor();
            fileDescriptor.setFile(fileStream);
            fileDescriptor.setFullQualifiedName(fileName);
            fileDescriptor.setExtension(fileNameSegments[fileNameSegments.length - 1]);
            fileDescriptor.setFileName(fileNameSegments[fileNameSegments.length - 2]);
            fileDescriptor.setAbsolutePath(DataHolder.getInstance().
                    getApplicationStorageManager().getAbsolutePathOfFile(appHash, folderName, fileName, tenantId));
            return fileDescriptor;
        } catch (ApplicationStorageManagementException e) {
            throw new FileTransferServiceHelperUtilException("Error encountered while getting file input stream", e);
        }
    }
}
