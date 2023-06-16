
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

-- -----------------------------------------------------
-- Table `AD_DEVICE`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `AD_DEVICE` (
  `DEVICE_ID` VARCHAR(45) NOT NULL,
  `FCM_TOKEN` VARCHAR(1000) NULL DEFAULT NULL,
  `DEVICE_INFO` VARCHAR(8000) NULL DEFAULT NULL,
  `IMEI` VARCHAR(45) NULL DEFAULT NULL,
  `IMSI` VARCHAR(45) NULL DEFAULT NULL,
  `OS_VERSION` VARCHAR(45) NULL DEFAULT NULL,
  `DEVICE_MODEL` VARCHAR(45) NULL DEFAULT NULL,
  `VENDOR` VARCHAR(45) NULL DEFAULT NULL,
  `LATITUDE` VARCHAR(45) NULL DEFAULT NULL,
  `LONGITUDE` VARCHAR(45) NULL DEFAULT NULL,
  `SERIAL` VARCHAR(45) NULL DEFAULT NULL,
  `MAC_ADDRESS` VARCHAR(45) NULL DEFAULT NULL,
  `DEVICE_NAME` VARCHAR(100) NULL DEFAULT NULL,
  `OS_BUILD_DATE` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`DEVICE_ID`));

-- -----------------------------------------------------
-- Table `AD_FEATURE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `AD_FEATURE` (
  `ID` INT NOT NULL AUTO_INCREMENT,
  `CODE` VARCHAR(45) NOT NULL,
  `NAME` VARCHAR(100) NULL,
  `DESCRIPTION` VARCHAR(200) NULL,
  PRIMARY KEY (`ID`));

