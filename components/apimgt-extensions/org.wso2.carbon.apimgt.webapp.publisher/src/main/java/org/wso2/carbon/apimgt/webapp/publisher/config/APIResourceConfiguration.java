/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.webapp.publisher.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "ResourceConfiguration")
public class APIResourceConfiguration {

	private String name;
	private String context;
	private String version;
	private List<APIResource> resources;
	private String[] tags;
    private String endpointType;
    private String inSequenceName;
    private String inSequenceConfig;
    private String asyncApiDefinition;

    public List<APIResource> getResources() {
		return resources;
	}

	@XmlElement(name = "Resources", required = true)
	public void setResources(List<APIResource> resources) {
		this.resources = resources;
	}

	public String getContext() {
		return context;
	}

	@XmlElement(name = "Context", required = true)
	public void setContext(String context) {
		this.context = context;
	}

	public String getName() {
		return name;
	}

	@XmlElement(name = "Name")
	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	@XmlElement(name = "Version")
	public void setVersion(String version) {
		this.version = version;
	}

	public String[] getTags() {
		return tags;
	}

	@XmlElement(name = "Tags")
	public void setTags(String[] tags) {
		this.tags = tags;
	}

    public String getEndpointType() {
        return endpointType;
    }

    @XmlElement(name = "EndpointType")
    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }

    public String getInSequenceName() {
        return inSequenceName;
    }

    @XmlElement(name = "inSequenceName")
    public void setInSequenceName(String inSequenceName) {
        this.inSequenceName = inSequenceName;
    }

    public String getInSequenceConfig() {
        return inSequenceConfig;
    }

    @XmlElement(name = "inSequenceConfig")
    public void setInSequenceConfig(String inSequenceConfig) {
        this.inSequenceConfig = inSequenceConfig;
    }

    public String getAsyncApiDefinition() {
        return asyncApiDefinition;
    }
    @XmlElement(name = "asyncApiDefinition")
    public void setAsyncApiDefinition(String asyncApiDefinition) {
        this.asyncApiDefinition = asyncApiDefinition;
    }
}
