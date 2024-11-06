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
package io.entgra.device.mgt.core.certificate.mgt.core.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

public class CommonUtil {

    public Date getValidityStartDate() {
        Date targetDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(targetDate);
        calendar.add(Calendar.DATE, -2);
        return calendar.getTime();
    }

    public Date getValidityEndDate() {
        Date targetDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(targetDate);
        calendar.add(Calendar.YEAR, 100);
        return calendar.getTime();
    }

    public static synchronized BigInteger generateSerialNumber() {
        return BigInteger.valueOf(System.currentTimeMillis());
    }

    /**
     * Returns the value of the given attribute from the subject distinguished name. eg: "entgra.net"
     * from "CN=entgra.net"
     * @param requestCertificate {@link X509Certificate} that needs to extract an attribute from
     * @param attribute the attribute name that needs to be extracted from the cert. eg: "CN="
     * @return the value of the attribute
     */
    public static String getSubjectDnAttribute(X509Certificate requestCertificate, String attribute) {
        String distinguishedName = requestCertificate.getSubjectDN().getName();
        if (StringUtils.isNotEmpty(distinguishedName)) {
            String[] dnSplits = distinguishedName.split(",");
            for (String dnSplit : dnSplits) {
                if (dnSplit.contains(attribute)) {
                    String[] cnSplits = dnSplit.split("=");
                    if (StringUtils.isNotEmpty(cnSplits[1])) {
                        return cnSplits[1];
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks if the organizational unit (OU) attribute has a valid tenant id in order to verify that it is
     * a SCEP certificate. eg: OU=tenant_1
     * <br/><br/>
     * Refer to engineering mail SCEP implementation for Android
     * @param orgUnit organizational unit (OU) of the certificate
     * @return true if it is a valid SCEP org unit else false
     */
    public static boolean isScepOrgUnit(String orgUnit) {
        if (StringUtils.isNotEmpty(orgUnit)) {
            if (orgUnit.contains(CertificateManagementConstants.ORG_UNIT_TENANT_PREFIX)) {
                String[] orgUnitArray = orgUnit.split(("_"));
                if (orgUnitArray.length > 1) {
                    return NumberUtils.isNumber(orgUnitArray[1]);
                }
            }
        }
        return false;
    }
}
