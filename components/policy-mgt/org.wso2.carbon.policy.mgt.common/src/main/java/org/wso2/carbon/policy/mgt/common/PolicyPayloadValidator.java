package org.wso2.carbon.policy.mgt.common;

import java.util.List;

public interface PolicyPayloadValidator {

    List<ProfileFeature> validate(List<ProfileFeature> features);
}
