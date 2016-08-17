package com.hugbio.runner;

import android.os.Handler;
import android.text.TextUtils;

import com.hugbio.core.EventManager.OnEventRunner;
import com.hugbio.download.DownloadParams;
import com.hugbio.utils.ErrorMsgException;
import com.hugbio.utils.HttpUtils;
import com.hugbio.utils.StringIdException;

import org.json.JSONObject;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class HttpRunner implements OnEventRunner {

    private List<Integer> resultForOK = Arrays.asList(0, 1000, 1001, 1002);
    private static String SERVER = null;
    private static String SERVERBYIP = null;

    public static boolean isDomainToIP = false;

    public static void setDomainToIP(String server) {
        if (!TextUtils.isEmpty(server) && server.contains(".c")) {
            isDomainToIP = true;
            SERVER = server;
        }
    }

    private void domainToIP() {
        String server = SERVER;
        if (!TextUtils.isEmpty(server) && server.contains(".c")) {
            int start = 0;
            int end = server.length();
            if (server.contains("http")) {
                start = server.indexOf("//") + 2;
            }
            int indexOf = server.indexOf("/", start);
            if (indexOf > 0) {
                end = indexOf;
            }
            java.net.InetAddress inetAddress;
            try {
                inetAddress = java.net.InetAddress.getByName(server.substring(start, end));
                SERVERBYIP = server.substring(0, start) + inetAddress.getHostAddress() + server.substring(end);// 得到字符串形式的ip地址
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    public String processUrl(String url) {
        if (!isDomainToIP || TextUtils.isEmpty(url) || !url.startsWith(SERVER)) {
            return url;
        }
        if (TextUtils.isEmpty(SERVERBYIP)) {
            domainToIP();
            if (TextUtils.isEmpty(SERVERBYIP)) {
                return url;
            }
        }
        return url.replaceFirst(SERVER, SERVERBYIP);
    }


    protected String getUrlParam(String strUrl, String strParamName) {
        int nIndexStart = strUrl.indexOf("&" + strParamName);
        if (nIndexStart >= 0) {
            nIndexStart = strUrl.indexOf("=", nIndexStart) + 1;
            int nEnd = strUrl.indexOf("&", nIndexStart);
            if (nEnd == -1) {
                nEnd = strUrl.length();
            }
            return strUrl.substring(nIndexStart, nEnd);
        }
        return "";
    }

    protected boolean checkRequestSuccess(JSONObject jsonObject) {
        try {
            int result = jsonObject.getInt("result");
            if (resultForOK.contains(result)) {
                return true;
            } else if (result == 3003) {
                HttpUtils.sessid = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    protected boolean checkRequestSuccess(String strJson) {
        try {
            JSONObject jsonObject = new JSONObject(strJson);
            return "0".equals(jsonObject.getString("result"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected JSONObject doGet(String url) throws Exception {
        url = processUrl(url);
        final String fixUrl = addUrlCommonParams(url);
        // Log.e("----", "---请求地址----" + fixUrl);
        final String ret = HttpUtils.doGetString(fixUrl);
        // Log.e("----", "--返回数据---" + ret);
        return onHandleHttpRet(ret);
    }

    protected JSONObject doGet(String url, HashMap<String, String> mapValues) throws Exception {
        url = processUrl(url);
        final String fixUrl = addUrlCommonParams(url);
        // Log.e("----", "---请求地址-get---" + fixUrl);
        StringBuffer stringBuffer = new StringBuffer(fixUrl);
        Set<String> keysSet = mapValues.keySet();
        String s = "?";
        for (String key : keysSet) {
            stringBuffer.append(s + key + "=" + mapValues.get(key));
            s = "&";
        }

        final String ret = HttpUtils.doGetString(stringBuffer.toString());
        // Log.e("----", "--返回数据----" + ret);
        return onHandleHttpRet(ret);
    }

    protected JSONObject doPost(String url, HashMap<String, String> mapValues) throws Exception {
        return doPost(url, mapValues, null);
    }

    protected JSONObject doPostForJson(String url, Object jsonObject) throws Exception {
        url = processUrl(url);
        final String fixUrl = addUrlCommonParams(url);
        if (jsonObject == null) {
            jsonObject = new JSONObject();
        }
        final String ret = HttpUtils.doPostForJson(fixUrl, jsonObject);
        return onHandleHttpRet(ret);
    }

    protected JSONObject doPost(String url, HashMap<String, String> mapValues, HashMap<String, String> mapFiles) throws Exception {
        return doPost(url, mapValues, mapFiles, null, null, null);
    }

    protected JSONObject doPost(String url, HashMap<String, String> mapValues, HashMap<String, String> mapFiles, HttpUtils.ProgressRunnable pr,
                                Handler handler, AtomicBoolean cancel) throws Exception {
        url = processUrl(url);
        final String fixUrl = addUrlCommonParams(url);
        if (mapValues == null) {
            mapValues = new HashMap<String, String>();
        }
        final String ret = HttpUtils.doPostForJson(fixUrl, mapValues);
        return onHandleHttpRet(ret);
    }

    protected boolean doDownload(String url, String strSavePath) {
        DownloadParams params = new DownloadParams(url,strSavePath);
        params.setResume(false);
        int isSuccees = HttpUtils.doDownload(url, strSavePath,params);
        return isSuccees == 0;
    }

    protected int doDownloadForResume(String url, String strSavePath, boolean isRestart) {
        DownloadParams params = new DownloadParams(url,strSavePath);
        params.setResume(true);
        params.setRestart(isRestart);
        int isSuccees = HttpUtils.doDownloadForResume(url, strSavePath,params);
        return isSuccees;
    }

    protected int doDownload(DownloadParams params){
        int isSuccees;
        if(params.isResume()){
            isSuccees = HttpUtils.doDownloadForResume(params.getUrl(), params.getSavePath(),params);
        }else {
            isSuccees = HttpUtils.doDownload(params.getUrl(), params.getSavePath(),params);
        }
        return isSuccees;
    }

    // 处理http响应
    protected JSONObject onHandleHttpRet(String ret) throws Exception {
        // 判断IO流是否为空，如果为空，说明没有获得服务器数据，打印网络中断，不抛异常。如果不为空，进入解析.
        if (TextUtils.isEmpty(ret)) {
            throw new StringIdException(com.hugbio.androidevent.R.string.toast_disconnect);
        } else {
            if (ret.startsWith("HttpStatusCode")) {
                throw setMsgException(ret, 1122);
            }
            int indexOf = ret.indexOf("{");
            if (indexOf > 0) {
                ret = ret.substring(indexOf);
            }
            JSONObject jo = new JSONObject(ret.trim());
            String msg = "";// 消息
            int show_type = 0;// 显示类型1 alert 2 toaster 0不显示

            // 判断json串中的ok字段是否为true，是的话跳过，正常解析有用的部分，如果为false，取出服务器返回的error，扔到异常类处理
            if (!checkRequestSuccess(jo)) {
                /**
                 * 如果ok为false，丢给异常类，终止方法的执行,
                 * AndroidEventManager中的processEvent方法会catch到这个异常，然后处理
                 */
                if (jo.has("result")) {
                    msg = jo.getString("result");
                } else {
                    msg = jo.toString();
                }
                throw setMsgException(msg + ":" + jo.toString(), show_type);

            }
            return jo;
        }
    }

    // 设置返回的额外信息，根据需要，自己重写
    protected ErrorMsgException setMsgException(String msg, int show_type) {

        return new ErrorMsgException(msg, show_type);
    }

    // 给url增加的公共参数，根据需要，自己重写
    protected String addUrlCommonParams(String url) {
        return url;
    }
}
