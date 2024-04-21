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

package io.entgra.device.mgt.core.application.mgt.common;

import java.io.InputStream;

public class ChunkDescriptor {
    private FileDescriptor associateFileDescriptor;
    private long size;
    private InputStream chunk;

    public FileDescriptor getAssociateFileDescriptor() {
        return associateFileDescriptor;
    }

    public void setAssociateFileDescriptor(FileDescriptor associateFileDescriptor) {
        this.associateFileDescriptor = associateFileDescriptor;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public InputStream getChunk() {
        return chunk;
    }

    public void setChunk(InputStream chunk) {
        this.chunk = chunk;
    }
}
