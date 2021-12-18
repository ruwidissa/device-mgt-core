package io.entgra.application.mgt.core.dao;

import io.entgra.application.mgt.common.IdentityServer;
import io.entgra.application.mgt.common.dto.ApplicationDTO;
import io.entgra.application.mgt.core.exception.ApplicationManagementDAOException;

import java.util.List;

public interface SPApplicationDAO {
    /**
     *
     * @param tenantId
     * @return the application with the provided installer location
     * @throws ApplicationManagementDAOException
     */
    List<ApplicationDTO> getSPApplications(int identityServerId, String spUID, int tenantId) throws ApplicationManagementDAOException;

    /**
     *
     * @param tenantId
     * @return the application with the provided installer location
     * @throws ApplicationManagementDAOException
     */
    int attachSPApplication(int identityServerId, String spUID, int appId, int tenantId) throws ApplicationManagementDAOException;

    /**
     *
     * @param tenantId
     * @return the application with the provided installer location
     * @throws ApplicationManagementDAOException
     */
    void detachSPApplication(int identityServerId, String spUID, int appId, int tenantId) throws ApplicationManagementDAOException;

    List<IdentityServer> getIdentityServers(int tenantId) throws ApplicationManagementDAOException;

    IdentityServer getIdentityServerById(int id, int tenantId) throws ApplicationManagementDAOException;

    /**
     * Verify whether application exist for given application name and device type. Because a name and device type is
     * unique for an application.
     *
     * @param appId     id of the application.
     * @param spUID  UID of the service provider.
     * @param tenantId ID of the tenant.
     * @return ID of the ApplicationDTO.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    boolean isSPApplicationExist(int identityServerId, String spUID, int appId, int tenantId) throws ApplicationManagementDAOException;

    void deleteApplicationFromServiceProviders(int applicationId, int tenantId) throws ApplicationManagementDAOException;


}
