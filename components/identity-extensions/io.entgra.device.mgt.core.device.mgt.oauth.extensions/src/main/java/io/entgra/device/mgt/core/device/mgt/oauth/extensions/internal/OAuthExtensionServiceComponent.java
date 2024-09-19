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

package io.entgra.device.mgt.core.device.mgt.oauth.extensions.internal;

import io.entgra.device.mgt.core.device.mgt.oauth.extensions.validators.ExtendedJDBCScopeValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.validators.JDBCScopeValidator;
import org.wso2.carbon.identity.oauth2.validators.OAuth2ScopeValidator;
import org.wso2.carbon.user.core.service.RealmService;

@Component(
        name = "io.entgra.device.mgt.core.device.mgt.oauth.extensions.internal.OAuthExtensionServiceComponent",
        immediate = true)
public class OAuthExtensionServiceComponent {

    private static final Log log = LogFactory.getLog(OAuthExtensionServiceComponent.class);
    private static final String REPOSITORY = "repository";
    private static final String CONFIGURATION = "conf";
    private static final String APIM_CONF_FILE = "api-manager.xml";
    private static final String PERMISSION_SCOPE_PREFIX = "perm";
    private static final String DEFAULT_PREFIX = "default";


    @SuppressWarnings("unused")
    @Activate
    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Starting OAuthExtensionBundle");
        }

        ExtendedJDBCScopeValidator permissionBasedScopeValidator = new ExtendedJDBCScopeValidator();
        JDBCScopeValidator roleBasedScopeValidator = new JDBCScopeValidator();
        OAuthExtensionsDataHolder.getInstance().addScopeValidator(permissionBasedScopeValidator,
                PERMISSION_SCOPE_PREFIX);
        OAuthExtensionsDataHolder.getInstance().addScopeValidator(roleBasedScopeValidator,
                DEFAULT_PREFIX);

    }

    @SuppressWarnings("unused")
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Stopping OAuthExtensionBundle");
        }
    }

    /**
     * Sets Realm Service.
     *
     * @param realmService An instance of RealmService
     */
    @Reference(
            name = "realm.service",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Realm Service");
        }
        OAuthExtensionsDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * Unsets Realm Service.
     *
     * @param realmService An instance of RealmService
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Realm Service");
        }
        OAuthExtensionsDataHolder.getInstance().setRealmService(null);
    }

    /**
     * Sets OAuth2TokenValidation Service.
     *
     * @param tokenValidationService An instance of OAuth2TokenValidationService
     */
    @Reference(
            name = "oauth2.token.validation.service",
            service = org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOAuth2ValidationService")
    protected void setOAuth2ValidationService(OAuth2TokenValidationService tokenValidationService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting OAuth2TokenValidation Service");
        }
        OAuthExtensionsDataHolder.getInstance().setoAuth2TokenValidationService(tokenValidationService);
    }

    /**
     * Unsets OAuth2TokenValidation Service.
     *
     * @param tokenValidationService An instance of OAuth2TokenValidationService
     */
    protected void unsetOAuth2ValidationService(OAuth2TokenValidationService tokenValidationService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting OAuth2TokenValidation Service");
        }
        OAuthExtensionsDataHolder.getInstance().setoAuth2TokenValidationService(null);
    }

    /**
     * Add scope validator to the map.
     * @param scopesValidator
     */
    @Reference(
            name = "oauth2.scope.validator",
            service = org.wso2.carbon.identity.oauth2.validators.OAuth2ScopeValidator.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeScopeValidator")
    protected void addScopeValidator(OAuth2ScopeValidator scopesValidator) {
        OAuthExtensionsDataHolder.getInstance().addScopeValidator(scopesValidator, DEFAULT_PREFIX);
    }

    /**
     * unset scope validator.
     * @param scopesValidator
     */
    protected void removeScopeValidator(OAuth2ScopeValidator scopesValidator) {
        OAuthExtensionsDataHolder.getInstance().removeScopeValidator();
    }


}
