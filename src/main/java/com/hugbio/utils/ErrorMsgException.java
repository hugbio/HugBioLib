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
	private String retCode;

	public ErrorMsgException(String ErrorMsg, String retCode) {
		this.ErrorMsg = ErrorMsg;
		this.retCode = retCode;
	}

	public String getErrorMsg() {
		return ErrorMsg;

	}

	public String getResultCode() {
		return retCode;
	}

	@Override
	public String getMessage() {
		return super.getMessage()+"("+"ErrorMsg : "+ErrorMsg+")";
	}
}
