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

package io.entgra.device.mgt.core.application.mgt.core.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.entgra.device.mgt.core.application.mgt.common.DepConfig;
import io.entgra.device.mgt.core.application.mgt.common.dto.ItuneAppDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.ProxyResponse;
import io.entgra.device.mgt.core.application.mgt.common.dto.VppAssetDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.VppItuneUserDTO;
import io.entgra.device.mgt.core.application.mgt.common.exception.DBConnectionException;
import io.entgra.device.mgt.core.application.mgt.common.exception.TransactionManagementException;
import io.entgra.device.mgt.core.application.mgt.common.response.Application;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.VppItuneUserRequestWrapper;
import io.entgra.device.mgt.core.application.mgt.common.dto.VppUserDTO;
import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.device.mgt.core.application.mgt.common.services.VPPApplicationManager;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.VppItuneAssetResponseWrapper;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.VppItuneUserRequestWrapper;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.VppItuneUserResponseWrapper;
import io.entgra.device.mgt.core.application.mgt.core.dao.ApplicationDAO;
import io.entgra.device.mgt.core.application.mgt.core.dao.SPApplicationDAO;
import io.entgra.device.mgt.core.application.mgt.core.dao.VisibilityDAO;
import io.entgra.device.mgt.core.application.mgt.core.dao.VppApplicationDAO;
import io.entgra.device.mgt.core.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import io.entgra.device.mgt.core.application.mgt.core.exception.ApplicationManagementDAOException;
import io.entgra.device.mgt.core.application.mgt.core.internal.DataHolder;
import io.entgra.device.mgt.core.application.mgt.core.lifecycle.LifecycleStateManager;
import io.entgra.device.mgt.core.application.mgt.core.util.ApplicationManagementUtil;
import io.entgra.device.mgt.core.application.mgt.core.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.application.mgt.core.util.Constants;
import io.entgra.device.mgt.core.application.mgt.core.util.VppHttpUtil;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.license.mgt.License;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.IOException;
import java.util.List;

public class VppApplicationManagerImpl implements VPPApplicationManager {
    private static final String APP_API = "https://vpp.itunes.apple.com/mdm/v2";
    private static final String ASSETS = APP_API + "/assets";
    private static final String USER_CREATE = APP_API + "/users/create";
    private static final String USER_UPDATE = APP_API + "/users/update";
    private static final String USER_GET = APP_API + "/users";
    private static final String TOKEN = "";
    private static final String LOOKUP_API = "https://uclient-api.itunes.apple" +
            ".com/WebObjects/MZStorePlatform.woa/wa/lookup?version=2&id=";
    private static final String LOOKUP_API_PREFIX =
            "&p=mdm-lockup&caller=MDM&platform=enterprisestore&cc=us&l=en";


    private static final Log log = LogFactory.getLog(VppApplicationManagerImpl.class);

    private ApplicationDAO applicationDAO;
    private SPApplicationDAO spApplicationDAO;
    private VisibilityDAO visibilityDAO;
    private final LifecycleStateManager lifecycleStateManager;
    private VppApplicationDAO vppApplicationDAO;

    public VppApplicationManagerImpl() {
        initDataAccessObjects();
        lifecycleStateManager = DataHolder.getInstance().getLifecycleStateManager();
    }

    private void initDataAccessObjects() {
        this.applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
        this.visibilityDAO = ApplicationManagementDAOFactory.getVisibilityDAO();
        this.spApplicationDAO = ApplicationManagementDAOFactory.getSPApplicationDAO();
        this.vppApplicationDAO = ApplicationManagementDAOFactory.getVppApplicationDAO();
    }

