package com.ifugle.etl.entity.task;

import java.util.List;

import com.ifugle.etl.entity.base.Task;
import com.ifugle.etl.entity.component.InPutFile;
import com.ifugle.etl.entity.component.SourceDb;

public class Import extends Task{
	private String connectionId;
	private String destTable ;
	private int sourceType;
	private List importColumns;
	private InPutFile sourceFile;
	private SourceDb sourceDb;
	
	public String getConnectionId() {
		return connectionId;
	}
	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}
	public String getDestTable() {
		return destTable;
	}
	public void setDestTable(String destTable) {
		this.destTable = destTable;
	}
	public int getSourceType() {
		return sourceType;
	}
	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}
	public List getImportColumns() {
		return importColumns;
	}
	public void setImportColumns(List importColumns) {
		this.importColumns = importColumns;
	}
	public InPutFile getSourceFile() {
		return sourceFile;
	}
	public void setSourceFile(InPutFile sourceFile) {
		this.sourceFile = sourceFile;
	}
	public SourceDb getSourceDb() {
		return sourceDb;
	}
	public void setSourceDb(SourceDb sourceDb) {
		this.sourceDb = sourceDb;
	}
}
