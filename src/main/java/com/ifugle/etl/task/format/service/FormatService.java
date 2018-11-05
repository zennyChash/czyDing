package com.ifugle.etl.task.format.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ifugle.etl.conncet.service.ConnectionFactory;
import com.ifugle.etl.conncet.service.IConnectionPool;
import com.ifugle.etl.entity.component.Column;
import com.ifugle.etl.entity.component.InPutFile;
import com.ifugle.etl.entity.component.OutPutFile;
import com.ifugle.etl.entity.task.Extract;
import com.ifugle.etl.entity.task.FormatTrans;
import com.ifugle.etl.entity.task.Import;
import com.ifugle.etl.schedule.SchedulerUtils;
import com.ifugle.etl.task.extract.entity.FileWriter;
import com.ifugle.etl.task.extract.service.DBExtractService;

public class FormatService {
	public static final Logger log = LoggerFactory.getLogger(FormatService.class);  
	private ConnectionFactory connFactory;
	
	@Autowired
	public void setConnFactory(ConnectionFactory connFactory) {
		this.connFactory = connFactory;
	}
	//导入xml格式的数据
	public int importXml(Import task, Map params, Map paramVals) {
		int flag = 0;
		InPutFile src = task.getSourceFile();
		if(src.getDoType()==0){
			flag = import2DbOfXml(task, params, paramVals);
		}else if(src.getDoType()==1){
			flag = outPutTxtOfXml(task, params, paramVals);
		}else{
			flag = import2DbOfXml(task, params, paramVals);
			if(flag !=1){
				log.error("任务{}配置为：从xml导入数据库并输出txt文件。导入失败，任务{}终止！",task.getId(),task.getId());
			}
			flag = outPutTxtOfXml(task, params, paramVals);
		}
		return flag;
	}

	private int import2DbOfXml(Import task, Map params, Map paramVals) {
		int flag = 0;
		int cc = 0;
		SAXReader reader = new SAXReader();
		Document doc = null;
		String conId = task.getConnectionId();
		IConnectionPool dbpool = initConnectPool(conId);
		Connection conn = (Connection)dbpool.getConnection();
		PreparedStatement ps = null;
		if (conn == null) {
			log.error("获得数据库连接失败！");
		}
		try{
			InPutFile src = task.getSourceFile();
			String sql = buildSql(task);
			if(StringUtils.isEmpty(sql)){
				log.error("未成功组织导入sql，任务{}执行中断！",task.getId());
				return 9;
			}
			String path = src.getDir();
			path = SchedulerUtils.parseParamValue(path, paramVals);
			doc = reader.read(new File(path));
			List cols = task.getImportColumns();
			//先找出key列的值，存放在map中。
			Map keyVals = new HashMap();
			for(int i=0;i<cols.size();i++){
				Column col = (Column) cols.get(i);
				if(col.getIsKey()==1){
					List keyResults = doc.selectNodes(col.getSource()); 
			        if(keyResults!=null){
			        	Node node = (Node) keyResults.get(0); 
			        	String val = node.getText();
			        	keyVals.put(col.getSource(), val);
			        }
				}
			}
			String dtRootPath = src.getDataRootNode();
			List details = doc.selectNodes(dtRootPath);
			//处理数据行
			if(details!=null){
				conn.setAutoCommit(false);
				ps = conn.prepareStatement(sql);
				for (int i = 0; i < details.size(); i++) {
					Node node = (Node) details.get(i); 
					for(int j=0;j<cols.size();j++){
						Column col = (Column) cols.get(j);
						String value=""; 
						if(col.getIsKey()==1){
							value = (String)keyVals.get(col.getSource());
						}else{
							value = node.selectSingleNode(col.getSource()).getText();
						}
						if (col.getType() == 1) {
							int iv = 0;
							try {
								iv = Integer.parseInt(value);
							} catch (Exception e) {
							}
							ps.setInt(j + 1, iv);
						} else if (col.getType() == 2) {
							double dv = 0;
							try {
								dv = Double.parseDouble(value);
							} catch (Exception e) {
							}
							ps.setDouble(j + 1, dv);
						} else {
							ps.setString(j + 1, value);
						}
					}
					ps.addBatch();
					cc++;
					if (0 == i % 200) {
		    			ps.executeBatch();
		    		}
				}
			}
			ps.executeBatch();
			ps.close();
			conn.commit();
			conn.close();
			log.info("xml导入任务{}执行完成！共导入{}条记录！",task.getId(),cc);
			flag = 1;
		}catch(DocumentException e){
			log.error("导入xml，读取xml文件时发生错误：{}",e.toString());
			return 9;
		}catch(Exception e) {
			log.error("任务{},导入中断在第{}条,错误：{}" ,task.getId(),cc-1, e.toString());
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
			return 9;
		} finally {
			List resources = new ArrayList();
			resources.add(conn);
			resources.add(null);
			resources.add(ps);
			dbpool.close(resources);
		}
		flag = 1;
		return flag;
	}
	
