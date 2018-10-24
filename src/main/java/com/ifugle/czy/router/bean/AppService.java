package com.ifugle.czy.router.bean;

import java.util.List;
import java.util.Map;

public class AppService {
	private String id ;
	private String name;
	private String desc;
	private String rootURI;
	private List methods;
	private Map methodsMap;
	public Map getMethodsMap() {
		return methodsMap;
	}
	public void setMethodsMap(Map methodsMap) {
		this.methodsMap = methodsMap;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getRootURI() {
		return rootURI;
	}
	public void setRootURI(String rootURI) {
		this.rootURI = rootURI;
	}
	public List getMethods() {
		return methods;
	}
	public void setMethods(List methods) {
		this.methods = methods;
	}
	public AppMethod getMethod(String reqMethod) {
		if(methodsMap!=null&&methodsMap.containsKey(reqMethod)){
			return (AppMethod)methodsMap.get(reqMethod);
		}
		return null;
	}
	
}
