package com.ifugle.etl.task.imp.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ifugle.etl.conncet.service.ConnectionFactory;
import com.ifugle.etl.conncet.service.IConnectionPool;
import com.ifugle.etl.entity.component.*;
import com.ifugle.etl.entity.task.Extract;
import com.ifugle.etl.entity.task.Import;
import com.ifugle.etl.entity.base.Task;
import com.ifugle.etl.schedule.SchedulerUtils;
import com.ifugle.etl.task.extract.service.DBExtractService;
import com.ifugle.etl.task.format.service.FormatService;
import com.ifugle.etl.task.imp.entity.FileReader;
import com.ifugle.etl.utils.TemplateLoader;
import com.ifugle.etl.utils.TaskException;

public class DBImportService implements IImport{
	private ConnectionFactory connFactory;
	private static Logger log = LoggerFactory.getLogger(DBImportService.class);
	@Autowired
	public void setConnFactory(ConnectionFactory connFactory) {
		this.connFactory = connFactory;
	}
	public synchronized int importData(Import task,Map params,Map paramVals) {
		int flag=0;
		if(task==null){
			log.error("任务配置错误：未配置导入数据的相关信息！");
			return flag;
		}
		log.info("DBImportService开始执行任务：{}",task.getId());
		int type = task.getSourceType();
		if(type==0&&task.getSourceFile()==null){
			log.error("任务配置错误：未配置数据导入源信息！");
		}
		InPutFile src = task.getSourceFile();
		if (src == null) {
			log.error("任务配置错误：未配置数据导入目标！");
		}
		if(src.getDir()==null){
			log.error("任务配置错误：未指定源文件的目录！");
			return flag;
		}
		String fmt = src.getFormat();
		if("excel".equalsIgnoreCase(fmt)){
			FormatService fsv = new FormatService();
			flag = fsv.importExcel(task,params,paramVals);
		}else if("xml".equalsIgnoreCase(fmt)){
			FormatService fsv = new FormatService();
			flag = fsv.importXml(task,params,paramVals);
		}else{ 
			flag = importTxt(task,params,paramVals);
		}
		return flag;
	}
	
