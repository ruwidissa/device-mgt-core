package io.entgra.devicemgt.apimgt.extension.rest.api.constants;

public final class Constants {

    private Constants() {
    }

    public static final String EMPTY_STRING = "";
    public static final String CLIENT_NAME = "rest_api_publisher_code";
    public static final String OWNER = "admin";
    public static final String GRANT_TYPE = "client_credentials password refresh_token";
    public static final String REFRESH_TOKEN_GRANT_TYPE_PARAM_NAME = "refresh_token";
    public static final String OAUTH_EXPIRES_IN = "expires_in";
    public static final String OAUTH_TOKEN_SCOPE = "scope";
    public static final String OAUTH_TOKEN_TYPE = "token_type";
    public static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";
    public static final String SCOPE_PARAM_NAME = "scope";
    public static final String SCOPES = "apim:api_create apim:api_view apim:shared_scope_manage";

    public static final String ADAPTER_CONF_KEEP_ALIVE = "keepAlive";
    public static final int ADAPTER_CONF_DEFAULT_KEEP_ALIVE = 60000;

    public static final int DEFAULT_MIN_THREAD_POOL_SIZE = 8;
    public static final int DEFAULT_MAX_THREAD_POOL_SIZE = 100;
    public static final int DEFAULT_EXECUTOR_JOB_QUEUE_SIZE = 2000;
    public static final long DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS = 20000;
    public static final String ADAPTER_MIN_THREAD_POOL_SIZE_NAME = "minThread";
    public static final String ADAPTER_MAX_THREAD_POOL_SIZE_NAME = "maxThread";
    public static final String ADAPTER_KEEP_ALIVE_TIME_NAME = "keepAliveTimeInMillis";
    public static final String ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME = "jobQueueSize";

    public static final String DEFAULT_CALLBACK = "";
    public static final String DEFAULT_PASSWORD = "";
    public static final String TOKEN_SCOPE = "production";
    public static final String APPLICATION_NAME_PREFIX = "OutputAdapter_";
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";

    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    public static final String AUTHORIZATION_HEADER_VALUE_PREFIX = "Basic ";
    public static final String AUTHORIZATION_HEADER_PREFIX_BEARER = "Bearer ";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String PASSWORD_GRANT_TYPE = "password";
    public static final String PASSWORD_GRANT_TYPE_USERNAME = "username";
    public static final String PASSWORD_GRANT_TYPE_PASSWORD = "password";
    public static final String PASSWORD_GRANT_TYPE_SCOPES = "scopes";
    public static final String ACCESS_TOKEN_GRANT_TYPE_PARAM_NAME = "access_token";
    public static final String GRANT_TYPE_PARAM_NAME = "grant_type";
}


