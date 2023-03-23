 -- -----------------------------------------------------
 -- Table `DM_DEVICE_SUB_TYPE`
 -- -----------------------------------------------------

 CREATE TABLE IF NOT EXISTS `DM_DEVICE_SUB_TYPE` (
   `TENANT_ID` INT DEFAULT 0,
   `SUB_TYPE_ID` INT NOT NULL,
   `DEVICE_TYPE` VARCHAR(25) NOT NULL,
   `SUB_TYPE_NAME` VARCHAR(45) NOT NULL,
   `TYPE_DEFINITION` TEXT NOT NULL,
    PRIMARY KEY (`SUB_TYPE_ID`,`DEVICE_TYPE`)
 );

-- -----------------------------------------------------
-- Sample data for DAO test cases
-- -----------------------------------------------------
INSERT INTO DM_DEVICE_TYPE(NAME, DEVICE_TYPE_META, LAST_UPDATED_TIMESTAMP, PROVIDER_TENANT_ID, SHARED_WITH_ALL_TENANTS) VALUES
('android','NULL','2020-10-14 16:05:15',-1234,1),('power-meter','NULL','2020-10-14 16:05:15',-1234,1),
('communication-module','NULL','2020-10-14 16:05:15',-1234,1),('sim','NULL','2020-10-14 16:05:15',-1234,1);
INSERT INTO DM_DEVICE(DESCRIPTION, NAME, DEVICE_TYPE_ID, DEVICE_IDENTIFICATION, LAST_UPDATED_TIMESTAMP, TENANT_ID) VALUES
('+94713192111', '192.168.118.111', 4, '413012689159555', '2022-04-04 16:55:52', '-1234'),
('NULL', '864495036647555', 3, '864495036647555', '2022-04-04 16:56:52', '-1234'),
('NULL', '000019729555', 2, '000019729555', '2022-04-04 16:56:52', '-1234');

INSERT INTO DM_ENROLMENT(DEVICE_ID,OWNER,OWNERSHIP,STATUS,IS_TRANSFERRED,DATE_OF_ENROLMENT,DATE_OF_LAST_UPDATE,TENANT_ID) VALUES
(1,'admin','COPE','ACTIVE',0,'2022-04-11 15:41:30','2022-04-11 15:41:30',-1234),
(2,'admin','COPE','ACTIVE',0,'2022-04-11 15:41:30','2022-04-11 15:41:30',-1234),
(3,'admin','COPE','ACTIVE',0,'2022-04-11 15:41:30','2022-04-11 15:41:30',-1234);

INSERT INTO DM_DEVICE_SUB_TYPE (SUB_TYPE_ID, TENANT_ID, DEVICE_TYPE, SUB_TYPE_NAME, TYPE_DEFINITION) VALUES
(3,-1234,'Meter','TestSubType','{"make": "TestSubType", "model": "ATx-Mega SIM800", "subTypeId": 3, "hasSMSSupport": true, "hasICMPSupport": true, "socketServerPort": 8071}'),
(4,-1234,'Meter','TestSubType','{"make": "TestSubType", "model": "ATx-Mega SIM800", "subTypeId": 4, "hasSMSSupport": true, "hasICMPSupport": true, "socketServerPort": 8071}');

