/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.application.mgt.common.wrapper;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value = "ApplicationWrapper", description = "ApplicationWrapper represents the an ApplicationDTO in ApplicationDTO Store")
public class ApplicationUpdateWrapper {


    @ApiModelProperty(name = "name",
            value = "Name of the application",
            required = true)
    private String name;

    @ApiModelProperty(name = "description",
            value = "Description of the application",
            required = true)
    private String description;

    @ApiModelProperty(name = "categories",
            value = "List of app categories.",
            required = true,
            example = "Educational, Gaming, Travel, Entertainment etc")
    private List<String> categories;

    @ApiModelProperty(name = "subMethod",
            value = "Subscription type of the application",
            required = true,
            example = "PAID, FREE")
    private String subMethod;

    @ApiModelProperty(name = "paymentCurrency",
            value = "Payment currency of the application",
            required = true,
            example = "$")
    private String paymentCurrency;

    @ApiModelProperty(name = "tags",
            value = "List of application tags")
    private List<String> tags;

    @ApiModelProperty(name = "unrestrictedRoles",
            value = "List of roles that users should have to access the application")
    private List<String> unrestrictedRoles;

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public List<String> getCategories() { return categories; }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getTags() { return tags; }

    public void setTags(List<String> tags) { this.tags = tags; }

    public String getSubMethod() { return subMethod; }

    public void setSubMethod(String subMethod) { this.subMethod = subMethod; }

    public String getPaymentCurrency() { return paymentCurrency; }

    public void setPaymentCurrency(String paymentCurrency) { this.paymentCurrency = paymentCurrency; }

    public List<String> getUnrestrictedRoles() { return unrestrictedRoles; }

    public void setUnrestrictedRoles(List<String> unrestrictedRoles) { this.unrestrictedRoles = unrestrictedRoles; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}
