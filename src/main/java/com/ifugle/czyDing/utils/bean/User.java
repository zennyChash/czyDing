package com.ifugle.czyDing.utils.bean;

import java.util.List;

public class User {
	private String userid;
	private String dingname;
	private String dinginfo;
	private int qybj;
	private String czfpbm;
	private String czfp;
	private List menus;
	private String postIds;
	private String postNames ;
	private int pswd_on;    //默认0，不打开密码校验功能
	private int pswd_mode;  //默认0，键盘密码模式
	public int getPswd_on() {
		return pswd_on;
	}
	public void setPswd_on(int pswd_on) {
		this.pswd_on = pswd_on;
	}
	public int getPswd_mode() {
		return pswd_mode;
	}
	public void setPswd_mode(int pswd_mode) {
		this.pswd_mode = pswd_mode;
	}
	public String getCzfp() {
		return czfp;
	}
	public void setCzfp(String czfp) {
		this.czfp = czfp;
	}
	public String getPostIds() {
		return postIds;
	}
	public void setPostIds(String postIds) {
		this.postIds = postIds;
	}
	public String getPostNames() {
		return postNames;
	}
	public void setPostNames(String postNames) {
		this.postNames = postNames;
	}
	public List getMenus() {
		return menus;
	}
	public void setMenus(List menus) {
		this.menus = menus;
	}
	public String getCzfpbm() {
		return czfpbm;
	}
	public void setCzfpbm(String czfpbm) {
		this.czfpbm = czfpbm;
	}
	public String getDinginfo() {
		return dinginfo;
	}
	public void setDinginfo(String dinginfo) {
		this.dinginfo = dinginfo;
	}
	public int getQybj() {
		return qybj;
	}
	public void setQybj(int qybj) {
		this.qybj = qybj;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getDingname() {
		return dingname;
	}
	public void setDingname(String dingname) {
		this.dingname = dingname;
	}
}
