/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.cea.mgt.common.bean;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MailboxProfile {
    private final Set<String> activeSyncAllowedEASIdentifiers = new HashSet<>();
    private final Set<String> activeSyncBlockedEASIdentifiers = new HashSet<>();
    private String identity;

    public Set<String> getActiveSyncAllowedEASIdentifiers() {
        return activeSyncAllowedEASIdentifiers;
    }

    public Set<String> getActiveSyncBlockedEASIdentifiers() {
        return activeSyncBlockedEASIdentifiers;
    }

    public void addActiveSyncAllowedEASIdentifier(String EASIdentifier) {
        activeSyncAllowedEASIdentifiers.add(EASIdentifier);
    }

    public void addActiveSyncBlockEASIdentifier(String EASIdentifier) {
        activeSyncBlockedEASIdentifiers.add(EASIdentifier);
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getAllowedEASIdentifierString() {
        String add = "", remove = "";
        if (!activeSyncAllowedEASIdentifiers.isEmpty()) {
            Set<String> processedEASIdentifiers = new HashSet<>();
            for (String activeSyncAllowedEASIdentifier : activeSyncAllowedEASIdentifiers) {
                processedEASIdentifiers.add("'" + activeSyncAllowedEASIdentifier + "'");
            }
            add = String.join(",", processedEASIdentifiers);
        }

        if (!activeSyncBlockedEASIdentifiers.isEmpty()) {
            Set<String> processedEASIdentifiers = new HashSet<>();
            for (String activeSyncBlockedEASIdentifier : activeSyncBlockedEASIdentifiers) {
                processedEASIdentifiers.add("'" + activeSyncBlockedEASIdentifier + "'");
            }
            remove = String.join(",", processedEASIdentifiers);
        }

        String begin = "@{", end = "}";
        if (!add.isEmpty()) {
            begin = begin + "Add=" + add + ";";
        }
        if (!remove.isEmpty()) {
            begin = begin + "Remove=" + remove + ";";
        }
        return begin + end;
    }

    public String getBlockedEASIdentifierString() {
        String add = "", remove = "";
        if (!activeSyncAllowedEASIdentifiers.isEmpty()) {
            Set<String> processedEASIdentifiers = new HashSet<>();
            for (String activeSyncAllowedEASIdentifier : activeSyncAllowedEASIdentifiers) {
                processedEASIdentifiers.add("'" + activeSyncAllowedEASIdentifier + "'");
            }
            remove = String.join(",", processedEASIdentifiers);
        }

        if (!activeSyncBlockedEASIdentifiers.isEmpty()) {
            Set<String> processedEASIdentifiers = new HashSet<>();
            for (String activeSyncBlockedEASIdentifier : activeSyncBlockedEASIdentifiers) {
                processedEASIdentifiers.add("'" + activeSyncBlockedEASIdentifier + "'");
            }
            add = String.join(",", processedEASIdentifiers);
        }

        String begin = "@{", end = "}";
        if (!add.isEmpty()) {
            begin = begin + "Add=" + add + ";";
        }
        if (!remove.isEmpty()) {
            begin = begin + "Remove=" + remove + ";";
        }
        return begin + end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MailboxProfile)) return false;
        MailboxProfile that = (MailboxProfile) o;
        return Objects.equals(identity, that.identity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identity);
    }
}
