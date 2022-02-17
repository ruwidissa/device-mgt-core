/*
 * Copyright (c) 2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
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
package io.entgra.ui.request.interceptor.beans;

import org.apache.http.Header;

public class ProxyResponse {

    public static class Status {
        public static int SUCCESS = 1;
        public static int ERROR = 0;
    }

    private int code;
    private String data;
    private String executorResponse;
    private int status;
    private Header[] headers;

    public int getCode() { return code; }

    public void setCode(int code) { this.code = code; }

    public String getData() { return data; }

    public void setData(String data) { this.data = data; }

    public String getExecutorResponse() { return executorResponse; }

    public void setExecutorResponse(String executorResponse) { this.executorResponse = executorResponse; }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Header[] getHeaders() {
        return headers;
    }

    public void setHeaders(Header[] headers) {
        this.headers = headers;
    }

}
