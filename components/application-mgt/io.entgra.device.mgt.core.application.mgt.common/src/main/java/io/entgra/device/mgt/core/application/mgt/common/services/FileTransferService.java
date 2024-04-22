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

package io.entgra.device.mgt.core.application.mgt.common.services;

import io.entgra.device.mgt.core.application.mgt.common.ChunkDescriptor;
import io.entgra.device.mgt.core.application.mgt.common.FileDescriptor;
import io.entgra.device.mgt.core.application.mgt.common.FileMetaEntry;
import io.entgra.device.mgt.core.application.mgt.common.TransferLink;
import io.entgra.device.mgt.core.application.mgt.common.exception.FileTransferServiceException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.NotFoundException;

import java.io.InputStream;
import java.net.URL;

public interface FileTransferService {
    /**
     * Create an upload link
     * @param fileMetaEntry {@link FileMetaEntry}
     * @return {@link TransferLink}
     * @throws FileTransferServiceException Throws when error encountered while generating upload link
     */
    TransferLink generateUploadLink(FileMetaEntry fileMetaEntry) throws FileTransferServiceException;

    /**
     * Resolve {@link ChunkDescriptor} using artifactHolder UUID and a given chunk
     * @param artifactHolder Artifact holder's UUID string
     * @param chunk Data chunk
     * @return {@link ChunkDescriptor}
     * @throws FileTransferServiceException Throws when error encountered while resolving chunk descriptor
     * @throws NotFoundException Throws when artifact holder not exists in the file system
     */
    ChunkDescriptor resolve(String artifactHolder, InputStream chunk) throws FileTransferServiceException, NotFoundException;

    /**
     * Write chunk of data
     * @param chunkDescriptor {@link ChunkDescriptor}
     * @throws FileTransferServiceException Throws when error encountered while writing chunk
     */
    void writeChunk(ChunkDescriptor chunkDescriptor) throws FileTransferServiceException;

    /**
     * Check if the provided download url point to a file which exists on the local env or not
     * @param downloadUrl Download URL
     * @return Returns true if the download URL point to a file which resides in local
     * @throws FileTransferServiceException Throws when error encountered while checking
     */
    boolean isExistsOnLocal(URL downloadUrl) throws FileTransferServiceException;

    /**
     * Resolve {@link FileDescriptor} from a given download URL
     * @param downloadUrl Download URL
     * @return {@link java.io.FileDescriptor}
     * @throws FileTransferServiceException Throws when error encountered while resolving file descriptor
     */
    FileDescriptor resolve(URL downloadUrl) throws FileTransferServiceException;
}
