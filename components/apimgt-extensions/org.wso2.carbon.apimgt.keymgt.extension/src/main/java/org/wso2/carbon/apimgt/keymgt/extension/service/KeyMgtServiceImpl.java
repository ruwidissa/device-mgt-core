/*
 * Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.apimgt.keymgt.extension.service;

import com.google.gson.Gson;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.extension.DCRResponse;
import org.wso2.carbon.apimgt.keymgt.extension.KeyManagerPayload;
import org.wso2.carbon.apimgt.keymgt.extension.KeyMgtConstants;
import org.wso2.carbon.apimgt.keymgt.extension.OAuthApplication;
import org.wso2.carbon.apimgt.keymgt.extension.TokenRequest;
import org.wso2.carbon.apimgt.keymgt.extension.TokenResponse;
import org.wso2.carbon.apimgt.keymgt.extension.exception.BadRequestException;
import org.wso2.carbon.apimgt.keymgt.extension.exception.KeyMgtException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.config.keymanager.KeyManagerConfigurations;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KeyMgtServiceImpl implements KeyMgtService {

    private static final Log log = LogFactory.getLog(KeyMgtServiceImpl.class);

    private static final OkHttpClient client = getOkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson gson = new Gson();
    private KeyManagerConfigurations kmConfig = null;
    RealmService realmService = null;
    String subTenantUserUsername, subTenantUserPassword, keyManagerName, msg = null;

    public DCRResponse dynamicClientRegistration(String clientName, String owner, String grantTypes, String callBackUrl,
                                                 String[] tags, boolean isSaasApp) throws KeyMgtException {

        String tenantDomain = MultitenantUtils.getTenantDomain(owner);
        int tenantId;

        try {
            tenantId = getRealmService()
                    .getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            msg = "Error while loading tenant configuration";
            log.error(msg);
            throw new KeyMgtException(msg);
        }

        kmConfig = getKeyManagerConfig();

        if (KeyMgtConstants.SUPER_TENANT.equals(tenantDomain)) {
            OAuthApplication superTenantOauthApp = createOauthApplication(
                    KeyMgtConstants.RESERVED_OAUTH_APP_NAME_PREFIX + KeyMgtConstants.SUPER_TENANT,
                    kmConfig.getAdminUsername(), tags);
            return new DCRResponse(superTenantOauthApp.getClientId(), superTenantOauthApp.getClientSecret());
        } else {
            // super-tenant admin dcr and token generation
            OAuthApplication superTenantOauthApp = createOauthApplication(
                    KeyMgtConstants.RESERVED_OAUTH_APP_NAME_PREFIX + KeyMgtConstants.SUPER_TENANT,
                    kmConfig.getAdminUsername(), null);
            String superAdminAccessToken = createAccessToken(superTenantOauthApp);

            // create new key manager for the tenant, under super-tenant space
            createKeyManager(tenantId, tenantDomain, superAdminAccessToken);

            // create a sub-tenant user
            try {
                subTenantUserUsername = getRealmService()
                        .getTenantUserRealm(tenantId).getRealmConfiguration()
                        .getRealmProperty("reserved_tenant_user_username") + "@" + tenantDomain;
                subTenantUserPassword = getRealmService()
                        .getTenantUserRealm(tenantId).getRealmConfiguration()
                        .getRealmProperty("reserved_tenant_user_password");
            } catch (UserStoreException e) {
                msg = "Error while loading user realm configuration";
                log.error(msg);
                throw new KeyMgtException(msg);
            }
            createUserIfNotExists(subTenantUserUsername, subTenantUserPassword);

            // DCR for the requesting user
            OAuthApplication dcrApplication = createOauthApplication(clientName, owner, tags);
            String requestingUserAccessToken = createAccessToken(dcrApplication);

            // get application id
            Application application = getApplication(clientName, owner);
            String applicationUUID = application.getUUID();

            // do app key mapping
            mapApplicationKeys(dcrApplication.getClientId(), dcrApplication.getClientSecret(), keyManagerName,
                    applicationUUID, requestingUserAccessToken);
            return new DCRResponse(dcrApplication.getClientId(), dcrApplication.getClientSecret());
        }
    }

    public TokenResponse generateAccessToken(TokenRequest tokenRequest) throws KeyMgtException, BadRequestException {
        try {
            Application application = APIUtil.getApplicationByClientId(tokenRequest.getClientId());
            String tenantDomain = MultitenantUtils.getTenantDomain(application.getOwner());

            String username, password;
            if (KeyMgtConstants.SUPER_TENANT.equals(tenantDomain)) {
                kmConfig = getKeyManagerConfig();
                username = kmConfig.getAdminUsername();
                password = kmConfig.getAdminUsername();
            } else {
                try {
                    username = getRealmService()
                            .getTenantUserRealm(-1234).getRealmConfiguration()
                            .getRealmProperty("reserved_tenant_user_username") + "@" + tenantDomain;
                    password = getRealmService()
                            .getTenantUserRealm(-1234).getRealmConfiguration()
                            .getRealmProperty("reserved_tenant_user_password");
                } catch (UserStoreException e) {
                    msg = "Error while loading user realm configuration";
                    log.error(msg);
                    throw new KeyMgtException(msg);
                }
            }

            JSONObject jsonObject = new JSONObject();
            if ("client_credentials".equals(tokenRequest.getGrantType())) {
                jsonObject.put("grant_type", "password");
                jsonObject.put("username", username);
                jsonObject.put("password", password);
            } else if ("refresh_token".equals(tokenRequest.getGrantType())) {
                jsonObject.put("grant_type", "refresh_token");
                jsonObject.put("refresh_token", tokenRequest.getRefreshToken());
            } else {
                msg = "Invalid grant type: " + tokenRequest.getGrantType();
                throw new BadRequestException(msg);
            }
            jsonObject.put("scope", tokenRequest.getScope());

            RequestBody appTokenPayload = RequestBody.Companion.create(jsonObject.toString(), JSON);
            kmConfig = getKeyManagerConfig();
            String appTokenEndpoint = kmConfig.getServerUrl() + KeyMgtConstants.OAUTH2_TOKEN_ENDPOINT;
            Request request = new Request.Builder()
                    .url(appTokenEndpoint)
                    .addHeader(KeyMgtConstants.AUTHORIZATION_HEADER, Credentials.basic(tokenRequest.getClientId(), tokenRequest.getClientSecret()))
                    .post(appTokenPayload)
                    .build();

            Response response = client.newCall(request).execute();
            jsonObject = new JSONObject(response.body().string());
            String accessToken;
            if (KeyMgtConstants.SUPER_TENANT.equals(tenantDomain)) {
                accessToken = jsonObject.getString("access_token");
            } else {
                int tenantId = getRealmService()
                        .getTenantManager().getTenantId(tenantDomain);
                accessToken = tenantId + "_" + jsonObject.getString("access_token");
            }
            return new TokenResponse(accessToken,
                    jsonObject.getString("refresh_token"),
                    jsonObject.getString("scope"),
                    jsonObject.getString("token_type"),
                    jsonObject.getInt("expires_in"));

        } catch (APIManagementException e) {
            msg = "Error occurred while retrieving application";
            log.error(msg);
            throw new KeyMgtException(msg);
        } catch (IOException e) {
            msg = "Error occurred while mapping application keys";
            log.error(msg);
            throw new KeyMgtException(msg);
        } catch (UserStoreException e) {
            msg = "Error occurred while fetching tenant id";
            log.error(msg);
            throw new KeyMgtException(msg);
        }
    }

    /***
     * Maps the application's keys with the given key manager
     *
     * @param consumerKey consumer key of the application
     * @param consumerSecret consumer secret of the application
     * @param keyManager key-manager name to which the keys should be mapped with
     * @param applicationUUID application's UUID
     * @param accessToken access token of the tenant user
     * @throws KeyMgtException if an error occurs while mapping application keys with the key-manager
     */
    private void mapApplicationKeys(String consumerKey, String consumerSecret, String keyManager,
                                    String applicationUUID, String accessToken) throws KeyMgtException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("consumerKey", consumerKey);
        jsonObject.put("consumerSecret", consumerSecret);
        jsonObject.put("keyManager", keyManager);
        jsonObject.put("keyType", "PRODUCTION");

        RequestBody keyMappingPayload = RequestBody.Companion.create(jsonObject.toString(), JSON);
        kmConfig = getKeyManagerConfig();
        String keyMappingEndpoint = kmConfig.getServerUrl() +
                KeyMgtConstants.APPLICATION_KEY_MAPPING_ENDPOINT.replaceAll("<applicationId>", applicationUUID);
        Request request = new Request.Builder()
                .url(keyMappingEndpoint)
                .addHeader(KeyMgtConstants.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                .addHeader(KeyMgtConstants.X_WSO2_TENANT_HEADER, KeyMgtConstants.SUPER_TENANT)
                .post(keyMappingPayload)
                .build();

        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            msg = "Error occurred while mapping application keys";
            throw new KeyMgtException(msg);
        }
    }

    /***
     * Creates user if not exists already in the user store
     *
     * @param username username of the user
     * @param password password of the user
     * @throws KeyMgtException if any error occurs while fetching tenant details
     */
    private void createUserIfNotExists(String username, String password) throws KeyMgtException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = getRealmService()
                    .getTenantManager().getTenantId(tenantDomain);
            UserRealm userRealm = getRealmService()
                    .getTenantUserRealm(tenantId);
            UserStoreManager userStoreManager = userRealm.getUserStoreManager();

            if (!userStoreManager.isExistingUser(MultitenantUtils.getTenantAwareUsername(username))) {
                String[] roles = {"admin"};
                userStoreManager.addUser(MultitenantUtils.getTenantAwareUsername(username), password, roles, null, "");
            }
        } catch (UserStoreException e) {
            msg = "Error when trying to fetch tenant details";
            log.error(msg);
            throw new KeyMgtException(msg);
        }
    }

    /***
     * Creates an OAuth Application
     *
     * @param clientName Name of the client application
     * @param owner Owner's name of the client application
     * @return @{@link OAuthApplication} OAuth application object
     * @throws KeyMgtException if any error occurs while creating response object
     */
    private OAuthApplication createOauthApplication (String clientName, String owner, String[] tags) throws KeyMgtException {
        String oauthAppCreationPayloadStr = createOauthAppCreationPayload(clientName, owner, tags);
        RequestBody oauthAppCreationPayload = RequestBody.Companion.create(oauthAppCreationPayloadStr, JSON);
        kmConfig = getKeyManagerConfig();
        String dcrEndpoint = kmConfig.getServerUrl() + KeyMgtConstants.DCR_ENDPOINT;
        String username, password;

        if (KeyMgtConstants.SUPER_TENANT.equals(MultitenantUtils.getTenantDomain(owner))) {
            username = kmConfig.getAdminUsername();
            password = kmConfig.getAdminPassword();
        } else {
            username = subTenantUserUsername;
            password = subTenantUserPassword;
        }

        Request request = new Request.Builder()
                .url(dcrEndpoint)
                .addHeader(KeyMgtConstants.AUTHORIZATION_HEADER, Credentials.basic(username, password))
                .post(oauthAppCreationPayload)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return gson.fromJson(response.body().string(), OAuthApplication.class);
        } catch (IOException e) {
            msg = "Error occurred while processing the response";
            throw new KeyMgtException(msg);
        }
    }

    /***
     * Creates access token with client credentials grant type
     *
     * @param oAuthApp OAuth application object
     * @return Access token
     * @throws KeyMgtException if any error occurs while reading access token from the response
     */
    private String createAccessToken (OAuthApplication oAuthApp) throws KeyMgtException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("grant_type", KeyMgtConstants.CLIENT_CREDENTIALS_GRANT_TYPE);
        jsonObject.put("scope", KeyMgtConstants.DEFAULT_ADMIN_SCOPES);

        RequestBody accessTokenReqPayload = RequestBody.Companion.create(jsonObject.toString(), JSON);
        kmConfig = getKeyManagerConfig();
        String tokenEndpoint = kmConfig.getServerUrl() + KeyMgtConstants.OAUTH2_TOKEN_ENDPOINT;
        Request request = new Request.Builder()
                .url(tokenEndpoint)
                .addHeader(KeyMgtConstants.AUTHORIZATION_HEADER, Credentials.basic(oAuthApp.getClientId(), oAuthApp.getClientSecret()))
                .post(accessTokenReqPayload)
                .build();

        try {
            Response response = client.newCall(request).execute();
            jsonObject = new JSONObject(response.body().string());
            return jsonObject.getString("access_token");
        } catch (IOException e) {
            msg = "Error occurred while reading access token from response";
            throw new KeyMgtException(msg);
        }
    }

    /***
     * Creates a key manager for a given tenant, under super-tenant space
     *
     * @param tenantId tenant-id of the key-manager
     * @param tenantDomain tenant domain of the key-manager
     * @param accessToken access token of the super-tenant user
     * @throws KeyMgtException if any error occurs while creating a key-manager
     */
    private void createKeyManager(int tenantId, String tenantDomain, String accessToken) throws KeyMgtException {
        try {
            List<String> kmGrantTypes = new ArrayList<>();
            kmGrantTypes.add("client_credentials");

            kmConfig = getKeyManagerConfig();
            Map<String, Object> additionalProperties = new HashMap<>();
            additionalProperties.put("Username", kmConfig.getAdminUsername());
            additionalProperties.put("Password", kmConfig.getAdminPassword());
            additionalProperties.put("self_validate_jwt", true);

            keyManagerName = generateCustomKeyManagerName(tenantDomain);
            KeyManagerPayload keyManagerPayload = new KeyManagerPayload(
                    tenantDomain, tenantId, kmConfig.getServerUrl(),
                    keyManagerName, kmGrantTypes, additionalProperties
            );
            String createKeyManagerPayload = gson.toJson(keyManagerPayload);
            RequestBody requestBody = RequestBody.Companion.create(createKeyManagerPayload, JSON);
            String keyManagerEndpoint = kmConfig.getServerUrl() + KeyMgtConstants.CREATE_KEY_MANAGER_ENDPOINT;
            Request request = new Request.Builder()
                    .url(keyManagerEndpoint)
                    .addHeader(KeyMgtConstants.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                    .post(requestBody)
                    .build();
            client.newCall(request).execute();
        } catch (IOException e) {
            msg = "Error occurred while invoking create key manager endpoint";
            log.error(msg);
            throw new KeyMgtException(msg);
        }
    }

    /***
     * Retrieves an application by name and owner
     *
     * @param applicationName name of the application
     * @param owner owner of the application
     * @return @{@link Application} Application object
     * @throws KeyMgtException if any error occurs while retrieving the application
     */
    private Application getApplication(String applicationName, String owner) throws KeyMgtException {
        try {
            APIManagerFactory apiManagerFactory = APIManagerFactory.getInstance();
            APIConsumer apiConsumer = apiManagerFactory.getAPIConsumer(owner);
            return apiConsumer.getApplicationsByName(owner, applicationName, "");
        } catch (APIManagementException e) {
            msg = "Error while trying to retrieve the application";
            log.error(msg);
            throw new KeyMgtException(msg);
        }
    }

    private String createOauthAppCreationPayload(String clientName, String owner, String[] tags) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("applicationName", clientName);
        jsonObject.put("username", owner);
        jsonObject.put("tags", tags);
        return jsonObject.toString();
    }

    private String generateCustomKeyManagerName(String tenantDomain) {
        return KeyMgtConstants.CUSTOM_KEY_MANAGER_NAME_PREFIX + tenantDomain;
    }

    private RealmService getRealmService() {
        if(realmService == null) {
            PrivilegedCarbonContext context = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            return (RealmService) context.getOSGiService(RealmService.class, null);
        } else {
            return realmService;
        }
    }

    private static OkHttpClient getOkHttpClient() {
        X509TrustManager trustAllCerts = new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }

            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }
        };
        return new OkHttpClient.Builder()
                .sslSocketFactory(getSimpleTrustedSSLSocketFactory(), trustAllCerts)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                }).build();
    }

    private static SSLSocketFactory getSimpleTrustedSSLSocketFactory() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            return sc.getSocketFactory();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            return null;
        }

    }

    private KeyManagerConfigurations getKeyManagerConfig() {
        if (kmConfig != null) {
            return kmConfig;
        } else {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
            return deviceManagementConfig.getKeyManagerConfigurations();
        }
    }
}
