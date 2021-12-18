package io.entgra.application.mgt.common.services;

import io.entgra.application.mgt.common.IdentityServer;
import io.entgra.application.mgt.common.IdentityServerList;
import io.entgra.application.mgt.common.SPApplication;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.RequestValidatingException;
import io.entgra.application.mgt.common.response.Application;

import java.util.List;

public interface SPApplicationManager {


    void addExistingApps(int identityServerId, List<SPApplication> applications) throws ApplicationManagementException;

    void detachSPApplications(int identityServerId, String spUID, List<Integer> appIds) throws ApplicationManagementException;

    void attachSPApplications(int identityServerId, String spUID, List<Integer> appIds)  throws ApplicationManagementException;

    IdentityServer getIdentityServer(int identityServerId) throws ApplicationManagementException;

    IdentityServerList getIdentityServers() throws ApplicationManagementException;

    List<Application> getSPApplications(int identityServerId, String spUID) throws ApplicationManagementException;

    <T> Application createSPApplication(T app, int identityServerId, String spId) throws ApplicationManagementException, RequestValidatingException;

    void validateAttachAppsRequest(int identityServerId, List<Integer> appIds) throws ApplicationManagementException;

    void validateDetachAppsRequest(int identityServerId, String spId, List<Integer> appIds) throws ApplicationManagementException;

}
