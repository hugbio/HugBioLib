package com.hugbio.utils;


import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;

/**
 * SSLContextEx的代理。用于使用httpClient实现的网络连接。如果使用HttpsURLConnection实现网络连接则不需要使用此类，直接使用SSLContextEx
 * 作者： huangbiao
 * 时间： 2017-02-07
 */
public class SSLSocketFactoryEx extends SSLSocketFactory {
    SSLContext sslContext = null;

    public SSLSocketFactoryEx(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(truststore);
        sslContext = SSLContextEx.getSSlContext();
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        return sslContext.getSocketFactory().createSocket(socket, host, port,
                autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }

}
