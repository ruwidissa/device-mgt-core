/*
 * Copyright (c) 2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.transport.mgt.sms.handler.api.service.addons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class GsonMessageBodyHandler {

    public static final String DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";
    private Gson gson;
    private static final String UTF_8 = "UTF-8";

    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    private Gson getGson() {
        if (gson == null) {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            gson = gsonBuilder.setDateFormat(DATE_FORMAT).create();
        }
        return gson;
    }

    public Object readFrom(Class<Object> objectClass, Type type, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> stringStringMultivaluedMap, InputStream entityStream)
            throws IOException, WebApplicationException {

        InputStreamReader reader = new InputStreamReader(entityStream, "UTF-8");

        try {
            return getGson().fromJson(reader, type);
        } finally {
            reader.close();
        }
    }

    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    public long getSize(Object o, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    public void writeTo(Object object, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> stringObjectMultivaluedMap, OutputStream entityStream)
            throws IOException, WebApplicationException {

        OutputStreamWriter writer = new OutputStreamWriter(entityStream, UTF_8);
        try {
            getGson().toJson(object, type, writer);
        } finally {
            writer.close();
        }
    }
}
