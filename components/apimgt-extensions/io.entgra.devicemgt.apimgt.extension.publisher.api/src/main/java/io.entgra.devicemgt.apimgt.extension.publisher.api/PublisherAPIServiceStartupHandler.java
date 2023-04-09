package io.entgra.devicemgt.apimgt.extension.publisher.api;

import io.entgra.devicemgt.apimgt.extension.publisher.api.dto.AccessTokenInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.ServerStartupObserver;

public class PublisherAPIServiceStartupHandler implements ServerStartupObserver {

    private static final Log log = LogFactory.getLog(PublisherAPIServiceStartupHandler.class);
    private PublisherRESTAPIServices publisherRESTAPIServices;
    private AccessTokenInfo accessTokenInfo;

    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {

//        String cid = null;
//        String cS = null;
//        String token = null;
//        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
//
//        try {
//            URL url = new URL("https://localhost:9443/client-registration/v0.17/register");
//            HttpClient httpclient = PublisherRESTAPIUtil.getHttpClient(url.getProtocol());
//            HttpPost request = new HttpPost(url.toString());
//
//            RegistrationProfile registrationProfile = new RegistrationProfile();
//            registrationProfile.setCallbackUrl(Constants.EMPTY_STRING);
//            registrationProfile.setClientName(Constants.CLIENT_NAME);
//            registrationProfile.setOwner(Constants.OWNER);
//            registrationProfile.setGrantType(Constants.GRANT_TYPE);
//            registrationProfile.setIsSaasApp(true);
//
//            String jsonString = registrationProfile.toJSON();
//            StringEntity entity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
//            request.setEntity(entity);
//
//            String basicAuth = PublisherRESTAPIServices.getBase64Encode("admin", "admin");
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
//                        String clientId = (String) jsonPayload.get(Constants.CLIENT_ID);
//                        String clientSecret = (String) jsonPayload.get(Constants.CLIENT_SECRET);
//                        cid = clientId;
//                        cS = clientSecret;
//                    }
//                } catch (ParseException e) {
//                    throw new RuntimeException(e);
//                }
//
//                System.out.println(response);
//            }
//            System.out.println(httpResponse.getStatusLine().getStatusCode());
//
//
//        } catch (IOException | NoSuchAlgorithmException | KeyStoreException |
//                 KeyManagementException e) {
//            log.error("failed to call http client.", e);
//        }
//
//
//        String response = null;
//
//        List<NameValuePair> params = new ArrayList<>();
//        params.add(new BasicNameValuePair(Constants.GRANT_TYPE_PARAM_NAME, Constants.PASSWORD_GRANT_TYPE));
//        params.add(new BasicNameValuePair(Constants.PASSWORD_GRANT_TYPE_USERNAME, "admin"));
//        params.add(new BasicNameValuePair(Constants.PASSWORD_GRANT_TYPE_PASSWORD, "admin"));
//        params.add(new BasicNameValuePair(Constants.SCOPE_PARAM_NAME, Constants.SCOPES));
//        try {
//            URL url = new URL("https://localhost:9443/oauth2/token");
//            HttpClient httpclient = PublisherRESTAPIUtil.getHttpClient(url.getProtocol());
//            HttpPost request = new HttpPost(url.toString());
//
//            request.addHeader("Authorization", "Basic " + PublisherRESTAPIServices.getBase64Encode(cid, cS));
//            request.addHeader("Content-Type", "application/x-www-form-urlencoded");
//            request.setEntity(new UrlEncodedFormEntity(params));
//            HttpResponse httpResponse = httpclient.execute(request);
//            response = PublisherRESTAPIUtil.getResponseString(httpResponse);
//            JSONParser jsonParser = new JSONParser();
//            JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
//            token = (String) jsonObject.get(Constants.ACCESS_TOKEN_GRANT_TYPE_PARAM_NAME);
//            if (token != null && !token.isEmpty()){
//                accessTokenInfo.setRefreshToken(token);
//                accessTokenInfo.setRefreshToken((String) jsonObject.get(Constants.REFRESH_TOKEN_GRANT_TYPE_PARAM_NAME));
//                accessTokenInfo.setExpiresIn((Long) jsonObject.get(Constants.OAUTH_EXPIRES_IN));
//                accessTokenInfo.setTokenType((String) jsonObject.get(Constants.OAUTH_TOKEN_TYPE));
//                accessTokenInfo.setScope((String) jsonObject.get(Constants.OAUTH_TOKEN_SCOPE));
//            }
//            System.out.println(accessTokenInfo);
//
//        } catch (IOException | KeyStoreException | NoSuchAlgorithmException |
//                 KeyManagementException| ParseException e) {
//            throw new RuntimeException(e);
//        }
//
//        String key = "perm:sms-handler:view-configuration";
//        String value = new String(Base64.encodeBase64((key).getBytes())).replace("=", "");
//
//
//        String getScopeUrl = "https://localhost:9443/api/am/publisher/v2/scopes/" + value;
//        try {
//            URL url = new URL(getScopeUrl);
//            HttpClient httpclient = PublisherRESTAPIUtil.getHttpClient(url.getProtocol());
//            HttpHead request = new HttpHead(url.toString());
//
//            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
//            HttpResponse httpResponse = httpclient.execute(request);
//
//            String code = String.valueOf(httpResponse.getStatusLine().getStatusCode());
//            System.out.println(code);
//
//        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
//            throw new RuntimeException(e);
//        }
//
//        String updateScopeUrl = "https://localhost:9443/api/am/publisher/v2/scopes/" + "27fce6f1-6741-4ad5-b700-a56427fd3dbb";
//        try {
//            URL url = new URL(updateScopeUrl);
//            HttpClient httpclient = PublisherRESTAPIUtil.getHttpClient(url.getProtocol());
//            HttpPut request = new HttpPut(url.toString());
//
//            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
//            request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
//
//
//            String jsonString = "{\"name\": \"" + "name" + "\",\"displayName\": \"" + "displayname" +
//                    "\", \"description\": \"" + "description" + "\"," + "\"bindings\": [" +
//                    "\"Internal/devicemgt-user\"]}";
//            StringEntity entity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
//            request.setEntity(entity);
//
//            HttpResponse httpResponse = httpclient.execute(request);
//            int code = httpResponse.getStatusLine().getStatusCode();
//            System.out.println(code);
//        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
//            throw new RuntimeException(e);
//        }


//        publisherRESTAPIServices = new PublisherRESTAPIServices();
//        publisherRESTAPIServices.isSharedScopeNameExists("perm:sms-handler:view-configuration");

    }
}
