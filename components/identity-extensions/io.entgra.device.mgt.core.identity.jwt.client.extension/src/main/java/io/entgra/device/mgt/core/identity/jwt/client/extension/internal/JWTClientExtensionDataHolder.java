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
package io.entgra.device.mgt.core.identity.jwt.client.extension.internal;

import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

public class JWTClientExtensionDataHolder {
	private static JWTClientExtensionDataHolder thisInstance = new JWTClientExtensionDataHolder();

	private TenantRegistryLoader tenantRegistryLoader;
	private TenantIndexingLoader indexLoader;
	private RegistryService registryService;
	private RealmService realmService;
	private TenantManager tenantManager;

	private JWTClientExtensionDataHolder() {
	}


	public static JWTClientExtensionDataHolder getInstance() {
		return thisInstance;
	}


	public void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader){
		this.tenantRegistryLoader = tenantRegistryLoader;
	}

	public TenantRegistryLoader getTenantRegistryLoader(){
		return tenantRegistryLoader;
	}

	public void setIndexLoaderService(TenantIndexingLoader indexLoader) {
		this.indexLoader = indexLoader;
	}

	public TenantIndexingLoader getIndexLoaderService(){
		return indexLoader;
	}

	public RegistryService getRegistryService() {
		return registryService;
	}

	public void setRegistryService(RegistryService registryService) {
		this.registryService = registryService;
	}

	public RealmService getRealmService() {
		if (realmService == null) {
			throw new IllegalStateException("Realm service is not initialized properly");
		}
		return realmService;
	}

	public void setRealmService(RealmService realmService) {
		this.realmService = realmService;
		setTenantManager(realmService != null ?
				realmService.getTenantManager() : null);
	}

	private void setTenantManager(TenantManager tenantManager) {
		this.tenantManager = tenantManager;
	}

	public TenantManager getTenantManager() {
		if (tenantManager == null) {
			throw new IllegalStateException("Tenant manager is not initialized properly");
		}
		return tenantManager;
	}
}
