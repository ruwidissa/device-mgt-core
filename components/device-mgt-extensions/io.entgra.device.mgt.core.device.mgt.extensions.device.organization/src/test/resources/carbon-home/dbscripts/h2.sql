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
 -- Table `DM_DEVICE_ORGANIZATION`
 -- -----------------------------------------------------

 -- DM_DEVICE_ORGANIZATION TABLE--
 CREATE TABLE IF NOT EXISTS DM_DEVICE_ORGANIZATION (
     ORGANIZATION_ID INT NOT NULL AUTO_INCREMENT,
     TENANT_ID INT DEFAULT 0,
     DEVICE_ID INT(11) NOT NULL,
     PARENT_DEVICE_ID INT(11) DEFAULT NULL,
     DEVICE_ORGANIZATION_META TEXT DEFAULT NULL,
     LAST_UPDATED_TIMESTAMP TIMESTAMP NOT NULL,
     PRIMARY KEY (ORGANIZATION_ID),
     CONSTRAINT fk_DM_DEVICE_DM_ID FOREIGN KEY (DEVICE_ID)
     REFERENCES DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
     CONSTRAINT fk_DM_PARENT_DEVICE_DM_ID FOREIGN KEY (PARENT_DEVICE_ID)
     REFERENCES DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
     CONSTRAINT CHILD_PARENT_COMP_KEY UNIQUE (DEVICE_ID, PARENT_DEVICE_ID)
     );
 -- END OF DM_DEVICE_ORGANIZATION TABLE--

-- -----------------------------------------------------
-- Sample data for test cases
-- -----------------------------------------------------
INSERT INTO DM_DEVICE_TYPE (NAME,DEVICE_TYPE_META,LAST_UPDATED_TIMESTAMP,PROVIDER_TENANT_ID,SHARED_WITH_ALL_TENANTS)
VALUES ('METER','meter',CURRENT_TIMESTAMP(),1,true);
INSERT INTO DM_DEVICE (DESCRIPTION,NAME,DEVICE_TYPE_ID,DEVICE_IDENTIFICATION,LAST_UPDATED_TIMESTAMP,TENANT_ID) VALUES
('test device 1','Meter_01',1,'0001',CURRENT_TIMESTAMP(),1),
('test device 2','Meter_02',1,'0002',CURRENT_TIMESTAMP(),1),
('test device 3','Meter_03',1,'0003',CURRENT_TIMESTAMP(),1),
('test device 4','Meter_04',1,'0004',CURRENT_TIMESTAMP(),1),
('test device 5','Meter_05',1,'0005',CURRENT_TIMESTAMP(),1),
('test device 6','Meter_06',1,'0006',CURRENT_TIMESTAMP(),1),
('test device 7','Meter_07',1,'0007',CURRENT_TIMESTAMP(),1),
('test device 8','Meter_08',1,'0008',CURRENT_TIMESTAMP(),1),
('test device 9','Meter_09',1,'0009',CURRENT_TIMESTAMP(),1),
('test device 10','Meter_10',1,'0010',CURRENT_TIMESTAMP(),1),
('test device 11','Meter_11',1,'0011',CURRENT_TIMESTAMP(),1),
('test device 12','Meter_12',1,'0012',CURRENT_TIMESTAMP(),1),
('test device 13','Meter_13',1,'0013',CURRENT_TIMESTAMP(),1),
('test device 14','Meter_14',1,'0014',CURRENT_TIMESTAMP(),1),
('test device 15','Meter_15',1,'0015',CURRENT_TIMESTAMP(),1),
('test device 16','Meter_16',1,'0016',CURRENT_TIMESTAMP(),1),
('test device 17','Meter_17',1,'0017',CURRENT_TIMESTAMP(),1),
('test device 18','Meter_18',1,'0018',CURRENT_TIMESTAMP(),1),
('test device 19','Meter_19',1,'0019',CURRENT_TIMESTAMP(),1),
('test device 20','Meter_20',1,'0020',CURRENT_TIMESTAMP(),1)
;

