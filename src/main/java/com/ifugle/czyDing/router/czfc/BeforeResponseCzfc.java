package com.ifugle.czyDing.router.czfc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ifugle.czyDing.router.IBeforeResponse;
import com.ifugle.czyDing.router.bean.ProxyResponse;
import com.ifugle.czyDing.utils.JResponse;
import com.ifugle.czyDing.utils.bean.SimpleValue;
@Transactional
public class BeforeResponseCzfc implements IBeforeResponse{
	private static Logger log = Logger.getLogger(BeforeResponseCzfc.class);
	protected JdbcTemplate jdbcTemplate;
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	public JResponse process(String serviceName,String reqMethod,ProxyResponse rpTmp,JSONObject responses){
		JResponse jr = new JResponse();
		JSONObject data = new JSONObject();
		if(responses==null||responses.keySet().size()==0){
			jr = new JResponse("9","未获取到远程服务的响应！","");
			return jr;
		}
		if("getAppListCheckInfo".equals(reqMethod)){
			JSONObject res = responses.getJSONObject("_RETURNED");
			if(res!=null){
				Map um = getUserMapping(serviceName);
				transformUserOfRows(res,um);
			}
		}else if("getMsgAfterCheck".equals(reqMethod)){
			JSONObject res = responses.getJSONObject("_RETURNED");
			if(res!=null){
				Map um = getUserMapping(serviceName);
				log.info("getMsgAfterCheck方法中，转换前的接收者："+res.getString("users"));
				transformUserString(res,um);
				log.info("getMsgAfterCheck方法中转换后的接收者："+res.getString("users"));
			}
		}else if("checkDspsByUser".equals(reqMethod)){
			JSONObject res = responses.getJSONObject("_RETURNED");
			log.info("钉消息远程返回："+res.toJSONString());
			if(res!=null){
				Map um = getUserMapping(serviceName);
				addDingUserOfRows(res,um);
			}
		}
		for(String key :responses.keySet()){
			JSONObject strRes = (JSONObject)responses.get(key);
			//如果多个记录集，只要有一个中有错误代码，就组织成财智云的错误信息格式立即返回。
			if(strRes.containsKey("errcode")){
				jr = new JResponse("9",strRes.getString("errmsg"),"");
				return jr;
			}
			//之前为了将多个返回统一处理，对于没设置returnProperty的设置了默认的_RETURNED属性，此处解除该外包属性。
			if("_RETURNED".equals(key)){
				data.putAll(strRes);
			}else{
				data.put(key, strRes);
			}
		}
		jr.setRetCode("0");
		jr.setRetData(data);
		return jr;
	}
	private Map getUserMapping(String serviceName){
		Map um = new HashMap();
		try{
			String sql = "select userid,dingid,dingname from usermapping where servicename=? ";
			List users = jdbcTemplate.queryForList(sql,new Object[]{serviceName});
			if(users!=null){
				for(int i=0;i<users.size();i++){
					Map u = (Map)users.get(i);
					String userid = (String)u.get("userid");
					String dingid = (String)u.get("dingid");
					String dingname = (String)u.get("dingname");
					SimpleValue fn = new SimpleValue(dingid, dingname);
					um.put(userid, fn);
				}
			}
		}catch(Exception e){}
		return um;
	}
	private void transformUserOfRows(JSONObject jr,Map um){
		JSONArray rows = (JSONArray)jr.getJSONArray("rows");
		if(rows!=null&&rows.size()>0){
			for(int j=0;j<rows.size();j++){
				JSONObject row = (JSONObject)rows.get(j);
				String userid= row.getString("userid");
				if(um!=null&&um.containsKey(userid)){
					SimpleValue fn = (SimpleValue)um.get(userid);
					row.put("userid", fn.getBm());
					row.put("uname", fn.getMc());
				}
			}
		}
	}
	private void transformUserString(JSONObject jr,Map um){
		String strUsers = jr.getString("users");
		if(!StringUtils.isEmpty(strUsers)){
			String[] users = strUsers.split(",");
			ArrayList newUsers = new ArrayList();
			for(int i=0;i<users.length;i++){
				String u = users[i];
				if(um!=null&&um.containsKey(u)){
					SimpleValue fn = (SimpleValue)um.get(u);
					newUsers.add(fn.getBm());
				}
			}
			String strNeweUsers = StringUtils.join(newUsers, ",");
			jr.put("users", strNeweUsers);
		}
	}
	private void addDingUserOfRows(JSONObject jr,Map um){
		JSONArray rows = (JSONArray)jr.getJSONArray("rows");
		if(rows!=null&&rows.size()>0){
			for(int j=0;j<rows.size();j++){
				JSONObject row = (JSONObject)rows.get(j);
				String userid= row.getString("userid");
				if(um!=null&&um.containsKey(userid)){
					SimpleValue fn = (SimpleValue)um.get(userid);
					row.put("czyuserid", fn.getBm());
				}else{
					row.put("czyuserid", "");
				}
			}
		}
	}
}
