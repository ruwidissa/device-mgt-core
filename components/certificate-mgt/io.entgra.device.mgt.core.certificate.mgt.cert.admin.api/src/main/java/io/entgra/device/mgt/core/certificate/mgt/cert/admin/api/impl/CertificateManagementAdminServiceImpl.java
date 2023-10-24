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

package io.entgra.device.mgt.core.certificate.mgt.cert.admin.api.impl;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import io.entgra.device.mgt.core.device.mgt.common.CertificatePaginationRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.certificate.mgt.cert.admin.api.CertificateManagementAdminService;
import io.entgra.device.mgt.core.certificate.mgt.cert.admin.api.beans.CertificateList;
import io.entgra.device.mgt.core.certificate.mgt.cert.admin.api.beans.EnrollmentCertificate;
import io.entgra.device.mgt.core.certificate.mgt.cert.admin.api.beans.ErrorResponse;
import io.entgra.device.mgt.core.certificate.mgt.cert.admin.api.beans.ValidationResponse;
import io.entgra.device.mgt.core.certificate.mgt.cert.admin.api.util.CertificateMgtAPIUtils;
import io.entgra.device.mgt.core.certificate.mgt.cert.admin.api.util.RequestValidationUtil;
import io.entgra.device.mgt.core.certificate.mgt.core.dto.CertificateResponse;
import io.entgra.device.mgt.core.certificate.mgt.core.exception.CertificateManagementException;
import io.entgra.device.mgt.core.certificate.mgt.core.exception.KeystoreException;
import io.entgra.device.mgt.core.certificate.mgt.core.scep.SCEPException;
import io.entgra.device.mgt.core.certificate.mgt.core.scep.SCEPManager;
import io.entgra.device.mgt.core.certificate.mgt.core.scep.TenantedDeviceWrapper;
import io.entgra.device.mgt.core.certificate.mgt.core.service.CertificateManagementService;
import io.entgra.device.mgt.core.certificate.mgt.core.service.PaginationResult;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.DeviceManagementConstants;
import io.entgra.device.mgt.core.identity.jwt.client.extension.exception.JWTClientException;
import io.entgra.device.mgt.core.identity.jwt.client.extension.service.JWTClientManagerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/admin/certificates")
public class CertificateManagementAdminServiceImpl implements CertificateManagementAdminService {

    private static Log log = LogFactory.getLog(CertificateManagementAdminServiceImpl.class);
    private static final String PROXY_AUTH_MUTUAL_HEADER = "proxy-mutual-auth-header";

