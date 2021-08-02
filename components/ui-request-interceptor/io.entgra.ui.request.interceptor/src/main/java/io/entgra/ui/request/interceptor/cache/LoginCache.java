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

import java.util.LinkedHashMap;

/**
 * Contains necessary functions to manage oAuth app cache during login handling
 */
public class LoginCache {

    private final LinkedHashMap<OAuthAppCacheKey, OAuthApp> cache;
    private final int capacity;

    public LoginCache(int capacity) {
        this.capacity = capacity;
        this.cache = new LinkedHashMap<>(capacity);
    }

    /**
     * Persists OAuth app cache if it is not already persisted
     *
     * @param oAuthAppCacheKey - The identifier key of the cache
     * @param oAuthApp         - The value of the cache which contains OAuth app data
     */
    public void addOAuthAppToCache(OAuthAppCacheKey oAuthAppCacheKey, OAuthApp oAuthApp) {
        if (cache.size() == capacity) {
            cache.remove(cache.entrySet().iterator().next().getKey());
        }
        cache.put(oAuthAppCacheKey, oAuthApp);
    }

    /**
     * Retrieves the OAuth app cache
     *
     * @param oAuthAppCacheKey - The key to identify the cache
     * @return - Returns OAuthApp object
     */
    public OAuthApp getOAuthAppCache(OAuthAppCacheKey oAuthAppCacheKey) {
        OAuthApp oAuthApp = cache.get(oAuthAppCacheKey);
        if (oAuthApp != null) {
            if (cache.size() == capacity) {
                cache.remove(oAuthAppCacheKey);
                cache.put(oAuthAppCacheKey, oAuthApp);
            }
        }
        return oAuthApp;
    }
}
