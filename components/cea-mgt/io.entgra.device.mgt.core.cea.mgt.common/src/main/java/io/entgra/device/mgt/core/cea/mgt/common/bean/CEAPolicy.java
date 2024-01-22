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

package io.entgra.device.mgt.core.cea.mgt.common.bean;

import java.io.Serializable;
import java.util.Date;

public class CEAPolicy implements Serializable {
    private static final long serialVersionUID = -4578284769501447L;
    private ActiveSyncServer activeSyncServer;
    private AccessPolicy accessPolicy;
    private GracePeriod gracePeriod;
    private Date created;
    private Date lastUpdated;
    private Date lastSynced;
    private boolean isSynced;
    private int tenantId;

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Date getLastSynced() {
        return lastSynced;
    }

    public void setLastSynced(Date lastSynced) {
        this.lastSynced = lastSynced;
    }

    public AccessPolicy getAccessPolicy() {
        return accessPolicy;
    }

    public void setAccessPolicy(AccessPolicy accessPolicy) {
        this.accessPolicy = accessPolicy;
    }

    public GracePeriod getGracePeriod() {
        return gracePeriod;
    }

    public void setGracePeriod(GracePeriod gracePeriod) {
        this.gracePeriod = gracePeriod;
    }

    public ActiveSyncServer getActiveSyncServer() {
        return activeSyncServer;
    }

    public void setActiveSyncServer(ActiveSyncServer activeSyncServer) {
        this.activeSyncServer = activeSyncServer;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }
}
