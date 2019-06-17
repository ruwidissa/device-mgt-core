/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var operationModule = function () {
    var log = new Log("/app/modules/business-controllers/operation.js");
    var utility = require('/app/modules/utility.js').utility;
    var constants = require('/app/modules/constants.js');
    var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    var serviceInvokers = require("/app/modules/oauth/token-protected-service-invokers.js")["invokers"];

    var publicMethods = {};
    var privateMethods = {};

    /**
     * This method reads the token from the Token client and return the access token.
     * If the token pair s not set in the session this will send a redirect to the login page.
     */
    function getAccessToken(deviceType, owner, deviceId) {
        var TokenClient = Packages.org.wso2.carbon.device.mgt.iot.apimgt.TokenClient;
        var accessTokenClient = new TokenClient(deviceType);
        var accessTokenInfo = accessTokenClient.getAccessToken(owner, deviceId);
        return accessTokenInfo.getAccess_token();
    }

    privateMethods.getOperationsFromFeatures = function (deviceType, operationType) {
        var url = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"] + "/device-types/"
            + deviceType + "/features?featureType=" + operationType + "&hidden=false";
        return serviceInvokers.XMLHttp.get(url, function (responsePayload) {
                var features = JSON.parse(responsePayload.responseText);
                var featureList = [];
                var feature;
                for (var i = 0; i < features.length; i++) {
                    feature = {};
                    feature["operation"] = features[i].code;
                    feature["name"] = features[i].name;
                    feature["description"] = features[i].description;
                    feature["contentType"] = features[i].contentType;
                    feature["deviceType"] = deviceType;
                    feature["params"] = [];
                    var metaData = features[i].metadataEntries;
                    if (metaData) {
                        for (var j = 0; j < metaData.length; j++) {
                            if (metaData[j].name === "operationMeta") {
                                var operationMeta = metaData[j].value;
                                var params = {};
                                params["method"] = operationMeta.method;
                                params["pathParams"] = operationMeta.pathParams;
                                params["queryParams"] = operationMeta.queryParams;
                                params["formParams"] = operationMeta.formParams ? operationMeta.formParams : [];
                                params["uri"] = operationMeta.uri;
                                params["contentType"] = operationMeta.contentType;
                                feature["params"].push(params);
                                feature["permission"] = operationMeta.permission;
                                if (operationMeta.icon) {
                                    //Check if icon is a path or font
                                    if (operationMeta.icon.indexOf("path:") === 0) {
                                        feature["icon"] = operationMeta.icon.replace("path:", "");
                                    } else {
                                        feature["iconFont"] = operationMeta.icon;
                                    }
                                }
                                if (operationMeta.uiParams && operationMeta.uiParams.length > 0) {
                                    feature["uiParams"] = operationMeta.uiParams;
                                }
                                if (operationMeta.filters) {
                                    feature["filters"] = operationMeta.filters;
                                }
                                if (operationMeta.ownershipDescription) {
                                    feature["ownershipDescription"] = operationMeta.ownershipDescription;
                                }
                                continue;
                            }
                            feature["metadata"].push(metaData[j].value);
                        }
                        featureList.push(feature);
                    }
                }
                return featureList;
            }, function (responsePayload) {
                var response = {};
                response["status"] = "error";
                return response;
            }
        );
    };

    publicMethods.getControlOperations = function (device) {
        var deviceType = device.type;
        var operations = privateMethods.getOperationsFromFeatures(deviceType, "operation");
        for (var op in operations) {
            if (operations.hasOwnProperty(op)) {
                operations[op]["isDisabled"] = false;
                if (device && operations[op].filters && operations[op].filters.length > 0) {
                    var filters = operations[op].filters;
                    for (var filter in filters) {
                        if (filters.hasOwnProperty(filter)) {
                            if (device[filters[filter].property] !== filters[filter].value) {
                                operations[op]["isDisabled"] = true;
                                operations[op]["disabledText"] = operations[op]["disabledText"] ?
                                    operations[op]["disabledText"] + ", " + filters[filter].description :
                                    filters[filter].description;
                            }
                        }
                    }
                }
            }
        }
        return operations;
    };

    publicMethods.getMonitorOperations = function (deviceType) {
        return privateMethods.getOperationsFromFeatures(deviceType, "monitor");
    };

    publicMethods.handlePOSTOperation = function (deviceType, operation, deviceId, params) {
        var user = session.get(constants.USER_SESSION_KEY);
        var endPoint = devicemgtProps["httpsURL"] + '/' + deviceType + "/controller/" + operation;
        var header = '{"owner":"' + user.username + '","deviceId":"' + deviceId +
            '","protocol":"mqtt", "sessionId":"' + session.getId() + '", "' +
            constants.AUTHORIZATION_HEADER + '":"' + constants.BEARER_PREFIX +
            getAccessToken(deviceType, user.username, deviceId) + '"}';
        return post(endPoint, params, JSON.parse(header), "json");
    };

    publicMethods.handleGETOperation = function (deviceType, operation, operationName, deviceId) {
        var user = session.get(constants.USER_SESSION_KEY);
        var endPoint = devicemgtProps["httpsURL"] + '/' + deviceType + "/controller/" + operation;
        var header = '{"owner":"' + user.username + '","deviceId":"' + deviceId +
            '","protocol":"mqtt", "' + constants.AUTHORIZATION_HEADER + '":"' +
            constants.BEARER_PREFIX + getAccessToken(deviceType, user.username, deviceId) +
            '"}';
        var result = get(endPoint, {}, JSON.parse(header), "json");
        if (result.data) {
            var values = result.data.sensorValue.split(',');
            if (operationName == 'gps') {
                result.data.map = {
                    lat: parseFloat(values[0]),
                    lng: parseFloat(values[1])
                }
            } else {
                var sqSum = 0;
                for (var v in values) {
                    sqSum += Math.pow(values[v], 2);
                }
                result.data[operationName] = Math.sqrt(sqSum);
            }
            delete result.data['sensorValue'];
        }
        return result;
    };

    return publicMethods;
}();