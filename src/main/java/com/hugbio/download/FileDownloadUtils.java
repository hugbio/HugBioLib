package com.hugbio.download;

import android.os.Handler;
import android.text.TextUtils;

import com.hugbio.utils.FileHelper;
import com.hugbio.utils.HttpUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Arrays;

/**
 * 文件下载工具类。支持断点续传
 * 由 HBOK 在 2016/8/16 创建.
 */
public class FileDownloadUtils {
    private static final int CHECK_SIZE = 512;


    public static String getTempFilePath(String saveFilePath) {
        return saveFilePath + ".tmp";
    }

    /**
     * 获取下载的开始位置
     *
     * @return 返回0 或者 "断点位置-CHECK_SIZE"
     */
    public static long getRange(String tempSaveFilePath) {
        File tempFile = new File(tempSaveFilePath);
        if (tempFile.exists() && !tempFile.isDirectory()) {
            long length = tempFile.length();
            if (length <= CHECK_SIZE) {
                FileHelper.deleteFileOrDir(tempFile);
            } else {
                return length - CHECK_SIZE;
            }
        }
        return 0;
    }

    /***
     * 判断服务器是否支持断点续传
     *
     * @return
     */
    public static boolean isSupportRange(HttpURLConnection connection) {
        if (connection == null) return false;
        String ranges = connection.getHeaderField("Accept-Ranges");
        if (ranges != null) {
            return ranges.contains("bytes");
        }
        ranges = connection.getHeaderField("Content-Range");
        return ranges != null && ranges.contains("bytes");
    }

    /***
     * 支持断点续传的下载
     *
     * @return 0-下载成功；1-本地文件被破坏或者与服务器文件不一致；3-用户中断下载；2010-未知错误
     */
    public static int downLoad(HttpURLConnection connection, String tempSaveFilePath, boolean isDeleteTempForFail, long range, DownloadParams params) {
        File targetFile = null;
        InputStream in = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            int contentLength = connection.getContentLength();
            in = connection.getInputStream();
            targetFile = new File(tempSaveFilePath);
            if (targetFile.isDirectory()) {
                // 删除重名的目录
                FileHelper.deleteFileOrDir(targetFile);
            }
            if (!targetFile.exists()) {
                File dir = targetFile.getParentFile();
                if (dir.exists() || dir.mkdirs()) {
                    targetFile.createNewFile();
                }
            }

            long targetFileLen = targetFile.length();
            if (range > 0 && targetFileLen > 0) {  //判断是否可以断点续传
                FileInputStream fis = null;
                try {
                    long filePos = targetFileLen - CHECK_SIZE;
                    if (filePos > 0) {
                        fis = new FileInputStream(targetFile);
                        byte[] fileCheckBuffer = IOUtil.readBytes(fis, filePos, CHECK_SIZE);
                        byte[] checkBuffer = IOUtil.readBytes(in, 0, CHECK_SIZE);
                        if (!Arrays.equals(checkBuffer, fileCheckBuffer)) {  //本地文件被破坏或者与服务器文件不一致
                            IOUtil.closeQuietly(fis); // 先关闭文件流, 否则文件删除会失败.
                            FileHelper.deleteFileOrDir(targetFile);
                            return DownloadResult.DOWNRESULT_FILE_INCONSISTENT;
                        } else {
                            contentLength -= CHECK_SIZE;
                        }
                    } else {  //本地文件太小，只要调用了getRange方法，几乎不会出现这个问题
                        return DownloadResult.DOWNRESULT_FILE_INCONSISTENT;
                    }
                } finally {
                    IOUtil.closeQuietly(fis);
                }
            } else if (targetFileLen > 0) {  //删除本地文件，重新下载
                FileHelper.deleteFileOrDir(targetFile);
                targetFileLen = 0;
            }

            //开始下载
            FileOutputStream fileOutputStream = null;
            if (range > 0) {
                range = targetFileLen;
                fileOutputStream = new FileOutputStream(targetFile, true);  //从文件末尾写入
            } else {
                fileOutputStream = new FileOutputStream(targetFile);  //从文件开头写入
            }
            long total = contentLength + range;
            bis = new BufferedInputStream(in);
            bos = new BufferedOutputStream(fileOutputStream);
            byte[] tmp = new byte[4096];
            int len;
            while ((len = bis.read(tmp)) != -1) {
                if (params.getDownStatus() > DownloadParams.DOWNSTATUS_DEFAULT && params.getDownStatus() < DownloadParams.DOWNSTATUS_COMPLETE) {  //用户中断下载
                    bos.flush();
                    if(params.getDownStatus() == DownloadParams.DOWNSTATUS_CANCELING || params.getDownStatus() == DownloadParams.DOWNSTATUS_CANCEL ){  //取消下载
                        params.setInterruptStatus(DownloadParams.DOWNSTATUS_CANCEL);
                        targetFile.delete();
                    }else {
                        params.setInterruptStatus(DownloadParams.DOWNSTATUS_PAUSE);
                    }
                    return DownloadResult.DOWNRESULT_USER_INTERRUPT;
                }
                bos.write(tmp, 0, len);
                range += len;
                HttpUtils.ProgressRunnable progressCallback = params.getProgressCallback();
                Handler handler = params.getHandler();
                if (progressCallback != null && handler != null && total > 0) {
                    int per = (int) (range * 100 / total);
                    if (progressCallback.mPercentage != per) {
                        progressCallback.mPercentage = per;
                        handler.post(progressCallback);
                    }
                }
            }
            bos.flush();
            return DownloadResult.DOWNRESULT_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            if (isDeleteTempForFail) {
                FileHelper.deleteFile(tempSaveFilePath);
            }
            return DownloadResult.DOWNRESULT_UNKNOWN_ERROR;
        } finally {
            IOUtil.closeQuietly(bis);
            IOUtil.closeQuietly(bos);
            IOUtil.closeQuietly(in);
        }
    }

    /***
     * 删除下载文件，包括临时缓存文件和下载完成的文件
     */
    public static void deleteDownloadFile(String saveFilePath){
        if(TextUtils.isEmpty(saveFilePath)){
            return;
        }
        FileHelper.deleteFile(saveFilePath);
        FileHelper.deleteFile(getTempFilePath(saveFilePath));
    }

}
