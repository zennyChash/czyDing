package com.ifugle.etl.task.imp.service;

import java.util.List;
import java.util.Map;

import com.ifugle.etl.entity.task.Import;
import com.ifugle.etl.utils.TaskException;

public interface IImport {
	public int importData(Import task,Map params,Map paramVals);
}
