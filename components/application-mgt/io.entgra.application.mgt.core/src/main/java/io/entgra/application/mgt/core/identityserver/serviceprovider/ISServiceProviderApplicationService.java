package io.entgra.application.mgt.core.identityserver.serviceprovider;

import io.entgra.application.mgt.common.SPApplication;
import io.entgra.application.mgt.common.SPApplicationListResponse;
import io.entgra.application.mgt.common.dto.IdentityServerDTO;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.InvalidConfigurationException;
import io.entgra.application.mgt.core.config.ConfigurationManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Constructor;
import java.util.List;

public interface ISServiceProviderApplicationService {

    Log log = LogFactory.getLog(ISServiceProviderApplicationService.class);

    static ISServiceProviderApplicationService of(String identityServerName) throws InvalidConfigurationException {
        String className = ConfigurationManager.getInstance().getIdentityServerConfiguration().
                getIdentityServerDetailByProviderName(identityServerName).getProviderClassName();
        try {
            Class theClass = Class.forName(className);
            Constructor<ISServiceProviderApplicationService> constructor = theClass.getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            String msg = "Unable to get instance of " + className;
            log.error(msg, e);
            throw new InvalidConfigurationException(msg, e);
        }
    }

    /**
     * Use to get IS Service specific api params
     *
     * @return IS Service specific api params
     */
    List<String> getRequiredApiParams();

    /**
     * Check if service provider application exists
     *
     * @param identityServer  {@link IdentityServerDTO}
     * @param spAppId uid of the service provider
     * @return if service provider exist
     * @throws ApplicationManagementException
     */
    boolean isSPApplicationExist(IdentityServerDTO identityServer, String spAppId) throws ApplicationManagementException;

    /**
     *  Get service provider by identity server id and service provider uid
     * @param identityServer  {@link IdentityServerDTO}
     * @param spAppId uid of service provider to be retrieved
     * @return {@link SPApplication}
     * @throws ApplicationManagementException
     */
    SPApplication retrieveSPApplication(IdentityServerDTO identityServer, String spAppId) throws ApplicationManagementException;

    /**
     * Retrieve service provider apps from identity server
     *
     * @param identityServer  {@link IdentityServerDTO}
     * @return {@link SPApplicationListResponse}
     * @throws ApplicationManagementException
     */
    SPApplicationListResponse retrieveSPApplications(IdentityServerDTO identityServer, Integer limit, Integer offset)
            throws ApplicationManagementException;
}
