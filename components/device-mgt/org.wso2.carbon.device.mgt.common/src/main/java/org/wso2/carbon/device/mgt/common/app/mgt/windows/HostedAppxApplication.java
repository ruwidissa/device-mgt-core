/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.common.app.mgt.windows;

import java.util.List;

public class HostedAppxApplication {

    private String packageUri;
    private String packageFamilyName;
    private List<String> dependencyPackageUri;
    private String certificateHash;
    private String encodedCertificate;

    public String getPackageUri() {
        return packageUri;
    }

    public void setPackageUri(String packageUri) {
        this.packageUri = packageUri;
    }

    public String getPackageFamilyName() {
        return packageFamilyName;
    }

    public void setPackageFamilyName(String packageFamilyName) {
        this.packageFamilyName = packageFamilyName;
    }

    public List<String> getDependencyPackageUri() {
        return dependencyPackageUri;
    }

    public void setDependencyPackageUri(List<String> dependencyPackageUri) {
        this.dependencyPackageUri = dependencyPackageUri;
    }

    public String getCertificateHash() {
        return certificateHash;
    }

    public void setCertificateHash(String certificateHash) {
        this.certificateHash = certificateHash;
    }

    public String getEncodedCertificate() {
        return encodedCertificate;
    }

    public void setEncodedCertificate(String encodedCertificate) {
        this.encodedCertificate = encodedCertificate;
    }

}
