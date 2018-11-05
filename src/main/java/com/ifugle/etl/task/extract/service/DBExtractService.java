package com.ifugle.etl.task.extract.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
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
import com.ifugle.etl.entity.ConnectInfo;
import com.ifugle.etl.entity.task.Extract;
import com.ifugle.etl.entity.base.Task;
import com.ifugle.etl.schedule.SchedulerUtils;
import com.ifugle.etl.task.extract.entity.FileWriter;
import com.ifugle.etl.utils.TemplateLoader;

public class DBExtractService implements IExtract {
	private ConnectionFactory connFactory;
	private static Logger log = LoggerFactory.getLogger(DBExtractService.class);
	@Autowired
	public void setConnFactory(ConnectionFactory connFactory){
		this.connFactory = connFactory;
	}
	public DBExtractService(){
	}
	
	/**
	 * 提取行
	 */
	public List extractRows(ConnectInfo cinfo) {
		List rows = null;
		return rows;
	}
	/**
	 * 把记录行保存到文件中
	 */
	public boolean saveRows(List rows, OutPutFile file) {
		boolean saved = false;
		return saved;
	}
	@SuppressWarnings("unchecked")
	public synchronized int extractToFile(Extract task,Map params,Map paramVals){
		int flag=0;
		if(task==null){
			log.error("任务配置错误：未配置提取数据信息！");
			return flag;
		}
		log.info("DBExtractService开始执行任务：{}",task.getId());
		OutPutFile df = task.getDestFile();
		if(df==null){
			log.error("任务配置错误：未配置数据输出文件信息！");
			return flag;
		}
		String dir = df.getDir();
		if(dir==null){
			log.error("未设置数据存放的根目录！");
			return flag;
		}
		String sql = task.getSql();
		ProcedureBean procedure = task.getProcedure();
		if(StringUtils.isEmpty(sql)&&procedure==null){
			log.error("未设置取数方式！");
			return flag;
		}
		if(task.getExtractBy()==1){
			flag = excuteSql(task,params,paramVals);
		}else if(task.getExtractBy()==2){
			flag = excuteProcedure(task,params,paramVals);
		}
		return flag;
	}
	
