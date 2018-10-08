package com.istt.service.dto;

public class TlvDTO {

	private String tag;
	private String value;

	@Override
	public String toString() {
		return "TlvDTO [tag=" + tag + ", value=" + value + "]";
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	

}
