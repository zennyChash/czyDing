package com.ifugle.czyDing.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.ifugle.czyDing.utils.bean.LogInfo;
import com.ifugle.utils.Configuration;
@Transactional
public class LogService {
	private static Logger log = Logger.getLogger(LogService.class);
	protected Configuration cg;
	@Autowired
	public void setCg(Configuration cg){
		this.cg = cg;
	}
	protected JdbcTemplate jdbcTemplate;
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	public void writeLogFromWeb(String userid, LogInfo log) {
		StringBuffer sql = new StringBuffer("insert into logs(lid,logtype,logid,userid,ltime,log)values(SQ_LOGS_ID.nextval,?,?,?,systimestamp,?)");
		Object[] p = new Object[]{log.getLogType(),log.getLogId(),userid,log.getLog()};
		jdbcTemplate.update(sql.toString(), p);
	}
}