	private int outPutTxtOfXml(Import task, Map params, Map paramVals){
		int flag = 0;
		int cc = 0;
		SAXReader reader = new SAXReader();
		Document doc = null;
		try{
			InPutFile src = task.getSourceFile();
			String path = src.getDir();
			path = SchedulerUtils.parseParamValue(path, paramVals);
			doc = reader.read(new File(path));
			List cols = task.getImportColumns();
			//先找出key列的值，存放在map中。
			Map keyVals = new HashMap();
			for(int i=0;i<cols.size();i++){
				Column col = (Column) cols.get(i);
				if(col.getIsKey()==1){
					List keyResults = doc.selectNodes(col.getSource()); 
			        if(keyResults!=null){
			        	Node node = (Node) keyResults.get(0); 
			        	String val = node.getText();
			        	keyVals.put(col.getSource(), val);
			        }
				}
			}
			String dtRootPath = src.getDataRootNode();
			List details = doc.selectNodes(dtRootPath);
			List dataList = new ArrayList();
			//处理数据行
			if(details!=null){
				for (int i = 0; i < details.size(); i++) {
					List row = new ArrayList();
					Node node = (Node) details.get(i); 
					for(int j=0;j<cols.size();j++){
						Column col = (Column) cols.get(j);
						String value=""; 
						if(col.getIsKey()==1){
							value = (String)keyVals.get(col.getSource());
						}else{
							value = node.selectSingleNode(col.getSource()).getText();
						}
						row.add(value);
					}
					dataList.add(row);
					cc++;
				}
			}
			FileWriter fw = buildFileWriter(dataList.size(),task, params, paramVals);
			boolean done = fw.write(dataList);
			fw.finishWriteFile();
			log.info("任务{}，xml格式转化为txt文件执行完成！共输出{}条记录！",task.getId(),cc);
			flag = done?1:9;
		}catch(DocumentException e){
			log.error("任务{},xml格式转化为txt时发生xml读取错误：{}",task.getId(),e.toString());
			return 9;
		}catch(Exception e){
			log.error("任务{},xml格式转化为txt时发生错误：{}",task.getId(),e.toString());
			return 9;
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
	
	//导入xml格式的数据
	public int importExcel(Import task, Map params, Map paramVals) {
		int flag = 0;
		InPutFile src = task.getSourceFile();
		if(src.getDoType()==0){
			flag = import2DbOfExcel(task, params, paramVals);
		}else if(src.getDoType()==1){
			flag = outPutTxtOfExcel(task, params, paramVals);
		}else{
			flag = import2DbOfExcel(task, params, paramVals);
			if(flag !=1){
				log.error("任务{}配置为：从xml导入数据库并输出txt文件。导入失败，任务{}终止！",task.getId(),task.getId());
			}
			flag = outPutTxtOfExcel(task, params, paramVals);
		}
		return flag;
	}
	private int import2DbOfExcel(Import task, Map params, Map paramVals){
		int flag = 0;
		int cc = 0;
		String conId = task.getConnectionId();
		IConnectionPool dbpool = initConnectPool(conId);
		Connection conn = (Connection)dbpool.getConnection();
		PreparedStatement ps = null;
		if (conn == null) {
			log.error("获得数据库连接失败！");
		}
		try{
			InPutFile src = task.getSourceFile();
			String sql = buildSql(task);
			if(StringUtils.isEmpty(sql)){
				log.error("未成功组织导入sql，任务{}执行中断！",task.getId());
				return 9;
			}
			String path = src.getDir();
			path = SchedulerUtils.parseParamValue(path, paramVals);
			File f = new File(path);
			if(!f.exists()) {
				log.error("指定的文件{}不存在，任务{}中断！",path,task.getId());
				return 9;
			}
			InputStream in = new FileInputStream(path);
			Workbook wb = WorkbookFactory.create(in);
			Sheet sheet = wb.getSheetAt(src.getSheetIndex()-1);
			List cols = task.getImportColumns();
			int si=src.getStartRow();
			boolean end=false;
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(sql);
            while(!end){
            	Row row = sheet.getRow(si-1);
            	if(row==null){
            		break;
            	}
            	for(int i=0;i<cols.size();i++){
    				Column col = (Column) cols.get(i);
            		String val="";
            		int cidx = -1;
            		try{
            			cidx = Integer.parseInt(col.getSource());
            		}catch(Exception e){}
        			Cell cell=row.getCell(cidx);
            		if(col.getType()==1){
            			int iVal=0;
            			double tmpdv=0;
            			if(cell!=null){
            				try{
            					tmpdv = cell.getNumericCellValue();
            				}catch(Exception e){
            				}
            			}
            			iVal = new Double(tmpdv).intValue();
            			ps.setInt(i + 1, iVal);
            		}else if(col.getType()==2){
            			double dVal=0;
            			if(cell!=null){
            				try{
            					dVal=cell.getNumericCellValue();
            				}catch(Exception e){
            				}
            			}
            			ps.setDouble(i+1, dVal);
            		}else{
            			if(cell!=null){
                			try{
                				if(cell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                					if (HSSFDateUtil.isCellDateFormatted(cell)) {   
                				        double d = cell==null?0:cell.getNumericCellValue();   
                				        Date date = HSSFDateUtil.getJavaDate(d); 
                				        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
                				        val = sdf.format(date);
                				    }else{
                				    	val = String.valueOf(cell.getNumericCellValue());
                				    }
                				}else if(cell.getCellType()==HSSFCell.CELL_TYPE_BLANK){
                					val="";	
                				}else{
                					val = cell.getRichStringCellValue().getString();
                				}
		                        if (null != val &&val.indexOf(".") != -1 && val.indexOf("E") != -1) {
		                            DecimalFormat df = new DecimalFormat();
		                            val = df.parse(val).toString();
		                        }
		                        if (null != val &&val.endsWith(".0")) {
		                            int size = val.length();
		                            val = val.substring(0, size - 2);
		                        }
                			}catch(Exception e){
                			}
            			}
            			ps.setString(i + 1, val);
            		}
            	}
            	//执行
            	ps.addBatch();
				cc++;
				if (0 == cc % 200) {
	    			ps.executeBatch();
	    		}
                si++;
                if(src.getEndRow()>0&&si==src.getEndRow()){
                	end=true;
                }
            }
			ps.executeBatch();
			ps.close();
			conn.commit();
			conn.close();
			log.info("Excel导入任务{}执行完成！共导入{}条记录！",task.getId(),cc);
			flag = 1;
		}catch(Exception e) {
			log.error("任务{},导入中断在第{}条,错误：{}" ,task.getId(),cc-1, e.toString());
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
			return 9;
		} finally {
			List resources = new ArrayList();
			resources.add(conn);
			resources.add(null);
			resources.add(ps);
			dbpool.close(resources);
		}
		flag = 1;
		return flag;
	}
	
	private int outPutTxtOfExcel(Import task, Map params, Map paramVals){
		int flag = 0;
		int cc = 0;
		List dataList = new ArrayList();
		try{
			InPutFile src = task.getSourceFile();
			String path = src.getDir();
			InputStream in = new FileInputStream(path);
			Workbook wb = WorkbookFactory.create(in);
			Sheet sheet = wb.getSheetAt(src.getSheetIndex()-1);
			List cols = task.getImportColumns();
			int si=src.getStartRow();
			boolean end=false;
            while(!end){
            	Row row = sheet.getRow(si-1);
            	if(row==null){
            		break;
            	}
            	List txtRow = new ArrayList();
            	for(int i=0;i<cols.size();i++){
    				Column col = (Column) cols.get(i);
            		String val="";
            		int cidx = -1;
            		try{
            			cidx = Integer.parseInt(col.getSource());
            		}catch(Exception e){}
        			Cell cell=row.getCell(cidx);
        			if(cell!=null){
            			try{
            				if(cell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
            					if (HSSFDateUtil.isCellDateFormatted(cell)) {   
            				        double d = cell==null?0:cell.getNumericCellValue();   
            				        Date date = HSSFDateUtil.getJavaDate(d); 
            				        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
            				        val = sdf.format(date);
            				    }else{
            				    	val = String.valueOf(cell.getNumericCellValue());
            				    }
            				}else if(cell.getCellType()==HSSFCell.CELL_TYPE_BLANK){
            					val="";	
            				}else{
            					val = cell.getRichStringCellValue().getString();
            				}
	                        if (null != val &&val.indexOf(".") != -1 && val.indexOf("E") != -1) {
	                            DecimalFormat df = new DecimalFormat();
	                            val = df.parse(val).toString();
	                        }
	                        if (null != val &&val.endsWith(".0")) {
	                            int size = val.length();
	                            val = val.substring(0, size - 2);
	                        }
            			}catch(Exception e){
            			}
        			}
        			txtRow.add(val);
            	}
            	dataList.add(txtRow);
				cc++;
                if(src.getEndRow()>0&&si==src.getEndRow()){
                	end=true;
                }else{
                	si++;
                }
            }
			FileWriter fw = buildFileWriter(dataList.size(),task, params, paramVals);
			boolean done = fw.write(dataList);
			fw.finishWriteFile();
			log.info("任务{}，Excel格式转化为txt文件执行完成！共输出{}条记录！",task.getId(),cc);
			flag = done?1:9;
		}catch(Exception e){
			log.error("任务{},Excel格式转化为txt时发生错误：{}",task.getId(),e.toString());
			return 9;
		}
		return flag;
		
	}
	private FileWriter buildFileWriter(int total,Import task,Map params,Map paramVals){
		FileWriter fw = new FileWriter();
		InPutFile f = task.getSourceFile();
		fw.setRecordCount(total);
		fw.setColSeparator(f.getColSeparator());
		fw.setSaveColumns(task.getImportColumns());
		fw.setEncode(f.getEncode());
		fw.setRowsPerFile(-1);
		String dir = f.getTransDir();
		String pDir = SchedulerUtils.parseParamValue(dir,paramVals);
		fw.setRootDir(pDir);
		return fw;
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
}
