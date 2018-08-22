package com.istt.service.dto;

public class SmppMessage {
	private String timeStamp;
	private String msg;
	private String info;
	
	@Override
	public String toString() {
		return "timeStamp:"+timeStamp+"\tmsg:"+msg+"\tinfo:"+info;
	}
	
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	
	
}
