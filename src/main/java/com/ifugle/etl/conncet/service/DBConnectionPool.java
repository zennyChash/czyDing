package com.ifugle.etl.conncet.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.dbcp.BasicDataSource;
import com.ifugle.etl.entity.ConnectInfo;

public class DBConnectionPool implements IConnectionPool {
	private static Logger log = LoggerFactory.getLogger(DBConnectionPool.class);
	private DataSource dataSource = null;
	private ConnectInfo cinfo;
	public DBConnectionPool() {
	}
	
	public void close(List resourcesToRelieve) {
		if(resourcesToRelieve==null){
			return;
		}
		Connection conn =null;
		ResultSet rs = null;
		Statement stmt = null;
		try {
			if(resourcesToRelieve.size()>0&&resourcesToRelieve.get(0) != null) {
				conn =(Connection)resourcesToRelieve.get(0);
				conn.rollback();
			}

			if (resourcesToRelieve.size()>1&&resourcesToRelieve.get(1) != null) {
				rs = (ResultSet)resourcesToRelieve.get(1);
				rs.close();
				rs = null;
			}
			if (resourcesToRelieve.size()>2&&resourcesToRelieve.get(2)!= null) {
				stmt = (Statement)resourcesToRelieve.get(2);
				stmt.close();
				stmt = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (Exception e) {
			rs = null;
			stmt = null;
			conn = null;
		}
	}

	public Object getConnection() {
		Connection conn = null;
		try {
			if (this.dataSource == null) {
				init(cinfo);
			}
			if(this.dataSource != null){
				conn = dataSource.getConnection();
			}
		}catch(Exception ex) {
			try {
				Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
				conn = DriverManager.getConnection(cinfo.getConurl(), cinfo.getUsername(), cinfo.getPassword());
				return conn;
			} catch (Exception e) {
				log.error("连接数据库发生错误！数据库URL：{}，用户名：{}，密码：{}。错误信息：", cinfo.getConurl()
						,cinfo.getUsername(), cinfo.getPassword() , e.toString());
			}
		}
		return conn;
	}

	public synchronized void init(ConnectInfo cinfo) {
		if(cinfo==null){
			return;
		}
		this.cinfo=cinfo;
		if (this.dataSource == null) {
			BasicDataSource ds = new BasicDataSource();
			ds.setDriverClassName(cinfo.getDriver_class());
			ds.setUsername(cinfo.getUsername());
			ds.setPassword(cinfo.getPassword());
			ds.setUrl(cinfo.getConurl());
			ds.setInitialSize(cinfo.getInitialSize());
			// 最小空闲连接
			ds.setMinIdle(cinfo.getMinIdle());
			// 最大空闲连接
			ds.setMaxIdle(cinfo.getMaxIdle());
			// 超时回收时间(以毫秒为单位)
			ds.setMaxWait(cinfo.getMaxWait());
			//最大连接数
			ds.setMaxActive(cinfo.getMaxActive());
			ds.setRemoveAbandoned(cinfo.isRemoveAbandoned());
			ds.setRemoveAbandonedTimeout(cinfo.getRemoveAbandonedTimeout());
			this.dataSource = ds;
			log.info("建立了数据库连接池。连接URL：{}，用户名：{}，密码：{}。",cinfo.getConurl(),cinfo.getUsername(), cinfo.getPassword());
		}
	}

	public synchronized void restart() {
		shutdown();
		init(cinfo);
	}

	public synchronized void shutdown() {
		try {
			BasicDataSource bds = (BasicDataSource)this.dataSource;
			bds.close();
		} catch (SQLException e) {
			log.error("关闭数据源发生错误:{}",e.toString());
		}
	}

}
