package com.hugbio.download;

/**
 * 作者： huangbiao
 * 时间： 2017-02-04
 */
public class DownloadResult {
 /*
  *2000  参数错误
  * 2001 服务器返回数据为空
  * 2002  连接错误
  * 2003 下载成功但文件保存（重命名）失败
  * 2010  未知的错误
  * 0 下载成功
  * 1-本地文件被破坏或者与服务器文件不一致；2-本地已下载的文件太小；3-用户中断下载；
  */
    public static final int DOWNRESULT_PARAMS_ERROR = 2000;
    public static final int DOWNRESULT_DATA_EMPTY = 2001;
    public static final int DOWNRESULT_CONNECTION_ERROR = 2002;
    public static final int DOWNRESULT_RENAME_FAILED = 2003;
    public static final int DOWNRESULT_UNKNOWN_ERROR = 2010;
    public static final int DOWNRESULT_SUCCESS = 0;
    public static final int DOWNRESULT_FILE_INCONSISTENT = 1;
    public static final int DOWNRESULT_FILE_TOO_SMALL = 2;
    public static final int DOWNRESULT_USER_INTERRUPT = 3;
}
