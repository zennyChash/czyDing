package com.ifugle.etl.task.execute.service;

import java.util.Map;
import com.ifugle.etl.conncet.service.IConnectionPool;
import com.ifugle.etl.entity.task.Execute;

public interface IExecute {
	public IConnectionPool initConnectPool(String conId);
	public int doExecute (Execute task,Map params,Map paramVals);
}
