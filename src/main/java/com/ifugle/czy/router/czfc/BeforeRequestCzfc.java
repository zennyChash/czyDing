package com.ifugle.czy.router.czfc;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ifugle.czy.router.bean.ProxyRequest;
import com.ifugle.czy.router.IBeforeRequest;
@Transactional
public class BeforeRequestCzfc implements IBeforeRequest{
	protected JdbcTemplate jdbcTemplate;
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	public String[] process(String serviceName,String reqMethod,ProxyRequest req,String svParams, String userid){
		String[] result = new String[]{"0",""};
		
		JSONObject jparams = null;
		try{
			jparams = JSON.parseObject(svParams);
		}catch(Exception e){}
		
		/* 取审批单列表，在财智云前端是同一个请求，以参数state区分取数类型而已。
		 * 产业扶持中实际对应两个不同请求。因此路由表中，对应两个业务请求，拦截处理
		 * 分析参数，不同state，要路由到不同的请求，或者取消掉不需要的那个请求（取已审批时，不需要请求未审批，反之亦然）。
		 */
		if("getApprovalLists".equals(reqMethod)){
			if(jparams!=null){
				String subUri = req.getSubURI();
				String state = jparams.getString("state");
				if(("getApprovalListsChecked".equals(subUri)&&"0".equals(state))
						||("getApprovalListsChecked".equals(subUri)&&"0".equals(state))){
					return new String[]{"9",""};
				}
			}
		}
		if(jparams==null){
			jparams = new JSONObject();
		}
		//转换并增加用户参数
		if(!StringUtils.isEmpty(userid)){
			try{
				String sql = "select userid from usermapping where servicename=? and dingid=?";
				String czfcUserid = jdbcTemplate.queryForObject(sql,new Object[]{serviceName,userid}, String.class);
				jparams.put("userid", czfcUserid);
			}catch(Exception e){
				System.out.println(e.toString());
			}
		}
		result[1]=jparams.toJSONString();
		return result;
	}
}
