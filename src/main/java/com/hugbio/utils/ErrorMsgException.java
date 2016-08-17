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
	private int show_type;

	public ErrorMsgException(String ErrorMsg, int show_type) {
		this.ErrorMsg = ErrorMsg;
		this.show_type = show_type;
	}

	public String getErrorMsg() {
		return ErrorMsg;

	}

	public int getShowType() {
		return show_type;

	}
}
