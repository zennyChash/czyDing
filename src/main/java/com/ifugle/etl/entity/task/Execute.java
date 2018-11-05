package com.ifugle.etl.entity.task;

import com.ifugle.etl.entity.base.Task;
import com.ifugle.etl.entity.component.ProcedureBean;

public class Execute  extends Task{
	private String connectionId;
	private int executeType;
	private ProcedureBean procedure;
	private String sql;
	
	public String getConnectionId() {
		return connectionId;
	}
	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}
	public int getExecuteType() {
		return executeType;
	}
	public void setExecuteType(int executeType) {
		this.executeType = executeType;
	}
	public ProcedureBean getProcedure() {
		return procedure;
	}
	public void setProcedure(ProcedureBean procedure) {
		this.procedure = procedure;
	}
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
}
