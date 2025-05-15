/*
 * Copyright (C) 2018 - 2025 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.device.mgt.common.type;

public final class Constants {

    // Private constructor to prevent instantiation
    private Constants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Constants for Meta Keys
    public static final String EVENT_DEFINITIONS = "EVENT_DEFINITIONS";
    public static final String EVENT_NAME = "eventName";
    public static final String TRANSPORT = "transport";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String ATTRIBUTES = "attributes";
    public static final String EVENT_ATTRIBUTES = "eventAttributes";
    public static final String EVENT_TOPIC_STRUCTURE = "eventTopicStructure";

}

