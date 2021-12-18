package io.entgra.application.mgt.publisher.api.services.util;

import com.google.gson.Gson;
import io.entgra.application.mgt.common.IdentityServer;
import io.entgra.application.mgt.common.SPApplication;
import io.entgra.application.mgt.common.SPApplicationListResponse;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.services.SPApplicationManager;
import io.entgra.application.mgt.core.util.APIUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.wso2.carbon.device.mgt.core.common.util.HttpUtil;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

public class SPAppRequestHandlerUtil {

    private static final Log log = LogFactory.getLog(SPAppRequestHandlerUtil.class);

    public static boolean isSPApplicationExist(int identityServerId, String spAppId) throws ApplicationManagementException {
        SPApplication application = retrieveSPApplication(identityServerId, spAppId);
        if (application == null) {
            return false;
        }
        return true;
    }

    public static SPApplicationListResponse getServiceProvidersFromIdentityServer(int identityServerId, Integer limit, Integer offSet)
            throws ApplicationManagementException {
        return retrieveSPApplications(identityServerId, limit, offSet);
    }

    public static SPApplication retrieveSPApplication(int identityServerId, String spAppId)
            throws ApplicationManagementException {
        IdentityServer identityServer = getIdentityServer(identityServerId);
        HttpGet req = new HttpGet();
        URI uri = HttpUtil.createURI(getSPApplicationsAPI(identityServer));
        uri = UriBuilder.fromUri(uri).path(spAppId).build();
        req.setURI(uri);
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpResponse response = invokeISAPI(identityServer, client, req);
            String responseBody = HttpUtil.getResponseString(response);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return new Gson().fromJson(responseBody,
                        SPApplication.class);
            }
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            String msg = "Error occurred while calling SP Applications API";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } catch (IOException e) {
            String msg = "Error occurred while calling SP Applications API";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                log.error("Error occurred while closing http connection");
            }
        }
    }


    public static SPApplicationListResponse retrieveSPApplications(int identityServerId, Integer limit, Integer offset)
            throws ApplicationManagementException {
        IdentityServer identityServer = getIdentityServer(identityServerId);
        HttpGet req = new HttpGet();
        URI uri = HttpUtil.createURI(getSPApplicationsAPI(identityServer));
        UriBuilder uriBuilder = UriBuilder.fromUri(uri);
        if (limit != null) {
            uriBuilder = uriBuilder.queryParam(io.entgra.application.mgt.core.util.Constants.LIMIT_QUERY_PARAM, limit);
        }
        if (offset != null) {
            uriBuilder = uriBuilder.queryParam(io.entgra.application.mgt.core.util.Constants.OFFSET_QUERY_PARAM, offset);
        }
        uri = uriBuilder.build();
        req.setURI(uri);
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpResponse response = invokeISAPI(identityServer, client, req);
            String responseBody = HttpUtil.getResponseString(response);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return new Gson().fromJson(responseBody,
                        SPApplicationListResponse.class);
            }
            String msg = "Error occurred while calling SP Applications API";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } catch (IOException e) {
            String msg = "Error occurred while calling SP Applications API";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                log.error("Error occurred while closing http connection");
            }
        }
    }

    public static IdentityServer getIdentityServer(int identityServerId) throws ApplicationManagementException {
        SPApplicationManager spApplicationManager = APIUtil.getSPApplicationManager();
        return spApplicationManager.getIdentityServer(identityServerId);
    }

    public static HttpResponse invokeISAPI(IdentityServer identityServer, HttpClient client, HttpRequestBase request) throws IOException {
        setBasicAuthHeader(identityServer, request);
        return client.execute(request);
    }

    public static void setBasicAuthHeader(IdentityServer identityServer, HttpRequestBase request) {
        String basicAuthHeader = HttpUtil.getBasicAuthBase64Header(identityServer.getUserName(),
                identityServer.getPassword());
        request.setHeader(HttpHeaders.AUTHORIZATION, basicAuthHeader);
    }

    private static String getSPApplicationsAPI(IdentityServer identityServer) {
        String api = identityServer.getSpAppsApi();
        return api;
    }

}