package com.ifugle.czyDing.utils.bean.template;

import java.util.List;

public class JOutput {
	private String id ;
	private String name;
	private String desc;
	private int type=0; //输出的类型。分page（0），option（1）两种。默认page，0
	private int optionsType=0;//如果是option内容，输出的类型，是输出一个范围（1），还是逐个选项。默认逐个选项（0）
	private String jTemplate;
	private List valuedDs;
	private String infile;
	public String getInfile() {
		return infile;
	}

	public void setInfile(String infile) {
		this.infile = infile;
	}
	public List getValuedDs() {
		return valuedDs;
	}
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getOptionsType() {
		return optionsType;
	}

	public void setOptionsType(int optionsType) {
		this.optionsType = optionsType;
	}

	public void setValuedDs(List valuedDs) {
		this.valuedDs = valuedDs;
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

	public String getjTemplate() {
		return jTemplate;
	}

	public void setjTemplate(String jTemplate) {
		this.jTemplate = jTemplate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
