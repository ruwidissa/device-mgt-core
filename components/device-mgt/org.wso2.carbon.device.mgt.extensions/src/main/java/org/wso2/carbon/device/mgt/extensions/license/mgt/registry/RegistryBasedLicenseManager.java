/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.device.mgt.extensions.license.mgt.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManager;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.api.Registry;

import javax.xml.namespace.QName;
import java.lang.String;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("unused")
public class RegistryBasedLicenseManager implements LicenseManager {

    private static final Log log = LogFactory.getLog(RegistryBasedLicenseManager.class);

    public RegistryBasedLicenseManager() {}

    private GenericArtifactManager getArtifactManager() {
        Registry registry = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_GOVERNANCE);
        if (registry == null) {
            throw new IllegalArgumentException("Registry instance retrieved is null. Hence, " +
                    "'Registry based license manager cannot be initialized'");
        }
        try {
            return GenericArtifactManagerFactory.getTenantAwareGovernanceArtifactManager(registry);
        } catch (LicenseManagementException e) {
            throw new IllegalStateException("Failed to initialize generic artifact manager bound to " +
                    "Registry based license manager", e);
        }
    }

    @Override
    public License getLicense(final String deviceType, final String languageCode) throws LicenseManagementException {
        GenericArtifactManager artifactManager = getArtifactManager();
        try {
            GenericArtifact artifact = this.getGenericArtifact(artifactManager, deviceType, languageCode);
            if (artifact == null) { //Adding a default license
                License license = new License();
                license.setName(deviceType);
                license.setVersion("1.0.0");
                license.setLanguage("en_US");
                license.setText("This is license text");
                addLicense(deviceType, license);
                return license;
            }
            return this.populateLicense(artifact);
        } catch (GovernanceException e) {
            throw new LicenseManagementException("Error occurred while retrieving license corresponding to " +
                    "device type '" + deviceType + "'", e);
        } catch (ParseException e) {
            throw new LicenseManagementException("Error occurred while parsing the ToDate/FromDate date string " +
                    "of the license configured upon the device type '" + deviceType + "'", e);
        }
    }

    private License populateLicense(GenericArtifact artifact) throws GovernanceException, ParseException {
        License license = new License();
        license.setName(artifact.getAttribute(DeviceManagementConstants.LicenseProperties.NAME));
        license.setProvider(artifact.getAttribute(DeviceManagementConstants.LicenseProperties.PROVIDER));
        license.setVersion(artifact.getAttribute(DeviceManagementConstants.LicenseProperties.VERSION));
        license.setLanguage(artifact.getAttribute(DeviceManagementConstants.LicenseProperties.LANGUAGE));
        license.setText(artifact.getAttribute(DeviceManagementConstants.LicenseProperties.TEXT));

        DateFormat format = new SimpleDateFormat("dd-mm-yyyy", Locale.ENGLISH);
        String validFrom = artifact.getAttribute(DeviceManagementConstants.LicenseProperties.VALID_FROM);
        if (validFrom != null && !validFrom.isEmpty()) {
            license.setValidFrom(format.parse(validFrom));
        }
        String validTo = artifact.getAttribute(DeviceManagementConstants.LicenseProperties.VALID_TO);
        if (validTo != null && !validTo.isEmpty()) {
            license.setValidFrom(format.parse(validTo));
        }
        return license;
    }

    @Override
    public void addLicense(final String deviceType, final License license) throws LicenseManagementException {
        GenericArtifactManager artifactManager = getArtifactManager();
        try {
            GenericArtifact artifact = this.getGenericArtifact(artifactManager, deviceType, license.getLanguage());
            if(artifact != null) {
                artifact.setAttribute(DeviceManagementConstants.LicenseProperties.NAME, license.getName());
                artifact.setAttribute(DeviceManagementConstants.LicenseProperties.VERSION, license.getVersion());
                artifact.setAttribute(DeviceManagementConstants.LicenseProperties.PROVIDER, license.getProvider());
                artifact.setAttribute(DeviceManagementConstants.LicenseProperties.LANGUAGE, license.getLanguage());
                artifact.setAttribute(DeviceManagementConstants.LicenseProperties.TEXT, license.getText());
                artifact.setAttribute(DeviceManagementConstants.LicenseProperties.ARTIFACT_NAME, license.getName());
                Date validTo = license.getValidTo();
                if (validTo != null) {
                    artifact.setAttribute(DeviceManagementConstants.LicenseProperties.VALID_TO, validTo.toString());
                }
                Date validFrom = license.getValidFrom();
                if (validFrom != null) {
                    artifact.setAttribute(DeviceManagementConstants.LicenseProperties.VALID_FROM, validFrom.toString());
                }
                artifactManager.updateGenericArtifact(artifact);
            } else {
                artifact =
                        artifactManager.newGovernanceArtifact(new QName("http://www.wso2.com", deviceType));
                artifact.setAttribute(DeviceManagementConstants.LicenseProperties.NAME, license.getName());
                artifact.setAttribute(DeviceManagementConstants.LicenseProperties.VERSION, license.getVersion());
                artifact.setAttribute(DeviceManagementConstants.LicenseProperties.PROVIDER, license.getProvider());
                artifact.setAttribute(DeviceManagementConstants.LicenseProperties.LANGUAGE, license.getLanguage());
                artifact.setAttribute(DeviceManagementConstants.LicenseProperties.TEXT, license.getText());
                artifact.setAttribute(DeviceManagementConstants.LicenseProperties.ARTIFACT_NAME, license.getName());
                Date validTo = license.getValidTo();
                if (validTo != null) {
                    artifact.setAttribute(DeviceManagementConstants.LicenseProperties.VALID_TO, validTo.toString());
                }
                Date validFrom = license.getValidFrom();
                if (validFrom != null) {
                    artifact.setAttribute(DeviceManagementConstants.LicenseProperties.VALID_FROM, validFrom.toString());
                }
                artifactManager.addGenericArtifact(artifact);
            }
        } catch (GovernanceException e) {
            throw new LicenseManagementException("Error occurred while adding license for device type " +
                    deviceType + "'", e);
        }
    }

    private GenericArtifact getGenericArtifact(GenericArtifactManager artifactManager, final String deviceType, final
    String languageCode) throws GovernanceException {
        GenericArtifact[] artifacts = artifactManager.findGenericArtifacts(new GenericArtifactFilter() {
            @Override
            public boolean matches(GenericArtifact artifact) throws GovernanceException {
                String attributeNameVal = artifact.getAttribute(
                        DeviceManagementConstants.LicenseProperties.NAME);
                String attributeLangVal = artifact.getAttribute(
                        DeviceManagementConstants.LicenseProperties.LANGUAGE);
                return (attributeNameVal != null && attributeLangVal != null && attributeNameVal.
                        equalsIgnoreCase(deviceType) && attributeLangVal.equalsIgnoreCase(languageCode));
            }
        });
        return (artifacts == null || artifacts.length == 0) ? null : artifacts[0];
    }

}
