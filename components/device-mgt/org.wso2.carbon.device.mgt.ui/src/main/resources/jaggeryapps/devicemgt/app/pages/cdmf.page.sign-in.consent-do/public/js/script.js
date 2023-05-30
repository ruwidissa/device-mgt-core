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

function approved(ssoProtocol) {
    var mandatoryClaimCBs = $(".mandatory-claim");
    var checkedMandatoryClaimCBs = $(".mandatory-claim:checked");

    if (checkedMandatoryClaimCBs.length == mandatoryClaimCBs.length) {
        if(ssoProtocol === "saml") {
            document.getElementById('consent').value = "approve";
        } else if(ssoProtocol === "oidc") {
            document.getElementById('consent').value = "approveAlways";
        }
        document.getElementById("consentForm").submit();
    } else {
        $("#modal_claim_validation").modal();
    }
}

function deny() {
    document.getElementById('consent').value = "deny";
    document.getElementById("consentForm").submit();
}

$(document).ready(function () {
    $("#consent_select_all").click(function () {
        if (this.checked) {
            $('.checkbox input:checkbox').each(function () {
                $(this).prop("checked", true);
            });
        } else {
            $('.checkbox :checkbox').each(function () {
                $(this).prop("checked", false);
            });
        }
    });
    $(".checkbox input").click(function (e) {
        if (e.target.id !== 'consent_select_all') {
            $("#consent_select_all").prop("checked", false);
        }
    });
});
