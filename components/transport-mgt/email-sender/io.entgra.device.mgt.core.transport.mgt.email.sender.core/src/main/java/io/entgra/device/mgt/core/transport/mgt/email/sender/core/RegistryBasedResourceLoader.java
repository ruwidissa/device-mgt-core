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
package io.entgra.device.mgt.core.transport.mgt.email.sender.core;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.ExtProperties;

import java.io.InputStream;
import java.io.Reader;

public class RegistryBasedResourceLoader extends ResourceLoader {

    private static final String EMAIL_CONFIG_BASE_LOCATION = "email-templates";

    @Override
    public void init(ExtProperties extProperties) {

    }

    @Override
    public Reader getResourceReader(String name, String encoding) throws ResourceNotFoundException {
//        try {
//            Registry registry =
//                    CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_CONFIGURATION);
//            if (registry == null) {
//                throw new IllegalStateException("No valid registry instance is attached to the current carbon context");
//            }
//            if (!registry.resourceExists(EMAIL_CONFIG_BASE_LOCATION + "/" + name)) {
//                throw new ResourceNotFoundException("Resource '" + name + "' does not exist");
//            }
//            org.wso2.carbon.registry.api.Resource resource =
//                    registry.get(EMAIL_CONFIG_BASE_LOCATION + "/" + name);
//            resource.setMediaType("text/plain");
//
//            return new InputStreamReader(resource.getContentStream());
//        } catch (RegistryException e) {
//            throw new ResourceNotFoundException("Error occurred while retrieving resource", e);
//        }
        return null;
    }

    public InputStream getResourceStream(String name) throws ResourceNotFoundException {
//        try {
//            Registry registry =
//                    CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_CONFIGURATION);
//            if (registry == null) {
//                throw new IllegalStateException("No valid registry instance is attached to the current carbon context");
//            }
//            if (!registry.resourceExists(EMAIL_CONFIG_BASE_LOCATION + "/" + name)) {
//                throw new ResourceNotFoundException("Resource '" + name + "' does not exist");
//            }
//            org.wso2.carbon.registry.api.Resource resource =
//                    registry.get(EMAIL_CONFIG_BASE_LOCATION + "/" + name);
//            resource.setMediaType("text/plain");
//            return resource.getContentStream();
//        } catch (RegistryException e) {
//            throw new ResourceNotFoundException("Error occurred while retrieving resource", e);
//        }
        return null;
    }

    @Override
    public boolean isSourceModified(Resource resource) {
        return false;
    }

    @Override
    public long getLastModified(Resource resource) {
        return 0;
    }

}