    /**
     * Save a list of certificates and relevant information in the database.
     *
     * @param enrollmentCertificates List of all the certificates which includes the tenant id, certificate as
     *                               a pem and a serial number.
     * @return Status of the data persist operation.
     */
    @POST
    public Response addCertificate(EnrollmentCertificate[] enrollmentCertificates) {
        CertificateManagementService certificateService;
        List<io.entgra.device.mgt.core.certificate.mgt.core.bean.Certificate> certificates = new ArrayList<>();
        io.entgra.device.mgt.core.certificate.mgt.core.bean.Certificate certificate;
        certificateService = CertificateMgtAPIUtils.getCertificateManagementService();
        try {
            for (EnrollmentCertificate enrollmentCertificate : enrollmentCertificates) {
                certificate = new io.entgra.device.mgt.core.certificate.mgt.core.bean.Certificate();
                certificate.setTenantId(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                certificate.setSerial(enrollmentCertificate.getSerial());
                certificate.setCertificate(certificateService.pemToX509Certificate(enrollmentCertificate.getPem()));
                CertificateResponse existingCertificate = certificateService.getCertificateBySerial(enrollmentCertificate.getSerial());
                if (existingCertificate != null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Certificate with serial number " + enrollmentCertificate.getSerial() + " already exists.")
                            .build();
                }

                certificates.add(certificate);
            }
            certificateService.saveCertificate(certificates);
            return Response.status(Response.Status.CREATED).entity("Added successfully.").build();
        } catch (KeystoreException e) {
            String msg = "Error occurred while converting PEM file to X509Certificate.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
        }
    }

    /**
     * Get a certificate when the serial number is given.
     *
     * @param serialNumber serial of the certificate needed.
     * @return certificate response.
     */
    @GET
    @Path("/{serialNumber}")
    public Response getCertificate(
            @PathParam("serialNumber") String serialNumber,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        RequestValidationUtil.validateSerialNumber(serialNumber);

        CertificateManagementService certificateService = CertificateMgtAPIUtils.getCertificateManagementService();
        List<CertificateResponse> certificateResponse;
        try {
            certificateResponse = certificateService.searchCertificates(serialNumber);
            return Response.status(Response.Status.OK).entity(certificateResponse).build();
        } catch (CertificateManagementException e) {
            String msg = "Error occurred while converting PEM file to X509Certificate";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
        }
    }

    /**
     * Get all certificates in a paginated manner.
     *
     * @param offset index of the first record to be fetched
     * @param limit  number of records to be fetched starting from the start index.
     * @return paginated result of certificate.
     */
    @GET
    public Response getAllCertificates(
            @QueryParam("serialNumber") String serialNumber,
            @QueryParam("deviceIdentifier") String deviceIdentifier,
            @QueryParam("username") String username,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        RequestValidationUtil.validatePaginationInfo(offset, limit);
        CertificateManagementService certificateService = CertificateMgtAPIUtils.getCertificateManagementService();
        CertificatePaginationRequest request = new CertificatePaginationRequest(offset, limit);

        if (StringUtils.isNotEmpty(serialNumber)) {
            request.setSerialNumber(serialNumber);
        }
        if (StringUtils.isNotEmpty(deviceIdentifier)){
            request.setDeviceIdentifier(deviceIdentifier);
        }
        if (StringUtils.isNotEmpty(username)){
            request.setUsername(username);
        }
        try {
            PaginationResult result = certificateService.getAllCertificates(request);
            CertificateList certificates = new CertificateList();
            certificates.setCount(result.getRecordsTotal());
            certificates.setList((List<CertificateResponse>) result.getData());
            return Response.status(Response.Status.OK).entity(certificates).build();
        } catch (CertificateManagementException e) {
            String msg = "Error occurred while fetching all certificates.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @DELETE
    public Response removeCertificate(@QueryParam("serialNumber") String serialNumber) {
        RequestValidationUtil.validateSerialNumber(serialNumber);

        CertificateManagementService certificateService = CertificateMgtAPIUtils.getCertificateManagementService();
        try {
            boolean decision = certificateService.getValidateMetaValue();
            if (decision) {
                try {
                    boolean status = certificateService.removeCertificate(serialNumber);
                    if (!status) {
                        return Response.status(Response.Status.NOT_FOUND).entity(
                                "No certificate is found with the given " +
                                        "serial number '" + serialNumber + "'").build();
                    } else {
                        return Response.status(Response.Status.OK).entity(
                                "Certificate that carries the serial number '" +
                                        serialNumber + "' has been removed").build();
                    }
                } catch (CertificateManagementException e) {
                    String msg = "Error occurred while removing certificate with the given " +
                            "serial number '" + serialNumber + "'";
                    log.error(msg, e);
                    return Response.serverError().entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
                }
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity(
                        "User unauthorized to delete certificate with " +
                                "serial number '" + serialNumber + "'").build();
            }
        } catch (CertificateManagementException e) {
            String msg = "Error occurred while getting the metadata entry for certificate deletion.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @POST
    @Path("/verify/{type}")
    public Response verifyCertificate(@PathParam("type") String type, EnrollmentCertificate certificate) {
        try {
            CertificateManagementService certMgtService = CertificateMgtAPIUtils.getCertificateManagementService();

            if (DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS.equalsIgnoreCase(type)) {
                X509Certificate cert = certMgtService.extractCertificateFromSignature(certificate.getPem());
                String challengeToken = certMgtService.extractChallengeToken(cert);

                if (challengeToken != null) {
                    Pattern regexPattern = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9-]+$");
                    Matcher regexMatcher = regexPattern.matcher(challengeToken);
                    if (regexMatcher.find()) {
                        challengeToken = regexMatcher.group();
                    }
                    challengeToken = challengeToken.substring(challengeToken.indexOf("(") + 1).trim();

                    SCEPManager scepManager = CertificateMgtAPIUtils.getSCEPManagerService();
                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(challengeToken);
                    deviceIdentifier.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS);
                    TenantedDeviceWrapper tenantedDeviceWrapper = scepManager.getValidatedDevice(deviceIdentifier);

                    Map<String, String> claims = new HashMap<>();

                    claims.put("http://wso2.org/claims/enduserTenantId",
                            String.valueOf(tenantedDeviceWrapper.getTenantId()));
                    claims.put("http://wso2.org/claims/enduser",
                            tenantedDeviceWrapper.getDevice().getEnrolmentInfo().getOwner() + "@"
                                    + tenantedDeviceWrapper.getTenantDomain());
                    claims.put("http://wso2.org/claims/deviceIdentifier",
                            tenantedDeviceWrapper.getDevice().getDeviceIdentifier());
                    claims.put("http://wso2.org/claims/deviceIdType", tenantedDeviceWrapper.getDevice().getType());

                    String jwdToken;
                    try {
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                .setTenantId(tenantedDeviceWrapper.getTenantId());
                        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                .setTenantDomain(tenantedDeviceWrapper.getTenantDomain());
                        JWTClientManagerService jwtClientManagerService = CertificateMgtAPIUtils
                                .getJwtClientManagerService();
                        jwdToken = jwtClientManagerService.getJWTClient()
                                .getJwtToken(tenantedDeviceWrapper.getDevice().getEnrolmentInfo().getOwner(), claims,
                                        true);
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }

                    ValidationResponse validationResponse = new ValidationResponse();
                    validationResponse.setDeviceId(challengeToken);
                    validationResponse.setDeviceType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS);
                    validationResponse.setJWTToken(jwdToken);
                    validationResponse.setTenantId(tenantedDeviceWrapper.getTenantId());

                    return Response.status(Response.Status.OK).entity(validationResponse).build();
                }
            }

            if (DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID.equalsIgnoreCase(type)) {
                CertificateResponse certificateResponse = null;
                if (certificate.getSerial().toLowerCase().contains(PROXY_AUTH_MUTUAL_HEADER)) {
                    certificateResponse = certMgtService.verifySubjectDN(certificate.getPem());
                } else {
                    X509Certificate clientCertificate = certMgtService.pemToX509Certificate(certificate.getPem());
                    if (clientCertificate != null) {
                        certificateResponse = certMgtService.verifyPEMSignature(clientCertificate);
                    }
                }

                if (certificateResponse != null && certificateResponse.getCommonName() != null && !certificateResponse
                        .getCommonName().isEmpty()) {
                    return Response.status(Response.Status.OK).entity("valid").build();
                }
            }
        } catch (SCEPException e) {
            String msg = "Error occurred while extracting information from certificate.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
        } catch (KeystoreException e) {
            String msg = "Error occurred while converting PEM file to X509Certificate.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
        } catch (JWTClientException e) {
            String msg = "Error occurred while getting jwt token.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity("invalid").build();
    }
}
