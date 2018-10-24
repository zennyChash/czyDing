package com.ifugle.czy.utils.bean;

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
