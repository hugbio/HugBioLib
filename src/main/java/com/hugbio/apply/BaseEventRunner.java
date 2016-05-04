package com.hugbio.apply;

import com.hugbio.core.Event;
import com.hugbio.runner.HttpRunner;
import com.hugbio.utils.ErrorMsgException;

public abstract  class BaseEventRunner extends HttpRunner {

	public abstract void onEventRun(Event event) throws Exception;
	
	/****
	 * 根据需要设置服务器返回的信息
	 */
	@Override
	protected ErrorMsgException setMsgException(String msg, String resultcode) {
		return super.setMsgException(msg, resultcode);
	}
	
	/**
	 * 给url增加的公共参数，根据需要，自己重写
	 */
	@Override
	protected String addUrlCommonParams(String url) {
		return url;
	}
}
