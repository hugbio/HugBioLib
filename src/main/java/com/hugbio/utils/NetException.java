package com.hugbio.utils;

public class NetException extends Exception {

	private static final long serialVersionUID = 1L;

	private int	mStringId = 0;

	private String response = null;
	
	public NetException(int stringId){
		mStringId = stringId;
	}
	public NetException(String response){
		this.response = response;
	}
	public NetException(int stringId,String response){
		mStringId = stringId;
		this.response = response;
	}

	public int getStringId(){
		return mStringId;
	}

	public String getResponse(){
		return response;
	}

	@Override
	public String getMessage() {
		String m = "  ,String Id : "+mStringId + ",response : "+response;
		return super.getMessage()+ m;
	}
}
