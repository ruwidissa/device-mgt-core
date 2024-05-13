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

package io.entgra.device.mgt.core.device.mgt.core.util;

import io.entgra.device.mgt.core.device.mgt.common.MDMAppConstants;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.android.AppStoreApplication;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.android.EnterpriseApplication;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.android.WebApplication;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.ProfileOperation;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.App;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.UnknownApplicationTypeException;

/**
 *
 * This class contains the all the operations related to Android.
 */
public class MDMAndroidOperationUtil {

	/**
	 * This method is used to create Install Authentication operation.
	 *
	 * @param application MobileApp application
	 * @return operation
	 *
	 */
	public static Operation createInstallAppOperation(App application) throws UnknownApplicationTypeException {

		ProfileOperation operation = new ProfileOperation();
		operation.setCode(MDMAppConstants.AndroidConstants.OPCODE_INSTALL_APPLICATION);
		operation.setType(Operation.Type.PROFILE);

		switch (application.getType()) {
			case ENTERPRISE:
				EnterpriseApplication enterpriseApplication = new EnterpriseApplication();
				enterpriseApplication.setType(application.getType().toString());
				enterpriseApplication.setUrl(application.getLocation());
				enterpriseApplication.setAppIdentifier(application.getIdentifier());
				enterpriseApplication.setProperties(application.getProperties());
				operation.setPayLoad(enterpriseApplication.toJSON());
				break;
			case PUBLIC:
				AppStoreApplication appStoreApplication =
						new AppStoreApplication();
				appStoreApplication.setType(application.getType().toString());
				appStoreApplication.setAppIdentifier(application.getIdentifier());
				appStoreApplication.setProperties(application.getProperties());
				operation.setPayLoad(appStoreApplication.toJSON());
				break;
			case WEBAPP:
			case WEB_CLIP:
				WebApplication webApplication = new WebApplication();
				webApplication.setUrl(application.getLocation());
				webApplication.setName(application.getName());
				webApplication.setType(application.getType().toString());
				webApplication.setProperties(application.getProperties());
				operation.setPayLoad(webApplication.toJSON());
				break;
			default:
				throw new UnknownApplicationTypeException("Application type '" + application.getType() +
                        "' is not supported");
		}
		return operation;
	}

	/**
	 * This method is used to create Uninstall Authentication operation.
	 * @param application MobileApp application
	 * @return operation
	 */
	public static Operation createAppUninstallOperation(App application) throws UnknownApplicationTypeException {

		ProfileOperation operation = new ProfileOperation();
		operation.setCode(MDMAppConstants.AndroidConstants.OPCODE_UNINSTALL_APPLICATION);
		operation.setType(Operation.Type.PROFILE);

		switch (application.getType()) {
			case ENTERPRISE:
				EnterpriseApplication enterpriseApplication =
						new EnterpriseApplication();
				enterpriseApplication.setType(application.getType().toString());
				enterpriseApplication.setAppIdentifier(application.getIdentifier());
				enterpriseApplication.setProperties(application.getProperties());
				operation.setPayLoad(enterpriseApplication.toJSON());
				break;
			case PUBLIC:
				AppStoreApplication appStoreApplication =
						new AppStoreApplication();
				appStoreApplication.setType(application.getType().toString());
				appStoreApplication.setAppIdentifier(application.getIdentifier());
				operation.setPayLoad(appStoreApplication.toJSON());
				break;
			case WEBAPP:
			case WEB_CLIP:
				WebApplication webApplication = new WebApplication();
				webApplication.setUrl(application.getLocation());
				webApplication.setName(application.getName());
//				webApplication.setType(application.getType().toString());
//				Hard-corded "type" to "webapp". Some agent versions accept only "webapp" as the type.
				webApplication.setType("webapp");
				operation.setPayLoad(webApplication.toJSON());
				break;
			default:
                throw new UnknownApplicationTypeException("Application type '" + application.getType() +
                        "' is not supported");
		}
		return operation;
	}

}
