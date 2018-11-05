package com.ifugle.etl.entity.component;
import com.ifugle.etl.entity.base.ETLFile;
public class OutPutFile extends ETLFile {
	private boolean deleteDuplicate;
	private int rowsPerFile = 10000;
	public boolean isDeleteDuplicate() {
		return deleteDuplicate;
	}
	public void setDeleteDuplicate(boolean deleteDuplicate) {
		this.deleteDuplicate = deleteDuplicate;
	}
	
	public int getRowsPerFile() {
		return rowsPerFile;
	}
	public void setRowsPerFile(int rowsPerFile) {
		this.rowsPerFile = rowsPerFile;
	}
	
}
