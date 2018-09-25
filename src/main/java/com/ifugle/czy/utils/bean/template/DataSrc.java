package com.ifugle.czy.utils.bean.template;

import java.util.List;
import java.util.Map;

public class DataSrc {
	private String id ;
	private String name;
	private String desc;
	private int sourceType; //1/sql:通过sql取数；2/procedure：通过存储过程取数
	private String sql;
	private ProcedureBean procedure;
	private List cols;
	private Map colMap;
	private String infile;
	public String getInfile() {
		return infile;
	}
	public void setInfile(String infile) {
		this.infile = infile;
	}
	public int getSourceType() {
		return sourceType;
	}
	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}
	public String getId() {
		return id;
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
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public ProcedureBean getProcedure() {
		return procedure;
	}
	public void setProcedure(ProcedureBean procedure) {
		this.procedure = procedure;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List getCols() {
		return cols;
	}
	public void setCols(List cols) {
		this.cols = cols;
	}
	public Map getColMap() {
		return colMap;
	}
	public void setColMap(Map colMap) {
		this.colMap = colMap;
	}
	
}
