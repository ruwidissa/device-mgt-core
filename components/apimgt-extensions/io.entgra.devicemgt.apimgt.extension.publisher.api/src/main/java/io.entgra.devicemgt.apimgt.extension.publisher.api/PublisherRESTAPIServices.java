package io.entgra.devicemgt.apimgt.extension.publisher.api;

import io.entgra.devicemgt.apimgt.extension.publisher.api.constants.Constants;
import io.entgra.devicemgt.apimgt.extension.publisher.api.dto.APIApplicationKey;
import io.entgra.devicemgt.apimgt.extension.publisher.api.dto.AccessTokenInfo;
import io.entgra.devicemgt.apimgt.extension.publisher.api.exceptions.APIApplicationServicesException;
import io.entgra.devicemgt.apimgt.extension.publisher.api.exceptions.BadRequestException;
import okhttp3.*;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.ssl.Base64;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.model.Scope;

import java.io.IOException;
import static io.entgra.devicemgt.apimgt.extension.publisher.api.APIApplicationServicesImpl.getOkHttpClient;

public class PublisherRESTAPIServices {
    private static final Log log = LogFactory.getLog(PublisherRESTAPIServices.class);
    private static final OkHttpClient client = getOkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public boolean isSharedScopeNameExists(APIApplicationKey apiApplicationKey,  AccessTokenInfo accessTokenInfo, String key)
            throws APIApplicationServicesException, BadRequestException {

        String keyValue = new String(Base64.encodeBase64((key).getBytes())).replace("=", "");
        String getScopeUrl = "https://localhost:9443/api/am/publisher/v2/scopes/" + keyValue;

        Request request = new Request.Builder()
                .url(getScopeUrl)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, "Bearer " + accessTokenInfo.getAccessToken())
                .head()
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == HttpStatus.SC_OK){
                return true;
            }else if(HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefreshToken(), apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret() );
                //max attempt count
                return isSharedScopeNameExists(apiApplicationKey,refreshedAccessToken, key);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()){
                log.info(response);
                throw new BadRequestException(response.toString());
            } else {
                return false;
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            throw new APIApplicationServicesException(msg);
        }
    }

    public boolean updateSharedScope(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, Scope scope)
            throws APIApplicationServicesException, BadRequestException {

        String updateScopeUrl = "https://localhost:9443/api/am/publisher/v2/scopes/" + scope.getId();

        JSONObject setScope = new JSONObject();
        setScope.put("name", scope.getKey());
        setScope.put("displayName", scope.getName());
        setScope.put("description", scope.getDescription());
        setScope.put("bindings", scope.getRoles());


        RequestBody requestBody = RequestBody.Companion.create(setScope.toString(), JSON);
        Request request = new Request.Builder()
                .url(updateScopeUrl)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, "Bearer " + accessTokenInfo.getAccessToken())
                .put(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.code() == HttpStatus.SC_OK) {
                return true;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefreshToken(), apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                return updateSharedScope(apiApplicationKey, refreshedAccessToken, scope);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                log.info(response);
                throw new BadRequestException(response.toString());
            } else {
                return false;
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            throw new APIApplicationServicesException(msg);
        }
    }
}
