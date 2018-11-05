package com.ifugle.etl.task.request.service;

import com.alibaba.fastjson.JSONObject;
import com.ifugle.etl.entity.task.RequestTask;

public interface IResponseBefore {
	public JSONObject process(RequestTask req, JSONObject response);
}
