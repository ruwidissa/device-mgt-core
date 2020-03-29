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

var configRowId = 0;

$(document).ready(function () {

    var configParams = {
        "NOTIFIER_TYPE": "notifierType",
        "NOTIFIER_FREQUENCY": "notifierFrequency",
        "IS_EVENT_PUBLISHING_ENABLED": "isEventPublishingEnabled"
    };

    var responseCodes = {
        "CREATED": "Created",
        "SUCCESS": "201",
        "INTERNAL_SERVER_ERROR": "Internal Server Error"
    };

    /**
     * Checks if provided input is valid against RegEx input.
     *
     * @param regExp Regular expression
     * @param inputString Input string to check
     * @returns {boolean} Returns true if input matches RegEx
     */
    function isPositiveInteger(str) {
        return /^\+?(0|[1-9]\d*)$/.test(str);
    }

    invokerUtil.get(
        "/api/device-mgt/v1.0/configuration",
        function (data) {
            data = JSON.parse(data);
            if (data && data.configuration) {
                for (var i = 0; i < data.configuration.length; i++) {
                    var config = data.configuration[i];
                    if (config.name == configParams["NOTIFIER_FREQUENCY"]) {
                        $("input#monitoring-config-frequency").val(config.value / 1000);
                    } else if (config.name == configParams["IS_EVENT_PUBLISHING_ENABLED"]) {
                        $("select#publish-for-analytics").val(config.value);
                    }
                }
            }
        }, function (data) {
            console.log(data);
        });

    /**
     * Following click function would execute
     * when a user clicks on "Save" button
     * on General platform configuration page in Entgra devicemgt Console.
     */
    $("button#save-general-btn").click(function () {
        var notifierFrequency = $("input#monitoring-config-frequency").val();
        var publishEvents = $("select#publish-for-analytics").val();
        var errorMsgWrapper = "#email-config-error-msg";
        var errorMsg = "#email-config-error-msg span";

        if (!notifierFrequency) {
            $(errorMsg).text("Monitoring frequency is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!isPositiveInteger(notifierFrequency)) {
            $(errorMsg).text("Provided monitoring frequency is invalid. ");
            $(errorMsgWrapper).removeClass("hidden");
        } else {
            var addConfigFormData = {};
            var configList = new Array();

            var monitorFrequency = {
                "name": configParams["NOTIFIER_FREQUENCY"],
                "value": String((notifierFrequency * 1000)),
                "contentType": "text"
            };

            var publishEventsDetails = {
                "name": configParams["IS_EVENT_PUBLISHING_ENABLED"],
                "value": publishEvents,
                "contentType": "text"
            };

            configList.push(publishEventsDetails);
            configList.push(monitorFrequency);
            addConfigFormData.configuration = configList;

            var addConfigAPI = "/api/device-mgt/v1.0/configuration";
            invokerUtil.put(
                addConfigAPI,
                addConfigFormData,
                function (data, textStatus, jqXHR) {
                    data = jqXHR.status;
                    if (data == 200) {
                        $("#config-save-form").addClass("hidden");
                        $("#record-created-msg").removeClass("hidden");
                    } else if (data == 500) {
                        $(errorMsg).text("Exception occurred at backend.");
                    } else if (data == 403) {
                        $(errorMsg).text("Action was not permitted.");
                    } else {
                        $(errorMsg).text("An unexpected error occurred.");
                    }

                    $(errorMsgWrapper).removeClass("hidden");
                }, function (data) {
                    data = data.status;
                    if (data == 500) {
                        $(errorMsg).text("Exception occurred at backend.");
                    } else if (data == 403) {
                        $(errorMsg).text("Action was not permitted.");
                    } else {
                        $(errorMsg).text("An unexpected error occurred.");
                    }
                    $(errorMsgWrapper).removeClass("hidden");
                }
            );
        }
    });
});

// Start of HTML embedded invoke methods
var showAdvanceOperation = function (operation, button) {
    $(button).addClass('selected');
    $(button).siblings().removeClass('selected');
    var hiddenOperation = ".wr-hidden-operations-content > div";
    $(hiddenOperation + '[data-operation="' + operation + '"]').show();
    $(hiddenOperation + '[data-operation="' + operation + '"]').siblings().hide();
};

var artifactGeoUpload = function () {
    var contentType = "application/json";
    var backendEndBasePath = "/api/device-mgt/v1.0";
    var urix = backendEndBasePath + "/admin/publish-artifact/deploy/analytics";
    var defaultStatusClasses = "fw fw-stack-1x";
    var content = $("#geo-analytics-response-template").find(".content");
    var title = content.find("#title");
    var statusIcon = content.find("#status-icon");
    var data = {};
    invokerUtil.post(urix, data, function (data) {
        title.html("Deploying statistic artifacts. Please wait...");
        statusIcon.attr("class", defaultStatusClasses + " fw-check");
        $(modalPopupContent).html(content.html());
        showPopup();
        setTimeout(function () {
            hidePopup();
            location.reload(true);
        }, 5000);

    }, function (jqXHR) {
        title.html("Failed to deploy artifacts, Please contact administrator.");
        statusIcon.attr("class", defaultStatusClasses + " fw-error");
        $(modalPopupContent).html(content.html());
        showPopup();
    }, contentType);
};

var loadDynamicDeviceTypeConfig = function (deviceType) {
    var configAPI = '/api/device-mgt/v1.0/device-types/' + deviceType + '/configs';
    invokerUtil.get(
        configAPI,
        function (data) {
            data = JSON.parse(data);
            var fieldWrapper = "#" + escapeSelector(deviceType + "-config-field-wrapper");
            $(fieldWrapper).html("");
            if (data.configuration) {
                var config;
                var i;
                for (i = 0; i < data.configuration.length; i++) {
                    config = data.configuration[i];
                    onDynamicConfigAddNew(deviceType, config.name, config.value);
                }
            }
            $(fieldWrapper).append(
                '<div class="row form-group ' + deviceType + '-config-row"' +
                ' id="' + deviceType + '-config-row-' + (++configRowId) + '">' +
                '<div class="col-xs-3">' +
                '<input type="text" class="form-control ' + deviceType + '-config-name" placeholder="name"/>' +
                '</div>' +
                '<div class="col-xs-4">' +
                '<textarea aria-describedby="basic-addon1" placeholder="value" data-error-msg="invalid value"' +
                ' class="form-control ' + deviceType + '-config-value" rows="1" cols="30"></textarea>' +
                '</div>' +
                '<button type="button" class="wr-btn wr-btn-horizontal"' +
                ' onclick="onDynamicConfigAddNew(\'' + deviceType + '\', \'\', \'\')">' +
                '<i class="fa fa-plus"></i>' +
                '</button>' +
                '</div>'
            );
        }, function (data) {
            console.log(data);
        }
    );
};

var onDynamicConfigSubmit = function (deviceType) {

    var errorMsgWrapper = "#" + escapeSelector(deviceType + "-config-error-msg");
    var errorMsg = "#" + escapeSelector(deviceType + "-config-error-msg span");
    var filedRaw = '.' + escapeSelector(deviceType + '-config-row');
    var filedName = "." + escapeSelector(deviceType + "-config-name");
    var filedValue = "." + escapeSelector(deviceType + "-config-value");

    var addConfigFormData = {};
    var configList = [];

    $(filedRaw).each(function () {
        var configName = $(this).find(filedName).val();
        var configVal = $(this).find(filedValue).val();
        if (configName && configName.trim() !== "" && configVal && configVal.trim() !== "") {
            var configurationEntry = {};
            configurationEntry.name = configName.trim();
            configurationEntry.contentType = "text";
            configurationEntry.value = configVal.trim();
            configList.push(configurationEntry);
        }
    });

    addConfigFormData.type = deviceType;
    addConfigFormData.configuration = configList;

    var addConfigAPI = '/api/device-mgt/v1.0/admin/device-types/' + deviceType + '/configs';

    invokerUtil.post(
        addConfigAPI,
        addConfigFormData,
        function (data, textStatus, jqXHR) {
            data = jqXHR.status;
            if (data == 200) {
                $("#config-save-form").addClass("hidden");
                $("#record-created-msg").removeClass("hidden");
            } else if (data == 500) {
                $(errorMsg).text("Exception occurred at backend.");
            } else if (data == 400) {
                $(errorMsg).text("Configurations cannot be empty.");
            } else {
                $(errorMsg).text("An unexpected error occurred.");
            }

            $(errorMsgWrapper).removeClass("hidden");
        }, function (data) {
            data = data.status;
            if (data == 500) {
                $(errorMsg).text("Exception occurred at backend.");
            } else if (data == 403) {
                $(errorMsg).text("Action was not permitted.");
            } else {
                $(errorMsg).text("An unexpected error occurred.");
            }
            $(errorMsgWrapper).removeClass("hidden");
        }
    );
};

var onDynamicConfigAddNew = function (deviceType, name, value) {
    var fieldWrapper = "#" + escapeSelector(deviceType + "-config-field-wrapper");
    $(fieldWrapper).append(
        '<div class="row form-group ' + deviceType + '-config-row"' +
        ' id="' + deviceType + '-config-row-' + (++configRowId) + '">' +
        '<div class="col-xs-3">' +
        '<input type="text" class="form-control ' + deviceType + '-config-name" placeholder="name"' +
        ' value="' + name + '"/>' +
        '</div>' +
        '<div class="col-xs-4">' +
        '<textarea aria-describedby="basic-addon1" placeholder="value" data-error-msg="invalid value"' +
        ' class="form-control ' + deviceType + '-config-value" rows="1" cols="30">' + value + '</textarea>' +
        '</div>' +
        '<button type="button" class="wr-btn wr-btn-horizontal"' +
        ' onclick="onDynamicConfigRemove(\'' + deviceType + '\', ' + configRowId + ')">' +
        '<i class="fa fa-minus"></i>' +
        '</button>' +
        '</div>'
    );
};

var onDynamicConfigRemove = function (deviceType, rawId) {
    var fieldWrapper = "#" + escapeSelector(deviceType + "-config-row-" + rawId);
    $(fieldWrapper).remove()
};

var escapeSelector = function (text) {
    return text.replace(
        /([$%&()*+,./:;<=>?@\[\\\]^\{|}~])/g,
        '\\$1'
    );
};