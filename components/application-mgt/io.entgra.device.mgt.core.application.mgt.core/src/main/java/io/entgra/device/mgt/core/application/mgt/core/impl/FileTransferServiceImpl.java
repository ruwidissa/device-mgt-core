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

package io.entgra.device.mgt.core.application.mgt.core.impl;

import io.entgra.device.mgt.core.application.mgt.common.ChunkDescriptor;
import io.entgra.device.mgt.core.application.mgt.common.FileDescriptor;
import io.entgra.device.mgt.core.application.mgt.common.FileMetaEntry;
import io.entgra.device.mgt.core.application.mgt.common.TransferLink;
import io.entgra.device.mgt.core.application.mgt.common.exception.FileTransferServiceException;
import io.entgra.device.mgt.core.application.mgt.common.services.FileTransferService;
import io.entgra.device.mgt.core.application.mgt.core.exception.FileTransferServiceHelperUtilException;
import io.entgra.device.mgt.core.application.mgt.core.util.FileTransferServiceHelperUtil;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.NotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class FileTransferServiceImpl implements FileTransferService {
    private final static Log log = LogFactory.getLog(FileTransferServiceImpl.class);
    private static volatile FileTransferServiceImpl INSTANCE;

    private FileTransferServiceImpl() throws FileTransferServiceException {
        try {
            FileTransferServiceHelperUtil.createDefaultRootStructure();
        } catch (FileTransferServiceHelperUtilException e) {
            String msg = "Error occurred while initializing file transfer service";
            log.error(msg, e);
            throw new FileTransferServiceException(msg, e);
        }
    }

    public static FileTransferService getInstance() throws FileTransferServiceException{
        if (INSTANCE == null) {
            synchronized (FileTransferServiceImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FileTransferServiceImpl();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public TransferLink generateUploadLink(FileMetaEntry fileMetaEntry) throws FileTransferServiceException {
        try {
            Path artifactHolder = FileTransferServiceHelperUtil.createNewArtifactHolder(fileMetaEntry);
            String []pathSegments = artifactHolder.toString().split(FileSystems.getDefault().getSeparator());
            TransferLink.TransferLinkBuilder transferLinkBuilder =
                    new TransferLink.TransferLinkBuilder(pathSegments[pathSegments.length - 1]);
            return transferLinkBuilder.build();
        } catch (FileTransferServiceHelperUtilException e) {
            String msg = "Error encountered while generating upload link";
            log.error(msg, e);
            throw new FileTransferServiceException(msg, e);
        }
    }

    @Override
    public ChunkDescriptor resolve(String artifactHolder, InputStream chunk) throws FileTransferServiceException, NotFoundException {
        ChunkDescriptor chunkDescriptor = new ChunkDescriptor();
        try {
            FileTransferServiceHelperUtil.populateChunkDescriptor(artifactHolder, chunk, chunkDescriptor);
            return chunkDescriptor;
        } catch (FileTransferServiceHelperUtilException e) {
            String msg = "Error occurred while resolving chuck descriptor for " + artifactHolder;
            log.error(msg);
            throw new FileTransferServiceException(msg, e);
        }
    }

    @Override
    public void writeChunk(ChunkDescriptor chunkDescriptor) throws FileTransferServiceException {
        try {
            FileTransferServiceHelperUtil.writeChunk(chunkDescriptor);
        } catch (FileTransferServiceHelperUtilException e) {
            String msg = "Failed to write data to artifact located in " + chunkDescriptor.getAssociateFileDescriptor().getAbsolutePath();
            log.error(msg);
            throw new FileTransferServiceException(msg, e);
        }
    }

    @Override
    public boolean isExistsOnLocal(URL downloadUrl) throws FileTransferServiceException {
        try {
            FileDescriptor fileDescriptor = FileTransferServiceHelperUtil.resolve(downloadUrl);
            if (fileDescriptor != null && fileDescriptor.getFile() != null) {
                fileDescriptor.getFile().close();
                return true;
            }
            return false;
        } catch (FileTransferServiceHelperUtilException | IOException e) {
            String msg = "Error occurred while checking the existence of artifact on the local environment";
            log.error(msg, e);
            throw new FileTransferServiceException(msg, e);
        }
    }

    @Override
    public FileDescriptor resolve(URL downloadUrl) throws FileTransferServiceException {
        try {
            return FileTransferServiceHelperUtil.resolve(downloadUrl);
        } catch (FileTransferServiceHelperUtilException e) {
            String msg = "Error occurred while resolving file descriptor pointing from " + downloadUrl;
            log.error(msg, e);
            throw new FileTransferServiceException(msg, e);
        }
    }
}
