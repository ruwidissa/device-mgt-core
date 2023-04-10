package io.entgra.devicemgt.apimgt.extension.publisher.api;

import com.google.gson.JsonObject;
import io.entgra.devicemgt.apimgt.extension.publisher.api.bean.RegistrationProfile;
import io.entgra.devicemgt.apimgt.extension.publisher.api.constants.Constants;
import io.entgra.devicemgt.apimgt.extension.publisher.api.dto.APIApplicationKey;
import io.entgra.devicemgt.apimgt.extension.publisher.api.dto.AccessTokenInfo;
import io.entgra.devicemgt.apimgt.extension.publisher.api.exceptions.APIApplicationServicesException;
import io.entgra.devicemgt.apimgt.extension.publisher.api.exceptions.BadRequestException;
import io.entgra.devicemgt.apimgt.extension.publisher.api.util.PublisherRESTAPIUtil;
import okhttp3.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.ssl.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class APIApplicationServicesImpl implements APIApplicationServices {

    private static final Log log = LogFactory.getLog(APIApplicationServicesImpl.class);
<<<<<<< HEAD
    private static final OkHttpClient client = getOkHttpClient();

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
                .hostnameVerifier((hostname, sslSession) -> true).build();
    }
=======
//    private final OkHttpClient client;

//    public APIApplicationServicesImpl() {
//        this.client = new OkHttpClient();
//    }
>>>>>>> e76624c1de (Add exceptions)

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
    @Override
    public APIApplicationKey createAndRetrieveApplicationCredentials()
            throws APIApplicationServicesException, BadRequestException {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("callbackUrl",Constants.EMPTY_STRING);
        jsonObject.addProperty("clientName",Constants.CLIENT_NAME);
        jsonObject.addProperty("grantType",Constants.EMPTY_STRING);
        jsonObject.addProperty("owner",Constants.OWNER);
        jsonObject.addProperty("saasApp",true);

        MediaType jsonMediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(jsonMediaType, jsonObject.toString());

        String keyManagerEndpoint = "https://localhost:9443/client-registration/v0.17/register";

        Request request = new Request.Builder()
                .url(keyManagerEndpoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Credentials.basic("admin", "admin"))
                .post(requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            System.out.println(request);
            System.out.println("---------------------------");
            System.out.println(response);

//            JSONObject responseObj = new JSONObject(Objects.requireNonNull(response.body()).string());

<<<<<<< HEAD
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
//            //ToDo: Remove hardcoded value
//            String basicAuth = getBase64Encode("admin", "admin");
//            request.setHeader(HttpHeaders.AUTHORIZATION, Constants.AUTHORIZATION_HEADER_VALUE_PREFIX + basicAuth);
//            request.setHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON);
//
//            HttpResponse httpResponse = httpclient.execute(request);
//
//            if (httpResponse != null) {
//                String response = PublisherRESTAPIUtil.getResponseString(httpResponse);
//                try {
//                    if(response != null){
//                        JSONParser jsonParser = new JSONParser();
//                        JSONObject jsonPayload = (JSONObject) jsonParser.parse(response);
//                        APIApplicationKey apiApplicationKey = new APIApplicationKey();
//                        apiApplicationKey.setClientId((String) jsonPayload.get(Constants.CLIENT_ID));
//                        apiApplicationKey.setClientSecret((String) jsonPayload.get(Constants.CLIENT_SECRET));
//                        return apiApplicationKey;
//                    } else {
//                        return null;
//                    }
//                } catch (ParseException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//        } catch (IOException | NoSuchAlgorithmException | KeyStoreException |
//                 KeyManagementException e) {
//            log.error("failed to call http client.", e);
//        }
        return null;
=======
            String jsonString = registrationProfile.toJSON();
            StringEntity entity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
            request.setEntity(entity);

            //ToDo: Remove hardcoded value
            String basicAuth = getBase64Encode("admin", "admin");
            request.setHeader(HttpHeaders.AUTHORIZATION, Constants.AUTHORIZATION_HEADER_VALUE_PREFIX + basicAuth);
            request.setHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON);

            HttpResponse httpResponse = httpclient.execute(request);

            if (httpResponse != null) {
                String response = PublisherRESTAPIUtil.getResponseString(httpResponse);
                try {
                    if(response != null){
                        JSONParser jsonParser = new JSONParser();
                        JSONObject jsonPayload = (JSONObject) jsonParser.parse(response);
                        APIApplicationKey apiApplicationKey = new APIApplicationKey();
                        apiApplicationKey.setClientId((String) jsonPayload.get(Constants.CLIENT_ID));
                        apiApplicationKey.setClientSecret((String) jsonPayload.get(Constants.CLIENT_SECRET));
                        return apiApplicationKey;
                    } else {
                        String msg = "Request payload is null. Please verify the request payload.";
                        log.error(msg);
                        throw new BadRequestException(msg);
                    }
                } catch (ParseException e) {
                    throw new APIApplicationServicesException("Error when parsing the response " + response, e);
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new APIApplicationServicesException("Error when reading the response from buffer.", e);
        } catch (KeyStoreException e) {
            throw new APIApplicationServicesException("Failed loading the keystore.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new APIApplicationServicesException("No such algorithm found when loading the ssl socket", e);
        } catch (KeyManagementException e) {
            throw new APIApplicationServicesException("Failed setting up the ssl http client.", e);
        }
>>>>>>> e76624c1de (Add exceptions)
    }

    @Override
    public AccessTokenInfo generateAccessTokenFromRegisteredApplication(String consumerKey, String consumerSecret)
        throws APIApplicationServicesException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(Constants.GRANT_TYPE_PARAM_NAME, Constants.PASSWORD_GRANT_TYPE));
        //ToDo: Remove hardcoded value
        params.add(new BasicNameValuePair(Constants.PASSWORD_GRANT_TYPE_USERNAME, "admin"));
        params.add(new BasicNameValuePair(Constants.PASSWORD_GRANT_TYPE_PASSWORD, "admin"));
        params.add(new BasicNameValuePair(Constants.SCOPE_PARAM_NAME, Constants.SCOPES));
        return getToken(params, consumerKey, consumerSecret);
    }

    @Override
    public AccessTokenInfo generateAccessTokenFromRefreshToken(String refreshToken, String consumerKey, String consumerSecret)
            throws APIApplicationServicesException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(Constants.GRANT_TYPE_PARAM_NAME, Constants.REFRESH_TOKEN_GRANT_TYPE));
        params.add(new BasicNameValuePair(Constants.REFRESH_TOKEN_GRANT_TYPE_PARAM_NAME, refreshToken));
        params.add(new BasicNameValuePair(Constants.SCOPE_PARAM_NAME, Constants.SCOPES));
        return getToken(params, consumerKey, consumerSecret);
    }

    public AccessTokenInfo getToken(List<NameValuePair> nameValuePairs, String clientId, String clientSecret)
            throws APIApplicationServicesException{

        String response = null;
        try {
            URL url = new URL("https://localhost:9443/oauth2/token");
            HttpClient httpclient = PublisherRESTAPIUtil.getHttpClient(url.getProtocol());
            HttpPost request = new HttpPost(url.toString());

            request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + getBase64Encode(clientId, clientSecret));
            request.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse httpResponse = httpclient.execute(request);
            response = PublisherRESTAPIUtil.getResponseString(httpResponse);
            if (log.isDebugEnabled()) {
                log.debug(response);
            }
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
            AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
            String accessToken = (String) jsonObject.get(Constants.ACCESS_TOKEN_GRANT_TYPE_PARAM_NAME);
            if (accessToken != null && !accessToken.isEmpty()){
                accessTokenInfo.setAccessToken(accessToken);
                accessTokenInfo.setRefreshToken((String) jsonObject.get(Constants.REFRESH_TOKEN_GRANT_TYPE_PARAM_NAME));
                accessTokenInfo.setExpiresIn((Long) jsonObject.get(Constants.OAUTH_EXPIRES_IN));
                accessTokenInfo.setTokenType((String) jsonObject.get(Constants.OAUTH_TOKEN_TYPE));
                accessTokenInfo.setScope((String) jsonObject.get(Constants.OAUTH_TOKEN_SCOPE));
            }
            return accessTokenInfo;

        } catch (IOException e) {
            throw new APIApplicationServicesException("Error when reading the response from buffer.", e);
        } catch (KeyStoreException e) {
            throw new APIApplicationServicesException("Failed loading the keystore.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new APIApplicationServicesException("No such algorithm found when loading the ssl socket", e);
        } catch (ParseException e) {
            throw new APIApplicationServicesException("Error when parsing the response " + response, e);
        } catch (KeyManagementException e) {
            throw new APIApplicationServicesException("Failed setting up the ssl http client.", e);
        }
    }

    static String getBase64Encode(String key, String value) {
        return new String(Base64.encodeBase64((key + ":" + value).getBytes()));
    }
}
