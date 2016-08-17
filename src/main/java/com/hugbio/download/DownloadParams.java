package com.hugbio.download;

import android.os.Handler;

import com.hugbio.utils.HttpUtils;

import java.lang.ref.WeakReference;

/**
 * 下载参数与控制类
 * 由 HBOK 在 2016/8/17 创建.
 */
public class DownloadParams {
    public String url;
    public String savePath;
    private boolean isRestart = true;  //是否重新下载，只有开启断点续传时有效，默认开启，
                                                                // 需要先判断是否存在一个相同并且已中断的任务，当存在时关闭重新下载进行续传下载
    private WeakReference<HttpUtils.ProgressRunnable> wrCallback = null;
    private Handler handler;
    public int downStatus = 0;  //0:默认状态   1:暂停状态    2:取消状态  3:下载完成
    private boolean isResume = true;   //是否开启断点续传，默认开启，当isRestart为false时尝试进行续传下载否则重新下载

    public DownloadParams(String url, String savePath) {
        this(url,savePath,true);
    }

    public DownloadParams(String url, String savePath, boolean isRestart) {
        this.url = url;
        this.savePath = savePath;
        this.isRestart = isRestart;
    }

    public void setRestart(boolean restart) {
        isRestart = restart;
    }

    public boolean isResume() {
        return isResume;
    }

    public void setResume(boolean resume) {
        isResume = resume;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public boolean isRestart() {
        return isRestart;
    }

    public HttpUtils.ProgressRunnable getProgressCallback() {
        if(wrCallback != null){
            return wrCallback.get();
        }
        return null;
    }

    public void setProgressCallback(HttpUtils.ProgressRunnable progress) {
        handler = new Handler();
        this.wrCallback = new WeakReference<HttpUtils.ProgressRunnable>(progress);
    }

    public Handler getHandler() {
        return handler;
    }
//
//    public void setHandler(Handler handler) {
//        this.handler = handler;
//    }

    public int getDownStatus() {
        return downStatus;
    }

    public void cancel(){
        downStatus = 2;
    }

    public void pause(){
        downStatus = 1;
    }
    public void restore(){
        downStatus = 0;
    }

    public void setComplete(){
        downStatus = 3;
    }
}
