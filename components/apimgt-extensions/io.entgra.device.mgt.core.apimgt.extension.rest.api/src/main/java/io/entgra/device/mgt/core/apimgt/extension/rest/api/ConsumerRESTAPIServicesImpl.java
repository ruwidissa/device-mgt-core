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

package io.entgra.device.mgt.core.apimgt.extension.rest.api;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.APIInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Application;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.ApplicationKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.KeyManager;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Scopes;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Subscription;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.OAuthClientResponse;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.constants.Constants;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.OAuthClientException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.internal.APIManagerServiceDataHolder;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class ConsumerRESTAPIServicesImpl implements ConsumerRESTAPIServices {

    private static final Log log = LogFactory.getLog(ConsumerRESTAPIServicesImpl.class);
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson gson = new Gson();
    private static final String host = System.getProperty(Constants.IOT_CORE_HOST);
    private static final String port = System.getProperty(Constants.IOT_CORE_HTTPS_PORT);
    private static final String endPointPrefix = Constants.HTTPS_PROTOCOL + Constants.SCHEME_SEPARATOR + host
            + Constants.COLON + port;
    private static final IOAuthClientService client =
            APIManagerServiceDataHolder.getInstance().getIoAuthClientService();

    @Override
    public Application[] getAllApplications(String appName)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String getAllApplicationsUrl = endPointPrefix + Constants.APPLICATIONS_API + "?query=" + appName;

        Request.Builder builder = new Request.Builder();
        builder.url(getAllApplicationsUrl);
        builder.get();
        Request request = builder.build();

        try {
            OAuthClientResponse response = client.execute(request);
            JSONArray applicationList = (JSONArray) new JSONObject(response.getBody()).get("list");
            return gson.fromJson(applicationList.toString(), Application[].class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while retrieving applications for application name : [ " + appName + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public Application getDetailsOfAnApplication(String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String getDetailsOfAPPUrl = endPointPrefix + Constants.APPLICATIONS_API + Constants.SLASH + applicationId;

        Request.Builder builder = new Request.Builder();
        builder.url(getDetailsOfAPPUrl);
        builder.get();
        Request request = builder.build();

        try {
            OAuthClientResponse response = client.execute(request);
            return gson.fromJson(response.getBody(), Application.class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while retrieving details of application ID : [ " + applicationId + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public Application createApplication(Application application)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String getAllScopesUrl = endPointPrefix + Constants.APPLICATIONS_API;

        JSONArray groups = new JSONArray();
        JSONArray subscriptionScope = new JSONArray();

        if (application.getGroups() != null && application.getSubscriptionScopes() != null) {
            for (String string : application.getGroups()) {
                groups.put(string);
            }
            for (Scopes string : application.getSubscriptionScopes()) {
                subscriptionScope.put(string);
            }
        }

        JSONObject applicationInfo = new JSONObject();
        applicationInfo.put("name", application.getName());
        applicationInfo.put("throttlingPolicy", application.getThrottlingPolicy());
        applicationInfo.put("description", application.getDescription());
        applicationInfo.put("tokenType", application.getTokenType());
        applicationInfo.put("groups", groups);
        applicationInfo.put("attributes", new JSONObject());
        applicationInfo.put("subscriptionScopes", subscriptionScope);

        RequestBody requestBody = RequestBody.create(JSON, applicationInfo.toString());

        Request.Builder builder = new Request.Builder();
        builder.url(getAllScopesUrl);
        builder.post(requestBody);
        Request request = builder.build();

        try {
            OAuthClientResponse response = client.execute(request);
            return gson.fromJson(response.getBody(), Application.class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while creating application : [ " + application.getName() + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public Boolean deleteApplication(String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String deleteScopesUrl = endPointPrefix + Constants.APPLICATIONS_API + Constants.SLASH + applicationId;

        Request.Builder builder = new Request.Builder();
        builder.url(deleteScopesUrl);
        builder.delete();
        Request request = builder.build();

        try {
            OAuthClientResponse response = client.execute(request);
            return HttpStatus.SC_OK == response.getCode();
        } catch (OAuthClientException e) {
            String msg =
                    "Error occurred while deleting application associated with application ID : [ " + applicationId + "]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public Subscription[] getAllSubscriptions(String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String getAllScopesUrl = endPointPrefix + Constants.SUBSCRIPTION_API + "?applicationId=" + applicationId +
                "&limit=1000";

        Request.Builder builder = new Request.Builder();
        builder.url(getAllScopesUrl);
        builder.get();
        Request request = builder.build();

        try {
            OAuthClientResponse response = client.execute(request);
            JSONArray subscriptionList = (JSONArray) new JSONObject(response.getBody()).get("list");
            return gson.fromJson(subscriptionList.toString(), Subscription[].class);
        } catch (OAuthClientException e) {
            String msg =
                    "Error occurred while retrieving all subscription of application associated with application ID :" +
                            " [ " + applicationId + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public APIInfo[] getAllApis(Map<String, String> queryParams, Map<String, String> headerParams)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        StringBuilder getAPIsURL = new StringBuilder(endPointPrefix + Constants.DEV_PORTAL_API);

        for (Map.Entry<String, String> query : queryParams.entrySet()) {
            getAPIsURL.append(Constants.AMPERSAND).append(query.getKey()).append(Constants.EQUAL).append(query.getValue());
        }

        Request.Builder builder = new Request.Builder();
        builder.url(getAPIsURL.toString());

        for (Map.Entry<String, String> header : headerParams.entrySet()) {
            builder.addHeader(header.getKey(), header.getValue());
        }
        builder.get();
        Request request = builder.build();

        try {
            OAuthClientResponse response = client.execute(request);
            JSONArray apiList = (JSONArray) new JSONObject(response.getBody()).get("list");
            return gson.fromJson(apiList.toString(), APIInfo[].class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while retrieving all APIs";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public Subscription createSubscription(Subscription subscription)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String createSubscriptionUrl = endPointPrefix + Constants.SUBSCRIPTION_API;

        JSONObject subscriptionObject = new JSONObject();
        subscriptionObject.put("applicationId", subscription.getApplicationId());
        subscriptionObject.put("apiId", subscription.getApiId());
        subscriptionObject.put("throttlingPolicy", subscription.getThrottlingPolicy());
        subscriptionObject.put("requestedThrottlingPolicy", subscription.getRequestedThrottlingPolicy());

        RequestBody requestBody = RequestBody.create(JSON, subscriptionObject.toString());

        Request.Builder builder = new Request.Builder();
        builder.url(createSubscriptionUrl);
        builder.post(requestBody);
        Request request = builder.build();

        try {
            OAuthClientResponse response = client.execute(request);
            return gson.fromJson(response.getBody(), Subscription.class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while adding subscription : [ " + subscription.getSubscriptionId() + " ] for" +
                    " application associated with application ID : [ " + subscription.getApplicationId() + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public Subscription[] createSubscriptions(List<Subscription> subscriptions)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String createSubscriptionsUrl = endPointPrefix + Constants.SUBSCRIPTION_API + "/multiple";

        String subscriptionsList = gson.toJson(subscriptions);
        RequestBody requestBody = RequestBody.create(JSON, subscriptionsList);

        Request.Builder builder = new Request.Builder();
        builder.url(createSubscriptionsUrl);

        builder.post(requestBody);
        Request request = builder.build();

        try {
            OAuthClientResponse response = client.execute(request);
            return gson.fromJson(response.getBody(), Subscription[].class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while adding subscriptions";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public ApplicationKey generateApplicationKeys(String applicationId, String keyManager, String validityTime,
                                                  String keyType, String grantTypesToBeSupported, String callbackUrl)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String generateApplicationKeysUrl = endPointPrefix + Constants.APPLICATIONS_API + Constants.SLASH +
                applicationId + "/generate-keys";

        JSONObject keyInfo = new JSONObject();
        keyInfo.put("keyType", keyType);
        keyInfo.put("keyManager", keyManager);
        keyInfo.put("grantTypesToBeSupported", grantTypesToBeSupported.split(Constants.SPACE));
        if (!StringUtils.isEmpty(callbackUrl)) {
            keyInfo.put("callbackUrl", callbackUrl);
        }
        keyInfo.put("validityTime", validityTime);

        RequestBody requestBody = RequestBody.create(JSON, keyInfo.toString());

        Request.Builder builder = new Request.Builder();
        builder.url(generateApplicationKeysUrl);
        builder.post(requestBody);
        Request request = builder.build();

        try {
            OAuthClientResponse response = client.execute(request);
            return gson.fromJson(response.getBody(), ApplicationKey.class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while generating application keys for application associated with " +
                    "application ID : [ " + applicationId + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public ApplicationKey mapApplicationKeys(String consumerKey, String consumerSecret, Application application,
                                             String keyManager, String keyType)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String getAllScopesUrl = endPointPrefix + Constants.APPLICATIONS_API + Constants.SLASH +
                application.getApplicationId() + "/map-keys";

        JSONObject payload = new JSONObject();
        payload.put("consumerKey", consumerKey);
        payload.put("consumerSecret", consumerSecret);
        payload.put("keyManager", keyManager);
        payload.put("keyType", keyType);

        RequestBody requestBody = RequestBody.create(JSON, payload.toString());

        Request.Builder builder = new Request.Builder();
        builder.url(getAllScopesUrl);
        builder.post(requestBody);
        Request request = builder.build();

        try {
            OAuthClientResponse response = client.execute(request);
            return gson.fromJson(response.getBody(), ApplicationKey.class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while mapping application keys";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public ApplicationKey getKeyDetails(String applicationId, String keyMapId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String getKeyDetails = endPointPrefix + Constants.APPLICATIONS_API + Constants.SLASH + applicationId +
                "/oauth-keys/" + keyMapId;

        Request.Builder builder = new Request.Builder();
        builder.url(getKeyDetails);
        builder.get();
        Request request = builder.build();

        try {
            OAuthClientResponse response = client.execute(request);
            return gson.fromJson(response.getBody(), ApplicationKey.class);
        } catch (OAuthClientException e) {
            String msg =
                    "Error occurred while retrieving key details of application associated with application ID : [ " + applicationId + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public ApplicationKey[] getAllKeys(String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String getKeyDetails = endPointPrefix + Constants.APPLICATIONS_API + Constants.SLASH + applicationId +
                "/oauth-keys";

        Request.Builder builder = new Request.Builder();
        builder.url(getKeyDetails);
        builder.get();
        Request request = builder.build();

        try {
            OAuthClientResponse response = client.execute(request);
            JSONArray keyList = (JSONArray) new JSONObject(response.getBody()).get("list");
            return gson.fromJson(keyList.toString(), ApplicationKey[].class);
        } catch (OAuthClientException e) {
            String msg =
                    "Error occurred while retrieving key details of application associated with application ID : [ " + applicationId + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public ApplicationKey updateGrantType(String applicationId, String keyMapId, List<String> supportedGrantTypes, String callbackUrl)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String getKeyDetails = endPointPrefix + Constants.APPLICATIONS_API + Constants.SLASH + applicationId +
                "/oauth-keys/" + keyMapId;

        Request.Builder builder = new Request.Builder();
        builder.url(getKeyDetails);

        JSONObject payload = new JSONObject();
        payload.put("supportedGrantTypes", supportedGrantTypes);
        payload.put("callbackUrl", (callbackUrl != null ? callbackUrl : ""));

        RequestBody requestBody = RequestBody.create(JSON, payload.toString());

        builder.put(requestBody);
        Request request = builder.build();

        try {
            OAuthClientResponse response = client.execute(request);
            return gson.fromJson(response.getBody(), ApplicationKey.class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while updating the grant types of the application associated with " +
                    "application ID : [ " + applicationId + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public KeyManager[] getAllKeyManagers()
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String getAllKeyManagersUrl = endPointPrefix + Constants.KEY_MANAGERS_API;

        Request.Builder builder = new Request.Builder();
        builder.url(getAllKeyManagersUrl);
        builder.get();
        Request request = builder.build();

        try {
            OAuthClientResponse response = client.execute(request);
            JSONArray keyManagerList = (JSONArray) new JSONObject(response.getBody()).get("list");
            return gson.fromJson(keyManagerList.toString(), KeyManager[].class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while retrieving all key managers";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }
}
