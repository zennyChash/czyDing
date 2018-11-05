package com.ifugle.etl.utils.service;

import static org.quartz.JobBuilder.newJob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ifugle.etl.entity.ScheduledJob;
import com.ifugle.etl.entity.ScheduledTask;
import com.ifugle.etl.entity.SchedulerInfo;
import com.ifugle.etl.entity.base.Task;
import com.ifugle.etl.entity.component.Parameter;
import com.ifugle.etl.entity.component.TriggerInfo;
import com.ifugle.etl.entity.task.Execute;
import com.ifugle.etl.entity.task.Extract;
import com.ifugle.etl.entity.task.FtpTransfer;
import com.ifugle.etl.entity.task.Import;
import com.ifugle.etl.entity.task.Unzip;
import com.ifugle.etl.entity.task.Zip;
import com.ifugle.etl.schedule.SchedulerUtils;
import com.ifugle.etl.schedule.jobs.CollectionJob;
import com.ifugle.etl.schedule.service.SchedulService;
import com.ifugle.etl.task.execute.service.DBExecuteService;
import com.ifugle.etl.task.execute.service.IExecute;
import com.ifugle.etl.task.extract.service.DBExtractService;
import com.ifugle.etl.task.extract.service.IExtract;
import com.ifugle.etl.task.ftp.service.FtpService;
import com.ifugle.etl.task.imp.service.DBImportService;
import com.ifugle.etl.task.imp.service.IImport;
import com.ifugle.etl.task.zip.service.ZipService;
import com.ifugle.etl.utils.TemplateLoader;
import com.ifugle.etl.utils.entity.SimpleBean;

