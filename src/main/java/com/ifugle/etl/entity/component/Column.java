package com.ifugle.etl.entity.component;

public class Column {
	//来源。导出时，数据库列名做来源。导入时，文件的列名/节点/列序号作为来源
	private String source;
	//目标列。导出时，dest作为文件的列名。
	private String dest;
	private int type;
	//用于xml导入。如果当前节点被指定为key列，那么这个节点的数据只读取一次，但每列都写入。
	private int isKey;
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getSource() {
		return source;
	}
	public int getIsKey() {
		return isKey;
	}
	public void setIsKey(int isKey) {
		this.isKey = isKey;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getDest() {
		return dest;
	}
	public void setDest(String dest) {
		this.dest = dest;
	}
}
