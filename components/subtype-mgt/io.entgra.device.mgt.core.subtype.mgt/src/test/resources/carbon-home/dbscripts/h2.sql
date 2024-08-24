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
-- SUB_OPERATION_TEMPLATE TABLE--
CREATE TABLE IF NOT EXISTS SUB_OPERATION_TEMPLATE (
  SUB_OPERATION_TEMPLATE_ID INT NOT NULL AUTO_INCREMENT,
  OPERATION_DEFINITION TEXT NOT NULL,
  OPERATION_CODE VARCHAR (100) NOT NULL,
  SUB_TYPE_ID INT NOT NULL,
  DEVICE_TYPE VARCHAR (25) NOT NULL,
  CREATE_TIMESTAMP TIMESTAMP NULL DEFAULT NULL,
  UPDATE_TIMESTAMP TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (SUB_OPERATION_TEMPLATE_ID),
  UNIQUE (SUB_TYPE_ID,OPERATION_CODE, DEVICE_TYPE),
  CONSTRAINT fk_SUB_OPERATION_TEMPLATE_DM_DEVICE_SUB_TYPE FOREIGN KEY (SUB_TYPE_ID, DEVICE_TYPE)
  REFERENCES DM_DEVICE_SUB_TYPE (SUB_TYPE_ID, DEVICE_TYPE)
);
-- -----------------------------------------------------
-- Sample data for test cases
-- -----------------------------------------------------

INSERT INTO DM_DEVICE_SUB_TYPE (SUB_TYPE_ID, TENANT_ID, DEVICE_TYPE, SUB_TYPE_NAME, TYPE_DEFINITION) VALUES
(3,-1234,'Meter','TestSubType','{"make": "TestSubType", "model": "ATx-Mega SIM800", "subTypeId": 3, "hasSMSSupport": true, "hasICMPSupport": true, "socketServerPort": 8071}'),
(4,-1234,'Meter','TestSubType','{"make": "TestSubType", "model": "ATx-Mega SIM800", "subTypeId": 4, "hasSMSSupport": true, "hasICMPSupport": true, "socketServerPort": 8071}');

