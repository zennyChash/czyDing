package com.ifugle.etl.task.execute.service;

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
import com.ifugle.etl.entity.task.Execute;
import com.ifugle.etl.entity.task.Extract;
import com.ifugle.etl.entity.component.Column;
import com.ifugle.etl.entity.component.InPutFile;
import com.ifugle.etl.entity.component.Parameter;
import com.ifugle.etl.entity.component.ProParaIn;
import com.ifugle.etl.entity.component.ProParaOut;
import com.ifugle.etl.entity.component.ProcedureBean;
import com.ifugle.etl.schedule.SchedulerUtils;
import com.ifugle.etl.task.extract.entity.FileWriter;
import com.ifugle.etl.task.imp.entity.FileReader;

public class DBExecuteService implements IExecute {
	private ConnectionFactory connFactory;
	private static Logger log = LoggerFactory.getLogger(DBExecuteService.class);
	
	@Autowired
	public void setConnFactory(ConnectionFactory connFactory){
		this.connFactory = connFactory;
	}
	public DBExecuteService(){
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

	public int doExecute(Execute task, Map params, Map paramVals) {
		int flag=0;
		if(task==null){
			log.error("任务配置错误：未配置导入数据的相关信息！");
			return flag;
		}
		log.info("DBExecuteService开始执行任务：{}",task.getId());
		String sql = task.getSql();
		ProcedureBean procedure = task.getProcedure();
		if(StringUtils.isEmpty(sql)&&procedure==null){
			log.error("未设置取数方式！");
			return flag;
		}
		if(task.getExecuteType()==1){
			flag = excuteSql(task,params,paramVals);
		}else if(task.getExecuteType()==2){
			flag = excuteProcedure(task,params,paramVals);
		}
		return flag;
	}
	//根据sql取数，输出
	private int excuteSql(Execute task, Map params, Map paramVals) {
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
		try{
			ps = conn.prepareStatement(sql);
			ps.execute();
			ps.close();
			conn.close();
			log.info("execute任务{}执行完成！",task.getId());
			flag = 1;
		}catch(Exception e){
			log.error("取数时发生错误："+e.toString());
			try{
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
			src.add(ps);
			dbpool.close(src);
		}
		return flag;
	}
	private int excuteProcedure(Execute task, Map params, Map paramVals) {
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
				for(int i=0;i<parasIn.size();i++){
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
					}else{
						cs.registerOutParameter(oStart+i, oracle.jdbc.OracleTypes.CURSOR);
					}
				}
			}
			conn.setAutoCommit(false);
	        cs.execute();
	        String result = cs.getString(oStart);
	        if(!"1".equals(result)){
	        	flag=9;
	        	conn.rollback();
	        	String info = cs.getString(oStart+1);
	        	log.error("执行任务{}的存储过程{}时发生错误：",info);
	        }else{
				conn.commit();
				log.info("执行任务{}完成！",task.getId());
				flag = 1;
	        }
	        cs.close();
			conn.close();
		}catch(Exception e){
			log.error("执行任务{}的存储过程{}时发生错误：",task.getId(),task.getProcedure().getName(),e.toString());
			try{
				if(cs!=null){
					cs.close();
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
			src.add(cs);
			dbpool.close(src);
		}
		return flag;
	}
}
