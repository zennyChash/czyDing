package com.ifugle.etl.task.extract.service;

import java.util.List;
import java.util.Map;

import com.ifugle.etl.conncet.service.IConnectionPool;
import com.ifugle.etl.entity.task.*;

public interface IExtract {
	public IConnectionPool initConnectPool(String conId);
	public int extractToFile (Extract task,Map params,Map paramVals);
}
