/*
 * Copyright (c) 2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.ui.request.interceptor.cache;

import io.entgra.ui.request.interceptor.util.HandlerConstants;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

/**
 * Contains necessary functions to manage oAuth app cache during login handling
 */
public class LoginCacheManager {

    private CacheManager cacheManager = null;
    private Cache<OAuthAppCacheKey, OAuthApp> cache = null;

    /**
     * Initialize the cache manager if it is not already initialized
     */
    public void initializeCacheManager() {
        cacheManager = Caching.getCacheManagerFactory().getCacheManager(HandlerConstants.LOGIN_CACHE);
    }

    /**
     * Persists OAuth app cache if it is not already persisted
     *
     * @param oAuthAppCacheKey - The identifier key of the cache
     * @param oAuthApp         - The value of the cache which contains OAuth app data
     */
    public void addOAuthAppToCache(OAuthAppCacheKey oAuthAppCacheKey, OAuthApp oAuthApp) {
        cache = cacheManager.getCache(HandlerConstants.LOGIN_CACHE);
        cache.put(oAuthAppCacheKey, oAuthApp);
    }

    /**
     * Retrieves the OAuth app cache
     *
     * @param oAuthAppCacheKey - The key to identify the cache
     * @return - Returns OAuthApp object
     */
    public OAuthApp getOAuthAppCache(OAuthAppCacheKey oAuthAppCacheKey) {
        cache = cacheManager.getCache(HandlerConstants.LOGIN_CACHE);
        return cache.get(oAuthAppCacheKey);
    }
}
