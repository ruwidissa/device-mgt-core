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

package io.entgra.device.mgt.core.device.mgt.common;

/**
 * This class holds all the constants used for IOS, Android, Windows.
 */
public class MDMAppConstants {

	public class IOSConstants {

		private IOSConstants() {
			throw new AssertionError();
		}
		public static final String IS_REMOVE_APP = "isRemoveApp";
		public static final String IS_PREVENT_BACKUP = "isPreventBackup";
		public static final String I_TUNES_ID = "iTunesId";
		public static final String LABEL = "label";
		public static final String PLIST = "plist";
		public static final String WEB_CLIP_URL = "webClipURL";
		public static final String OPCODE_INSTALL_ENTERPRISE_APPLICATION = "INSTALL_ENTERPRISE_APPLICATION";
		public static final String OPCODE_INSTALL_STORE_APPLICATION = "INSTALL_STORE_APPLICATION";
		public static final String OPCODE_INSTALL_WEB_APPLICATION = "WEB_CLIP";
		public static final String OPCODE_REMOVE_APPLICATION = "REMOVE_APPLICATION";
	}

	public class AndroidConstants {

		private AndroidConstants() {
			throw new AssertionError();
		}
		public static final String IS_BLOCK_UNINSTALL = "isBlockUninstall";
		public static final String OPCODE_INSTALL_APPLICATION = "INSTALL_APPLICATION";
		public static final String OPCODE_UNINSTALL_APPLICATION = "UNINSTALL_APPLICATION";
		public static final String UNMANAGED_APP_UNINSTALL= "UNMANAGED_APP_UNINSTALL";
	}

	public class WindowsConstants {

		private WindowsConstants() {
			throw new AssertionError();
		}
		public static final String INSTALL_ENTERPRISE_APPLICATION = "INSTALL_ENTERPRISE_APPLICATION";
		public static final String UNINSTALL_ENTERPRISE_APPLICATION = "UNINSTALL_ENTERPRISE_APPLICATION";
		public static final String INSTALL_STORE_APPLICATION = "INSTALL_STORE_APPLICATION";
		public static final String UNINSTALL_STORE_APPLICATION = "UNINSTALL_STORE_APPLICATION";
		public static final String INSTALL_WEB_CLIP_APPLICATION = "INSTALL_WEB_CLIP";
		public static final String UNINSTALL_WEB_CLIP_APPLICATION = "UNINSTALL_WEB_CLIP";
		//App type constants related to window device type
		public static final String MSI = "MSI";
		public static final String APPX = "APPX";

		//MSI Meta Key Constant
		public static final String MSI_PRODUCT_ID = "Product_Id";
		public static final String MSI_CONTENT_URI = "Content_Uri";
		public static final String MSI_FILE_HASH = "File_Hash";

		//APPX Meta Key Constant
		public static final String APPX_PACKAGE_URI = "Package_Url";
		public static final String APPX_DEPENDENCY_PACKAGE_URL = "Dependency_Package_Url";
		public static final String APPX_CERTIFICATE_HASH = "Certificate_Hash";
		public static final String APPX_ENCODED_CERT_CONTENT = "Encoded_Cert_Content";
		public static final String APPX_PACKAGE_FAMILY_NAME = "Package_Family_Name";
	}

	public class RegistryConstants {

		private RegistryConstants() {
			throw new AssertionError();
		}
		public static final String GENERAL_CONFIG_RESOURCE_PATH = "general";
	}
}