INSERT INTO DM_DEVICE_SUB_TYPE (SUB_TYPE_ID, TENANT_ID, DEVICE_TYPE, SUB_TYPE_NAME, TYPE_DEFINITION) VALUES (
'5', -1234, 'METER','Microstar - IEC Bulk',
'{
    "compatibleComModules": {
      "4": {
        "subTypeId": 4,
        "make": "Microstar",
        "model": "IEC-GSM",
        "socketServerPort": 5258,
        "hasICMPSupport": true,
        "hasSMSSupport": false,
        "hasNMDSupport":false,
        "tenantId": -1234
      }
    },
    "compatibleComModulesIds": [4],
    "supportedOperations": [
      "BILLING_REGISTERS_RETRIEVE",
      "TIME_SYNC",
      "SELF_TEST",
      "LOAD_PROFILE_RETRIEVE"
    ],
    "defaultRegisters": {
      "METER_ID": "1-0:0.0.0()",
      "METER_FW_VER": "1-0:0.2.0()",
      "TIME": "1-0:0.9.1()",
      "DATE": "1-0:0.9.2()"
    },
    "userClientMapping": {
      "0": 0,
      "1": 1,
      "2": 2
    },
    "make": "MicroStar",
    "model": "IEC Bulk",
    "serverAddress": 0,
    "defaultClientAddress": 0,
    "registerMapping": {
      "0.0.0_0": {
        "obis": "1-0:0.0.0()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_71.7.0": {
        "obis": "1-1:71.7.0",
        "attributeIndex": -1,
        "classId": -1,
        "scalingOption": "CUSTOM",
        "scalar": 1000.0
      },
      "LP": {
        "obis": "P.01",
        "attributeIndex": -1,
        "classId": -1,
        "isProfileRegister": true
      },
      "LP_3.8.0": {
        "obis": "1-1:3.8.0",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_63.7.0": {
        "obis": "1-1:73.7.0",
        "attributeIndex": -1,
        "classId": -1
      },
      "1.8.2_0": {
        "obis": "1-1:1.8.2()",
        "attributeIndex": -1,
        "classId": -1
      },
      "2.8.0*01_0": {
        "obis": "1-1:2.8.0*1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "1.8.2*01_0": {
        "obis": "1-1:1.8.2*1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_72.7.0": {
        "obis": "1-1:72.7.0",
        "attributeIndex": -1,
        "classId": -1,
        "scalingOption": "CUSTOM",
        "scalar": 1000.0
      },
      "2.8.1_0": {
        "obis": "1-1:2.8.1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "0.9.2_0": {
        "obis": "1-0:0.9.2()",
        "attributeIndex": -1,
        "classId": -1
      },
      "31.7.0_0": {
        "obis": "1-1:31.7.0()",
        "attributeIndex": -1,
        "classId": -1
      },
      "2.8.3_0": {
        "obis": "1-1:2.8.3()",
        "attributeIndex": -1,
        "classId": -1
      },
      "51.7.0_0": {
        "obis": "1-1:51.7.0()",
        "attributeIndex": -1,
        "classId": -1
      },
      "1.8.0_0": {
        "obis": "1-1:1.8.0()",
        "attributeIndex": -1,
        "classId": -1
      },
      "10.6.0*01_0": {
        "obis": "1-1:10.6.0*1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "71.7.0_0": {
        "obis": "1-1:71.7.0()",
        "attributeIndex": -1,
        "classId": -1
      },
      "2.8.2*01_0": {
        "obis": "1-1:2.8.2*1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "72.7.0_0": {
        "obis": "1-1:72.7.0()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_9.5.0": {
        "obis": "1-1:9.4.0",
        "attributeIndex": -1,
        "classId": -1
      },
      "52.7.0_0": {
        "obis": "1-1:52.7.0()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_13.7.0": {
        "obis": "1-1:13.7.0",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_31.7.0": {
        "obis": "1-1:31.7.0",
        "attributeIndex": -1,
        "classId": -1,
        "scalingOption": "CUSTOM",
        "scalar": 1000.0
      },
      "0.4.3_0": {
        "obis": "1-0:0.4.3()",
        "attributeIndex": -1,
        "classId": -1
      },
      "0.4.5_0": {
        "obis": "1-0:0.4.5()",
        "attributeIndex": -1,
        "classId": -1
      },
      "1.8.3*01_0": {
        "obis": "1-1:1.8.3*1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "32.7.0_0": {
        "obis": "1-1:32.7.0()",
        "attributeIndex": -1,
        "classId": -1
      },
      "1.8.0*01_0": {
        "obis": "1-1:1.8.0*1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_32.7.0": {
        "obis": "1-1:32.7.0",
        "attributeIndex": -1,
        "classId": -1,
        "scalingOption": "CUSTOM",
        "scalar": 1000.0
      },
      "LP_2.5.0": {
        "obis": "1-1:2.4.0",
        "attributeIndex": -1,
        "classId": -1
      },
      "10.6.0_0": {
        "obis": "1-1:10.6.0()",
        "attributeIndex": -1,
        "classId": -1
      },
      "2.8.3*01_0": {
        "obis": "1-1:2.8.3*1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "1.8.3_0": {
        "obis": "1-1:1.8.3()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_33.7.0": {
        "obis": "1-1:33.7.0",
        "attributeIndex": -1,
        "classId": -1
      },
      "0.9.1_0": {
        "obis": "1-0:0.9.1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "2.8.0_0": {
        "obis": "1-1:2.8.0()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_1.5.0": {
        "obis": "1-1:1.4.0",
        "attributeIndex": -1,
        "classId": -1
      },
      "2.8.2_0": {
        "obis": "1-1:2.8.2()",
        "attributeIndex": -1,
        "classId": -1
      },
      "1.8.1_0": {
        "obis": "1-1:1.8.1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_51.7.0": {
        "obis": "1-1:51.7.0",
        "attributeIndex": -1,
        "classId": -1,
        "scalingOption": "CUSTOM",
        "scalar": 1000.0
      },
      "LP_43.7.0": {
        "obis": "1-1:53.7.0",
        "attributeIndex": -1,
        "classId": -1
      },
      "1.8.1*01_0": {
        "obis": "1-1:1.8.1*1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "9.6.0_0": {
        "obis": "1-1:9.6.0()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_52.7.0": {
        "obis": "1-1:52.7.0",
        "attributeIndex": -1,
        "classId": -1,
        "scalingOption": "CUSTOM",
        "scalar": 1000.0
      },
      "14.7.0_0": {
        "obis": "1-1:14.7.0()",
        "attributeIndex": -1,
        "classId": -1
      },
      "9.6.0*01_0": {
        "obis": "1-1:9.6.0*1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_1.8.0": {
        "obis": "1-1:1.8.0",
        "attributeIndex": -1,
        "classId": -1
      },
      "0.4.6_0": {
        "obis": "1-0:0.4.6()",
        "attributeIndex": -1,
        "classId": -1
      },
      "2.8.1*01_0": {
        "obis": "1-1:2.8.1*1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_10.5.0": {
        "obis": "1-1:10.4.0",
        "attributeIndex": -1,
        "classId": -1
      },
      "0.4.2_0": {
        "obis": "1-0:0.4.2()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_15.4.0": {
        "obis": "1-1:15.4.0",
        "attributeIndex": -1,
        "classId": -1
      },
      "FW_VER": {
        "obis": "1-0:0.2.0()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_2.8.0": {
        "obis": "1-1:2.8.0",
        "attributeIndex": -1,
        "classId": -1
      }
    },
    "transportProtocol": "IEC",
    "defaultComModuleId": 4,
    "defaultComModule": {
      "subTypeId": 4,
      "make": "Microstar",
      "model": "IEC-GSM",
      "socketServerPort": 5258,
      "hasICMPSupport": true,
      "hasSMSSupport": false,
      "hasNMDSupport":false,
      "tenantId": -1234
    }
  }'
);

INSERT INTO DM_DEVICE_SUB_TYPE (SUB_TYPE_ID, TENANT_ID, DEVICE_TYPE, SUB_TYPE_NAME, TYPE_DEFINITION) VALUES (
'6', -1234, 'METER', 'Anteleco - IEC 3Phase',
'{
     "compatibleComModules": {
      "1": {
        "subTypeId": 1,
        "make": "AnteLeco",
        "model": "ATx-Mega SIM800",
        "socketServerPort": 8071,
        "hasICMPSupport": true,
        "hasSMSSupport": true,
        "hasNMDSupport":false,
        "tenantId": -1234
      },
      "8": {
        "subTypeId": 8,
        "make": "AnteLeco",
        "model": "NB-IoT B",
        "socketServerPort": 8071,
        "hasICMPSupport": true,
        "hasSMSSupport": true,
        "hasNMDSupport":false,
        "tenantId": -1234
      },
      "10": {
        "subTypeId": 10,
        "make": "AnteLeco",
        "model": "ATx-Mega SIM800 B",
        "socketServerPort": 8071,
        "hasICMPSupport": true,
        "hasSMSSupport": true,
        "hasNMDSupport":false,
        "tenantId": -1234
            }
    },
    "compatibleComModulesIds": [1,8,10],
    "supportedOperations": [
      "BILLING_REGISTERS_RETRIEVE",
      "REMOTE_RELAY_ON",
      "REMOTE_RELAY_OFF",
      "TIME_SYNC",
      "SELF_TEST",
      "LOAD_PROFILE_RETRIEVE"
    ],
    "defaultRegisters": {
      "METER_ID": "0.0.96.1.0.255()",
      "METER_FW_VER": "1.0.0.2.0.255()",
      "TIME": "1.0.0.9.1()",
      "DATE": "1.0.0.9.2()"
    },
    "userClientMapping": {
      "0": 0,
      "1": 1,
      "2": 2
    },
    "make": "Anteleco",
    "model": "IEC 3Phase",
    "serverAddress": 0,
    "defaultClientAddress": 0,
    "registerMapping": {
      "0.0.0_0": {
        "obis": "0.0.96.1.0.255()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_71.7.0": {
        "obis": "71.7",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP": {
        "obis": "P.01",
        "attributeIndex": -1,
        "classId": -1,
        "isProfileRegister": true
      },
      "CSRQ_RL": {
        "obis": "0.0.96.128.1.0()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_3.8.0": {
        "obis": "3.8",
        "attributeIndex": -1,
        "classId": -1
      },
      "2.8.3*01_0": {
        "obis": "1.0.2.8.3.1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "1.8.3_0": {
        "obis": "1.0.1.8.3.255()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_63.7.0": {
        "obis": "63.5",
        "attributeIndex": -1,
        "classId": -1
      },
      "1.8.2_0": {
        "obis": "1.0.1.8.2.255()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_33.7.0": {
        "obis": "33.5",
        "attributeIndex": -1,
        "classId": -1
      },
      "0.9.1_0": {
        "obis": "1.0.0.9.1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "2.8.0*01_0": {
        "obis": "1.0.2.8.0.1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "2.8.0_0": {
        "obis": "1.0.2.8.0.255()",
        "attributeIndex": -1,
        "classId": -1
      },
      "1.8.2*01_0": {
        "obis": "1.0.1.8.2.1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_72.7.0": {
        "obis": "72.7",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_1.5.0": {
        "obis": "1.5",
        "attributeIndex": -1,
        "classId": -1
      },
      "2.8.2_0": {
        "obis": "1.0.2.8.2.255()",
        "attributeIndex": -1,
        "classId": -1
      },
      "2.8.1_0": {
        "obis": "1.0.2.8.1.255()",
        "attributeIndex": -1,
        "classId": -1
      },
      "0.9.2_0": {
        "obis": "1.0.0.9.2()",
        "attributeIndex": -1,
        "classId": -1
      },
      "31.7.0_0": {
        "obis": "1.0.31.7.0.255()",
        "attributeIndex": -1,
        "classId": -1
      },
      "2.8.3_0": {
        "obis": "1.0.2.8.3.255()",
        "attributeIndex": -1,
        "classId": -1
      },
      "51.7.0_0": {
        "obis": "1.0.51.7.0.255()",
        "attributeIndex": -1,
        "classId": -1
      },
      "1.8.1_0": {
        "obis": "1.0.1.8.1.255()",
        "attributeIndex": -1,
        "classId": -1
      },
      "1.8.0_0": {
        "obis": "1.0.1.8.0.255()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_51.7.0": {
        "obis": "51.7",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_43.7.0": {
        "obis": "43.5",
        "attributeIndex": -1,
        "classId": -1
      },
      "71.7.0_0": {
        "obis": "1.0.71.7.0.255()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_4.8.0": {
        "obis": "4.8",
        "attributeIndex": -1,
        "classId": -1
      },
      "1.8.1*01_0": {
        "obis": "1.0.1.8.1.1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "2.8.2*01_0": {
        "obis": "1.0.2.8.2.1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "72.7.0_0": {
        "obis": "1.0.72.7.0.255()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_52.7.0": {
        "obis": "52.7",
        "attributeIndex": -1,
        "classId": -1
      },
      "14.7.0_0": {
        "obis": "1.0.14.7.0.255()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_9.5.0": {
        "obis": "9.5",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_13.5.0": {
        "obis": "13.5",
        "attributeIndex": -1,
        "classId": -1
      },
      "52.7.0_0": {
        "obis": "1.0.52.7.0.255()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_31.7.0": {
        "obis": "31.7",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_1.8.0": {
        "obis": "1.8",
        "attributeIndex": -1,
        "classId": -1
      },
      "1.8.3*01_0": {
        "obis": "1.0.1.8.3.1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "32.7.0_0": {
        "obis": "1.0.32.7.0.255()",
        "attributeIndex": -1,
        "classId": -1
      },
      "1.8.0*01_0": {
        "obis": "1.0.1.8.0.1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "2.8.1*01_0": {
        "obis": "1.0.2.8.1.1()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_10.5.0": {
        "obis": "10.5",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_32.7.0": {
        "obis": "32.7",
        "attributeIndex": -1,
        "classId": -1
      },
      "FW_VER": {
        "obis": "1.0.0.2.0.255()",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_2.8.0": {
        "obis": "2.8",
        "attributeIndex": -1,
        "classId": -1
      },
      "LP_2.5.0": {
        "obis": "2.5",
        "attributeIndex": -1,
        "classId": -1
      }
    },
    "transportProtocol": "IEC",
    "defaultComModuleId": 10,
    "defaultComModule": {
      "subTypeId": 10,
      "make": "AnteLeco",
      "model": "ATx-Mega SIM800 B",
      "socketServerPort": 8071,
      "hasICMPSupport": true,
      "hasSMSSupport": true,
      "hasNMDSupport":false,
      "tenantId": -1234
    }
  }'
);

INSERT INTO DM_DEVICE_SUB_TYPE (SUB_TYPE_ID, TENANT_ID, DEVICE_TYPE, SUB_TYPE_NAME, TYPE_DEFINITION) VALUES (
'7', -1234, 'METER', 'Anteleco - NMD',
'{
    "compatibleComModules": {
      "5": {
        "subTypeId": 5,
        "make": "AnteLeco",
        "model": "STM32 M65-NMD",
        "socketServerPort": 8071,
        "hasICMPSupport": true,
        "hasSMSSupport": true,
        "hasNMDSupport":true,
        "tenantId": -1234
      },
       "9": {
        "subTypeId": 9,
        "make": "AnteLeco",
        "model": "NB-IoT NMD",
        "socketServerPort": 8071,
        "hasICMPSupport": true,
        "hasSMSSupport": true,
        "hasNMDSupport":true,
        "tenantId": -1234
            }
    },
    "compatibleComModulesIds": [5,9],
    "supportedOperations": [
      "BILLING_REGISTERS_RETRIEVE",
      "STATUS_RETRIEVE",
      "TIME_SYNC",
      "SELF_TEST",
      "LOAD_PROFILE_RETRIEVE"
    ],
    "defaultRegisters": {
      "METER_ID": "0.0.96.1.0.255",
      "METER_FW_VER": "1.0.0.2.0.255",
      "CLOCK": "0.0.1.0.0.255"
    },
    "userClientMapping": {
      "1": 2,
      "2": 3,
      "3": 4
    },
    "make": "Anteleco",
    "model": "NMD",
    "serverAddress": 1,
    "defaultClientAddress": 16,
    "registerMapping": {
      "CO_128.0.12": {
        "obis": "1.0.128.0.12.255",
        "attributeIndex": 2,
        "classId": 1
      },
      "0.0.0_0": {
        "obis": "0.0.96.1.0.255",
        "attributeIndex": 2,
        "classId": 1
      },
      "CO_128.0.11": {
        "obis": "1.0.128.0.11.255",
        "attributeIndex": 2,
        "classId": 1
      },
      "CO_128.32.0": {
        "obis": "1.0.128.32.0.255",
        "attributeIndex": 2,
        "classId": 1
      },
      "LP": {
        "obis": "1.0.99.1.1.255",
        "attributeIndex": 2,
        "classId": 7,
        "isProfileRegister": true
      },
      "CO_128.36.0": {
        "obis": "1.0.128.36.0.255",
        "attributeIndex": 2,
        "classId": 1
      },
      "CO_52.7.0": {
        "obis": "1.0.52.7.0.255",
        "attributeIndex": 2,
        "classId": 3
      },
      "CO_96.50.22": {
        "obis": "0.0.96.50.22.255",
        "attributeIndex": 2,
        "classId": 8
      },
      "CO_12.26.0": {
        "obis": "1.0.12.26.0.255",
        "attributeIndex": 2,
        "classId": 3
      },
      "CO_96.50.21": {
        "obis": "0.0.96.50.21.255",
        "attributeIndex": 2,
        "classId": 8
      },
      "0.9.0_0": {
        "obis": "0.0.1.0.0.255",
        "attributeIndex": 2,
        "classId": 8
      },
      "C3DE": {
        "obis": "0.0.99.98.10.255",
        "attributeIndex": 2,
        "classId": 7,
        "isProfileRegister": true
      },
      "CO_128.0.10": {
        "obis": "1.0.128.0.10.255",
        "attributeIndex": 2,
        "classId": 3
      },
      "C2DE": {
        "obis": "0.0.99.98.9.255",
        "attributeIndex": 2,
        "classId": 7,
        "isProfileRegister": true
      },
      "C1DE": {
        "obis": "0.0.99.98.8.255",
        "attributeIndex": 2,
        "classId": 7,
        "isProfileRegister": true
      },
      "CO_0.9.0_0": {
        "obis": "0.0.1.0.0.255",
        "attributeIndex": 2,
        "classId": 8
      },
      "CO_72.128.0": {
        "obis": "1.0.72.128.0.255",
        "attributeIndex": 2,
        "classId": 3
      },
      "CO_52.128.0": {
        "obis": "1.0.52.128.0.255",
        "attributeIndex": 2,
        "classId": 3
      },
      "CO_32.128.0": {
        "obis": "1.0.32.128.0.255",
        "attributeIndex": 2,
        "classId": 3
      },
      "CO_72.7.0": {
        "obis": "1.0.72.7.0.255",
        "attributeIndex": 2,
        "classId": 3
      },
      "CO_128.40.0": {
        "obis": "1.0.128.40.0.255",
        "attributeIndex": 2,
        "classId": 1
      },
      "72.7.0_0": {
        "obis": "1.0.72.7.0.255",
        "attributeIndex": 2,
        "classId": 3
      },
      "14.7.0_0": {
        "obis": "1.0.14.7.0.255",
        "attributeIndex": 2,
        "classId": 3
      },
      "52.7.0_0": {
        "obis": "1.0.52.7.0.255",
        "attributeIndex": 2,
        "classId": 3
      },
      "CO_32.7.0": {
        "obis": "1.0.32.7.0.255",
        "attributeIndex": 2,
        "classId": 3
      },
      "32.7.0_0": {
        "obis": "1.0.32.7.0.255",
        "attributeIndex": 2,
        "classId": 3
      },
      "CO_12.23.0": {
        "obis": "1.0.12.23.0.255",
        "attributeIndex": 2,
        "classId": 3
      },
      "PFEL": {
        "obis": "1.0.99.97.0.255",
        "attributeIndex": 2,
        "classId": 7,
        "isProfileRegister": true
      },
      "FW_VER": {
        "obis": "1.0.0.2.0.255",
        "attributeIndex": 2,
        "classId": 1
      },
      "96.6.0_0": {
        "obis": "0.0.96.6.0.255",
        "attributeIndex": 2,
        "classId": 3
      },
      "96.6.3_0": {
        "obis": "0.0.96.6.3.255",
        "attributeIndex": 2,
        "classId": 3
      },
      "96.6.3_1": {
        "obis": "0.1.96.6.3.255",
        "attributeIndex": 2,
        "classId": 3
      }
    },
    "transportProtocol": "DLMS",
    "defaultComModuleId": 5,
    "defaultComModule": {
      "subTypeId": 5,
      "make": "AnteLeco",
      "model": "STM32 M65-NMD",
      "socketServerPort": 8071,
      "hasICMPSupport": true,
      "hasSMSSupport": true,
      "hasNMDSupport":true,
      "tenantId": -1234
    }
  }'
);

INSERT INTO SUB_OPERATION_TEMPLATE(OPERATION_DEFINITION,OPERATION_CODE,SUB_TYPE_ID,DEVICE_TYPE,CREATE_TIMESTAMP,UPDATE_TIMESTAMP) VALUES('{"subTypeId":"5","deviceType":"METER","code":"BILLING_REGISTERS_RETRIEVE","type":"PROFILE","control":"NO_REPEAT","maxAttempts":1,"waitingTime":0,"isEnabled":true,"properties":{"requireDateValidation":"3600000","requireAuthentication":"0"},"transportMode":"NET_ONLY","registerTransactions":[],"registers":["0.0.0_0","0.9.1_0","0.9.2_0","1.8.0_0","1.8.1_0","1.8.2_0","1.8.3_0","2.8.0_0","2.8.1_0","2.8.2_0","2.8.3_0","1.8.0*01_0","1.8.1*01_0","1.8.2*01_0","1.8.3*01_0","2.8.0*01_0","2.8.1*01_0","2.8.2*01_0","2.8.3*01_0","9.6.0_0","9.6.0*01_0","10.6.0_0","10.6.0*01_0","0.4.2_0","0.4.3_0","0.4.5_0","0.4.6_0","14.7.0_0","31.7.0_0","32.7.0_0","51.7.0_0","52.7.0_0","71.7.0_0","72.7.0_0"]}','BILLING_REGISTERS_RETRIEVE',5,'METER',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());

INSERT INTO SUB_OPERATION_TEMPLATE(OPERATION_DEFINITION,OPERATION_CODE,SUB_TYPE_ID,DEVICE_TYPE,CREATE_TIMESTAMP,UPDATE_TIMESTAMP) VALUES('{"subTypeId":"5","deviceType":"METER","code":"LOAD_PROFILE_RETRIEVE","type":"PROFILE","control":"NO_REPEAT","maxAttempts":1,"waitingTime":0,"isEnabled":true,"properties":{"requireDateValidation":"3600000","requireAuthentication":"0"},"transportMode":"NET_ONLY","registerTransactions":[],"registers":["LP"]}','LOAD_PROFILE_RETRIEVE',5,'METER',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());

INSERT INTO SUB_OPERATION_TEMPLATE(OPERATION_DEFINITION,OPERATION_CODE,SUB_TYPE_ID,DEVICE_TYPE,CREATE_TIMESTAMP,UPDATE_TIMESTAMP) VALUES('{"subTypeId":"5","deviceType":"METER","code":"TIME_SYNC","type":"PROFILE","control":"NO_REPEAT","maxAttempts":1,"waitingTime":0,"isEnabled":true,"properties":{"requireDateAdjust":"30000","requireAuthentication":"0"},"registerTransactions":[],"registers":["0.9.1_0","0.9.2_0"]}','TIME_SYNC',5,'METER',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());

INSERT INTO SUB_OPERATION_TEMPLATE(OPERATION_DEFINITION,OPERATION_CODE,SUB_TYPE_ID,DEVICE_TYPE,CREATE_TIMESTAMP,UPDATE_TIMESTAMP) VALUES('{"subTypeId":"5","deviceType":"METER","code":"SELF_TEST","type":"PROFILE","control":"NO_REPEAT","maxAttempts":1,"waitingTime":0,"isEnabled":true,"properties":{"requireDateAdjust":"30000","requireAuthentication":"0"},"transportMode":"NET_ONLY","registerTransactions":[],"registers":["0.0.0_0","0.9.1_0","0.9.2_0","FW_VER","1.8.0_0","1.8.1_0","1.8.2_0","1.8.3_0","2.8.0_0","2.8.1_0","2.8.2_0","2.8.3_0","1.8.0*01_0","1.8.1*01_0","1.8.2*01_0","1.8.3*01_0","2.8.0*01_0","2.8.1*01_0","2.8.2*01_0","2.8.3*01_0","9.6.0_0","9.6.0*01_0","10.6.0_0","10.6.0*01_0","0.4.2_0","0.4.3_0","0.4.5_0","0.4.6_0","14.7.0_0","31.7.0_0","32.7.0_0","51.7.0_0","52.7.0_0","71.7.0_0","72.7.0_0"]}','SELF_TEST',5,'METER',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());

INSERT INTO SUB_OPERATION_TEMPLATE(OPERATION_DEFINITION,OPERATION_CODE,SUB_TYPE_ID,DEVICE_TYPE,CREATE_TIMESTAMP,UPDATE_TIMESTAMP) VALUES('{"subTypeId":"6","deviceType":"METER","code":"REMOTE_RELAY_ON","type":"PROFILE","control":"NO_REPEAT","maxAttempts":1,"waitingTime":0,"isEnabled":true,"properties":{"requireAuthentication":"0"},"transportMode":"ALLOW_SMS_FALLBACK","registerTransactions":[{"globalRegName":"CSRQ_RL","remoteMethod":{"index":2,"data":"003()","type":"STRING"}}],"registers":["0.9.1_0","0.9.2_0","CSRQ_RL","1.8.0_0","1.8.1_0","1.8.2_0","1.8.3_0","2.8.0_0","2.8.1_0","2.8.2_0","2.8.3_0"]}','REMOTE_RELAY_ON',6,'METER',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());

INSERT INTO SUB_OPERATION_TEMPLATE(OPERATION_DEFINITION,OPERATION_CODE,SUB_TYPE_ID,DEVICE_TYPE,CREATE_TIMESTAMP,UPDATE_TIMESTAMP) VALUES('{"subTypeId":"6","deviceType":"METER","code":"REMOTE_RELAY_OFF","type":"PROFILE","control":"NO_REPEAT","maxAttempts":1,"waitingTime":0,"isEnabled":true,"properties":{"requireAuthentication":"0"},"transportMode":"ALLOW_SMS_FALLBACK","registerTransactions":[{"globalRegName":"CSRQ_RL","remoteMethod":{"index":1,"data":"004()","type":"STRING"}}],"registers":["0.9.1_0","0.9.2_0","CSRQ_RL","1.8.0_0","1.8.1_0","1.8.2_0","1.8.3_0","2.8.0_0","2.8.1_0","2.8.2_0","2.8.3_0"]}','REMOTE_RELAY_OFF',6,'METER',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());

INSERT INTO SUB_OPERATION_TEMPLATE(OPERATION_DEFINITION,OPERATION_CODE,SUB_TYPE_ID,DEVICE_TYPE,CREATE_TIMESTAMP,UPDATE_TIMESTAMP) VALUES('{"subTypeId":"6","deviceType":"METER","code":"BILLING_REGISTERS_RETRIEVE","type":"PROFILE","control":"NO_REPEAT","maxAttempts":1,"waitingTime":0,"isEnabled":true,"properties":{"requireDateAdjust":"30000","requireAuthentication":"0"},"transportMode":"ALLOW_SMS_FALLBACK","registerTransactions":[],"registers":["0.0.0_0","0.9.1_0","0.9.2_0","1.8.0_0","1.8.1_0","1.8.2_0","1.8.3_0","2.8.0_0","2.8.1_0","2.8.2_0","2.8.3_0","1.8.0*01_0","1.8.1*01_0","1.8.2*01_0","1.8.3*01_0","2.8.0*01_0","2.8.1*01_0","2.8.2*01_0","2.8.3*01_0","14.7.0_0","31.7.0_0","32.7.0_0","51.7.0_0","52.7.0_0","71.7.0_0","72.7.0_0","CSRQ_RL"]}','BILLING_REGISTERS_RETRIEVE',6,'METER',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());

INSERT INTO SUB_OPERATION_TEMPLATE(OPERATION_DEFINITION,OPERATION_CODE,SUB_TYPE_ID,DEVICE_TYPE,CREATE_TIMESTAMP,UPDATE_TIMESTAMP) VALUES('{"subTypeId":"6","deviceType":"METER","code":"LOAD_PROFILE_RETRIEVE","type":"PROFILE","control":"NO_REPEAT","maxAttempts":1,"waitingTime":0,"isEnabled":true,"properties":{"requireDateValidation":"3600000","requireAuthentication":"0"},"transportMode":"NET_ONLY","registerTransactions":[],"registers":["LP"]}','LOAD_PROFILE_RETRIEVE',6,'METER',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());

INSERT INTO SUB_OPERATION_TEMPLATE(OPERATION_DEFINITION,OPERATION_CODE,SUB_TYPE_ID,DEVICE_TYPE,CREATE_TIMESTAMP,UPDATE_TIMESTAMP) VALUES('{"subTypeId":"6","deviceType":"METER","code":"TIME_SYNC","type":"PROFILE","control":"NO_REPEAT","maxAttempts":1,"waitingTime":0,"isEnabled":true,"properties":{"requireDateAdjust":"30000","requireAuthentication":"0"},"registerTransactions":[],"registers":["0.9.1_0","0.9.2_0"]}','TIME_SYNC',6,'METER',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());