	private int excuteProcedure(Extract task, Map params, Map paramVals) {
		int flag = 0;
		//连接
		String conId = task.getConnectionId();
		IConnectionPool dbpool = initConnectPool(conId);
		Connection conn = (Connection)dbpool.getConnection();
		if(conn==null){
			log.error("获得数据库连接失败！");
			return flag;
		}
		ProcedureBean pro=task.getProcedure();
		if(pro==null||StringUtils.isEmpty(pro.getName())){
			log.error("未设置取数存储过程！");
			return flag;
		}
		List parasIn=pro.getInParas();
		CallableStatement cs = null;
		ResultSet rs=null;
		StringBuffer proStmt=new StringBuffer("{call ");
		proStmt.append(pro.getName());
		//根据输入参数定义的个数设置?
		if(parasIn!=null&&parasIn.size()>0){
			proStmt.append("(");
			for(int i=0;i<parasIn.size();i++){
				proStmt.append("?");
				if(i<parasIn.size()-1){
					proStmt.append(",");
				}
			}
		}
		//根据输出参数定义继续设置?
		List parasOut=pro.getOutParas();
		if(parasOut!=null&&parasOut.size()>0){
			if(parasIn==null||parasIn.size()==0){
				proStmt.append("(");
			}else{
				proStmt.append(",");
			}
			for(int i=0;i<parasOut.size();i++){
				proStmt.append("?");
				if(i<parasOut.size()-1){
					proStmt.append(",");
				}else{
					proStmt.append(")");
				}
			}
		}else{
			if(parasIn!=null&&parasIn.size()>0){
				proStmt.append(")");
			}
		}
		proStmt.append("}");
		try{
			cs = conn.prepareCall(proStmt.toString());
			//如果有输入参数
			if(parasIn!=null&&parasIn.size()>0){
				int piStart = 0,piEnd = parasIn.size();
				if(task.isInBatches()){
					piStart = 2;
				}
				for(int i=piStart;i<parasIn.size();i++){
					//过程参数引用方式分直接引用固定值和引用参数两种
					ProParaIn pi=(ProParaIn)parasIn.get(i);
					if(pi!=null&&pi.getReferMode()==0){
						if(pi.getDataType()==1){
							int ival=0;
							try{ival=Integer.parseInt(pi.getValue());}
							catch(Exception e){}
							cs.setInt(i+1, ival);
							log.info("参数(整型)"+pi.getReferTo()+":"+ival);
						}else if(pi.getDataType()==2){
							double dval=0;
							try{dval=Double.parseDouble(pi.getValue());}
							catch(Exception e){}
							cs.setDouble(i+1, dval);
							log.info("参数(小数)"+pi.getReferTo()+":"+dval);
						}else{
							cs.setString(i+1, pi.getValue());
							log.info("参数(字符串)"+pi.getReferTo()+":"+pi.getValue());
						}
					}else{
						if(params==null){
							cs.close();
							conn.close();
							log.error("设计文件中缺少参数定义部分！");
						}
						//找出输入参数的定义
						Parameter para=(Parameter)params.get(pi.getReferTo());
						if(para==null){
							cs.close();
							conn.close();
							log.error("取数存储过程中引用的参数"+pi.getReferTo()+"，未在参数定义中找到！");
						}
						String val=(String)paramVals.get(pi.getReferTo());
						if(val==null){
							cs.close();
							conn.close();
							log.error("缺少参数"+pi.getReferTo()+"的值！");
						}
						if(para.getDataType()==1){
							int iVal=0;
							try{
								iVal=Integer.parseInt(val);
							}catch(Exception e){}
							cs.setInt(i+1, iVal);
							log.info("参数(整型)"+pi.getReferTo()+":"+iVal);
						}else if(para.getDataType()==2){
							double dVal=0;
							try{
								dVal=Double.parseDouble(val);
							}catch(Exception e){}
							cs.setDouble(i+1, dVal);
							log.info("参数(小数)"+pi.getReferTo()+":"+dVal);
						}else{
							cs.setString(i+1,val);
							log.info("参数(字符串)"+pi.getReferTo()+":"+val);
						}
					}
				}
			}
			//注册输出参数
			int oStart=parasIn==null?1:parasIn.size()+1;
			if(parasOut!=null){
				for(int i=0;i<parasOut.size();i++){
					ProParaOut po=(ProParaOut)parasOut.get(i);
					if(po.getDataType()==1||po.getDataType()==2){
						cs.registerOutParameter(oStart+i, Types.NUMERIC);
					}else if(po.getDataType()==0){
						cs.registerOutParameter(oStart+i, Types.VARCHAR);
					}else if(po.getDataType()==3){
						cs.registerOutParameter(oStart+i, oracle.jdbc.OracleTypes.CURSOR);
					}
				}
			}
			conn.setAutoCommit(false);
			cs.setInt(1, 0);
		    cs.setInt(2, 0);
	        cs.execute();
	        
	        int ti=pro.getTotalIndex();
	        int total = cs.getInt(oStart-1+ti);
	        FileWriter fw = buildFileWriter(total,task, paramVals);
	        
			//存储过程取数，start，limit必须是前两个参数
			if(task.isInBatches()){
				int extCounter = 0,start=0;
				int limit = task.getLimit();
				while(extCounter<total){
			        cs.setInt(1, start);
			        cs.setInt(2, limit);
			        cs.execute();
			        rs = (ResultSet)cs.getObject(oStart-1+pro.getDataSetIndex()); 
					int outRows = outPutData(conn,rs,task.getSaveColumns(),fw);
					start = start+limit;
					extCounter +=outRows;
					rs.close();
				}
			}else{
				rs = (ResultSet)cs.getObject(oStart-1+pro.getDataSetIndex()); 
				outPutData(conn,rs,task.getSaveColumns(),fw);
				rs.close();
			}
			fw.finishWriteFile();
			conn.commit();
			cs.close();
			conn.close();
			log.info("取数完成！");
			flag = 1;
			
			
		}catch(Exception e){
			log.error("取数时发生错误："+e.toString());
			try{
				if(rs!=null){
					rs.close();
				}
				if(cs!=null){
					cs.close();
				}
				if(conn!=null){
					conn.rollback();
					conn.close();
				}
			}catch(Exception ex){
				
			}
			log.error("取数时发生错误：{}",e.toString());
			return flag;
		}finally{
			List src = new ArrayList();
			src.add(conn);
			src.add(rs);
			src.add(cs);
			dbpool.close(src);
		}
		return flag;
	}
	//根据sql取数，输出
	private int excuteSql(Extract task, Map params, Map paramVals) {
		int flag = 0;
		//连接
		String conId = task.getConnectionId();
		IConnectionPool dbpool = initConnectPool(conId);
		Connection conn = (Connection)dbpool.getConnection();
		if(conn==null){
			log.error("获得数据库连接失败！");
			return 0;
		}
		String sql = task.getSql();
		sql = SchedulerUtils.parseParamValue(sql,paramVals);
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			//先获取总数信息
			ps = conn.prepareStatement("select count(*)cc from ("+sql+")");
			rs = ps.executeQuery();
			int total = 0 ;
			if(rs.next()){
				total = rs.getInt(1);
			}
			if(total==0){
				log.info("取得的记录数为0！");
				return 9;
			}
			rs.close();
			ps.close();
			//构造输出文本对象
			FileWriter fw = buildFileWriter(total,task, paramVals);
			//根据是否分批取数，确定取数的次数及每次取数的限制。
			if(task.isInBatches()){
				int extCounter = 0,start=0;
				int limit = task.getLimit();
				StringBuffer qSql = new StringBuffer("SELECT * FROM (SELECT A.*, rownum r FROM (");
		        qSql.append(sql);
		        qSql.append(") A WHERE rownum<=?) B WHERE r>?");
		        ps = conn.prepareStatement(qSql.toString());
				while(extCounter<total){
			        ps.setInt(1, start+limit);
			        ps.setInt(2, start);
			        rs = ps.executeQuery();
					int outRows = outPutData(conn,rs,task.getSaveColumns(),fw);
					start = start+limit;
					extCounter +=outRows;
					rs.close();
				}
				ps.close();
			}else{
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				outPutData(conn,rs,task.getSaveColumns(),fw);
				rs.close();
				ps.close();
			}
			fw.finishWriteFile();
			conn.close();
			log.info("取数完成！");
			flag = 1;
		}catch(Exception e){
			log.error("取数时发生错误："+e.toString());
			try{
				if(rs!=null){
					rs.close();
				}
				if(ps!=null){
					ps.close();
				}
				if(conn!=null){
					conn.close();
				}
			}catch(Exception ex){
				
			}
			log.error("取数时发生错误：{}",e.toString());
			return flag;
		}finally{
			List src = new ArrayList();
			src.add(conn);
			src.add(rs);
			src.add(ps);
			dbpool.close(src);
		}
		return flag;
	}
	private FileWriter buildFileWriter(int total,Extract task,Map paramVals){
		FileWriter fw = new FileWriter();
		OutPutFile df = task.getDestFile();
		fw.setRecordCount(total);
		fw.setColSeparator(df.getColSeparator());
		fw.setSaveColumns(task.getSaveColumns());
		fw.setEncode(df.getEncode());
		fw.setRowsPerFile(df.getRowsPerFile());
		String dir = df.getDir();
		String pDir = SchedulerUtils.parseParamValue(dir,paramVals);
		fw.setRootDir(pDir);
		fw.setDeleteDuplicate(df.isDeleteDuplicate());
		return fw;
	}
	private int outPutData(Connection conn,ResultSet rs ,List cols,FileWriter fw)throws Exception{
		//值要按task配置的顺序组装。即，mapping节点下col的顺序。
		List dataList = new ArrayList();
		try{
			while(rs.next()){
				List row = new ArrayList();
				for(int i=0;i<cols.size();i++){
					Column col = (Column)cols.get(i);
					String value ="";
					if(col.getType()==1){
						int iv = rs.getInt(col.getSource().toUpperCase());
						value = String.valueOf(iv);
					}else if(col.getType()==2){
						double dv = rs.getDouble(col.getSource().toUpperCase());
						value = String.valueOf(dv);
					}else{
						value = rs.getString(col.getSource().toUpperCase());
					}
					row.add(value);
				}
				dataList.add(row);
			}
			fw.write(dataList);
		}catch(Exception e){
			try{
				if(rs!=null){
					rs.close();
				}
			}catch(Exception ex){
			}
			throw e;
		}
		return dataList.size();
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
	public static void main(String[] args){
		TemplateLoader tl = TemplateLoader.getLoader();
		Task task = (Task)tl.getETLObjectTemplate(0,"dj_gongshu");
		DBExtractService dbExp = new DBExtractService();
		dbExp.setConnFactory(ConnectionFactory.getConnectionFactory());
		try{
			long time = System.currentTimeMillis();
			Extract et = null;
			if(task instanceof Extract){
				et=(Extract)task; 
			}
			dbExp.extractToFile(et,null,null);
			long time1 = System.currentTimeMillis();
			System.out.println("用时："+(time1-time));
		}catch(Exception e){
			log.error(e.toString());
		}
	}
}
