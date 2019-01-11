package com.ifugle.etl.utils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import com.ifugle.etl.entity.component.*;
import com.ifugle.etl.entity.task.Execute;
import com.ifugle.etl.entity.task.Extract;
import com.ifugle.etl.entity.task.FtpTransfer;
import com.ifugle.etl.entity.task.Import;
import com.ifugle.etl.entity.task.RequestTask;
import com.ifugle.etl.entity.task.SendMail;
import com.ifugle.etl.entity.task.Unzip;
import com.ifugle.etl.entity.task.Zip;
import com.ifugle.etl.entity.base.Task;

public class TaskTemplateParser {
	private static TaskTemplateParser tmpParser;
	private TaskTemplateParser(){
	}
	/**
	 * 获取解析器实例。
	 * singleton模式，调用返回的是同一个解析器实例。 
	 * @return 解析器实例。
	 */
	public static TaskTemplateParser getParser(){
		if(tmpParser==null)
			tmpParser=new TaskTemplateParser();
		return tmpParser;
	}
	/**
	 * 根据报表设计内容解析成内存的报表对象
	 * @param tmpContent 任务设计内容的字符串，xml格式。
	 * @return 经过解析的任务对象。
	 * @throws ParseTaskException
	 */
	public Task parseTemplate(String tmpContent)throws ParseException{
		if(tmpContent==null||"".equals(tmpContent))return null;
		Task task = null;
		try{
			SAXReader reader = new SAXReader();
		    Document doc = reader.read(new ByteArrayInputStream(tmpContent.getBytes("utf-8")));
		    Element root = doc.getRootElement();
		    if(root==null){
		    	return null;
		    }
		    String ttype = root.attributeValue("type");
		    if("import".equalsIgnoreCase(ttype)||"1".equals(ttype)){
		    	task = parseImport(root);
		    }else if("execute".equalsIgnoreCase(ttype)||"2".equals(ttype)){
		    	task = parseExecute(root);
		    }else if("ftp".equalsIgnoreCase(ttype)||"3".equals(ttype)){
		    	task = parseFtp(root);
		    }else if("zip".equalsIgnoreCase(ttype)||"4".equals(ttype)){
		    	task = parseZip(root);
		    }else if("unzip".equalsIgnoreCase(ttype)||"5".equalsIgnoreCase(ttype)){
		    	task = parseUnZip(root);
		    }else if("mail".equalsIgnoreCase(ttype)||"6".equalsIgnoreCase(ttype)){
		    	task = parseSendMail(root);
		    }else if("httpRequest".equalsIgnoreCase(ttype)||"7".equalsIgnoreCase(ttype)){
		    	task = parseRequest(root);
		    }else{
		    	task = parseExtract(root);
		    }
		}catch(Exception e){
			throw new ParseException(e.toString());
		}
		return task;
	}
	
