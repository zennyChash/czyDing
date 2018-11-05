package com.ifugle.etl.entity.base;
import com.ifugle.etl.entity.base.ETLObject;
public class Task extends ETLObject{
	private int taskType;
	public static final int TASKTYPE_EXTRACT=0;
	public static final int TASKTYPE_IMPORT=1;
	public static final int TASKTYPE_EXECUTE=2;
	public static final int TASKTYPE_FTPDOWNLOAD=2;
	public int getTaskType() {
		return taskType;
	}
	public void setTaskType(int taskType) {
		this.taskType = taskType;
	}
}
