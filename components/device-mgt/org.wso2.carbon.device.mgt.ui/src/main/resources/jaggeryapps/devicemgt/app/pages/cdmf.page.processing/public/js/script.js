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

var pollingCount = 24;
function poll() {
    $.ajax({
               url: context + "/api/user/environment-loaded",
               type: "GET",
               success: function (data) {
                   if (data.isLoaded) {
                       window.location = context + "/";
                   }
               },
               dataType: "json",
               complete: setTimeout(function () {
                   pollingCount = pollingCount - 1;
                   if (pollingCount > 0) {
                       poll();
                   } else {
                       $(".loading-animation .logo").hide();
                       $(".loading-animation").prepend(
                           '<i class="fw fw-error fw-inverse fw-2x" style="float: left;"></i>');
                       $(".loading-animation p").css("width", "150%")
                           .html("Ops... it seems something went wrong.<br/> Refresh the page to retry!");
                   }
               }, 5000),
               timeout: 5000
           });
}

$(document).ready(function () {
    poll();
});