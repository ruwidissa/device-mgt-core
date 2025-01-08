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

package io.entgra.device.mgt.core.apimgt.extension.rest.api.util;

import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HttpsTrustManagerUtils {

    private static final Log log = LogFactory.getLog(HttpsTrustManagerUtils.class);
    private static final String KEY_STORE_TYPE = "JKS";
    /**
     * Default truststore type of the client
     */
    private static final String TRUST_STORE_TYPE = "JKS";
    /**
     * Default keymanager type of the client
     */
    private static final String KEY_MANAGER_TYPE = "SunX509"; //Default Key Manager Type
    /**
     * Default trustmanager type of the client
     */
    private static final String TRUST_MANAGER_TYPE = "SunX509"; //Default Trust Manager Type
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_HOST_IP = "127.0.0.1";
    private static final int TIMEOUT = 1000;

    public static OkHttpClient getSSLClient() {

        boolean isIgnoreHostnameVerification = Boolean.parseBoolean(System.getProperty("org.wso2"
                + ".ignoreHostnameVerification"));
        OkHttpClient okHttpClient;
        final String proxyHost = System.getProperty("http.proxyHost");
        final String proxyPort = System.getProperty("http.proxyPort");
        final String nonProxyHostsValue = System.getProperty("http.nonProxyHosts");

        final ProxySelector proxySelector = new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                List<Proxy> proxyList = new ArrayList<>();
                String host = uri.getHost();

                if (!StringUtils.isEmpty(host)) {
                    if (host.startsWith(DEFAULT_HOST_IP) || host.startsWith(DEFAULT_HOST) || StringUtils
                            .isEmpty(nonProxyHostsValue) || StringUtils.contains(nonProxyHostsValue, host) ||
                            StringUtils.isEmpty(proxyHost) || StringUtils.isEmpty(proxyPort)) {
                        proxyList.add(Proxy.NO_PROXY);
                    } else {
                        proxyList.add(new Proxy(Proxy.Type.HTTP,
                                new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort))));
                    }
                } else {
                    log.error("Host is null. Host could not be empty or null");
                }
                return proxyList;
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        X509TrustManager trustAllCerts = new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }

            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }
        };

        if (isIgnoreHostnameVerification) {
            SSLSocketFactory simpleSSLSocketFactory = getSimpleTrustedSSLSocketFactory();
            if (simpleSSLSocketFactory == null) {
                String msg = "Null received as the simple ssl socket factory";
                log.error(msg);
                throw new IllegalStateException(msg);
            }

            okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .connectionPool(new ConnectionPool(TIMEOUT, TIMEOUT, TimeUnit.SECONDS))
                    .sslSocketFactory(simpleSSLSocketFactory, trustAllCerts)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return true;
                        }
                    }).proxySelector(proxySelector).build();
            return okHttpClient;
        } else {
            SSLSocketFactory trustedSSLSocketFactory = getTrustedSSLSocketFactory();
            if (trustedSSLSocketFactory == null) {
                String msg = "Null received as the trusted ssl socket factory";
                log.error(msg);
                throw new IllegalStateException(msg);
            }

            try {
                TrustManagerFactory trustManagerFactory =
                        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(loadTrustStore(ServerConfiguration.getInstance().getFirstProperty(
                        "Security.TrustStore.Location"), ServerConfiguration.getInstance().getFirstProperty(
                        "Security.TrustStore.Password")));
                TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

                if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                    String msg = "Unexpected default trust managers:" + Arrays.toString(trustManagers);
                    log.error(msg);
                    throw new IllegalStateException(msg);
                }

                okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                        .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                        .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                        .connectionPool(new ConnectionPool(TIMEOUT, TIMEOUT, TimeUnit.SECONDS))
                        .sslSocketFactory(trustedSSLSocketFactory, (X509TrustManager) trustManagers[0])
                        .proxySelector(proxySelector)
                        .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
                        .build();
            } catch (NoSuchAlgorithmException e) {
                String msg = "Error encountered when initializing the trust manager factory with default algorithm : "
                        + TrustManagerFactory.getDefaultAlgorithm();
                log.error(msg, e);
                throw new IllegalStateException(msg, e);
            } catch (CertificateException e) {
                String msg = "Any of the certificates in the keystore could not be loaded";
                log.error(msg, e);
                throw new IllegalStateException(msg, e);
            } catch (KeyStoreException e) {
                String msg = "Provided keystore provider does not support the keystore service provider implementation";
                log.error(msg);
                throw new IllegalStateException(msg, e);
            } catch (IOException e) {
                String msg = "IO exception encountered while loading keystore";
                log.error(msg);
                throw new IllegalStateException(msg, e);
            }
            return okHttpClient;
        }
    }

    private static SSLSocketFactory getSimpleTrustedSSLSocketFactory() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            return sc.getSocketFactory();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            log.error("Error while creating the SSL socket factory due to " + e.getMessage(), e);
            return null;
        }
    }

    private static SSLSocketFactory getTrustedSSLSocketFactory() {
        try {
            String keyStorePassword = ServerConfiguration.getInstance().getFirstProperty("Security.KeyStore.Password");
            String keyStoreLocation = ServerConfiguration.getInstance().getFirstProperty("Security.KeyStore.Location");
            String trustStorePassword = ServerConfiguration.getInstance().getFirstProperty(
                    "Security.TrustStore.Password");
            String trustStoreLocation = ServerConfiguration.getInstance().getFirstProperty(
                    "Security.TrustStore.Location");
            KeyStore keyStore = loadKeyStore(keyStoreLocation, keyStorePassword, KEY_STORE_TYPE);
            KeyStore trustStore = loadTrustStore(trustStoreLocation, trustStorePassword);

            return initSSLConnection(keyStore, keyStorePassword, trustStore);
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException
                 | CertificateException | IOException | UnrecoverableKeyException e) {
            log.error("Error while creating the SSL socket factory due to " + e.getMessage(), e);
            return null;
        }
    }

    private static SSLSocketFactory initSSLConnection(KeyStore keyStore, String keyStorePassword, KeyStore trustStore)
            throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, KeyManagementException {
        final String tlsProtocol = System.getProperty("tls.protocol");
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KEY_MANAGER_TYPE);
        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TRUST_MANAGER_TYPE);
        trustManagerFactory.init(trustStore);

        // Create and initialize SSLContext for HTTPS communication
        SSLContext sslContext = SSLContext.getInstance(tlsProtocol);
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        SSLContext.setDefault(sslContext);
        return sslContext.getSocketFactory();
    }

    private static KeyStore loadKeyStore(String keyStorePath, String ksPassword, String type)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        InputStream fileInputStream = null;
        try {
            char[] keypassChar = ksPassword.toCharArray();
            KeyStore keyStore = KeyStore.getInstance(type);
            fileInputStream = new FileInputStream(keyStorePath);
            keyStore.load(fileInputStream, keypassChar);
            return keyStore;
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    private static KeyStore loadTrustStore(String trustStorePath, String tsPassword)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        return loadKeyStore(trustStorePath, tsPassword, TRUST_STORE_TYPE);
    }
}
