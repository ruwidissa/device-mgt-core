package io.entgra.devicemgt.apimgt.extension.publisher.api;

import io.entgra.devicemgt.apimgt.extension.publisher.api.constants.Constants;
import io.entgra.devicemgt.apimgt.extension.publisher.api.dto.APIApplicationKey;
import io.entgra.devicemgt.apimgt.extension.publisher.api.dto.AccessTokenInfo;
import io.entgra.devicemgt.apimgt.extension.publisher.api.util.PublisherRESTAPIUtil;
import io.entgra.devicemgt.apimgt.extension.publisher.api.util.ScopeUtils;
import org.apache.axis2.databinding.types.xsd._boolean;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.ssl.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.wso2.carbon.apimgt.api.model.Scope;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class PublisherRESTAPIServices {
    private static final Log log = LogFactory.getLog(PublisherRESTAPIServices.class);

//    private String clientId;
//    private String clientSecret;
//    private String accessToken;
//
//    public AccessTokenInfo registerApplication() {
//
//        try {
//            URL url = new URL("https://localhost:9443/client-registration/v0.17/register");
//            HttpClient httpclient = PublisherRESTAPIUtil.getHttpClient(url.getProtocol());
//            HttpPost request = new HttpPost(url.toString());
//
//            RegistrationProfile registrationProfile = new RegistrationProfile();
//            registrationProfile.setCallbackUrl(Constants.EMPTY_STRING);
//            registrationProfile.setClientName(Constants.CLIENT_NAME);
//            registrationProfile.setGrantType(Constants.GRANT_TYPE);
//            registrationProfile.setOwner(Constants.OWNER);
//            registrationProfile.setIsSaasApp(true);
//
//            String jsonString = registrationProfile.toJSON();
//            StringEntity entity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
//            request.setEntity(entity);
//
//            String basicAuth = getBase64Encode("admin", "admin");
//
//            request.setHeader("Authorization", "Basic " + basicAuth);
//            request.setHeader("Content-Type", "application/json");
//
//            HttpResponse httpResponse = httpclient.execute(request);
//
//            if (httpResponse != null) {
//
//                String response = PublisherRESTAPIUtil.getResponseString(httpResponse);
//                try {
//                    if(response != null){
//                        JSONParser jsonParser = new JSONParser();
//                        JSONObject jsonPayload = (JSONObject) jsonParser.parse(response);
//                        clientId = (String) jsonPayload.get(Constants.CLIENT_ID);
//                        clientSecret = (String) jsonPayload.get(Constants.CLIENT_SECRET);
//                    }
//                } catch (ParseException e) {
//                    throw new RuntimeException(e);
//                }
//                System.out.println(response);
//            }
//            System.out.println(httpResponse.getStatusLine().getStatusCode());
//
//        } catch (IOException | NoSuchAlgorithmException | KeyStoreException |
//                 KeyManagementException e) {
//            log.error("failed to call http client.", e);
//        }
//        return getAccessTokenFromRegisteredApplication(clientId, clientSecret);
//
//    }
//
//    public AccessTokenInfo getAccessTokenFromRegisteredApplication(String consumerKey, String consumerSecret) {
//        List<NameValuePair> params = new ArrayList<>();
//        params.add(new BasicNameValuePair(Constants.GRANT_TYPE_PARAM_NAME, Constants.PASSWORD_GRANT_TYPE));
//        params.add(new BasicNameValuePair(Constants.PASSWORD_GRANT_TYPE_USERNAME, "admin"));
//        params.add(new BasicNameValuePair(Constants.PASSWORD_GRANT_TYPE_PASSWORD, "admin"));
//        params.add(new BasicNameValuePair(Constants.SCOPE_PARAM_NAME, Constants.SCOPES));
//        return getToken(params, consumerKey, consumerSecret);
//    }
//
//    public AccessTokenInfo getToken(List<NameValuePair> nameValuePairs, String clientId, String clientSecret) {
//
//        String token = null;
//        String response = null;
//        try {
//            URL url = new URL("https://localhost:9443/oauth2/token");
//            HttpClient httpclient = PublisherRESTAPIUtil.getHttpClient(url.getProtocol());
//            HttpPost request = new HttpPost(url.toString());
//
//            request.addHeader("Authorization", "Basic " + getBase64Encode(clientId, clientSecret));
//            request.addHeader("Content-Type", "application/x-www-form-urlencoded");
//            request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//            HttpResponse httpResponse = httpclient.execute(request);
//            response = PublisherRESTAPIUtil.getResponseString(httpResponse);
//            JSONParser jsonParser = new JSONParser();
//            JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
//            AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
//            token = (String) jsonObject.get(Constants.ACCESS_TOKEN_GRANT_TYPE_PARAM_NAME);
//            if (token != null && !token.isEmpty()){
//                accessTokenInfo.setRefreshToken(token);
//                accessTokenInfo.setRefreshToken((String) jsonObject.get(Constants.REFRESH_TOKEN_GRANT_TYPE_PARAM_NAME));
//                accessTokenInfo.setExpiresIn((Long) jsonObject.get(Constants.OAUTH_EXPIRES_IN));
//                accessTokenInfo.setTokenType((String) jsonObject.get(Constants.OAUTH_TOKEN_TYPE));
//                accessTokenInfo.setScope((String) jsonObject.get(Constants.OAUTH_TOKEN_SCOPE));
//            }
//            accessToken = token;
//            return accessTokenInfo;
//
//        } catch (IOException | KeyStoreException | NoSuchAlgorithmException |
//                 KeyManagementException| ParseException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public boolean isSharedScopeNameExists(APIApplicationKey apiApplicationKey,  AccessTokenInfo accessTokenInfo, String key){

        String keyValue = new String(Base64.encodeBase64((key).getBytes())).replace("=", "");
        String getScopeUrl = "https://localhost:9443/api/am/publisher/v2/scopes/" + keyValue;
        try {
            URL url = new URL(getScopeUrl);
            HttpClient httpclient = PublisherRESTAPIUtil.getHttpClient(url.getProtocol());
            HttpHead request = new HttpHead(url.toString());

            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenInfo.getAccessToken());
            HttpResponse httpResponse = httpclient.execute(request);

            if (HttpStatus.SC_OK == httpResponse.getStatusLine().getStatusCode()){
                return true;
            } else if(HttpStatus.SC_UNAUTHORIZED == httpResponse.getStatusLine().getStatusCode()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefreshToken(), apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret() );
                return isSharedScopeNameExists(apiApplicationKey,refreshedAccessToken, key);
            } else{
                return false;
            }

        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateSharedScope(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, Scope scope){

//        String keyValue = new String(Base64.encodeBase64((scope.getKey()).getBytes())).replace("=", "");
//        String updateScopeUrl = "https://localhost:9443/api/am/publisher/v2/scopes/" + keyValue;
        String updateScopeUrl = "https://localhost:9443/api/am/publisher/v2/scopes/" + scope.getId();
        try {
            URL url = new URL(updateScopeUrl);
            HttpClient httpclient = PublisherRESTAPIUtil.getHttpClient(url.getProtocol());
            HttpPut request = new HttpPut(url.toString());

            request.setHeader(HttpHeaders.AUTHORIZATION, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER +
                    accessTokenInfo.getAccessToken());
            request.setHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON);

            ScopeUtils setScope = new ScopeUtils();
            setScope.setKey(scope.getKey());
            setScope.setName(scope.getName());
            setScope.setDescription(scope.getDescription());
            setScope.setRoles(scope.getRoles());
            String jsonString = setScope.toJSON();
            StringEntity entity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
            request.setEntity(entity);

            HttpResponse httpResponse = httpclient.execute(request);
            if (HttpStatus.SC_OK != httpResponse.getStatusLine().getStatusCode() && HttpStatus.SC_UNAUTHORIZED == httpResponse.getStatusLine().getStatusCode()){
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo accessTokenInfo1 = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefreshToken(), apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret() );
                updateSharedScope(apiApplicationKey, accessTokenInfo1, scope);
            } else {
                String response = httpResponse.toString();
                log.info(response);
            }

        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }
//    static String getBase64Encode(String key, String value) {
//        return new String(Base64.encodeBase64((key + ":" + value).getBytes()));
//    }
}
