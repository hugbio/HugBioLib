package com.hugbio.utils;

/**
 * 服务器错误数据显示处理
 * 
 * @author sda
 * 
 */
@SuppressWarnings("serial")
public class ErrorMsgException extends Exception {
	private String ErrorMsg;
	private String resultcode;

	public ErrorMsgException(String ErrorMsg, String resultcode) {
		this.ErrorMsg = ErrorMsg;
		this.resultcode = resultcode;
	}

	public String getErrorMsg() {
		return ErrorMsg;

	}

	public String getResultCode() {
		return resultcode;

	}
}
