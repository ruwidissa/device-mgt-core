/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.grafana.mgt.sql.query.encoder;

import org.apache.commons.lang.StringEscapeUtils;
import org.wso2.carbon.device.mgt.core.grafana.mgt.sql.query.PreparedQuery;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericQueryEncoder implements QueryEncoder{

    public GenericQueryEncoder() {
    }

    @Override
    public String encode(PreparedQuery preparedQuery) throws SQLException {
        return generateEncodedSQL(preparedQuery.getPreparedSQL(), preparedQuery.getParameters());
    }

    private String generateEncodedSQL(String preparedSQL, List<String> parameters) throws SQLException {
        List<String> escapedParams = escapeParameters(parameters);
        Matcher placeHolderMatcher = Pattern.compile(Pattern.quote(PreparedQuery.PREPARED_SQL_PARAM_PLACEHOLDER)).matcher(preparedSQL);
        StringBuffer sb = new StringBuffer();
        for (String param: escapedParams) {
            if(placeHolderMatcher.find()) {
                placeHolderMatcher.appendReplacement(sb, Matcher.quoteReplacement(param));
            } else {
                String errMsg = "Given parameter count doesn't match parameters available in SQL";
                throw new SQLException(errMsg);
            }
        }
        return sb.toString();
    }

    private List<String> escapeParameters(List<String> parameters) {
        List<String> escapedParams = new ArrayList<>();
        for (String param : parameters) {
            String escapedParam = StringEscapeUtils.escapeSql(param);
            escapedParams.add(escapedParam);
        }
        return escapedParams;
    }

}
