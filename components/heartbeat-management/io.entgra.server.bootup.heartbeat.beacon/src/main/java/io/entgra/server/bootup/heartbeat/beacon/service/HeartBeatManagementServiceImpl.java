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
import io.entgra.server.bootup.heartbeat.beacon.dto.ElectedCandidate;
import io.entgra.server.bootup.heartbeat.beacon.dto.HeartBeatEvent;
import io.entgra.server.bootup.heartbeat.beacon.dto.ServerContext;
import io.entgra.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import io.entgra.server.bootup.heartbeat.beacon.internal.HeartBeatBeaconDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.ServerCtxInfo;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class HeartBeatManagementServiceImpl implements HeartBeatManagementService {

    private static final Log log = LogFactory.getLog(HeartBeatManagementServiceImpl.class);

    private final HeartBeatDAO heartBeatDAO;

    public HeartBeatManagementServiceImpl() {
        this.heartBeatDAO = HeartBeatBeaconDAOFactory.getHeartBeatDAO();
    }


    @Override
    public ServerCtxInfo getServerCtxInfo() throws HeartBeatManagementException {
        int hashIndex = -1;
        ServerContext localServerCtx = null;
        ServerCtxInfo serverCtxInfo = null;
        if (HeartBeatBeaconConfig.getInstance().isEnabled()) {
            try {
                HeartBeatBeaconDAOFactory.openConnection();
                int timeOutIntervalInSeconds = HeartBeatBeaconConfig.getInstance().getServerTimeOutIntervalInSeconds();
                int timeSkew = HeartBeatBeaconConfig.getInstance().getTimeSkew();
                int cumilativeTimeOut = timeOutIntervalInSeconds + timeSkew;
                String localServerUUID = HeartBeatBeaconDataHolder.getInstance().getLocalServerUUID();
                Map<String, ServerContext> serverCtxMap = heartBeatDAO.getActiveServerDetails(cumilativeTimeOut);
                if (!serverCtxMap.isEmpty()) {
                    localServerCtx = serverCtxMap.get(localServerUUID);
                    if (localServerCtx != null) {
                        hashIndex = localServerCtx.getIndex();
                        serverCtxInfo = new ServerCtxInfo(serverCtxMap.size(), hashIndex);
                    }
                }
            } catch (SQLException e) {
                String msg = "Error occurred while opening a connection to the underlying data source";
                log.error(msg, e);
                throw new HeartBeatManagementException(msg, e);
            } catch (HeartBeatDAOException e) {
                String msg = "Error occurred while retrieving active server count.";
                log.error(msg, e);
                throw new HeartBeatManagementException(msg, e);
            } finally {
                HeartBeatBeaconDAOFactory.closeConnection();
            }
        } else {
            String msg = "Heart Beat Configuration Disabled. Server Context Information Not available.";
            log.error(msg);
            throw new HeartBeatManagementException(msg);
        }
        return serverCtxInfo;
    }

    @Override
    public boolean isTaskPartitioningEnabled() throws HeartBeatManagementException {
        boolean enabled = false;
        if (HeartBeatBeaconConfig.getInstance() != null) {
            enabled = HeartBeatBeaconConfig.getInstance().isEnabled();
        } else {
            String msg = "Issue instantiating heart beat config.";
            log.error(msg);
            throw new HeartBeatManagementException(msg);
        }
        return enabled;
    }


    @Override
    public String updateServerContext(ServerContext ctx) throws HeartBeatManagementException {
        String uuid = null;
        if (HeartBeatBeaconConfig.getInstance().isEnabled()) {
            try {
                HeartBeatBeaconDAOFactory.beginTransaction();
                uuid = heartBeatDAO.retrieveExistingServerCtx(ctx);
                if (uuid == null) {
                    uuid = heartBeatDAO.recordServerCtx(ctx);
                    HeartBeatBeaconDAOFactory.commitTransaction();
                }
            } catch (HeartBeatDAOException e) {
                String msg = "Error Occured while retrieving server context.";
                log.error(msg, e);
                throw new HeartBeatManagementException(msg, e);
            } catch (TransactionManagementException e) {
                HeartBeatBeaconDAOFactory.rollbackTransaction();
                String msg = "Error occurred while updating server context. Issue in opening a connection to the underlying data source";
                log.error(msg, e);
                throw new HeartBeatManagementException(msg, e);
            } finally {
                HeartBeatBeaconDAOFactory.closeConnection();
            }
        } else {
            String msg = "Heart Beat Configuration Disabled. Updating Server Context Failed.";
            log.error(msg);
            throw new HeartBeatManagementException(msg);
        }
        return uuid;
    }

    @Override
    public boolean isQualifiedToExecuteTask() throws HeartBeatManagementException {
        boolean isQualified = false;
        if (HeartBeatBeaconConfig.getInstance().isEnabled()) {
            try {
                String localServerUUID = HeartBeatBeaconDataHolder.getInstance().getLocalServerUUID();
                HeartBeatBeaconDAOFactory.openConnection();
                ElectedCandidate candidate = heartBeatDAO.retrieveCandidate();
                if (candidate != null && candidate.getServerUUID().equalsIgnoreCase(localServerUUID)) {
                    isQualified = true;
                    if (log.isDebugEnabled()) {
                        log.debug("Node : " + localServerUUID + " Qualified to execute randomly assigned task.");
                    }
                }
            } catch (HeartBeatDAOException e) {
                String msg = "Error occurred while checking if server is qualified to execute randomly designated task.";
                log.error(msg, e);
                throw new HeartBeatManagementException(msg, e);
            } catch (SQLException e) {
                String msg = "Error occurred while opening a connection to the underlying data source";
                log.error(msg, e);
                throw new HeartBeatManagementException(msg, e);
            } finally {
                HeartBeatBeaconDAOFactory.closeConnection();
            }
        } else {
            String msg = "Heart Beat Configuration Disabled. Error occurred while checking if server is qualified to execute randomly designated task.";
            log.error(msg);
            throw new HeartBeatManagementException(msg);
        }
        return isQualified;
    }

    @Override
    public boolean updateTaskExecutionAcknowledgement(String newTask)
            throws HeartBeatManagementException {
        boolean result = false;
        if (HeartBeatBeaconConfig.getInstance().isEnabled()) {
            try {
                String serverUUID = HeartBeatBeaconDataHolder.getInstance().getLocalServerUUID();
                HeartBeatBeaconDAOFactory.beginTransaction();
                ElectedCandidate candidate = heartBeatDAO.retrieveCandidate();
                if (candidate != null && candidate.getServerUUID().equals(serverUUID)) {
                    List<String> taskList = candidate.getAcknowledgedTaskList();
                    boolean taskExecuted = false;
                    for (String task : taskList) {
                        if (task.equalsIgnoreCase(newTask)) {
                            taskExecuted = true;
                            break;
                        }
                    }
                    if (!taskExecuted) {
                        taskList.add(newTask);
                        result = heartBeatDAO.acknowledgeTask(serverUUID, taskList);
                        HeartBeatBeaconDAOFactory.commitTransaction();
                    }
                }
            } catch (HeartBeatDAOException e) {
                String msg = "Error occurred while updating acknowledged task.";
                log.error(msg, e);
                throw new HeartBeatManagementException(msg, e);
            } catch (TransactionManagementException e) {
                HeartBeatBeaconDAOFactory.rollbackTransaction();
                String msg = "Error occurred while updating acknowledged task.. Issue in opening a connection to the underlying data source";
                log.error(msg, e);
                throw new HeartBeatManagementException(msg, e);
            } finally {
                HeartBeatBeaconDAOFactory.closeConnection();
            }
        } else {
            String msg = "Heart Beat Configuration Disabled. Updating acknowledged task list failed.";
            log.error(msg);
            throw new HeartBeatManagementException(msg);
        }
        return result;
    }


    @Override
    public void electCandidate(int elapsedTimeInSeconds) throws HeartBeatManagementException {
        if (HeartBeatBeaconConfig.getInstance().isEnabled()) {
            try {
                HeartBeatBeaconDAOFactory.beginTransaction();
                Map<String, ServerContext> servers = heartBeatDAO.getActiveServerDetails(elapsedTimeInSeconds);
                if (servers != null && !servers.isEmpty()) {
                    ElectedCandidate presentCandidate = heartBeatDAO.retrieveCandidate();
                    if (presentCandidate != null) {
                        //if candidate is older than stipulated elapsed-time, purge and re-elect
                        if (presentCandidate.getTimeOfElection().before(new Timestamp(System.currentTimeMillis()
                                                                                      - TimeUnit.SECONDS.toMillis(elapsedTimeInSeconds)))) {
                            heartBeatDAO.purgeCandidates();
                            electCandidate(servers);
                        }
                    } else {
                        //first time execution, elect if not present
                        electCandidate(servers);
                    }
                    HeartBeatBeaconDAOFactory.commitTransaction();
                }
            } catch (HeartBeatDAOException e) {
                String msg = "Error occurred while electing candidate for dynamic task execution.";
                log.error(msg, e);
                throw new HeartBeatManagementException(msg, e);
            } catch (TransactionManagementException e) {
                HeartBeatBeaconDAOFactory.rollbackTransaction();
                String msg = "Error occurred while electing candidate for dynamic task execution. Issue in opening a connection to the underlying data source";
                log.error(msg, e);
                throw new HeartBeatManagementException(msg, e);
            } finally {
                HeartBeatBeaconDAOFactory.closeConnection();
            }
        } else {
            String msg = "Heart Beat Configuration Disabled. Error electing candidate for dynamic task execution.";
            log.error(msg);
            throw new HeartBeatManagementException(msg);
        }
    }

    private void electCandidate(Map<String, ServerContext> servers) throws HeartBeatDAOException {
        String electedCandidate = getRandomElement(servers.keySet());
        heartBeatDAO.recordElectedCandidate(electedCandidate);
    }


    private String getRandomElement(Set<String> valueSet) {
        Random rand = new Random();
        List<String> items = new ArrayList<>(valueSet);
        return items.get(rand.nextInt(items.size()));
    }


    @Override
    public boolean recordHeartBeat(HeartBeatEvent event) throws HeartBeatManagementException {
        boolean operationSuccess = false;
        if (HeartBeatBeaconConfig.getInstance().isEnabled()) {
            try {
                HeartBeatBeaconDAOFactory.beginTransaction();
                if (heartBeatDAO.checkUUIDValidity(event.getServerUUID())) {
                    operationSuccess = heartBeatDAO.recordHeatBeat(event);
                    HeartBeatBeaconDAOFactory.commitTransaction();
                } else {
                    String msg = "Server UUID Does not exist, heartbeat not recorded.";
                    log.error(msg);
                    throw new HeartBeatManagementException(msg);
                }
            } catch (HeartBeatDAOException e) {
                String msg = "Error occurred while recording heart beat.";
                log.error(msg);
                throw new HeartBeatManagementException(msg, e);
            } catch (TransactionManagementException e) {
                HeartBeatBeaconDAOFactory.rollbackTransaction();
                String msg = "Error occurred performing heart beat record transaction. " +
                             "Transaction rolled back.";
                log.error(msg, e);
                throw new HeartBeatManagementException(msg, e);
            } finally {
                HeartBeatBeaconDAOFactory.closeConnection();
            }
        } else {
            String msg = "Heart Beat Configuration Disabled. Recording Heart Beat Failed.";
            log.error(msg);
            throw new HeartBeatManagementException(msg);
        }
        return operationSuccess;
    }

    @Override
    public Map<Integer, ServerContext> getActiveServers() throws HeartBeatManagementException {
        Map<Integer, ServerContext> activeServers = new HashMap<>();

        if (HeartBeatBeaconConfig.getInstance().isEnabled()) {
            try {
                HeartBeatBeaconDAOFactory.openConnection();
                int timeOutIntervalInSeconds = HeartBeatBeaconConfig.getInstance().getServerTimeOutIntervalInSeconds();
                int timeSkew = HeartBeatBeaconConfig.getInstance().getTimeSkew();
                int cumilativeTimeOut = timeOutIntervalInSeconds + timeSkew;
                Map<String, ServerContext> serverCtxMap = heartBeatDAO.getActiveServerDetails(cumilativeTimeOut);
                for (String uuid : serverCtxMap.keySet()) {
                    ServerContext serverContext = serverCtxMap.get(uuid);
                    activeServers.put(serverContext.getIndex(), serverContext);
                }
            } catch (SQLException e) {
                String msg = "Error occurred while opening a connection to the underlying data source";
                log.error(msg, e);
                throw new HeartBeatManagementException(msg, e);
            } catch (HeartBeatDAOException e) {
                String msg = "Error occurred while retrieving active server details.";
                log.error(msg, e);
                throw new HeartBeatManagementException(msg, e);
            } finally {
                HeartBeatBeaconDAOFactory.closeConnection();
            }
        } else {
            String msg = "Heart Beat Configuration Disabled. Server Context Information Not available.";
            log.error(msg);
            throw new HeartBeatManagementException(msg);
        }
        return activeServers;
    }
}
