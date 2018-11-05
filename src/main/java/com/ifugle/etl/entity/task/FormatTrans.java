package com.ifugle.etl.entity.task;

import java.util.List;

import com.ifugle.etl.entity.base.Task;
import com.ifugle.etl.entity.component.OutPutFile;

public class FormatTrans extends Task{
	private OutPutFile destFile;
	private List saveColumns;
	public OutPutFile getDestFile() {
		return destFile;
	}
	public void setDestFile(OutPutFile destFile) {
		this.destFile = destFile;
	}
	public List getSaveColumns() {
		return saveColumns;
	}
	public void setSaveColumns(List saveColumns) {
		this.saveColumns = saveColumns;
	}
	
}
