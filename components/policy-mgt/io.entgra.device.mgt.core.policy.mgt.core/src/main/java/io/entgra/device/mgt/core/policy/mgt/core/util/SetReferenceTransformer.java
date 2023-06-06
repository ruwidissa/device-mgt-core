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

package io.entgra.device.mgt.core.policy.mgt.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class SetReferenceTransformer<T>{
        private List<T> objectsToRemove;
        private List<T> objectsToAdd;

    /**
     * Use the Set theory to find the objects to delete and objects to add

     The difference of objects in existingSet and newSet needed to be deleted

     new roles to add = newSet - The intersection of roles in existingSet and newSet
     * @param currentList
     * @param nextList
     */
        public void transform(List<T> currentList, List<T> nextList){
            TreeSet<T> existingSet = new TreeSet<T>(currentList);
            TreeSet<T> newSet = new TreeSet<T>(nextList);;

            existingSet.removeAll(newSet);

            objectsToRemove = new ArrayList<>(existingSet);

            // Clearing and re-initializing the set
            existingSet = new TreeSet<T>(currentList);

            newSet.removeAll(existingSet);
            objectsToAdd = new ArrayList<T>(newSet);
        }

        public List<T> getObjectsToRemove() {
            return objectsToRemove;
        }

        public List<T> getObjectsToAdd() {
            return objectsToAdd;
        }
}