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

package io.entgra.device.mgt.core.webapp.authenticator.framework.internal;

import io.entgra.device.mgt.core.certificate.mgt.core.scep.SCEPManager;
import io.entgra.device.mgt.core.certificate.mgt.core.service.CertificateManagementService;
import io.entgra.device.mgt.core.device.mgt.common.spi.OTPManagementService;
import io.entgra.device.mgt.core.webapp.authenticator.framework.WebappAuthenticationValve;
import io.entgra.device.mgt.core.webapp.authenticator.framework.WebappAuthenticatorRepository;
import io.entgra.device.mgt.core.webapp.authenticator.framework.authenticator.WebappAuthenticator;
import io.entgra.device.mgt.core.webapp.authenticator.framework.config.AuthenticatorConfig;
import io.entgra.device.mgt.core.webapp.authenticator.framework.config.AuthenticatorConfigService;
import io.entgra.device.mgt.core.webapp.authenticator.framework.config.WebappAuthenticatorConfig;
import io.entgra.device.mgt.core.webapp.authenticator.framework.config.impl.AuthenticatorConfigServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.TomcatValveContainer;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component(
        name = "io.entgra.device.mgt.core.webapp.authenticator.framework.internal.WebappAuthenticatorFrameworkServiceComponent",
        immediate = true)
public class WebappAuthenticatorFrameworkServiceComponent {
    private static final Log log = LogFactory.getLog(WebappAuthenticatorFrameworkServiceComponent.class);

    @SuppressWarnings("unused")
    @Activate
    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Starting Web Application Authenticator Framework Bundle");
        }
        try {
            WebappAuthenticatorConfig.init();
            WebappAuthenticatorRepository repository = new WebappAuthenticatorRepository();
            for (AuthenticatorConfig config : WebappAuthenticatorConfig.getInstance().getAuthenticators()) {
                WebappAuthenticator authenticator =
                        (WebappAuthenticator) Class.forName(config.getClassName()).newInstance();

                if ((config.getParams() != null) && (!config.getParams().isEmpty())) {
                    Properties properties = new Properties();
                    for (AuthenticatorConfig.Parameter param : config.getParams()) {
                        properties.setProperty(param.getName(), param.getValue());
                    }
                    authenticator.setProperties(properties);
                }
                authenticator.init();
                repository.addAuthenticator(authenticator);
            }

            //Register AuthenticatorConfigService to expose webapp-authenticator configs.
            BundleContext bundleContext = componentContext.getBundleContext();
            AuthenticatorConfigService authenticatorConfigService = new AuthenticatorConfigServiceImpl();
            bundleContext.registerService(AuthenticatorConfigService.class.getName(), authenticatorConfigService, null);

            AuthenticatorFrameworkDataHolder.getInstance().setWebappAuthenticatorRepository(repository);

            List<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();
            valves.add(new WebappAuthenticationValve());
            TomcatValveContainer.addValves(valves);

            if (log.isDebugEnabled()) {
                log.debug("Web Application Authenticator Framework Bundle has been started successfully");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing the bundle", e);
        }
    }

    @SuppressWarnings("unused")
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    @Reference(
            name = "realm.service",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("RealmService acquired");
        }
        AuthenticatorFrameworkDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        AuthenticatorFrameworkDataHolder.getInstance().setRealmService(null);
    }

    @Reference(
            name = "certificate.mgt.service",
            service =  io.entgra.device.mgt.core.certificate.mgt.core.service.CertificateManagementService.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCertificateManagementService")
    protected void setCertificateManagementService(CertificateManagementService certificateManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting certificate management service");
        }
        AuthenticatorFrameworkDataHolder.getInstance().setCertificateManagementService(certificateManagementService);
    }

    protected void unsetCertificateManagementService(CertificateManagementService certificateManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing certificate management service");
        }

        AuthenticatorFrameworkDataHolder.getInstance().setCertificateManagementService(null);
    }

    @Reference(
            name = "scep.mgr",
            service =  io.entgra.device.mgt.core.certificate.mgt.core.scep.SCEPManager.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSCEPManagementService")
    protected void setSCEPManagementService(SCEPManager scepManager) {
        if (log.isDebugEnabled()) {
            log.debug("Setting SCEP management service");
        }
        AuthenticatorFrameworkDataHolder.getInstance().setScepManager(scepManager);
    }

    protected void unsetSCEPManagementService(SCEPManager scepManager) {
        if (log.isDebugEnabled()) {
            log.debug("Removing SCEP management service");
        }

        AuthenticatorFrameworkDataHolder.getInstance().setScepManager(null);
    }

    /**
     * Sets OAuth2TokenValidation Service.
     *
     * @param tokenValidationService An instance of OAuth2TokenValidationService
     */
    @Reference(
            name = "oauth2.token.validation.service",
            service =  org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOAuth2ValidationService")
    protected void setOAuth2ValidationService(OAuth2TokenValidationService tokenValidationService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting OAuth2TokenValidationService Service");
        }
        AuthenticatorFrameworkDataHolder.getInstance().setOAuth2TokenValidationService(tokenValidationService);
    }

    /**
     * Unsets OAuth2TokenValidation Service.
     *
     * @param tokenValidationService An instance of OAuth2TokenValidationService
     */
    protected void unsetOAuth2ValidationService(OAuth2TokenValidationService tokenValidationService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting OAuth2TokenValidationService Service");
        }
        AuthenticatorFrameworkDataHolder.getInstance().setOAuth2TokenValidationService(null);
    }

    @Reference(
            name = "tenant.index.loader",
            service = org.wso2.carbon.registry.indexing.service.TenantIndexingLoader.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTenantIndexLoader")
    protected void setTenantIndexLoader(TenantIndexingLoader tenantIndexLoader) {
        AuthenticatorFrameworkDataHolder.getInstance().setTenantIndexingLoader(tenantIndexLoader);
    }

    protected void unsetTenantIndexLoader(TenantIndexingLoader tenantIndexLoader) {
        AuthenticatorFrameworkDataHolder.getInstance().setTenantIndexingLoader(null);
    }

    @Reference(
            name = "tenant.registry.loader",
            service = org.wso2.carbon.registry.core.service.TenantRegistryLoader.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTenantRegistryLoader")
    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        AuthenticatorFrameworkDataHolder.getInstance().setTenantRegistryLoader(tenantRegistryLoader);
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        AuthenticatorFrameworkDataHolder.getInstance().setTenantRegistryLoader(null);
    }

    @Reference(
            name = "otp.mgt.service",
            service = io.entgra.device.mgt.core.device.mgt.common.spi.OTPManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOTPManagementService")
    protected void setOTPManagementService(OTPManagementService otpManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting OTP Management OSGI Service");
        }
        AuthenticatorFrameworkDataHolder.getInstance().setOtpManagementService(otpManagementService);
    }

    protected void unsetOTPManagementService(OTPManagementService otpManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing OTP Management OSGI Service");
        }
        AuthenticatorFrameworkDataHolder.getInstance().setOtpManagementService(null);
    }
}
