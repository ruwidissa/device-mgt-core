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

package io.entgra.device.mgt.core.identity.jwt.client.extension.dto;

import io.entgra.device.mgt.core.identity.jwt.client.extension.constant.JWTConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class JWTConfig {

	private static Log log = LogFactory.getLog(JWTConfig.class);
	private static final String JWT_ISSUER = "iss";
	private static final String JWT_EXPIRATION_TIME = "exp";
	private static final String JWT_AUDIENCE = "aud";
	private static final String VALIDITY_PERIOD = "nbf";
	private static final String JWT_TOKEN_ID = "jti";
	private static final String JWT_ISSUED_AT = "iat";
	private static final String SERVER_TIME_SKEW="skew";
	private static final String JKS_PATH ="KeyStore";
	private static final String JKS_PRIVATE_KEY_ALIAS ="PrivateKeyAlias";
	private static final String JKS_PASSWORD ="KeyStorePassword";
	private static final String JKA_PRIVATE_KEY_PASSWORD = "PrivateKeyPassword";
	private static final String TOKEN_ENDPOINT = "TokenEndpoint";
	private static final String JWT_GRANT_TYPE_NAME = "GrantType";
	public static final String IOT_KM_HOST = "iot.keymanager.host";
	public static final String IOT_KM_HTTPS_PORT = "iot.keymanager.https.port";

	/**
	 * issuer of the JWT
	 */
	private String issuer;

	/**
	 * skew between IDP and issuer(milliseconds)
	 */
	private int skew;

	/**
	 * Audience of JWT claim
	 */
	private List<String> audiences;

	/**
	 * expiration time of JWT (number of minutes from the current time).
	 */
	private int expirationTime;

	/**
	 * issued Interval from current time of JWT (number of minutes from the current time).
	 */
	private int issuedInternal;

	/**
	 * nbf time of JWT (number of minutes from current time).
	 */
	private int validityPeriodInterval;

	/**
	 * JWT Id.
	 */
	private String jti;

	/**
	 * Token Endpoint;
	 */
	private String tokenEndpoint;

	/**
	 * Configuration for keystore.
	 */
	private String keyStorePath;
	private String keyStorePassword;
	private String privateKeyAlias;
	private String privateKeyPassword;

	/**
	 * Jwt Grant Type Name
	 */
	private String jwtGrantType;

	/**
	 * @param properties load the config from the properties file.
	 */
	public JWTConfig(Properties properties) {
		String iss = properties.getProperty(JWT_ISSUER, null);
		if (iss != null) {
			iss = Utils.replaceSystemProperty(iss);
			iss = resolvePlaceholders(iss);
			issuer = getIss(iss);
		}
		skew = Integer.parseInt(properties.getProperty(SERVER_TIME_SKEW, "0"));
		issuedInternal = Integer.parseInt(properties.getProperty(JWT_ISSUED_AT,"0"));
		expirationTime = Integer.parseInt(properties.getProperty(JWT_EXPIRATION_TIME,"15"));
		validityPeriodInterval = Integer.parseInt(properties.getProperty(VALIDITY_PERIOD,"0"));
		jti = properties.getProperty(JWT_TOKEN_ID, null);
		String audience = properties.getProperty(JWT_AUDIENCE, null);
		if(audience != null) {
			//Replace system property
			audience = Utils.replaceSystemProperty(audience);
			//Replace custom placeholders with system property values
			audience = resolvePlaceholders(audience);
			//Split and clean
			audiences = getAudience(audience);
		}
		//get Keystore params
		keyStorePath = properties.getProperty(JKS_PATH);
		keyStorePassword = properties.getProperty(JKS_PASSWORD);
		privateKeyAlias = properties.getProperty(JKS_PRIVATE_KEY_ALIAS);
		privateKeyPassword = properties.getProperty(JKA_PRIVATE_KEY_PASSWORD);
		tokenEndpoint = properties.getProperty(TOKEN_ENDPOINT, "");
		jwtGrantType = properties.getProperty(JWT_GRANT_TYPE_NAME, JWTConstants.JWT_GRANT_TYPE);

	}

	private static List<String> getAudience(String audience){
		List<String> audiences = new ArrayList<String>();
		for(String audi : audience.split(",")){
			audiences.add(audi.trim());
		}
		return audiences;
	}

	public static String getIss(String issuer) {
		return issuer;
	}

	public String getIssuer() {
		return issuer;
	}

	public int getSkew() {
		return skew;
	}

	public List<String> getAudiences() {
		return audiences;
	}

	public int getExpirationTime() {
		return expirationTime;
	}

	public int getIssuedInternal() {
		return issuedInternal;
	}

	public int getValidityPeriodFromCurrentTime() {
		return validityPeriodInterval;
	}

	public String getJti() {
		return jti;
	}

	public String getKeyStorePath() {
		return keyStorePath;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public String getPrivateKeyAlias() {
		return privateKeyAlias;
	}

	public String getPrivateKeyPassword() {
		return privateKeyPassword;
	}

	public String getTokenEndpoint() {
		String endpoint = Utils.replaceSystemProperty(tokenEndpoint);
		return resolvePlaceholders(endpoint);

	}

	public String getJwtGrantType() {
		return jwtGrantType;
	}

	/**
	 * Resolves known placeholders in the given input string by replacing them with corresponding
	 * system property values.
	 *
	 * <p>Currently supported placeholders:</p>
	 * <ul>
	 *   <li><code>${iot.keymanager.host}</code> - replaced with the value of the <code>IOT_KM_HOST</code> system property</li>
	 *   <li><code>${iot.keymanager.https.port}</code> - replaced with the value of the <code>IOT_KM_HTTPS_PORT</code> system property</li>
	 * </ul>
	 *
	 * <p>If the system property is not defined, an empty string is used as the replacement.</p>
	 *
	 * @param input the input string potentially containing placeholders
	 * @return the input string with placeholders replaced by system property values
	 */
	private String resolvePlaceholders(String input) {
		Map<String, String> placeholders = Map.of(
				"${iot.keymanager.host}", System.getProperty(IOT_KM_HOST, ""),
				"${iot.keymanager.https.port}", System.getProperty(IOT_KM_HTTPS_PORT, "")
		);
		for (Map.Entry<String, String> entry : placeholders.entrySet()) {
			if (entry.getValue() != null) {
				input = input.replace(entry.getKey(), entry.getValue());
			}
		}
		try {
			new URL(input);
		} catch (MalformedURLException e) {
			String msg = "Resolved URL is invalid: " + input ;
			log.error(msg, e);
			throw new IllegalArgumentException(msg, e);
		}
        return input;
	}

}
