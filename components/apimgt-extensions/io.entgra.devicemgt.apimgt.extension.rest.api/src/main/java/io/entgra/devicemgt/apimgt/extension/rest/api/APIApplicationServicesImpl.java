package io.entgra.devicemgt.apimgt.extension.rest.api;

import com.google.gson.Gson;
import io.entgra.devicemgt.apimgt.extension.rest.api.dto.APIApplicationKey;
import org.json.JSONObject;
import io.entgra.devicemgt.apimgt.extension.rest.api.constants.Constants;
import io.entgra.devicemgt.apimgt.extension.rest.api.dto.AccessTokenInfo;
import io.entgra.devicemgt.apimgt.extension.rest.api.exceptions.APIApplicationServicesException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;
import okhttp3.Credentials;
import okhttp3.ConnectionPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class APIApplicationServicesImpl implements APIApplicationServices {

    private static final Log log = LogFactory.getLog(APIApplicationServicesImpl.class);
    private static final OkHttpClient client = getOkHttpClient();
    private static final Gson gson = new Gson();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    String msg = null;
    //    private ApiApplicationConfigurations ApplicationConfig = null;

    @Override
    public APIApplicationKey createAndRetrieveApplicationCredentials()
            throws APIApplicationServicesException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("callbackUrl", Constants.EMPTY_STRING);
        jsonObject.put("clientName", Constants.CLIENT_NAME);
        jsonObject.put("grantType", Constants.GRANT_TYPE);
        jsonObject.put("owner", Constants.OWNER);
        jsonObject.put("saasApp", true);

        RequestBody requestBody = RequestBody.Companion.create(jsonObject.toString(), JSON);
        String applicationEndpoint = "https://localhost:9443/client-registration/v0.17/register";

        Request request = new Request.Builder()
                .url(applicationEndpoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Credentials.basic("admin", "admin"))
                .post(requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return gson.fromJson(response.body().string(), APIApplicationKey.class);
        } catch (IOException e) {
            msg = "Error occurred while processing the response";
            log.error(msg);
            throw new APIApplicationServicesException(msg);
        }
    }

    @Override
    public AccessTokenInfo generateAccessTokenFromRegisteredApplication(String consumerKey, String consumerSecret)
            throws APIApplicationServicesException {
        JSONObject params = new JSONObject();
        params.put(Constants.GRANT_TYPE_PARAM_NAME, Constants.PASSWORD_GRANT_TYPE);
        //ToDo: Remove hardcoded value
        params.put(Constants.PASSWORD_GRANT_TYPE_USERNAME, "admin");
        params.put(Constants.PASSWORD_GRANT_TYPE_PASSWORD, "admin");
        params.put(Constants.SCOPE_PARAM_NAME, Constants.SCOPES);
        return getToken(params, consumerKey, consumerSecret);
    }

    @Override
    public AccessTokenInfo generateAccessTokenFromRefreshToken(String refreshToken, String consumerKey, String consumerSecret)
            throws APIApplicationServicesException {

        JSONObject params = new JSONObject();
        params.put(Constants.GRANT_TYPE_PARAM_NAME, Constants.REFRESH_TOKEN_GRANT_TYPE);
        //ToDo: Remove hardcoded value
        params.put(Constants.REFRESH_TOKEN_GRANT_TYPE_PARAM_NAME, refreshToken);
        params.put(Constants.SCOPE_PARAM_NAME, Constants.SCOPES);
        return getToken(params, consumerKey, consumerSecret);
    }

    public AccessTokenInfo getToken(JSONObject nameValuePairs, String clientId, String clientSecret)
            throws APIApplicationServicesException {

        RequestBody requestBody = RequestBody.Companion.create(nameValuePairs.toString(), JSON);
        String tokenEndPoint = "https://localhost:9443/oauth2/token";

        Request request = new Request.Builder()
                .url(tokenEndPoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Credentials.basic(clientId, clientSecret))
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            return gson.fromJson(response.body().string(), AccessTokenInfo.class);
        } catch (IOException e) {
            msg = "Error occurred while processing the response";
            log.error(msg);
            throw new APIApplicationServicesException(msg);
        }
    }

    protected static OkHttpClient getOkHttpClient() {
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
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(500, 500, TimeUnit.SECONDS))
                .sslSocketFactory(getSimpleTrustedSSLSocketFactory(), trustAllCerts)
                .hostnameVerifier((hostname, sslSession) -> true).build();
        return okHttpClient;
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
}
