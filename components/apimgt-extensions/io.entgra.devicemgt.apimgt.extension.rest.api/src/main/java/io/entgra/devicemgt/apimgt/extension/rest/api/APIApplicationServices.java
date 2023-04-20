package io.entgra.devicemgt.apimgt.extension.rest.api;

import io.entgra.devicemgt.apimgt.extension.rest.api.dto.APIApplicationKey;
import io.entgra.devicemgt.apimgt.extension.rest.api.dto.AccessTokenInfo;
import io.entgra.devicemgt.apimgt.extension.rest.api.exceptions.APIApplicationServicesException;

public interface APIApplicationServices {

    APIApplicationKey createAndRetrieveApplicationCredentials() throws APIApplicationServicesException;

    AccessTokenInfo generateAccessTokenFromRegisteredApplication(String clientId, String clientSecret) throws APIApplicationServicesException;
    AccessTokenInfo generateAccessTokenFromRefreshToken(String refreshToken, String clientId, String clientSecret) throws APIApplicationServicesException;

}
