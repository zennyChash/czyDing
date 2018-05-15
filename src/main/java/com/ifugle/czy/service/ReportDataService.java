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
		JSONObject jrpt = null;
		Map rpt = jdbcTemplate.queryForMap("select rptinfo from rptdata where rptid=?",new Object[]{rptID});
		if(rpt!=null){
			String rptinfo=(String)rpt.get("rptinfo"); 
			jrpt = JSONObject.parseObject(rptinfo);
		}
		return jrpt;
	}
	
	public Map getParamOptions(String rptID, RptDataJson params) {
		Map mapOps = new HashMap();
		List allOptions = new ArrayList();
		JSONArray jparams = params==null?null:params.parseJOptionParams();
		if(jparams!=null){
			for(int i=0;i<jparams.size();i++){
				String spara = jparams.getString(i);
				Map opinfo = jdbcTemplate.queryForMap("select opsinfo from paramoptions where rptid=? and paramid=?",new Object[]{rptID,spara});
				if(opinfo!=null){
					String ostr=(String)opinfo.get("opsinfo"); 
					JSONObject op = JSONObject.parseObject(ostr);
					allOptions.add(op);
				}
			}
		}
		mapOps.put("paramOptions",allOptions);
		return mapOps;
	}
	/*private Map getFndxqData(RptDataJson params) {
		Map datas = new HashMap();
		Map data = new HashMap();
		JSONObject jparams = params.parseJRptParams();
		List czsrs = new ArrayList();
		Map czsr = new HashMap();
		czsr.put("date","2008");
		czsr.put("value","123");
		czsr.put("zz","-9");
		czsrs.add(czsr);
		czsr = new HashMap();
		czsr.put("date","2009");
		czsr.put("value","12");
		czsr.put("zz","21");
		czsrs.add(czsr);
		czsr = new HashMap();
		czsr.put("date","2010");
		czsr.put("value","44");
		czsr.put("zz","21");
		czsrs.add(czsr);
		czsr = new HashMap();
		czsr.put("date","2011");
		czsr.put("value","435");
		czsr.put("zz","21");
		czsrs.add(czsr);
		czsr = new HashMap();
		czsr.put("date","2012");
		czsr.put("value","2");
		czsr.put("zz","6");
		czsrs.add(czsr);
		czsr = new HashMap();
		czsr.put("date","2013");
		czsr.put("value","323");
		czsr.put("zz","21");
		czsrs.add(czsr);
		czsr = new HashMap();
		czsr.put("date","2014");
		czsr.put("value","432");
		czsr.put("zz","21");
		czsrs.add(czsr);
		czsr = new HashMap();
		czsr.put("date","2015");
		czsr.put("value","23");
		czsr.put("zz","21");
		czsrs.add(czsr);
		data.put("czzsr", czsrs);
		
		List dfs = new ArrayList();
		Map df = new HashMap();
		df.put("date","2011");
		df.put("value","37");
		df.put("zz","21");
		dfs.add(df);
		df = new HashMap();
		df.put("date","2012");
		df.put("value","43");
		df.put("zz","21");
		dfs.add(df);
		df = new HashMap();
		df.put("date","2013");
		df.put("value","45");
		df.put("zz","21");
		dfs.add(df);
		df = new HashMap();
		df.put("date","2014");
		df.put("value","12");
		df.put("zz","21");
		dfs.add(df);
		df = new HashMap();
		df.put("date","2015");
		df.put("value","44");
		df.put("zz","-9");
		dfs.add(df);
		df = new HashMap();
		df.put("date","2016");
		df.put("value","22");
		df.put("zz","-7");
		dfs.add(df);
		df = new HashMap();
		df.put("date","2017");
		df.put("value","121");
		df.put("zz","21");
		dfs.add(df);
		df = new HashMap();
		df.put("date","2018");
		df.put("value","23");
		df.put("zz","21");
		dfs.add(df);
		data.put("dfczzsr", dfs);
		datas.put("data", data);
		return datas;
	}
	private Map getDzjkData(RptDataJson params){
		Map datas = new HashMap();
		JSONObject jparams = params.parseJRptParams();
		String cyear = jparams.getString("thisYear");
		String lyear = jparams.getString("lastYear");
		cyear = cyear!=null&&cyear.length()>3?cyear.substring(0,4):"";
		lyear = lyear!=null&&lyear.length()>3?lyear.substring(0,4):"";
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
	private Map getFszxqData(RptDataJson params){
		Map datas = new HashMap();
		JSONObject jparams = params.parseJRptParams();
		List data = new ArrayList();
		Map szData = new HashMap();
		szData.put("value","335");
		szData.put("name","增值税");
		szData.put("bn","1212");
		szData.put("zz","22");
		szData.put("id","zzs");
		szData.put("bl","90");
		data.add(szData);
		szData = new HashMap();
		szData.put("value","310");
		szData.put("name","企业所得税");
		szData.put("bn","21");
		szData.put("zz","33");
		szData.put("id","qysds");
		szData.put("bl","76");
		data.add(szData);
		szData = new HashMap();
		szData.put("value","234");
		szData.put("name","其他税");
		szData.put("bn","23");
		szData.put("zz","-32");
		szData.put("id","qts");
		szData.put("bl","23");
		data.add(szData);
		szData = new HashMap();
		szData.put("value","135");
		szData.put("name","个人所得税");
		szData.put("bn","23");
		szData.put("zz","-3");
		szData.put("id","grsds");
		szData.put("bl","21");
		data.add(szData);
		datas.put("data", data);
		return datas;
	}*/
}
