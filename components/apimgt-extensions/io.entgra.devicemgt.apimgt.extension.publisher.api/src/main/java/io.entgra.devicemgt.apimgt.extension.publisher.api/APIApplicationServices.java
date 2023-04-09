package io.entgra.devicemgt.apimgt.extension.publisher.api;

import io.entgra.devicemgt.apimgt.extension.publisher.api.dto.APIApplicationKey;
import io.entgra.devicemgt.apimgt.extension.publisher.api.dto.AccessTokenInfo;

public interface APIApplicationServices {

    APIApplicationKey createAndRetrieveApplicationCredentials();

    AccessTokenInfo generateAccessTokenFromRegisteredApplication(String clientId, String clientSecret);
    AccessTokenInfo generateAccessTokenFromRefreshToken(String refreshToken, String clientId, String clientSecret);

}