public class ManageService {
	private static Logger log = LoggerFactory.getLogger(ManageService.class);
	public ManageService(){
	}
	public List getModules(String pid){
		List mds = new ArrayList();
		SimpleBean m = new SimpleBean();
    	m.setBm("connection");
    	m.setMc("连接");
    	m.setPid("");
    	m.setHref("manage/connections.jsp");
    	m.setTarget("mid_right");
    	m.setIsLeaf(1);
    	mds.add(m);
    	m = new SimpleBean();
    	m.setBm("task");
    	m.setMc("任务");
    	m.setPid("");
    	m.setHref("manage/tasks.jsp");
    	m.setTarget("mid_right");
    	m.setIsLeaf(1);
    	mds.add(m);
    	m = new SimpleBean();
    	m.setBm("schedule");
    	m.setMc("调度");
    	m.setPid("");
    	m.setHref("manage/scheduleJobs.jsp");
    	m.setTarget("mid_right");
    	m.setIsLeaf(1);
    	mds.add(m);
    	return mds;
	}
	public List getJobs() {
		List sjobs = new ArrayList();
		TemplateLoader ltmp=TemplateLoader.getLoader();
		List sdInfos = ltmp.getETLObjectTemplates(2);
		//解析成数据行
		if(sdInfos!=null&&sdInfos.size()>0){
			for(int i=0;i<sdInfos.size();i++){
				SchedulerInfo sd = (SchedulerInfo)sdInfos.get(i);
				List jobs = sd.getJobs();
				if(jobs==null||jobs.size()==0){
					continue;
				}
				for(int j=0;j<jobs.size();j++){
					Map row = new HashMap();
					ScheduledJob job = (ScheduledJob)jobs.get(j);
					row.put("schedulerId", sd.getId());
					row.put("id", job.getJobId());
					row.put("mc", job.getJobMc());
					row.put("disabled", job.getDisabled());
					sjobs.add(row);
				}
			}
		}
		return sjobs;
	}
	public List getJobTasks(String schedulerId, String jobid) {
		List tasks = new ArrayList();;
		Map taskInfoMap = TemplateLoader.getLoader().getETLObjectTemplatesMap(0);
		SchedulerInfo sd = (SchedulerInfo)TemplateLoader.getLoader().getETLObjectTemplate(2,schedulerId);
		//解析成数据行
		if(sd!=null){
			Map jm = sd.getJobsMap();
			if(jm!=null){
				ScheduledJob sjob = (ScheduledJob)jm.get(jobid);
				Map schTaskMap = sjob.getTaskMap();
				ScheduledTask st = sjob.getHeadTask();
				paseTaskLink(schTaskMap,st,taskInfoMap,tasks);
				sjob.getTaskMap();
			}
		}
		return tasks;
	}
	private void paseTaskLink(Map schTaskMap,ScheduledTask st,Map taskInfoMap,List tasks){
		if(st==null){
			return;
		}
		String taskid = st.getTaskId();
		Task t = (Task)taskInfoMap.get(taskid);
		if(t!=null){
			Map row = new HashMap();
			row.put("id", t.getId());
			row.put("mc", t.getName());
			row.put("type", t.getTaskType());
			row.put("description", t.getDesc());
			row.put("disabled",st.getDisabled());
			tasks.add(row);
			String os = st.getOnSuccess();
			String of = st.getOnFail();
			ScheduledTask snst =(ScheduledTask)schTaskMap.get(os);
			ScheduledTask fnst =(ScheduledTask)schTaskMap.get(of);
			paseTaskLink(schTaskMap,snst,taskInfoMap,tasks);
			paseTaskLink(schTaskMap,fnst,taskInfoMap,tasks);
		}
	}
	public boolean toggleJobStatus(int doDisable, String schedulerId,String jobid)throws Exception{
		TemplateLoader ltmp=TemplateLoader.getLoader();
		Map sdInfoMap = ltmp.getETLObjectTemplatesMap(2);
		SchedulerInfo sd = (SchedulerInfo)sdInfoMap.get(schedulerId);
		if(sd!=null){
			Map jm = sd.getJobsMap();
			ScheduledJob sjob = (ScheduledJob)jm.get(jobid);
			if(sjob!=null){
				//设置为禁用状态，重启时就可以不注册/注册进调度器了，保持禁用/启用状态。
				sjob.setDisabled(doDisable);
				//暂停/继续当前工作单元
				SchedulerFactory sf = new StdSchedulerFactory();
		        Scheduler sched = sf.getScheduler();
		        JobKey jk = new JobKey(jobid, schedulerId);
		        if(sched.checkExists(jk)){
		        	if(doDisable==1){
		        		sched.pauseJob(jk);
		        	}else{
		        		sched.resumeJob(jk);
		        	}
		        }else if(doDisable==0){//如果是启用调度器中不存在的job，添加到调度器
        			JobDetail jcDt = newJob(CollectionJob.class).withIdentity(jobid, schedulerId)
    					      .build(); 
    				jcDt.getJobDataMap().put("schedulerId", schedulerId);
    				jcDt.getJobDataMap().put("jobId",jobid);
    				
    				TriggerInfo tinfo = sjob.getTrigger();
    				Trigger tg = SchedulService.getSchedulService().getTrigger(tinfo,sjob.getJobId(),schedulerId);
    				sched.scheduleJob(jcDt, tg);
		        }
			}
		}		
		return true;
	}
	public boolean toggleTaskStatus(int doDisable, String schedulerId,String jobid, String tid) {
		TemplateLoader ltmp=TemplateLoader.getLoader();
		Map sdInfoMap = ltmp.getETLObjectTemplatesMap(2);
		SchedulerInfo sd = (SchedulerInfo)sdInfoMap.get(schedulerId);
		if(sd!=null){
			Map jm = sd.getJobsMap();
			ScheduledJob sjob = (ScheduledJob)jm.get(jobid);
			if(sjob!=null){
				Map taskMap = sjob.getTaskMap();
				if(taskMap!=null){
					ScheduledTask st = (ScheduledTask)taskMap.get(tid);
					st.setDisabled(doDisable);
				}
			}
		}
		return true;
	}
	public String buildParamForm(String schedulerId, String jobid) {
		int lbwidth = 80,fwidth=120;
		StringBuffer sform = new StringBuffer("{id:'pForm',border:false,frame:true,labelWidth :");
		sform.append(lbwidth).append(",width:").append(fwidth).append(",labelAlign: 'right',layout:'form',items:[");
		TemplateLoader ltmp=TemplateLoader.getLoader();
		Map sdInfoMap = ltmp.getETLObjectTemplatesMap(2);
		SchedulerInfo sd = (SchedulerInfo)sdInfoMap.get(schedulerId);
		List params = null;
		if(sd!=null){
			Map jm = sd.getJobsMap();
			ScheduledJob sjob = (ScheduledJob)jm.get(jobid);
			if(sjob!=null){
				params = sjob.getParameters();
			}
		}
		if (params != null && params.size() > 0) {
			for(int i=0;i<params.size();i++){
				Parameter p = (Parameter)params.get(i);
				sform.append("{name:'").append(p.getName()).append("',fieldLabel: '").append(p.getMc()).append("',xtype:'textfield'}");
				if (i < params.size() - 1) {
					sform.append(",");
				}
			}
		}
		sform.append("]}");
		return sform.toString();
	}
	public boolean executeJob(String schedulerId, String jobid, String strParams)throws Exception{
		SchedulerInfo sdinfo = (SchedulerInfo) TemplateLoader.getLoader().getETLObjectTemplate(2, schedulerId);
		ScheduledTask firstScheduledTask = null;
		ScheduledJob sjob = null;
		Map jobsMap = null;
		if (sdinfo != null) {
			jobsMap = sdinfo.getJobsMap();
			sjob = jobsMap == null ? null : (ScheduledJob) jobsMap.get(jobid);
			if(sjob!=null){
				firstScheduledTask = sjob.getHeadTask();
			}
		}
		if(firstScheduledTask==null){
			throw new Exception("工作单元中未定义任务，或任务定义解析失败！");
		}
		String tid = firstScheduledTask.getTaskId();
		Task task = (Task)TemplateLoader.getLoader().getETLObjectTemplate(0,tid);
		if(task==null){
			throw new Exception("手工执行工作单元时，未找到指定的任务"+tid);
		}
		//解析参数，参数名值对值放入paramsVals中。
		Map mparams = sjob.getParamMap();
		List params = sjob.getParameters();
		Map paramVals = new HashMap();
		JSONObject jparams = null;
		try{
			jparams = new JSONObject(strParams);
		}catch(Exception e){
		}
		if(params!=null){
			for(int i=0;i<params.size();i++){
				Parameter pa = (Parameter)params.get(i);
				String v = "";
				try{
					v = jparams.getString(pa.getName());
				}catch(Exception e){
				}
				paramVals.put(pa.getName(), v);
			}
		}
		CollectionJob cjob = new CollectionJob();
		cjob.excuteTasks(task,mparams,paramVals,firstScheduledTask,1);
		return true;
	}
	
