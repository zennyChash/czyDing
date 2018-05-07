package com.ifugle.czy.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

@Transactional
public class ReportDataService {
	private static Logger log = Logger.getLogger(ReportDataService.class);
	protected JdbcTemplate jdbcTemplate;
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	public Map getData(String dtID,String params){
		JSONObject jparams = JSONObject.parseObject(params);
		String cyear = jparams.getString("thisYear");
		String lyear = jparams.getString("lastYear");
		Map datas = new HashMap();
		datas.put("total", "290912.78");
		datas.put("rate", "75");
		Map line = new HashMap();
		line.put("month", new int[]{1,2,3,4,5,6,7,8,9,10,11,12});
		List dtObjs = new ArrayList();
		Map dObj= new HashMap();
		dObj.put("name", cyear+"年");
		dObj.put("data", new Double[]{1900.89,2018.9,3309.80,4099.93,3809.1,5090.89,4578.9,6789.9,4456.89,2988.9,5674.55,3456.78});
		dtObjs.add(dObj);
		dObj= new HashMap();
		dObj.put("name", lyear+"年");
		dObj.put("data", new Double[]{1878.9,3009.87,3456.65,5543.5,2347.8,4567.34,3356.78,5643.22,3122.11,2987.5,4322.12,1233.45});
		dtObjs.add(dObj);
		line.put("object",dtObjs);
		datas.put("line", line);
		return datas;
	}
}