	private Task parseRequest(Element root) {
		if(root==null){
			return null;
		}
		Element rnode = root.element("request");
		if(rnode==null){
			return null;
		}
		RequestTask rt = new RequestTask();
		parseTaskRoot(rt,root,7);
		rt.setMethod(rnode.attributeValue("method"));
		rt.setMode(rnode.attributeValue("mode"));
		rt.setUri(rnode.elementText("uri"));
		String sckto = rnode.attributeValue("socketTimeout");
		String cto = rnode.attributeValue("connTimeout"); 
		int socketTimeout = 5000, connTimeout = 5000;
		try{
			socketTimeout = Integer.parseInt(sckto);
		}catch(Exception e){}
		try{
			connTimeout = Integer.parseInt(cto);
		}catch(Exception e){}
		rt.setConnTimeout(connTimeout);
		rt.setSocketTimeout(socketTimeout);
		rt.setBeforeReq(rnode.attributeValue("before"));
		if(rnode.element("props")!=null&&rnode.element("props").elementIterator("prop")!=null){
			Map props=new HashMap();
			for(Iterator it=rnode.element("props").elementIterator("prop");it.hasNext();){
				Element pnode=(Element)it.next();
				String n = pnode.attributeValue("name");
				String v = pnode.attributeValue("value");
				props.put(n,v);
			}
			rt.setProps(props);
		}
		if(rnode.element("response")!=null){
			rt.setBeforeResponse(rnode.element("response").attributeValue("before"));
		}
		return rt;
	}
	private Task parseSendMail(Element root) {
		if(root==null){
			return null;
		}
		Element mnode = root.element("mail");
		if(mnode==null){
			return null;
		}
		SendMail sm = new SendMail();
		parseTaskRoot(sm,root,6);
		sm.setHost(mnode.elementText("host"));
		sm.setUsername(mnode.elementText("username"));
		sm.setPassword(mnode.elementText("password"));
		sm.setFrom(mnode.elementText("from"));
		sm.setDest(mnode.elementText("dest"));
		sm.setSubject(mnode.elementText("subject"));
		sm.setContent(mnode.elementText("content"));
		Element attaches = mnode.element("attachments");
		if(mnode.element("attachments")!=null){
	    	Element atsnode =mnode.element("attachments");
		    if(atsnode!=null&&atsnode.elementIterator("attach")!=null){
				List files=new ArrayList();
				for(Iterator it=atsnode.elementIterator("attach");it.hasNext();){
					Element anode=(Element)it.next();
					String f = anode.attributeValue("filename");
					files.add(f);
				}
				sm.setAttachments(files);
		    }
		}
		return sm;
	}
	private Task parseUnZip(Element root) {
		if(root==null){
			return null;
		}
		Element unode = root.element("unzip");
		if(unode==null){
			return null;
		}
		Unzip uz = new Unzip();
		parseTaskRoot(uz,root,5);
		uz.setFileToUnzip(unode.attributeValue("fileToUnzip"));
		uz.setStoredPath(unode.attributeValue("storedPath"));
		return uz;
	}
	private Task parseZip(Element root) {
		if(root==null){
			return null;
		}
		Element znode = root.element("zip");
		if(znode==null){
			return null;
		}
		Zip zip = new Zip();
		parseTaskRoot(zip,root,4);
		zip.setDirectoryToZip(znode.attributeValue("directoryToZip"));
		zip.setZippedFileName(znode.attributeValue("zippedFileName"));
		return zip;
	}
	private Task parseFtp(Element root) {
		if(root==null){
			return null;
		}
		Element fnode = root.element("ftp");
		if(fnode==null){
			return null;
		}
		FtpTransfer ftp = new FtpTransfer();
		parseTaskRoot(ftp,root,3);
		String sPasv = fnode.attributeValue("isPasv");
		int isPasv = 1;
		try{
			isPasv = Integer.parseInt(sPasv);
		}catch(Exception e){
		}
		ftp.setIsPasv(isPasv);
		String sUpload = fnode.attributeValue("isUpload");
		int isUpload = 0;
		try{
			isUpload = Integer.parseInt(sUpload);
		}catch(Exception e){
		}
		ftp.setIsUpload(isUpload);
		Element snode = fnode.element("server");
		if(snode!=null){
			ftp.setServerIp(snode.attributeValue("ip"));
			int serverPort = 21;
			try{
				serverPort = Integer.parseInt(snode.attributeValue("port"));
			}catch(Exception e){
			}
			ftp.setServerPort(serverPort);
			int anonymous = 0;
			try{
				anonymous = Integer.parseInt(snode.attributeValue("isAnonymous"));
			}catch(Exception e){
			}
			ftp.setIsAnonymous(anonymous);
			if(anonymous>0){
				ftp.setAnonymousUser(snode.attributeValue("anonymousUser"));
				ftp.setAnonymousPswd(snode.attributeValue("anonymousPassword"));
			}else{
				ftp.setUserName(snode.attributeValue("userName"));
				ftp.setPassword(snode.attributeValue("password"));
			}
			ftp.setWorkingDir(snode.attributeValue("workingDir"));
		}
		Element tnode = fnode.element("transfer");
		if(tnode!=null){
			if(ftp.getIsUpload()>0){
				ftp.setFileToUpload(tnode.attributeValue("fileToUpload"));
				ftp.setServerStoredName(tnode.attributeValue("serverStoredName"));
			}else{
				ftp.setFileToDownload(tnode.attributeValue("fileToDownload"));
				ftp.setLocalStoredPath(tnode.attributeValue("localStoredName"));
			}
		}
		return ftp;
	}
	private Execute parseExecute(Element root) {
		if(root==null){
			return null;
		}
		Element enode=root.element("execute");
	    if(enode==null){
		    return null;
	    }
	    Execute execute = new Execute();
	    parseTaskRoot(execute,root,2);
		execute.setConnectionId(enode.attributeValue("connectionId"));
		String sExType = enode.attributeValue("executeType");
		if(StringUtils.isEmpty(sExType)||"sql".equalsIgnoreCase(sExType)||"1".equals(sExType)){
			execute.setExecuteType(1);
			String sql = enode.elementText("sql");
			execute.setSql(sql);
		}else if("procedure".equalsIgnoreCase(sExType)||"2".equals(sExType)){
			execute.setExecuteType(2);
			ProcedureBean procedure = parseProcedure(enode.element("procedure"));
			execute.setProcedure(procedure);
		}
	    return execute;
	}
	private Import parseImport(Element root) {
		if(root==null){
			return null;
		}
		Element inode=root.element("import");
	    if(inode==null){
		    return null;
	    }
	    Import imp = new Import();
	    parseTaskRoot(imp,root,1);
	    imp.setConnectionId(inode.attributeValue("connectionId"));
	    imp.setDestTable(inode.attributeValue("destTable"));
	    String stype = inode.attributeValue("sourceType");
	    if("db".equalsIgnoreCase(stype)||"1".equals(stype)){
	    	imp.setSourceType(1);
	    }else{
	    	imp.setSourceType(0);
	    }
	    Element ipnode=inode.element("sourceFile");
	    if(ipnode!=null){
		    InPutFile sf = new InPutFile();
		    sf.setColSeparator(ipnode.attributeValue("colSeparator"));
			sf.setDir(ipnode.attributeValue("rootDir"));
			sf.setEncode(ipnode.attributeValue("encode"));
			sf.setFormat(ipnode.attributeValue("format"));
			String sDotype = ipnode.attributeValue("doType");
			if("2".equals(sDotype)||"db_file".equalsIgnoreCase(sDotype)){
				sf.setDoType(2);
			}else if("1".equals(sDotype)||"file".equalsIgnoreCase(sDotype)){
				sf.setDoType(1);
			}else{
				sf.setDoType(0);
			}
			String transDir = ipnode.attributeValue("transDir");
			sf.setTransDir(transDir);
		    imp.setSourceFile(sf);
	    }
	    Element dbnode=inode.element("sourceDb");
	    if(dbnode!=null){
		    SourceDb sdb = new SourceDb();
		    sdb.setConnectionId(dbnode.attributeValue("connectionId"));
		    sdb.setEncode(dbnode.attributeValue("encode"));
		    sdb.setSourceTable(dbnode.attributeValue("sourceTable"));
		    imp.setSourceDb(sdb);
	    }
	    if(inode.element("importColumns")!=null){
	    	Element scnode =inode.element("importColumns");
	    	String dtRoot = scnode.attributeValue("dataRootNode");
	    	if(!StringUtils.isEmpty(dtRoot)){
	    		String lstChar = dtRoot.substring(dtRoot.length()-1, dtRoot.length());
	    		if("/".equals(lstChar)){
	    			dtRoot = dtRoot.substring(0,dtRoot.length()-1);
	    		}
	    		imp.getSourceFile().setDataRootNode(dtRoot);
	    	}
	    	String sStart = scnode.attributeValue("startRow");
	    	String sEnd = scnode.attributeValue("endRow");
	    	int start = 2;
	    	if(!StringUtils.isEmpty(sStart)){
	    		try{
	    			start = Integer.parseInt(sStart);
	    		}catch(Exception e){}
	    	}
	    	imp.getSourceFile().setStartRow(start);
	    	
	    	int end = -1;
	    	if(!StringUtils.isEmpty(sEnd)){
	    		try{
	    			end = Integer.parseInt(sEnd);
	    		}catch(Exception e){}
	    	}
	    	imp.getSourceFile().setEndRow(end);
	    	
	    	int sheet = 1;
	    	String sSheet = scnode.attributeValue("sheetIndex");
	    	if(!StringUtils.isEmpty(sSheet)){
	    		try{
	    			sheet = Integer.parseInt(sSheet);
	    		}catch(Exception e){}
	    	}
	    	imp.getSourceFile().setSheetIndex(sheet);
	    	
		    if(scnode!=null&&scnode.elementIterator("col")!=null){
				List columns=new ArrayList();
				for(Iterator it=scnode.elementIterator("col");it.hasNext();){
					Element clnode=(Element)it.next();
					Column col = new Column();
					String src = clnode.attributeValue("source");
					col.setSource(src);
					int type=0;
					try{
						type= Integer.parseInt(clnode.attributeValue("type"));
					}catch(Exception e){
						type=0;
					}
					col.setType(type);
					//默认输出txt的列头名和列名相同
					if(!StringUtils.isEmpty(clnode.attributeValue("dest"))){
						col.setDest(clnode.attributeValue("dest"));
					}else{
						col.setDest(col.getSource());
					}
					String sKey = clnode.attributeValue("isKey");
					int isKey = 0;
					try{
						isKey = Integer.parseInt(sKey);
					}catch(Exception e){}
					col.setIsKey(isKey);
					columns.add(col);
				}
				imp.setImportColumns(columns);
			}
		}
	    return imp;
	}
	private Extract parseExtract(Element root) {
		if(root==null){
			return null;
		}
		Element enode = root.element("extract");
		if(enode==null){
			return null;
		}
		Extract ext = new Extract();
		parseTaskRoot(ext,root,0);
		String sbatches = enode.attributeValue("inBatches");
		int inBatches = 0;
		try{
			inBatches = Integer.parseInt(sbatches);
		}catch(Exception e){
		}
		ext.setInBatches(inBatches==1);
		String slimit = enode.attributeValue("limit");
		int limit = 100;
		try{
			limit = Integer.parseInt(slimit);
		}catch(Exception e){
		}
		ext.setLimit(limit);
		ext.setConnectionId(enode.attributeValue("connectionId"));
		String sExtBy = enode.attributeValue("extractBy");
		if(StringUtils.isEmpty(sExtBy)||"sql".equalsIgnoreCase(sExtBy)||"1".equals(sExtBy)){
			ext.setExtractBy(1);
			String sql = enode.elementText("sql");
			ext.setSql(sql);
		}else if("procedure".equalsIgnoreCase(sExtBy)||"2".equals(sExtBy)){
			ext.setExtractBy(2);
			ProcedureBean procedure = parseProcedure(enode.element("procedure"));
			ext.setProcedure(procedure);
		}
		Element snode = enode.element("save");
		if(snode!=null){
			OutPutFile dfile = new OutPutFile();
			dfile.setColSeparator(snode.attributeValue("colSeparator"));
			String sDelDup = snode.attributeValue("deleteDuplicate");
			boolean delDup = ("true".equals(sDelDup)||"1".equals(sDelDup));
			dfile.setDeleteDuplicate(delDup);
			dfile.setDir(snode.attributeValue("rootDir"));
			dfile.setEncode(snode.attributeValue("encode"));
			dfile.setFormat(snode.attributeValue("format"));
			int rowsPerFile = 10000;
			try{
				rowsPerFile= Integer.parseInt(snode.attributeValue("rowsPerFile"));
			}catch(Exception e){
			}
			dfile.setRowsPerFile(rowsPerFile);
			ext.setDestFile(dfile);
		}
		if(enode.element("saveColumns")!=null){
	    	Element scnode =enode.element("saveColumns");
		    if(scnode!=null&&scnode.elementIterator("col")!=null){
				List columns=new ArrayList();
				for(Iterator it=scnode.elementIterator("col");it.hasNext();){
					Element clnode=(Element)it.next();
					Column col = new Column();
					col.setSource(clnode.attributeValue("source"));
					int type=0;
					try{
						type= Integer.parseInt(clnode.attributeValue("type"));
					}catch(Exception e){
						type=0;
					}
					col.setType(type);
					//默认输出txt的列头名和列名相同
					if(!StringUtils.isEmpty(clnode.attributeValue("dest"))){
						col.setDest(clnode.attributeValue("dest"));
					}else{
						col.setDest(col.getSource());
					}
					String sKey = clnode.attributeValue("isKey");
					int isKey = 0;
					try{
						isKey = Integer.parseInt(sKey);
					}catch(Exception e){}
					col.setIsKey(isKey);
					columns.add(col);
				}
				ext.setSaveColumns(columns);
			}
		}
		return ext;
	}
	private ProcedureBean parseProcedure(Element proNode){
		if(proNode==null)return null;
		
		ProcedureBean pro=new ProcedureBean();
		pro.setName(proNode.attributeValue("name"));
		int dsi=1;
		try{
			String sdIndex=proNode.attributeValue("datasetIndex");
			dsi=Integer.parseInt(sdIndex);
		}catch(Exception e){
			dsi=1;
		}
		pro.setDataSetIndex(dsi);
		int ti=1;
		try{
			String tIndex=proNode.attributeValue("totalIndex");
			ti=Integer.parseInt(tIndex);
		}catch(Exception e){
			ti=1;
		}
		pro.setTotalIndex(ti);
		
		int opIndex=1;
		try{
			String sOpIndex=proNode.attributeValue("outPutInfoIndex");
			opIndex=Integer.parseInt(sOpIndex);
		}catch(Exception e){
			opIndex=0;
		}
		pro.setOutPutInfoIndex(opIndex);
		
		//过程的输入参数
		if(proNode!=null&&proNode.elementIterator("in")!=null){
			List proIns=new ArrayList();
			for(Iterator iit=proNode.elementIterator("in");iit.hasNext();){
				ProParaIn ppi=new ProParaIn();
				Element piNode=(Element)iit.next();
				int refMode=1;
				try{
					refMode=Integer.parseInt(piNode.attributeValue("referMode"));
				}catch(Exception e){}
				ppi.setReferMode(refMode);
				
				if(refMode==1){
					ppi.setReferTo(piNode.attributeValue("referTo"));
				}else{
					ppi.setValue(piNode.attributeValue("value"));
					int piDt=0;
					String sPidt=piNode.attributeValue("dataType");
					if("int".equalsIgnoreCase(sPidt)||"1".equals(sPidt)){
						piDt=1;
					}else if("double".equalsIgnoreCase(sPidt)||"2".equals(sPidt)){
						piDt=2;
					}else if("cursor".equalsIgnoreCase(sPidt)||"3".equals(sPidt)){
						piDt=3;
					}
					ppi.setDataType(piDt);
				}									
				proIns.add(ppi);
			}
			pro.setInParas(proIns);
		}
		//过程的输出参数
		if(proNode!=null&&proNode.elementIterator("out")!=null){
			List proOuts=new ArrayList();
			for(Iterator oit=proNode.elementIterator("out");oit.hasNext();){
				ProParaOut ppo=new ProParaOut();
				Element poNode=(Element)oit.next();
				int poDt=0;
				String sPodt=poNode.attributeValue("dataType");
				if("int".equalsIgnoreCase(sPodt)||"1".equals(sPodt)){
					poDt=1;
				}else if("double".equalsIgnoreCase(sPodt)||"2".equals(sPodt)){
					poDt=2;
				}else if("cursor".equalsIgnoreCase(sPodt)||"3".equals(sPodt)){
					poDt=3;
				}
				ppo.setDataType(poDt);
				proOuts.add(ppo);
			}
			pro.setOutParas(proOuts);
		}
		return pro;
	}
	private void parseTaskRoot(Task task,Element root,int taskType){
		task.setId(root.attributeValue("id"));
	    task.setName(root.attributeValue("name"));
	    task.setDesc(root.attributeValue("description"));
	    task.setTaskType(taskType);
	}
}
