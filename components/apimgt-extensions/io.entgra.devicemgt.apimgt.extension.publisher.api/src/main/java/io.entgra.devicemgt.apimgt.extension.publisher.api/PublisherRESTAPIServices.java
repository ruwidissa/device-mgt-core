package io.entgra.devicemgt.apimgt.extension.publisher.api;

import io.entgra.devicemgt.apimgt.extension.publisher.api.constants.Constants;
import io.entgra.devicemgt.apimgt.extension.publisher.api.dto.APIApplicationKey;
import io.entgra.devicemgt.apimgt.extension.publisher.api.dto.AccessTokenInfo;
import io.entgra.devicemgt.apimgt.extension.publisher.api.exceptions.APIApplicationServicesException;
import io.entgra.devicemgt.apimgt.extension.publisher.api.exceptions.BadRequestException;
import io.entgra.devicemgt.apimgt.extension.publisher.api.util.PublisherRESTAPIUtil;
import io.entgra.devicemgt.apimgt.extension.publisher.api.util.ScopeUtils;
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

    public boolean isSharedScopeNameExists(APIApplicationKey apiApplicationKey,  AccessTokenInfo accessTokenInfo, String key)
            throws APIApplicationServicesException, BadRequestException {

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
            } else if (HttpStatus.SC_BAD_REQUEST == httpResponse.getStatusLine().getStatusCode()){
                String response = httpResponse.toString();
                log.info(response);
                throw new BadRequestException(response);
            } else {
                return false;
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
    }

    public void updateSharedScope(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, Scope scope)
            throws APIApplicationServicesException, BadRequestException {

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
            } else if (HttpStatus.SC_BAD_REQUEST == httpResponse.getStatusLine().getStatusCode()){
                String response = httpResponse.toString();
                log.info(response);
                throw new BadRequestException(response);
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
    }
}
