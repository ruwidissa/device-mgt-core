package io.entgra.device.mgt.core.application.mgt.core.dao;

import io.entgra.device.mgt.core.application.mgt.common.dto.VppUserDTO;
import io.entgra.device.mgt.core.application.mgt.core.exception.ApplicationManagementDAOException;


public interface VppApplicationDAO {

    int addVppUser(VppUserDTO userDTO) throws ApplicationManagementDAOException;

    VppUserDTO updateVppUser(VppUserDTO userDTO) throws ApplicationManagementDAOException;

    VppUserDTO getUserByDMUsername(String emmUsername) throws ApplicationManagementDAOException;
}
