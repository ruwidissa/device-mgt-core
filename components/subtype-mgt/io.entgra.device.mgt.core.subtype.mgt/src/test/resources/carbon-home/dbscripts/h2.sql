 -- -----------------------------------------------------
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

