package io.entgra.devicemgt.apimgt.extension.publisher.api.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class PublisherRESTAPIUtil {
    private static final Log log = LogFactory.getLog(PublisherRESTAPIUtil.class);
    private static final String HTTPS_PROTOCOL = "https";

    /**
     * Return a http client instance
     *
     * @param protocol- service endpoint protocol http/https
     * @return
     */
    public static HttpClient getHttpClient(String protocol)
            throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        HttpClient httpclient;
        if (HTTPS_PROTOCOL.equals(protocol)) {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
            httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).useSystemProperties().build();
        } else {
            httpclient = HttpClients.createDefault();
        }
        return httpclient;
    }

    public static String getResponseString(HttpResponse httpResponse) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            String readLine;
            String response = "";
            while (((readLine = br.readLine()) != null)) {
                response += readLine;
            }
            return response;
        } finally {
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.warn("Error while closing the connection! " + e.getMessage());
                }
            }
        }
    }
}
