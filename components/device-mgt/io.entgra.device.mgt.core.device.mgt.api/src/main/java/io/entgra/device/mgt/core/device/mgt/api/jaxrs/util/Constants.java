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

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.util;

/**
 * Holds the constants used by DeviceImpl Management Admin web application.
 */
public class Constants {

	public static final String USER_CLAIM_EMAIL_ADDRESS = "http://wso2.org/claims/emailaddress";
	public static final String USER_CLAIM_FIRST_NAME = "http://wso2.org/claims/givenname";
	public static final String USER_CLAIM_LAST_NAME = "http://wso2.org/claims/lastname";
	public static final String USER_CLAIM_CREATED = "http://wso2.org/claims/created";
	public static final String USER_CLAIM_MODIFIED = "http://wso2.org/claims/modified";
	public static final String USER_CLAIM_DEVICES = "http://wso2.org/claims/devices";
	public static final String PRIMARY_USER_STORE = "PRIMARY";
	public static final String APIM_RESERVED_USER = "apim_reserved_user";
	public static final String SCOPE_PUBLISH_RESERVED_USER = "scope_publish_reserved_user";
	public static final String RESERVED_USER = "reserved_user";
	public static final String DEFAULT_STREAM_VERSION = "1.0.0";
	public static final String SCOPE = "scope";
	public static final String JDBC_USERSTOREMANAGER = "org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager";
	public static final String DEFAULT_SIMPLE_DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";
	public static final int DEFAULT_PAGE_LIMIT = 50;
	public static final String FORWARD_SLASH = "/";
	public static final String ANDROID = "android";
	public static final String IS_USER_ABLE_TO_VIEW_ALL_ROLES = "isUserAbleToViewAllRoles";
	public static final String ANDROID_POLICY_VALIDATOR = "io.entgra.proprietary.uem.platform.android." +
			"core.polcy.AndroidPolicyPayloadValidator";
	public static final String IOS = "ios";
	public static final String WINDOWS = "windows";


	public final class OperationStatus {
		private OperationStatus () { throw new AssertionError(); }
		public static final String COMPLETED = "completed";
		public static final String ERROR = "error";
		public static final String IN_PROGRESS = "in_progress";
		public static final String PENDING = "pending";
		public static final String NOTNOW = "notnow";
		public static final String REPEATED = "repeated";
		public static final String REQUIRED_CONFIRMATION = "required_confirmation";
		public static final String CONFIRMED = "confirmed";
	}
	public static final String DEVICES = "devices";
	public static final String ATTRIBUTE_DISPLAY_NAME = "DisplayName";
	public static final String ATTRIBUTE_DESCRIPTION = "Description";
	public static final String EXTERNAL_DEVICE_CLAIM_DISPLAY_NAME = "Devices";
	public static final String EXTERNAL_DEVICE_CLAIM_DESCRIPTION = "Device list";

	public final class ErrorMessages {
		private ErrorMessages () { throw new AssertionError(); }

		public static final String STATUS_BAD_REQUEST_MESSAGE_DEFAULT = "Bad Request";

	}

	public final class DeviceConstants {
		private DeviceConstants () { throw new AssertionError(); }

		public static final String APPLICATION_JSON = "application/json";
		public static final String HEADER_CONTENT_TYPE = "Content-Type";
	}

	public final class Permission {
		private Permission() { throw new AssertionError(); }

		public static final String ADMIN = "/permission/admin";
		public static final String LOGIN = "/permission/admin/login";
		public static final String DEVICE_MGT = "/permission/admin/device-mgt";
		public static final String APP_MGT = "/permission/admin/app-mgt";
		public static final String TENANT = "/permission/admin/tenants";
		public static final String UI_VISIBILITY = "/permission/admin/ui-visibility-permissions";
	}
}
