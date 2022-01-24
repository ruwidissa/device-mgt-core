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
package io.entgra.analytics.mgt.grafana.proxy.core.sql.query;

import io.entgra.analytics.mgt.grafana.proxy.core.exception.QueryMisMatch;
import io.entgra.analytics.mgt.grafana.proxy.core.util.GrafanaConstants;
import io.entgra.analytics.mgt.grafana.proxy.core.util.GrafanaUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GrafanaPreparedQueryBuilder {

    private static final Log log = LogFactory.getLog(GrafanaPreparedQueryBuilder.class);

    private static final String QUERY_MISMATCH_EXCEPTION_MESSAGE = "Grafana query api request rawSql does not match relevant query template";
    private static final String COMMA_SEPARATOR = ",";
    private static final String VAR_PARAM_TEMPLATE = "$param";
    private static final String GRAFANA_QUOTED_VAR_REGEX = "('\\$(\\d|\\w|_)+')|('\\$\\{.*?\\}')|(\"\\$(\\d|\\w|_)+\")|(\"\\$\\{.*?\\}\")";
    private static final String GRAFANA_VAR_REGEX = "(\\$(\\d|\\w|_)+)|(\\$\\{.*?\\})";


    public static PreparedQuery build(String queryTemplate, String rawQuery) throws QueryMisMatch {
        // In Grafana, a variable can coexist with a hardcoded parameter (ie: '${__from:date:YYYY-MM-DD} 00:00:00')
        // hence this function templates the whole parameter in order to make valid prepared statement
        queryTemplate = templateParamWGrafanaVariables(queryTemplate);

        String queryUpToVarStart = queryTemplate;
        String rawQueryUpToVarStart = rawQuery;

        // Pattern matcher to get query string before the first grafana variable
        Pattern toVarStart = Pattern.compile("([^\\$]+(?=\"\\$))|([^\\$]+(?='\\$))|([^\\$]+(?=\\$))");
        Matcher matchToVarStart = toVarStart.matcher(queryTemplate);

        if (matchToVarStart.find()) {
            queryUpToVarStart = matchToVarStart.group();
            if(rawQuery.length() < queryUpToVarStart.length()) {
                throw new QueryMisMatch(QUERY_MISMATCH_EXCEPTION_MESSAGE);
            }
            rawQueryUpToVarStart = rawQuery.substring(0, queryUpToVarStart.length());
        }
        if(!queryUpToVarStart.equals(rawQueryUpToVarStart)){
            throw new QueryMisMatch(QUERY_MISMATCH_EXCEPTION_MESSAGE);
        }
        StringBuilder preparedQueryBuilder = new StringBuilder().append(queryUpToVarStart);

        String queryFromVarStart = queryTemplate.substring(queryUpToVarStart.length());
        String rawQueryFromVarStart = rawQuery.substring(queryUpToVarStart.length());
        queryTemplate = queryFromVarStart;
        rawQuery = rawQueryFromVarStart;
        Pattern varPattern = Pattern.compile(GRAFANA_QUOTED_VAR_REGEX + "|" + GRAFANA_VAR_REGEX);
        Matcher varMatch = varPattern.matcher(queryTemplate);
        List<String> parameters = new ArrayList<>();
        while(varMatch.find()) {
            String currentVar = varMatch.group();
            // Pattern matcher to get query string between grafana current variable and next variable
            matchToVarStart = toVarStart.matcher(queryTemplate.substring(currentVar.length()));

            String matchToLookBehindRawQuery;
            if (matchToVarStart.find()) {
                matchToLookBehindRawQuery = matchToVarStart.group();
            } else {
                // If next variable does not exist get query string after the current variable
                matchToLookBehindRawQuery = queryTemplate.substring(currentVar.length());
            }
            String currentVarInput;
            if (matchToLookBehindRawQuery.isEmpty()) {
                // If there is no string after the current variable, then remaining segment of rawQuery is the
                // current variable input (rawQuery is sliced up to the next variable)
                currentVarInput = rawQuery;
            } else {
                Matcher lookBehindRQ = Pattern.compile("(.*?)(?=" + Pattern.quote(matchToLookBehindRawQuery) + ")").matcher(rawQuery);
                if (!lookBehindRQ.find()) {
                    throw new QueryMisMatch(QUERY_MISMATCH_EXCEPTION_MESSAGE);
                }
                currentVarInput = lookBehindRQ.group();
            }
            if (isTenantIdVar(currentVar)) {
                preparedQueryBuilder.append(GrafanaUtil.getTenantId());
            } else {
                // Grafana variable input can be multi-valued, which are separated by comma by default
                String[] varValues = splitByComma(currentVarInput);
                List<String> preparedStatementPlaceHolders = new ArrayList<>();
                for (String v : varValues) {
                    String param = unQuoteString(v);
                    if (isSafeVariableInput(param)) {
                        preparedStatementPlaceHolders.add(v);
                    } else {
                        parameters.add(param);
                        preparedStatementPlaceHolders.add(PreparedQuery.PREPARED_SQL_PARAM_PLACEHOLDER);
                    }
                }
                preparedQueryBuilder.append(String.join(COMMA_SEPARATOR, preparedStatementPlaceHolders));
            }
            preparedQueryBuilder.append(matchToLookBehindRawQuery);
            // Get template and raw query string from next variable
            queryTemplate = queryTemplate.substring(currentVar.length() + matchToLookBehindRawQuery.length());
            rawQuery = rawQuery.substring(currentVarInput.length() + matchToLookBehindRawQuery.length());

            varMatch = varPattern.matcher(queryTemplate);
        }
        if (!queryTemplate.equals(rawQuery)) {
            throw new QueryMisMatch(QUERY_MISMATCH_EXCEPTION_MESSAGE);
        }
        return new PreparedQuery(preparedQueryBuilder.toString(), parameters);
    }

    private static String[] splitByComma(String str) {
        // Using regex to avoid splitting by comma inside quotes
        return str.split("(\\s|\\t)*,(\\s|\\t)*(?=(?:[^'\"]*['|\"][^'\"]*['|\"])*[^'\"]*$)");
    }

    private static String templateParamWGrafanaVariables(String queryTemplate) {
        // TODO: handle escaped quotes and special characters
        Pattern quotedStringPattern = Pattern.compile("(\"(.+?)\")|('(.+?)')");
        Matcher quotedStringMatch = quotedStringPattern.matcher(queryTemplate);
        while(quotedStringMatch.find()) {
            String quotedString = quotedStringMatch.group();
            Matcher varMatcher = Pattern.compile(GRAFANA_VAR_REGEX).matcher(quotedString);
            // If grafana variable exists in single quoted string
            if(varMatcher.find()) {
                String var = varMatcher.group();
                if (!isTenantIdVar(var)) {
                    String templatedQuotedString = templateQuotedString(quotedString);
                    // escape any special characters
                    templatedQuotedString = Matcher.quoteReplacement(templatedQuotedString);
                    queryTemplate = queryTemplate.replaceFirst(Pattern.quote(quotedString), templatedQuotedString);
                }
            }
        }
        return queryTemplate;
    }

    private static String templateQuotedString(String quotedString) {
        return quotedString.replaceFirst("[^']+|[^\"]+",
                Matcher.quoteReplacement(VAR_PARAM_TEMPLATE));
    }

    private static boolean isSafeVariableInput(String currentVarInput) {
        if (StringUtils.isEmpty(currentVarInput)) {
            return true;
        }
        return currentVarInput.matches("\\$?[a-zA-Z0-9-_\\.]+|^\"[a-zA-Z0-9-_\\.\\s]+\"$|^'[a-zA-Z0-9-_\\.\\s]+'$");
    }

    private static String unQuoteString(String str) {
        if (isQuoted(str)) {
            int firstCharIndex = 0;
            int lastCharIndex = str.length() - 1;
            return str.substring(firstCharIndex + 1, lastCharIndex);
        }
        return str;
    }

    private static boolean isQuoted(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        int firstCharIndex = 0;
        int lastCharIndex = str.length() - 1;
        return (str.charAt(firstCharIndex) == '\'' && str.charAt(lastCharIndex) == '\'') ||
                (str.charAt(firstCharIndex) == '"' && str.charAt(lastCharIndex) == '"');
    }

    private static boolean isTenantIdVar(String var) {
        return var.equals(GrafanaConstants.TENANT_ID_VAR) || var.equals(singleQuoteString(GrafanaConstants.TENANT_ID_VAR))
                || var.equals(doubleQuoteString(GrafanaConstants.TENANT_ID_VAR));
    }

    private static String doubleQuoteString(String str) {
        return "\"" + str + "\"";
    }

    private static String singleQuoteString(String str) {
        return "'" + str + "'";
    }

}
