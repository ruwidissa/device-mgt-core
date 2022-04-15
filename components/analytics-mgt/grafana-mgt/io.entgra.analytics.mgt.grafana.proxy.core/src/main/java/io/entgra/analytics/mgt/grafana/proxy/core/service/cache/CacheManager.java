/*
 * Copyright (c) 2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.analytics.mgt.grafana.proxy.core.service.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.entgra.analytics.mgt.grafana.proxy.common.exception.GrafanaManagementException;
import io.entgra.analytics.mgt.grafana.proxy.core.config.GrafanaConfiguration;
import io.entgra.analytics.mgt.grafana.proxy.core.config.GrafanaConfigurationManager;
import io.entgra.analytics.mgt.grafana.proxy.core.config.xml.bean.CacheConfiguration;
import io.entgra.analytics.mgt.grafana.proxy.core.service.bean.Datasource;
import io.entgra.analytics.mgt.grafana.proxy.core.util.GrafanaConstants;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class CacheManager {
    private static final Log log = LogFactory.getLog(CacheManager.class);

    private Cache<Integer, Datasource> datasourceAPICache;
    private Cache<QueryTemplateCacheKey, String> queryTemplateAPICache;
    private Cache<String, String> encodedQueryCache;

    private CacheManager() {
        initCache();
    }

    private static final class CacheManagerHolder {
        static final CacheManager cacheManager = new CacheManager();
    }

    public static CacheManager getInstance() {
        return CacheManagerHolder.cacheManager;
    }

    public Cache<String, String> getEncodedQueryCache() {
        return encodedQueryCache;
    }

    public Cache<QueryTemplateCacheKey, String> getQueryTemplateAPICache() {
        return queryTemplateAPICache;
    }


    public Cache<Integer, Datasource> getDatasourceAPICache() {
        return datasourceAPICache;
    }


    private void initCache() {
        this.datasourceAPICache = buildDatasourceCache();
        this.queryTemplateAPICache = buildQueryCacheByName(GrafanaConstants.QUERY_API_CACHE_NAME);
        this.encodedQueryCache = buildQueryCacheByName(GrafanaConstants.ENCODED_QUERY_CACHE_NAME);
    }

    private <K, V> Cache<K, V> buildDatasourceCache() {
        return CacheBuilder.newBuilder().build();
    }

    private <K , V> Cache<K, V> buildQueryCacheByName(String cacheName) {
        int capacity = getCacheCapacity(cacheName);
        return CacheBuilder.newBuilder().maximumSize(capacity).build();
    }



    private static int getCacheCapacity(String cacheName) {
        try {
            GrafanaConfiguration configuration = GrafanaConfigurationManager.getInstance().getGrafanaConfiguration();
            CacheConfiguration cacheConfig = configuration.getCacheByName(cacheName);
            if (cacheConfig == null) {
                log.error("CacheConfiguration config not defined for " + cacheName);
                throw new GrafanaManagementException("Query API CacheConfiguration configuration not properly defined");
            }
            return cacheConfig.getCapacity();
        } catch (GrafanaManagementException e) {
            String errMsg = "Error occurred while initializing cache capacity for " + cacheName;
            log.error(errMsg);
            throw new RuntimeException(errMsg, e);
        }
    }

}
