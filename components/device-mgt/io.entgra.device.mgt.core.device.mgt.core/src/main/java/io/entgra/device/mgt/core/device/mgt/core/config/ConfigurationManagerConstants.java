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
package io.entgra.device.mgt.core.device.mgt.core.config;

/**
 * This class holds the constants used throughout the configuration manager.
 */
public class ConfigurationManagerConstants {

	public static final class ContentTypes {
		private ContentTypes() {
			throw new AssertionError();
		}

		public static final String CONTENT_TYPE_ANY = "*/*";
		public static final String MEDIA_TYPE_XML = "application/xml";
	}

	public static final class CharSets {
		private CharSets() {
			throw new AssertionError();
		}

		public static final String CHARSET_UTF8 = "UTF8";
	}
}
