package com.ifugle.czyDing.utils.bean;

public class SimpleValue {
	private String bm;          
	private String mc;
	public String getBm() {
		return bm;
	}
	public void setBm(String bm) {
		this.bm = bm;
	}
	public String getMc() {
		return mc;
	}
	public void setMc(String mc) {
		this.mc = mc;
	} 
	public SimpleValue(){
	}
	public SimpleValue(String bm,String mc){
		this.bm = bm;
		this.mc = mc;
	}
}
