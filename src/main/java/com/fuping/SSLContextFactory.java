package com.fuping;

import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SSLContextFactory {


    public static SSLContext getSSLContext() {

        SSLContext sslContext = null;
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            TrustStrategy anyTrustStrategy = new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            };
            sslContext = SSLContexts.custom().useTLS().loadTrustMaterial(trustStore, anyTrustStrategy).build();
            sslContext = SSLContexts.custom().useSSL().loadTrustMaterial(trustStore, anyTrustStrategy).build();
        } catch (KeyStoreException e) {

        } catch (NoSuchAlgorithmException e) {

        } catch (KeyManagementException e) {

        }

        return sslContext;
    }
}