    @Override
    public VppUserDTO addUser(VppUserDTO userDTO) throws ApplicationManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);

        // Call the API to add
        try {
            VppItuneUserDTO ituneUserDTO = userDTO;
            VppItuneUserRequestWrapper wrapper = new VppItuneUserRequestWrapper();
            wrapper.getUser().add(ituneUserDTO);

            Gson gson = new Gson();
//            Gson gson = new GsonBuilder()
//                    .setExclusionStrategies(new NullEmptyExclusionStrategy())
//                    .create();
            String userPayload = gson.toJson(wrapper);

            ProxyResponse proxyResponse = callVPPBackend(USER_CREATE, userPayload, TOKEN, Constants.VPP.POST);
            if ((proxyResponse.getCode() == HttpStatus.SC_OK || proxyResponse.getCode() ==
                    HttpStatus.SC_CREATED) && proxyResponse.getData().contains(Constants.VPP.EVENT_ID)) {
                // Create user does not return any useful data. Its needed to call the backend again
                ProxyResponse getUserResponse = callVPPBackend(USER_GET + Constants.VPP.CLIENT_USER_ID_PARAM +
                                userDTO.getClientUserId(), userPayload, TOKEN, Constants.VPP.GET);
                if ((getUserResponse.getCode() == HttpStatus.SC_OK || getUserResponse.getCode() ==
                        HttpStatus.SC_CREATED) && getUserResponse.getData().contains(Constants.VPP.TOTAL_PAGES)) {
                    VppItuneUserResponseWrapper vppItuneUserResponseWrapper = gson.fromJson
                            (getUserResponse.getData(), VppItuneUserResponseWrapper.class);
                    userDTO.setInviteCode(vppItuneUserResponseWrapper.getUser().get(0)
                            .getInviteCode());
                    userDTO.setStatus(vppItuneUserResponseWrapper.getUser().get(0).getStatus());
                    log.error("userDTO " + userDTO.toString());
                    try {
                        ConnectionManagerUtil.beginDBTransaction();
                        if (vppApplicationDAO.addVppUser(userDTO, tenantId) != -1) {
                            ConnectionManagerUtil.commitDBTransaction();
                            return userDTO;
                        }
                        ConnectionManagerUtil.rollbackDBTransaction();
                        return null;
                    } catch (ApplicationManagementDAOException e) {
                        ConnectionManagerUtil.rollbackDBTransaction();
                        String msg = "Error occurred while adding the Vpp User.";
                        log.error(msg, e);
                        throw new ApplicationManagementException(msg, e);
                    } catch (TransactionManagementException e) {
                        String msg = "Error occurred while executing database transaction for adding Vpp User.";
                        log.error(msg, e);
                        throw new ApplicationManagementException(msg, e);
                    } catch (DBConnectionException e) {
                        String msg = "Error occurred while retrieving the database connection for adding Vpp User.";
                        log.error(msg, e);
                        throw new ApplicationManagementException(msg, e);
                    } finally {
                        ConnectionManagerUtil.closeDBConnection();
                    }
                }

            }
        } catch (IOException e) {
            String msg = "Error while calling VPP backend to add user";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
        return null;
    }

    @Override
    public VppUserDTO getUserByDMUsername(String emmUsername) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.openDBConnection();
            return vppApplicationDAO.getUserByDMUsername(emmUsername, tenantId);
        } catch (DBConnectionException e) {
            String msg = "DB Connection error occurs while getting vpp User data related to EMM user  " + emmUsername + ".";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }  catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred while getting vpp User data related to EMM user  " + emmUsername + ".";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void updateUser(VppUserDTO userDTO) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        VppItuneUserDTO ituneUserDTO = userDTO;
        VppItuneUserRequestWrapper wrapper = new VppItuneUserRequestWrapper();
        wrapper.getUser().add(ituneUserDTO);

        Gson gson = new Gson();
        String userPayload = gson.toJson(wrapper);
        try {
            ProxyResponse proxyResponse = callVPPBackend(USER_UPDATE, userPayload, TOKEN, Constants.VPP.POST);
            if ((proxyResponse.getCode() == HttpStatus.SC_OK || proxyResponse.getCode() ==
                    HttpStatus.SC_CREATED) && proxyResponse.getData().contains(Constants.VPP.EVENT_ID)) {

                log.error("userDTO " + userDTO.toString());

                try {
                    ConnectionManagerUtil.beginDBTransaction();
                    if (vppApplicationDAO.updateVppUser(userDTO, tenantId) == null) {
                        ConnectionManagerUtil.rollbackDBTransaction();
                        String msg = "Unable to update the Vpp user " +userDTO.getId();
                        log.error(msg);
                        throw new ApplicationManagementException(msg);
                    }
                    ConnectionManagerUtil.commitDBTransaction();
                } catch (ApplicationManagementDAOException e) {
                    ConnectionManagerUtil.rollbackDBTransaction();
                    String msg = "Error occurred while updating the Vpp User.";
                    log.error(msg, e);
                    throw new ApplicationManagementException(msg, e);
                } catch (TransactionManagementException e) {
                    String msg = "Error occurred while executing database transaction for Vpp User update.";
                    log.error(msg, e);
                    throw new ApplicationManagementException(msg, e);
                } catch (DBConnectionException e) {
                    String msg = "Error occurred while retrieving the database connection for Vpp User update.";
                    log.error(msg, e);
                    throw new ApplicationManagementException(msg, e);
                } finally {
                    ConnectionManagerUtil.closeDBConnection();
                }
            }
        } catch (IOException e) {
            String msg = "Error while calling VPP backend to update";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    @Override
    public void syncUsers(String clientId) throws ApplicationManagementException {
        ProxyResponse proxyResponse = null;
        try {
            proxyResponse = callVPPBackend(USER_GET, null, TOKEN, Constants
                    .VPP.GET);
            if ((proxyResponse.getCode() == HttpStatus.SC_OK || proxyResponse.getCode() ==
                    HttpStatus.SC_CREATED) && proxyResponse.getData().contains(Constants.VPP.TOTAL_PAGES)) {
                log.error("proxyResponse " + proxyResponse.getData());
                Gson gson = new Gson();
                VppItuneUserResponseWrapper vppUserResponseWrapper = gson.fromJson
                        (proxyResponse.getData(), VppItuneUserResponseWrapper.class);
                // TODO: to implement later
            }
        } catch (IOException e) {
            String msg = "Error while syncing VPP users with backend";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }

    }

    @Override
    public void syncAssets(int nextPageIndex) throws ApplicationManagementException {
        ProxyResponse proxyResponse = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            String url = ASSETS;
            if (nextPageIndex > 0) { // Not the first page
                url += "?pageIndex=" + nextPageIndex;
            }
            proxyResponse = callVPPBackend(url, null, TOKEN, Constants.VPP.GET);
            if ((proxyResponse.getCode() == HttpStatus.SC_OK || proxyResponse.getCode() ==
                    HttpStatus.SC_CREATED) && proxyResponse.getData().contains(Constants.VPP.TOTAL_PAGES)) {
                Gson gson = new Gson();
                VppItuneAssetResponseWrapper vppItuneAssetResponse = gson.fromJson
                        (proxyResponse.getData(), VppItuneAssetResponseWrapper.class);
                if (vppItuneAssetResponse.getSize() > 0) {
                    for (VppAssetDTO vppAssetDTO : vppItuneAssetResponse.getAssets()) {
                        vppAssetDTO.setTenantId(PrivilegedCarbonContext
                                .getThreadLocalCarbonContext().getTenantId());
                        vppAssetDTO.setCreatedTime(String.valueOf(System.currentTimeMillis()));
                        vppAssetDTO.setLastUpdatedTime(String.valueOf(System.currentTimeMillis()));
                    }

                    for (VppAssetDTO vppAssetDTO : vppItuneAssetResponse.getAssets()) {
                        ItuneAppDTO ituneAppDTO = lookupAsset(vppAssetDTO.getAdamId());
                        ApplicationManagementUtil.persistApp(ituneAppDTO);
                        List<Application> applications =  ApplicationManagementUtil.getAppDetails(vppAssetDTO.getAdamId());
                        for (Application application :applications) {
                            VppAssetDTO vppAssetDTOs = getAssetByAppId(application.getId());
                            if (vppAssetDTOs == null) {
                                vppAssetDTOs = new VppAssetDTO();
                                vppAssetDTOs.setAppId(application.getId());
                                try {
                                    ConnectionManagerUtil.beginDBTransaction();
                                    if (vppApplicationDAO.addAsset(vppAssetDTOs, tenantId) != -1) {
                                        ConnectionManagerUtil.commitDBTransaction();
                                    }
                                    ConnectionManagerUtil.rollbackDBTransaction();
                                } catch (ApplicationManagementDAOException e) {
                                    ConnectionManagerUtil.rollbackDBTransaction();
                                    String msg = "Error occurred while adding the Asset.";
                                    log.error(msg, e);
                                    throw new ApplicationManagementException(msg, e);
                                } catch (TransactionManagementException e) {
                                    String msg = "Error occurred while executing database transaction for adding Asset.";
                                    log.error(msg, e);
                                    throw new ApplicationManagementException(msg, e);
                                } catch (DBConnectionException e) {
                                    String msg = "Error occurred while retrieving the database connection for adding Asset.";
                                    log.error(msg, e);
                                    throw new ApplicationManagementException(msg, e);
                                } finally {
                                    ConnectionManagerUtil.closeDBConnection();
                                }
                            } else {
                                vppAssetDTOs.setAppId(application.getId());
                                try {
                                    ConnectionManagerUtil.beginDBTransaction();
                                    if (vppApplicationDAO.updateAsset(vppAssetDTOs, tenantId) == null) {
                                        ConnectionManagerUtil.rollbackDBTransaction();
                                        String msg = "Unable to update the asset: " +vppAssetDTOs.getAdamId();
                                        log.error(msg);
                                        throw new ApplicationManagementException(msg);
                                    }
                                    ConnectionManagerUtil.commitDBTransaction();
                                } catch (ApplicationManagementDAOException e) {
                                    ConnectionManagerUtil.rollbackDBTransaction();
                                    String msg = "Error occurred while updating the Asset.";
                                    log.error(msg, e);
                                    throw new ApplicationManagementException(msg, e);
                                } catch (TransactionManagementException e) {
                                    String msg = "Error occurred while executing database transaction for Asset update.";
                                    log.error(msg, e);
                                    throw new ApplicationManagementException(msg, e);
                                } catch (DBConnectionException e) {
                                    String msg = "Error occurred while retrieving the database connection for Asset update.";
                                    log.error(msg, e);
                                    throw new ApplicationManagementException(msg, e);
                                } finally {
                                    ConnectionManagerUtil.closeDBConnection();
                                }
                            }
                        }
                    }
                }

                if (vppItuneAssetResponse.getCurrentPageIndex() == (vppItuneAssetResponse
                        .getTotalPages() - 1)) {
                    return;
                } else {
                    syncAssets(vppItuneAssetResponse.getNextPageIndex());
                }
            }
        } catch (IOException e) {
            String msg = "Error while syncing VPP users with backend";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    private ItuneAppDTO lookupAsset(String packageName) throws ApplicationManagementException {
        String lookupURL = LOOKUP_API + packageName + LOOKUP_API_PREFIX;
        try {
            ProxyResponse proxyResponse = callVPPBackend(lookupURL, null, TOKEN, Constants.VPP.GET);
            if ((proxyResponse.getCode() == HttpStatus.SC_OK || proxyResponse.getCode() ==
                    HttpStatus.SC_CREATED) && proxyResponse.getData().contains(Constants.VPP.GET_APP_DATA_RESPONSE_START)) {
                String responseData = proxyResponse.getData();
                JsonObject responseJson = new JsonParser().parse(responseData)
                        .getAsJsonObject();

                JsonObject results = responseJson.getAsJsonObject(Constants.ApplicationProperties.RESULTS);
                JsonObject result = results.getAsJsonObject(packageName);

                String iconUrl = result.getAsJsonObject(Constants.ApplicationProperties.ARTWORK)
                        .get(Constants.ApplicationProperties.URL).getAsString();
                int lastSlashIndex = iconUrl.lastIndexOf("/");
                if (lastSlashIndex != -1) {
                    iconUrl = iconUrl.substring(0, lastSlashIndex + 1) + Constants.VPP.REMOTE_FILE_NAME;
                }

                String descriptionStandard = result.getAsJsonObject(Constants.ApplicationProperties.DESCRIPTION)
                        .get(Constants.ApplicationProperties.STANDARD).getAsString();
                if (descriptionStandard != null && !descriptionStandard.isEmpty()) {
                    descriptionStandard = descriptionStandard.substring(0, 199);
                }
                String name = result.get(Constants.ApplicationProperties.NAME).getAsString();
                double price = result.getAsJsonArray(Constants.ApplicationProperties.OFFERS).get(0)
                        .getAsJsonObject().get(Constants.ApplicationProperties.PRICE).getAsDouble();
                String version = result.getAsJsonArray(Constants.ApplicationProperties.OFFERS)
                        .get(0).getAsJsonObject().get(Constants.ApplicationProperties.VERSION)
                        .getAsJsonObject().get(Constants.ApplicationProperties.DISPLAY).getAsString();

                String[] genreNames = new Gson().fromJson(result.getAsJsonArray(Constants.ApplicationProperties.GENRE_NAMES),
                        String[].class);

                ItuneAppDTO ituneAppDTO = new ItuneAppDTO();
                ituneAppDTO.setPackageName(packageName);
                ituneAppDTO.setVersion(version);
                ituneAppDTO.setDescription(descriptionStandard);
                ituneAppDTO.setTitle(name);

                if (Constants.ApplicationProperties.PRICE_ZERO.equalsIgnoreCase(String.valueOf(price))) {
                    ituneAppDTO.setPaymentMethod(Constants.ApplicationProperties.FREE_SUB_METHOD);
                } else {
                    ituneAppDTO.setPaymentMethod(Constants.ApplicationProperties.PAID_SUB_METHOD);
                }
                ituneAppDTO.setIconURL(iconUrl);
                ituneAppDTO.setCategory(genreNames[0]);

                return ituneAppDTO;
            }
        } catch (IOException e) {
            String msg = "Error while looking up the app details";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
        return null;
    }


    public VppAssetDTO getAssetByAppId(int appId) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.openDBConnection();
            return vppApplicationDAO.getAssetByAppId(appId, tenantId);
        } catch (DBConnectionException e) {
            String msg = "DB Connection error occurs while getting asset related to app with app id " + appId + ".";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }  catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred while getting asset data related to  app with app id " + appId + ".";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public ProxyResponse callVPPBackend(String url,
                                        String payload,
                                        String accessToken,
                                        String method) throws IOException {
        return VppHttpUtil.execute(url, payload, accessToken, method);
    }

    public String getVppToken() throws ApplicationManagementException {
        String token = "";
        MetadataManagementService meta = DeviceManagementDataHolder
                .getInstance().getMetadataManagementService();
        Metadata metadata = null;
        try {
            metadata = meta.retrieveMetadata(DeviceManagementConstants.DEP_META_KEY);
            if (metadata != null) {

                Gson g = new Gson();
                DepConfig depConfigs = g.fromJson(metadata.getMetaValue(), DepConfig.class);
                token =  depConfigs.getAccessToken();
                return token;
            }
        }catch (MetadataManagementException e) {
            String msg = "Error when retrieving metadata of vpp feature";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
        return token;
    }
}
