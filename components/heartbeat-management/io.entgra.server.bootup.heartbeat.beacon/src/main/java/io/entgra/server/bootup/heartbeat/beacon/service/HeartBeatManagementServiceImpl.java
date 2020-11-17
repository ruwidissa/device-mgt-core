/*
 * Copyright (c) 2020, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
 *
 * Entgra Pvt Ltd. licenses this file to you under the Apache License,
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

package io.entgra.server.bootup.heartbeat.beacon.service;

import io.entgra.server.bootup.heartbeat.beacon.config.HeartBeatBeaconConfig;
import io.entgra.server.bootup.heartbeat.beacon.dao.HeartBeatBeaconDAOFactory;
import io.entgra.server.bootup.heartbeat.beacon.dao.HeartBeatDAO;
import io.entgra.server.bootup.heartbeat.beacon.dao.exception.HeartBeatDAOException;
import io.entgra.server.bootup.heartbeat.beacon.dto.HeartBeatEvent;
import io.entgra.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import io.entgra.server.bootup.heartbeat.beacon.dto.ServerContext;
import io.entgra.server.bootup.heartbeat.beacon.internal.HeartBeatBeaconDataHolder;
import org.wso2.carbon.device.mgt.common.ServerCtxInfo;

import java.sql.SQLException;
import java.util.Map;

public class HeartBeatManagementServiceImpl implements HeartBeatManagementService {

    @Override
    public ServerCtxInfo getServerCtxInfo() throws HeartBeatManagementException {
        HeartBeatDAO heartBeatDAO;
        int hashIndex = -1;
        ServerContext localServerCtx = null;
        ServerCtxInfo serverCtxInfo = null;
        try {
            HeartBeatBeaconDAOFactory.openConnection();
            heartBeatDAO = HeartBeatBeaconDAOFactory.getHeartBeatDAO();

            int timeOutIntervalInSeconds = HeartBeatBeaconConfig.getInstance().getServerTimeOutIntervalInSeconds();
            int timeSkew = HeartBeatBeaconConfig.getInstance().getTimeSkew();
            int cumilativeTimeOut = timeOutIntervalInSeconds + timeSkew;
            String localServerUUID = HeartBeatBeaconDataHolder.getInstance().getLocalServerUUID();
            Map<String, ServerContext> serverCtxMap = heartBeatDAO.getActiveServerDetails(cumilativeTimeOut);
            if(!serverCtxMap.isEmpty()) {
                localServerCtx = serverCtxMap.get(localServerUUID);
                if (localServerCtx != null) {
                    hashIndex = localServerCtx.getIndex();
                    serverCtxInfo = new ServerCtxInfo(serverCtxMap.size(), hashIndex);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the underlying data source";
            throw new HeartBeatManagementException(msg, e);
        } catch (HeartBeatDAOException e) {
            String msg = "Error occurred while retrieving active server count.";
            throw new HeartBeatManagementException(msg, e);
        } finally {
            HeartBeatBeaconDAOFactory.closeConnection();
        }
        return serverCtxInfo;
    }

    @Override
    public String updateServerContext(ServerContext ctx) throws HeartBeatManagementException {
        HeartBeatDAO heartBeatDAO;
        String uuid = null;
        try {
            HeartBeatBeaconDAOFactory.openConnection();
            heartBeatDAO = HeartBeatBeaconDAOFactory.getHeartBeatDAO();

            uuid = heartBeatDAO.retrieveExistingServerCtx(ctx);
            if(uuid == null){
                uuid = heartBeatDAO.recordServerCtx(ctx);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the underlying data source";
            throw new HeartBeatManagementException(msg, e);
        } catch (HeartBeatDAOException e) {
            String msg = "Error Occured while retrieving active server count.";
            throw new HeartBeatManagementException(msg, e);
        } finally {
            HeartBeatBeaconDAOFactory.closeConnection();
        }
        return uuid;
    }


    @Override
    public boolean recordHeartBeat(HeartBeatEvent event) throws HeartBeatManagementException {
        HeartBeatDAO heartBeatDAO;
        boolean operationSuccess = false;
        try {
            HeartBeatBeaconDAOFactory.openConnection();
            heartBeatDAO = HeartBeatBeaconDAOFactory.getHeartBeatDAO();
            operationSuccess = heartBeatDAO.recordHeatBeat(event);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the underlying data source";
            throw new HeartBeatManagementException(msg, e);
        } catch (HeartBeatDAOException e) {
            String msg = "Error Occured while retrieving active server count.";
            throw new HeartBeatManagementException(msg, e);
        } finally {
            HeartBeatBeaconDAOFactory.closeConnection();
        }
        return operationSuccess;
    }

}