	public int importTxt(Import task,Map params,Map paramVals){	
		int flag = 0;
		InPutFile src = task.getSourceFile();
		//连接
		String conId = task.getConnectionId();
		IConnectionPool dbpool = initConnectPool(conId);
		Connection conn = (Connection)dbpool.getConnection();
		PreparedStatement ps = null;
		if (conn == null) {
			log.error("获得数据库连接失败！");
		}
		try {
			FileReader fr = new FileReader();
			fr.setColSeparator(src.getColSeparator());
			fr.setEncode(src.getEncode());
			String dir = src.getDir();
			String pDir = SchedulerUtils.parseParamValue(dir,paramVals);
			String wholeDir = pDir.endsWith("/")? pDir : pDir + "/";
			fr.setDir(wholeDir);
			int fileNum = fr.getDataFileCount();
			if (fileNum < 1) {
				log.error("指定目录下未找到数据文件:{}",fr.getDir());
			}
			/*****************导入********************/
			String sql = buildSql(task);
			if(StringUtils.isEmpty(sql)){
				log.error("未成功组织导入sql，任务{}执行中断！",task.getId());
				return 9;
			}
			List cols = task.getImportColumns();
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(sql);
			boolean hasBatch = false;
			List dataList = null;
			// 逐个文件读取
			for (int i = 1; i <= fileNum; i++) {
				if (hasBatch) {
					ps.executeBatch();
					conn.commit();
					ps.clearBatch();
				}
				String fileName = "Fugle_" + i + ".txt";
				try {
					dataList = fr.readFile(fileName);
				} catch (Exception e) {
					log.error(e.toString());
				}
				if (dataList == null || dataList.size() == 0) {
					continue;
				}
				hasBatch = true;
				for (int j = 0; j < dataList.size(); j++) {
					String[] values = (String[]) dataList.get(j);
					for (int k = 0; k < cols.size(); k++) {
						String value = "";
						// 防止每行值的数量小于定义的列数量
						try {
							value = values[k];
						} catch (Exception e) {
						}
						Column col = (Column) cols.get(k);
						if ("null".equals(values[k])) {
							value = "";
						}
						if (col.getType() == 1) {
							int iv = 0;
							try {
								iv = Integer.parseInt(value);
							} catch (Exception e) {
							}
							ps.setInt(k + 1, iv);
						} else if (col.getType() == 2) {
							double dv = 0;
							try {
								dv = Double.parseDouble(value);
							} catch (Exception e) {
							}
							ps.setDouble(k + 1, dv);
						} else {
							ps.setString(k + 1, value);
						}
					}
					ps.addBatch();
				}
			}
			// 最后一个文件的导入提交
			ps.executeBatch();
			ps.close();
			conn.commit();
			conn.close();
			log.info("导入完成！");
			flag = 1;
		} catch(Exception e) {
			log.error("导入时发生错误：" + e.toString());
			try {
				if (ps != null) {
					ps.close();
				}
				if (conn != null) {
					conn.rollback();
					conn.close();
				}
			} catch (Exception ex) {
			}
			log.error("导入时发生错误：{}" , e.toString());
			return flag;
		} finally {
			List resources = new ArrayList();
			resources.add(conn);
			resources.add(null);
			resources.add(ps);
			dbpool.close(resources);
		}
		return flag;
	}
	private String buildSql(Import task) {
		String tbName = task.getDestTable();
		List cols = task.getImportColumns();
		if (tbName == null) {
			log.error("任务{}配置错误：未配置目标表名信息！",task.getId());
			return null;
		}
		if (cols == null||cols.size() == 0) {
			log.error("任务{}配置错误：未配置目标列信息！",task.getId());
			return null;
		}
		StringBuffer sql = new StringBuffer("insert into ");
		sql.append(tbName).append(" (");
		StringBuffer vsql = new StringBuffer("(");
		for (int i = 0; i < cols.size(); i++) {
			Column col = (Column) cols.get(i);
			String cname = col.getDest();
			sql.append(cname);
			vsql.append("?");
			if (i < cols.size() - 1) {
				sql.append(",");
				vsql.append(",");
			}
		}
		sql.append(") values ");
		vsql.append(")");
		sql.append(vsql);
		return sql.toString();
	}

	public IConnectionPool initConnectPool(String conId) {
		if(conId==null){
			return null;
		}
		if(connFactory==null){
			connFactory = ConnectionFactory.getConnectionFactory();
		}
		IConnectionPool dbpool = (IConnectionPool)connFactory.getConnectionPool(conId);
		return dbpool;
	}
	
	public static void main(String[] args) {
		TemplateLoader tl = TemplateLoader.getLoader();
		Task etask = (Task)tl.getETLObjectTemplate(0,"dj_gongshu_extract");
		Task itask = (Task)tl.getETLObjectTemplate(0,"dj_gongshu_import");
		DBExtractService dbExp = new DBExtractService();
		dbExp.setConnFactory(ConnectionFactory.getConnectionFactory());
		DBImportService dbImp = new DBImportService();
		dbImp.setConnFactory(ConnectionFactory.getConnectionFactory());
		try {
			Extract et = null;
			// 导出
			long time = System.currentTimeMillis();
			if(etask instanceof Extract){
				et=(Extract)etask; 
			}
			dbExp.extractToFile(et,null,null);
			long time1 = System.currentTimeMillis();
			System.out.println("导出用时：" + (time1 - time));
			// 导入
			Import it = null;
			long time2 = System.currentTimeMillis();
			if(itask instanceof Import){
				it=(Import)itask; 
			}
			dbImp.importData(it,null,null);
			long time3 = System.currentTimeMillis();
			System.out.println("导入用时：" + (time3 - time2));
		}catch (Exception e) {
			log.error(e.toString());
		}
	}
}
