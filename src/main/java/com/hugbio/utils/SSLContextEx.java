package com.hugbio.utils;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 实现https的网络连接验证。这里不验证证书，直接信任
 * 作者： huangbiao
 * 时间： 2017-02-07
 */
public class SSLContextEx {

    public static SSLContext getSSlContext() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        //如果需要验证证书。
        // 则需要先根据证书文件初始化Certificate（CA），然后通过CA创建KeyStore。
        // 再根据KeyStore（一个或多个）创建TrustManager（TrustManager是系统用于从服务器验证证书的工具），创建的 TrustManager 将仅信任这些 CA
        X509TrustManager tm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {  //校验服务端证书，这里不进行检查
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sslContext.init(null, new TrustManager[]{tm}, null);
        return sslContext;
    }
}