	public boolean executeTask(String schedulerId,String jobid,String tid, String strParams)throws Exception{
		SchedulerInfo sdinfo = (SchedulerInfo) TemplateLoader.getLoader().getETLObjectTemplate(2, schedulerId);
		ScheduledJob sjob = null;
		Map jobsMap = null;
		if (sdinfo != null) {
			jobsMap = sdinfo.getJobsMap();
			sjob = jobsMap == null ? null : (ScheduledJob) jobsMap.get(jobid);
		}
		Task task = (Task)TemplateLoader.getLoader().getETLObjectTemplate(0,tid);
		if(task==null){
			throw new Exception("手工执行任务时，未找到指定的任务"+tid);
		}
		Map paramVals = new HashMap();
		//解析参数，参数名值对值放入paramsVals中。
		Map mparams = sjob.getParamMap();
		List params = sjob.getParameters();
		JSONObject jparams = null;
		try{
			jparams = new JSONObject(strParams);
		}catch(Exception e){
		}
		if(params!=null){
			for(int i=0;i<params.size();i++){
				Parameter pa = (Parameter)params.get(i);
				String v = "";
				try{
					v = jparams.getString(pa.getName());
				}catch(Exception e){
				}
				paramVals.put(pa.getName(), v);
			}
		}
		boolean done = excuteSingleTask(task,mparams,paramVals);
		return done;
	}
	//手工执行指定任务，不执行后续任务。
	private boolean excuteSingleTask(Task task,Map mparams,Map paramVals){
		int flag = 0;
		if (task instanceof Extract) {
			Extract et = (Extract) task;
			IExtract expSvc = new DBExtractService();
			flag = expSvc.extractToFile(et,mparams,paramVals);
		} else if (task instanceof Import) {
			Import imp = (Import) task;
			IImport ipSvc = new DBImportService();
			flag =ipSvc.importData(imp,mparams,paramVals);
		} else if (task instanceof Execute) {
			Execute exe = (Execute) task;
			IExecute exeSvc = new DBExecuteService();
			flag =exeSvc.doExecute(exe,mparams,paramVals);
		}else if(task instanceof FtpTransfer){
			FtpTransfer ft = (FtpTransfer) task;
			FtpService exeSvc = new FtpService();
			flag = 0;
			if(ft!=null&&ft.getIsUpload()==0){
				flag = exeSvc.download(ft,mparams,paramVals);
			}else{
				flag = exeSvc.upload(ft,mparams,paramVals);
			}
		}else if(task instanceof Zip){
			Zip zip = (Zip) task;
			ZipService zSvc = new ZipService();
			flag = zSvc.zip(zip, mparams, paramVals);
		}else if(task instanceof Unzip){
			Unzip unzip = (Unzip) task;
			ZipService zSvc = new ZipService();
			flag = zSvc.unzip(unzip, mparams, paramVals);
		}
		if(flag!=1){
			return false;
		}
		return true;
	}
}
