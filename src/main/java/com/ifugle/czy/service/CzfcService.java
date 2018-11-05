package com.ifugle.czy.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.open.client.api.model.corp.MessageBody;
import com.dingtalk.open.client.api.model.corp.MessageType;
import com.ifugle.czy.ding.message.LightAppMessageDelivery;
import com.ifugle.czy.ding.message.MessageHelper;
import com.ifugle.czy.utils.DingHelper;
import com.ifugle.czy.utils.JResponse;
import com.ifugle.utils.Configuration;

@Transactional
public class CzfcService {
	private static Logger log = Logger.getLogger(CzfcService.class);
	protected JdbcTemplate jdbcTemplate;
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	@Autowired
	private Configuration cg ;
	public Map getApprovalLists2Check(int state,String userid, String qParams,String sort, String dir, int start, int limit) {
		Map infos=new HashMap();
		if(StringUtils.isEmpty(userid)){
			infos.put("errcode","9");
			infos.put("errmsg","缺少用户信息，请登录！");
			return infos;
		}
		List rows = new ArrayList();
		infos.put("totalCount", 0);
		
		StringBuffer sql = new StringBuffer("select lid,name,did,dep,totalmoney,encount,iid,itemname from sp_applists ");
		sql.append(" where userid=? and state=? ");
		if(StringUtils.isEmpty(sort)){
			sql.append("order by ").append("lid desc");
		}else{
			sql.append("order by ").append(sort).append(" ").append(dir);
		}
		StringBuffer csql = new StringBuffer("select count(*) from (");
		csql.append(sql).append(")");
		int cc = jdbcTemplate.queryForObject(csql.toString(), new Object[]{userid,state}, Integer.class);
		infos.put("totalCount", cc);
		StringBuffer rSql = new StringBuffer("select * from (select a.*, rownum r from (");
		rSql.append(sql);
		rSql.append(") a where rownum<=");
		rSql.append((start+limit));
		rSql.append(") b where r>");
		rSql.append(start);
		rows = jdbcTemplate.queryForList(rSql.toString(), new Object[]{userid,state});
		if(rows!=null&&rows.size()>0){
			infos.put("rows", key2LowerCaseKey(rows));
		}else{
			infos.put("rows", new ArrayList());
		}
		return infos;
	}
	public Map getApprovalListById(String userid, long lid) {
		Map infos=new HashMap();
		if(StringUtils.isEmpty(userid)){
			infos.put("errcode","9");
			infos.put("errmsg","缺少用户信息，请登录！");
			return infos;
		}
		StringBuffer sql = new StringBuffer("select lid,name,did,dep,totalmoney,encount,iid,itemname,remark from sp_applists ");
		sql.append(" where lid=? ");
		List rows = jdbcTemplate.queryForList(sql.toString(), new Object[]{lid});
		if(rows!=null&&rows.size()>0){
			infos= (Map)key2LowerCaseKey(rows).get(0);
		}
		return infos;
	}
	public Map checkAppByList(String userid, String doType, long lid,String remark) {
		Map infos=new HashMap();
		try{
			StringBuffer sql = new StringBuffer("update sp_applists set state=?,remark=? where lid=?");
			short state = 1;
			if("0".equals(doType)){
				state = 1;
			}else{
				state=9;
			}
			jdbcTemplate.update(sql.toString(),new Object[]{state,remark,lid});
			infos.put("result", true);
			infos.put("info", "");
		}catch(Exception e){
			infos.put("errcode","9");
			infos.put("errmsg",e.toString());
		}
		return infos;
	}
	public Map getAppDetailsInList(String userid, String qParams, String sort,String dir, int start, int limit) {
		Map infos=new HashMap();
		if(StringUtils.isEmpty(userid)){
			infos.put("errcode","9");
			infos.put("errmsg","缺少用户信息，请登录！");
			return infos;
		}
		JSONObject jp = JSON.parseObject(qParams);
		if(jp==null||!jp.containsKey("lid")){
			infos.put("errcode","9");
			infos.put("errmsg","未指定要查看的审批单ID！");
			return infos;
		}
		Long lid = jp.getLong("lid");
		List rows = new ArrayList();
		infos.put("totalCount", 0);
		
		StringBuffer sql = new StringBuffer("select swdjzh,qymc,money,item_content from sp_applist_content where lid=? ");
		if(StringUtils.isEmpty(sort)){
			sql.append("order by ").append("money desc");
		}else{
			sql.append("order by ").append(sort).append(" ").append(dir);
		}
		StringBuffer csql = new StringBuffer("select count(*) from (");
		csql.append(sql).append(")");
		int cc = jdbcTemplate.queryForObject(csql.toString(), new Object[]{lid}, Integer.class);
		infos.put("totalCount", cc);
		
		StringBuffer rSql = new StringBuffer("select * from (select a.*, rownum r from (");
		rSql.append(sql);
		rSql.append(") a where rownum<=");
		rSql.append((start+limit));
		rSql.append(") b where r>");
		rSql.append(start);
		rows = jdbcTemplate.queryForList(rSql.toString(), new Object[]{lid});
		if(rows!=null&&rows.size()>0){
			infos.put("rows", key2LowerCaseKey(rows));
		}else{
			infos.put("rows", new ArrayList());
		}
		return infos;
	}
	public Map getCommentsOfAppList(String userid, long lid) {
		Map infos=new HashMap();
		if(StringUtils.isEmpty(userid)){
			infos.put("errcode","9");
			infos.put("errmsg","缺少用户信息，请登录！");
			return infos;
		}
		List rows = new ArrayList();
		StringBuffer sql = new StringBuffer("select stepid,stepname,dkid,dkname,utype,utypename,");
		sql.append("userid,username uname,approvaldate,approval from sp_checkinfo where lid=? order by stepid");
		rows = jdbcTemplate.queryForList(sql.toString(), new Object[]{lid});
		if(rows!=null&&rows.size()>0){
			infos.put("rows", key2LowerCaseKey(rows));
		}else{
			infos.put("rows", new ArrayList());
		}
		return infos;
	}
	//key转化为小写。
    public List key2LowerCaseKey(List rows){
    	List nl = new ArrayList();
		for(int i=0;i<rows.size();i++){
			Map t = (Map)rows.get(i);
			Map nt = new HashMap();
			Iterator it = t.entrySet().iterator();         
			while(it.hasNext()){      
			     Map.Entry entry=(Map.Entry)it.next();    
			     String key= (String)entry.getKey();
			     nt.put( key.toLowerCase(),entry.getValue());
			} 
			nl.add(nt);
		}
		return nl;
    }
	public Map sendCzfcCheckMsgAuto() {
		Map infos=new HashMap();
		StringBuffer sql = new StringBuffer("select count(*)cc from sp_msg_state where state=0");
		int cc = jdbcTemplate.queryForObject(sql.toString(), Integer.class);
		if(cc==0){
			infos.put("retCode","0");
			JSONObject jr = new JSONObject();
			jr.put("sendCount", 0);
			infos.put("retData",jr);
			return infos;
		}
		sql = new StringBuffer("select s.id,s.lid,s.userid,s.msg,u.dingid,u.dingname from ");
		sql.append("sp_msg_state s,usermapping u where s.userid=u.userid(+) and state=0");
		List lst = jdbcTemplate.queryForList(sql.toString());
		for(int i=0;i<lst.size();i++){
			Map rd = (Map)lst.get(i);
			int id = ((BigDecimal)rd.get("ID")).intValue();
			String dingid = (String)rd.get("DINGID");
			String dingname = (String)rd.get("DINGNAME");
			String msg = (String)rd.get("MSG");
			if(!StringUtils.isEmpty(dingid)&&!StringUtils.isEmpty(msg)){
				//发钉钉消息
				String accessToken = DingHelper.getAccessToken();
				String agentid= cg.getString("AGENT_ID", "163161139");
				String touser = dingid,toparty="";
				//组织文本消息
				MessageBody.TextBody textBody = new MessageBody.TextBody();
	            textBody.setContent(msg);
	            //装配deliver
				LightAppMessageDelivery delivery = new LightAppMessageDelivery(touser,toparty,agentid);
				delivery.withMessage(MessageType.TEXT, textBody);
				try{
					MessageHelper.send(accessToken, delivery);
					log.info("成功发送微应用消息"+"，接收者:"+dingname);
				}catch(Exception e){
					
				}
				jdbcTemplate.update("update sp_msg_state set state=1 where id=?",new Object[]{id});
			}
		}
		return infos;
	}
}
