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

CREATE TABLE APPM_PLATFORM (
ID INT NOT NULL AUTO_INCREMENT,
IDENTIFIER VARCHAR (100) NOT NULL,
TENANT_DOMAIN VARCHAR (100) NOT NULL ,
NAME VARCHAR (255),
FILE_BASED BOOLEAN,
DESCRIPTION LONGVARCHAR,
IS_SHARED BOOLEAN,
ICON_NAME VARCHAR (100),
PRIMARY KEY (ID, IDENTIFIER, TENANT_DOMAIN)
);

CREATE TABLE APPM_PLATFORM_PROPERTIES (
ID INT NOT NULL AUTO_INCREMENT,
PLATFORM_ID INTEGER NOT NULL,
PROP_NAME VARCHAR (100) NOT NULL,
OPTIONAL BOOLEAN,
DEFAUL_VALUE VARCHAR (255),
FOREIGN KEY(PLATFORM_ID) REFERENCES APPM_PLATFORM(ID) ON DELETE CASCADE,
PRIMARY KEY (ID, PLATFORM_ID, PROP_NAME)
);

CREATE TABLE APPM_PLATFORM_TENANT_MAPPING (
ID INT NOT NULL AUTO_INCREMENT,
TENANT_DOMAIN VARCHAR (100) NOT NULL ,
PLATFORM_ID VARCHAR (100) NOT NULL,
FOREIGN KEY(PLATFORM_ID) REFERENCES APPM_PLATFORM(ID) ON DELETE CASCADE,
PRIMARY KEY (ID, TENANT_DOMAIN, PLATFORM_ID)
)

