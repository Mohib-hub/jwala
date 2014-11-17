package com.siemens.cto.aem.si.ssl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

public class SslClientHttpRequestFactory extends SimpleClientHttpRequestFactory {

    private HostnameVerifier verifier;
    private final SSLContext context;
    private final SSLSocketFactory socketFactory;
    
    public SslClientHttpRequestFactory() throws KeyManagementException, NoSuchAlgorithmException {
        
        context = SSLContext.getInstance("TLS");

        TrustManager[] tm = { new TrustingX509TrustManager() };
        context.init(null,  tm, new SecureRandom());

        socketFactory = context.getSocketFactory(); 
    }
    
    @Override
    protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
        
        if(connection instanceof HttpsURLConnection) { 
            HttpsURLConnection httpsConnection = (HttpsURLConnection)connection;
            httpsConnection.setHostnameVerifier(verifier);
            httpsConnection.setSSLSocketFactory(socketFactory);
        }
        
        super.prepareConnection(connection, httpMethod);
    }

    public HostnameVerifier getVerifier() {
        return verifier;
    }

    public void setVerifier(HostnameVerifier verifier) {
        this.verifier = verifier;
    }
}
