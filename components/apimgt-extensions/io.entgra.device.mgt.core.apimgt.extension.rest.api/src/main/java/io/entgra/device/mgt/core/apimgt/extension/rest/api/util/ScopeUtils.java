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

package io.entgra.device.mgt.core.apimgt.extension.rest.api.util;

import java.util.HashSet;
import java.util.Set;

/**
 * This class represents the scope data.
 */
public class ScopeUtils {

    private String key;
    private String name;
    private String roles;
    private String description;
    private int id;

    public ScopeUtils() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = removeDuplicatesFromRoleString(roles);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toJSON() {
        String jsonString = "{\n" +
                "   \"name\":\"" + key + "\",\n" +
                "   \"displayName\":\"" + name + "\",\n" +
                "   \"description\":\"" + description + "\",\n" +
                "   \"bindings\":[\n" +
                "      \"" + roles + "\"\n" +
                "   ]\n" +
                "}";
        return jsonString;
    }

    private static String removeDuplicatesFromRoleString(String roleString) {
        String[] roles = roleString.split(",");
        Set<String> roleSet = new HashSet<>();
        for(String role : roles) {
            roleSet.add(role.trim());
        }
        return String.join(",", roleSet);
    }
}
