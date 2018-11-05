package com.ifugle.etl.task.request.service;

import java.util.Map;
import com.ifugle.etl.entity.task.RequestTask;

public interface IRequestBefore {
	public String[] process(RequestTask req,Map params, Map paramVals);
}
