package io.entgra.devicemgt.apimgt.extension.publisher.api;

import io.entgra.devicemgt.apimgt.extension.publisher.api.dto.APIApplicationKey;
import io.entgra.devicemgt.apimgt.extension.publisher.api.dto.AccessTokenInfo;
import io.entgra.devicemgt.apimgt.extension.publisher.api.exceptions.APIApplicationServicesException;
import io.entgra.devicemgt.apimgt.extension.publisher.api.exceptions.BadRequestException;

public interface APIApplicationServices {

    APIApplicationKey createAndRetrieveApplicationCredentials() throws BadRequestException, APIApplicationServicesException;

    AccessTokenInfo generateAccessTokenFromRegisteredApplication(String clientId, String clientSecret) throws APIApplicationServicesException;
    AccessTokenInfo generateAccessTokenFromRefreshToken(String refreshToken, String clientId, String clientSecret) throws APIApplicationServicesException;

}
