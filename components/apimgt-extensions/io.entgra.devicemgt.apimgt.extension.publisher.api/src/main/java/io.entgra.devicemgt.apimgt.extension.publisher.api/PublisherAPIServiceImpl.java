package io.entgra.devicemgt.apimgt.extension.publisher.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class PublisherAPIServiceImpl implements PublisherAPIService {
    private static final Log log = LogFactory.getLog(PublisherAPIServiceImpl.class);

    @Override
    public void registerApplication() {
        try {
            HttpClient httpclient;
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
            httpclient = org.apache.http.impl.client.HttpClients.custom().setSSLSocketFactory(sslsf).useSystemProperties().build();

            URL url =new URL("https://localhost:9443/client-registration/v0.17/register");
            HttpPost request = new HttpPost(url.toString());

            String payload = "{\n" +
                    "  \"callbackUrl\":\"www.google.lk\",\n" +
                    "  \"clientName\":\"rest_api_publisher_code\",\n" +
                    "  \"owner\":\"admin\",\n" +
                    "  \"grantType\":\"client_credentials password refresh_token\",\n" +
                    "  \"saasApp\":true\n" +
                    "  }";

            StringEntity entity = new StringEntity(payload);
            request.setEntity(entity);
            String encoding = DatatypeConverter.printBase64Binary("admin:admin".getBytes("UTF-8"));

            request.setHeader("Authorization", "Basic " + encoding);
            request.setHeader("Content-Type", "application/json");

            HttpResponse httpResponse = httpclient.execute(request);

            if (httpResponse != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                String readLine;
                String response = "";
                while (((readLine = br.readLine()) != null)) {
                    response += readLine;
                }
                System.out.println(response);
            }

            System.out.println(httpResponse.getStatusLine().getStatusCode());

        } catch (IOException | NoSuchAlgorithmException | KeyStoreException |
                 KeyManagementException e) {
            log.error("failed to call http client.", e);
        }

    }
}
