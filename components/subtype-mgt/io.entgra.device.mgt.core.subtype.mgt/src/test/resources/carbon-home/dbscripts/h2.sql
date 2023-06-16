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
 */ -- -----------------------------------------------------
 -- Table `DM_DEVICE_SUB_TYPE`
 -- -----------------------------------------------------

 CREATE TABLE IF NOT EXISTS `DM_DEVICE_SUB_TYPE` (
   `TENANT_ID` INT DEFAULT 0,
   `SUB_TYPE_ID` VARCHAR(45) NOT NULL,
   `DEVICE_TYPE` VARCHAR(25) NOT NULL,
   `SUB_TYPE_NAME` VARCHAR(45) NOT NULL,
   `TYPE_DEFINITION` TEXT NOT NULL,
    PRIMARY KEY (`SUB_TYPE_ID`,`DEVICE_TYPE`)
 );

-- -----------------------------------------------------
-- Sample data for test cases
-- -----------------------------------------------------

INSERT INTO DM_DEVICE_SUB_TYPE (SUB_TYPE_ID, TENANT_ID, DEVICE_TYPE, SUB_TYPE_NAME, TYPE_DEFINITION) VALUES
(3,-1234,'Meter','TestSubType','{"make": "TestSubType", "model": "ATx-Mega SIM800", "subTypeId": 3, "hasSMSSupport": true, "hasICMPSupport": true, "socketServerPort": 8071}'),
(4,-1234,'Meter','TestSubType','{"make": "TestSubType", "model": "ATx-Mega SIM800", "subTypeId": 4, "hasSMSSupport": true, "hasICMPSupport": true, "socketServerPort": 8071}');

