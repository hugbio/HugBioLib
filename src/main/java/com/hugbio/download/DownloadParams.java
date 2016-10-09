package com.hugbio.download;

import android.os.Handler;

import com.hugbio.utils.HttpUtils;

import java.lang.ref.WeakReference;

/**
 * 下载参数与控制类
 * 由 HBOK 在 2016/8/17 创建.
 */
public class DownloadParams {

    public static final int DOWNSTATUS_DEFAULT = 0;  //默认状态
    public static final int DOWNSTATUS_PAUSEING = 1;  //暂停中
    public static final int DOWNSTATUS_PAUSE = 2;   //暂停
    public static final int DOWNSTATUS_CANCELING = 3;   //取消中
    public static final int DOWNSTATUS_CANCEL = 4;   //取消
    public static final int DOWNSTATUS_COMPLETE = 5;  //下载完成
    public static final int DOWNSTATUS_FAILURE = 6;  //下载失败

    public String url;
    public String savePath;
    private boolean isRestart = true;  //是否重新下载，只有开启断点续传时有效，默认开启，
                                                                // 需要先判断是否存在一个相同并且已中断的任务，当存在时关闭重新下载进行续传下载
    private WeakReference<HttpUtils.ProgressRunnable> wrCallback = null;
    private Handler handler;
    public int downStatus = DOWNSTATUS_DEFAULT;  // 用于控制下载
    private boolean isResume = true;   //是否开启断点续传，默认开启，当isRestart为false时尝试进行续传下载否则重新下载
    private int timeOut = 8000;  //默认8秒的超时时间（连接和读取）

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
        downStatus = DOWNSTATUS_CANCELING;
    }

    public void pause(){
        downStatus = DOWNSTATUS_PAUSEING;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public int getTimeOut() {
        return timeOut;
    }

    /***
     * 仅在库中使用。外部不能调用
     */
    public void setRestore(){
        downStatus = DOWNSTATUS_DEFAULT;
    }

    /***
     * 仅在库中使用。外部不能调用
     */
    public void setComplete(){
        downStatus = DOWNSTATUS_COMPLETE;
    }

    /***
     * 仅在库中使用。外部不能调用
     */
    public void setInterruptStatus(int status){
        if(status != DOWNSTATUS_CANCEL && status != DOWNSTATUS_PAUSE){
            return;
        }
        downStatus = status;
    }
}
