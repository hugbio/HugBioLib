/**
 * Copyright (c) 2012-2013, Michael Yang 杨福海 (www.yangfuhai.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tsz.afinal.bitmap.download;

import net.tsz.afinal.FinalBitmap;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/**
 * @title 根据 图片url地址下载图片 可以是本地和网络
 * @author 杨福海（michael） www.yangfuhai.com
 */
public class SimpleDownloader implements Downloader {

    @SuppressWarnings("unused")
    private static final String TAG = SimpleDownloader.class.getSimpleName();
    private static final int IO_BUFFER_SIZE = 8 * 1024; // 8k

    public byte[] download(String urlString, FinalBitmap.BitmapLoadAndDisplayTask task) {
        if (urlString == null)
            return null;

        if (urlString.trim().toLowerCase().startsWith("http")) {
            return getFromHttp(urlString, task);
        } else if (urlString.trim().toLowerCase().startsWith("file:")) {
            try {
                File f = new File(new URI(urlString));
                if (f.exists() && f.canRead()) {
                    return getFromFile(f);
                }
            } catch (URISyntaxException e) {
                //Log.e(TAG, "Error in read from file - " + urlString + " : " + e);
            }
        } else {
            File f = new File(urlString);
            if (f.exists() && f.canRead()) {
                return getFromFile(f);
            }
        }

        return null;
    }

    private byte[] getFromFile(File file) {
        if (file == null) return null;

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } catch (Throwable e) {
            //Log.e(TAG, "Error in read from file - " + file + " : " + e);
            if (e instanceof OutOfMemoryError) {
                System.gc();
            }
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                    fis = null;
                } catch (IOException e) {
                    // do nothing
                }
            }
        }

        return null;
    }

    private byte[] getFromHttp(String urlString, FinalBitmap.BitmapLoadAndDisplayTask task) {
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        FlushedInputStream in = null;

        try {
            final URL url = new URL(urlString);
            if (url.getProtocol().toLowerCase().contains("https")) {
                urlConnection = (HttpsURLConnection) url.openConnection();
                SSLContext ssl = SSLContextEx.getSSlContext();
                ((HttpsURLConnection) urlConnection).setSSLSocketFactory(ssl.getSocketFactory());
                ((HttpsURLConnection) urlConnection).setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); //允许所有主机的验证
            } else {
                urlConnection = (HttpURLConnection) url.openConnection();
            }
            urlConnection.setRequestProperty("Accept-Encoding", "identity");
            in = new FlushedInputStream(new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int max = urlConnection.getContentLength();
            int percent = 0;
            int length = 0;
            int b;
            while ((b = in.read()) != -1) {
                baos.write(b);

                length++;
                if (length >= max / 100 && max > 100 && task != null && task.isHaveDownLoadCallback()) {
                    percent++;
                    length = 0;
                    task.updateProgress(max, percent);
                }
            }

            byte[] byteArray = baos.toByteArray();
            baos.close();
            return byteArray;
        } catch (final Exception e) {
            //Log.e(TAG, "Error in downloadBitmap - " + urlString + " : " + e);
        } catch (OutOfMemoryError e) {
            System.gc();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
            }
        }
        return null;
    }

    public class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int by_te = read();
                    if (by_te < 0) {
                        break; // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
}
