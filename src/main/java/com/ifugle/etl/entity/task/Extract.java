package com.ifugle.etl.entity.task;

import java.util.List;

import com.ifugle.etl.entity.base.Task;
import com.ifugle.etl.entity.component.OutPutFile;
import com.ifugle.etl.entity.component.ProcedureBean;
public class Extract extends Task{
	private String sql;
	private boolean inBatches;
	private int limit;
	private String connectionId;
	private ProcedureBean procedure;
	private OutPutFile destFile;
	private List saveColumns;
	private int extractBy;
	
	public int getExtractBy() {
		return extractBy;
	}
	public void setExtractBy(int extractBy) {
		this.extractBy = extractBy;
	}
	
	public OutPutFile getDestFile() {
		return destFile;
	}
	public void setDestFile(OutPutFile destFile) {
		this.destFile = destFile;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public String getConnectionId() {
		return connectionId;
	}
	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public boolean isInBatches() {
		return inBatches;
	}
	public void setInBatches(boolean inBatches) {
		this.inBatches = inBatches;
	}
	public ProcedureBean getProcedure() {
		return procedure;
	}
	public void setProcedure(ProcedureBean procedure) {
		this.procedure = procedure;
	}
	public List getSaveColumns() {
		return saveColumns;
	}
	public void setSaveColumns(List saveColumns) {
		this.saveColumns = saveColumns;
	}
}
