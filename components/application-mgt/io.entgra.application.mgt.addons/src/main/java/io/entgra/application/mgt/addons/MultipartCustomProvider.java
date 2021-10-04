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
package io.entgra.application.mgt.addons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import io.entgra.application.mgt.addons.jaxrs.AnnotationExclusionStrategy;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Provider for the text/plain type of input. Particularly use-ful for the complex objects sent along with Multipart
 * request.
 */
@Provider
@Consumes(MediaType.TEXT_PLAIN)
public class MultipartCustomProvider implements MessageBodyReader<Object> {
    private Gson gson;

    public MultipartCustomProvider() {
        final GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
                .setExclusionStrategies(new AnnotationExclusionStrategy());
        gson = gsonBuilder.create();
    }
    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return !aClass.equals(Attachment.class);
    }

    @Override
    public Object readFrom(Class<Object> objectClass, Type type, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> headers, InputStream inputStream) throws IOException,
            WebApplicationException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        String jsonString = result.toString();
        JsonObject obj = new JsonParser().parse(jsonString).getAsJsonObject();
        return gson.fromJson(obj, type);
    }
}
