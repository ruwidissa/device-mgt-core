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


package io.entgra.device.mgt.core.device.mgt.core.search.mgt;

import io.entgra.device.mgt.core.device.mgt.common.search.Condition;

import java.util.List;
import java.util.Map;

public interface QueryBuilder {

    Map<String, List<QueryHolder>> buildQueries(List<Condition> conditions) throws InvalidOperatorException;

    String processAND(List<Condition> conditions, ValueType[] valueType, Integer intArr[]) throws InvalidOperatorException;

    String processOR(List<Condition> conditions, ValueType[] valueType, Integer intArr[]) throws InvalidOperatorException;

    List<QueryHolder>  processLocation(Condition condition) throws InvalidOperatorException;

    List<QueryHolder> processANDProperties(List<Condition> conditions) throws InvalidOperatorException;

    List<QueryHolder> processORProperties(List<Condition> conditions) throws InvalidOperatorException;

    QueryHolder processUpdatedDevices(long epochTime) throws InvalidOperatorException;

}
