package com.ifugle.czy.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ifugle.czy.utils.bean.RptDataJson;

@Transactional
public class ReportDataService {
	private static Logger log = Logger.getLogger(ReportDataService.class);
	protected JdbcTemplate jdbcTemplate;
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	public Map getData(String rptID,RptDataJson params){
		JSONObject jparams = params.parseJRptParams();
		String cyear = jparams.getString("thisYear");
		String lyear = jparams.getString("lastYear");
		cyear = cyear!=null&&cyear.length()>3?cyear.substring(0,4):"";
		lyear = lyear!=null&&lyear.length()>3?lyear.substring(0,4):"";
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
	public Map getParamOptions(String rptID, RptDataJson params) {
		Map datas = new HashMap();
		JSONArray joptions = params.parseJOptionParams();
		List allOptions = new ArrayList();
		for(int i=0;i<joptions.size();i++){
			Map paramOp = new HashMap();
			String spara = joptions.getString(i);
			paramOp.put("paraName", spara);
			if("pYearMonth".equals(spara)){
				List range = new ArrayList();
				range.add("201701");
				range.add("201804");
				paramOp.put("range", range);
				paramOp.put("type", "date");
				paramOp.put("format", "Ym");
				paramOp.put("defaultOp", "2018");
				allOptions.add(paramOp);
			}else{
				List ops = new ArrayList();
				Map oneop = new HashMap();
				oneop.put("bm", "01");
				oneop.put("name", "西湖区");
				ops.add(oneop);
				oneop = new HashMap();
				oneop.put("bm", "02");
				oneop.put("name", "下城区");
				ops.add(oneop);
				paramOp.put("options", ops);
				paramOp.put("defaultOp", "");
				allOptions.add(paramOp);
			}
			datas.put("paramOptions", allOptions);
		}
		return datas;
	}
}
