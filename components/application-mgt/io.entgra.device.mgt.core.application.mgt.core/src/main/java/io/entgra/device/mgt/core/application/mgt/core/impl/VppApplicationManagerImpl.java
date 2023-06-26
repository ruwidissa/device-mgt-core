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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.entgra.device.mgt.core.application.mgt.common.dto.ProxyResponse;
import io.entgra.device.mgt.core.application.mgt.common.dto.VppItuneUserDTO;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.VppItuneUserRequestWrapper;
import io.entgra.device.mgt.core.application.mgt.common.dto.VppUserDTO;
import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.device.mgt.core.application.mgt.common.services.VPPApplicationManager;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.VppItuneUserResponseWrapper;
import io.entgra.device.mgt.core.application.mgt.core.dao.ApplicationDAO;
import io.entgra.device.mgt.core.application.mgt.core.dao.SPApplicationDAO;
import io.entgra.device.mgt.core.application.mgt.core.dao.VisibilityDAO;
import io.entgra.device.mgt.core.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import io.entgra.device.mgt.core.application.mgt.core.internal.DataHolder;
import io.entgra.device.mgt.core.application.mgt.core.lifecycle.LifecycleStateManager;
import io.entgra.device.mgt.core.application.mgt.core.util.Constants;
import io.entgra.device.mgt.core.application.mgt.core.util.VppHttpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import java.io.IOException;

public class VppApplicationManagerImpl implements VPPApplicationManager {
    private static final String APP_API = "https://vpp.itunes.apple.com/mdm/v2";
    private static final String ASSETS = APP_API + "/assets";
    private static final String USER_CREATE = APP_API + "/users/create";
    private static final String USER_UPDATE = APP_API + "/users/update";
    private static final String USER_GET = APP_API + "/users";
    private static final String TOKEN = "";

    private static final Log log = LogFactory.getLog(VppApplicationManagerImpl.class);
    private ApplicationDAO applicationDAO;
    private SPApplicationDAO spApplicationDAO;
    private VisibilityDAO visibilityDAO;
    private final LifecycleStateManager lifecycleStateManager;

    public VppApplicationManagerImpl() {
        initDataAccessObjects();
        lifecycleStateManager = DataHolder.getInstance().getLifecycleStateManager();
    }

    private void initDataAccessObjects() {
        this.applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
        this.visibilityDAO = ApplicationManagementDAOFactory.getVisibilityDAO();
        this.spApplicationDAO = ApplicationManagementDAOFactory.getSPApplicationDAO();
    }

    @Override
    public VppUserDTO addUser(VppUserDTO userDTO) throws ApplicationManagementException {
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
                    // TODO: Save the userDTO in the DAO


                    return userDTO;
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
        // TODO: Return from DAO in a tenanted manner
        return null;
    }

    @Override
    public void updateUser(VppUserDTO userDTO) throws ApplicationManagementException {
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
                // TODO: Save the userDTO in the DAO


            }
        } catch (IOException e) {
            String msg = "Error while callng VPP backend to update";
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
            }
        } catch (IOException e) {
            String msg = "Error while syncing VPP users with backend";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }

    }

    @Override
    public ProxyResponse callVPPBackend(String url,
                                        String payload,
                                        String accessToken,
                                        String method) throws IOException {
        return VppHttpUtil.execute(url, payload, accessToken, method);
    }

}
