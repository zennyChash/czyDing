package com.ifugle.etl.task.mail.service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ifugle.etl.entity.task.SendMail;
import com.ifugle.etl.schedule.SchedulerUtils;

public class MailService {
	 public static final Logger log = LoggerFactory.getLogger(MailService.class); 
	 public int sendMail(SendMail task,Map params, Map paramVals){
    	if(task==null){
			log.error("任务配置错误：未配置发送邮件的相关信息！");
			return 9;
		}
    	int flag=0;
    	if (StringUtils.isEmpty(task.getHost())) {  
            log.error("邮箱服务器（SMTP）未设置！"); 
        	return 9;
        }
        if (StringUtils.isEmpty(task.getFrom())) {   
        	log.error("发送邮箱未设置！"); 
        	return 9;
        }  
        if (StringUtils.isEmpty(task.getDest())) {  
            log.error("目标邮箱未设置！"); 
        	return 9;
        }  
        try{
        	Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.host", task.getHost());
            final String username1 = task.getUsername();
            final String password1 = task.getPassword();
            Session session = Session.getInstance(props, 
                    new javax.mail.Authenticator(){
                 protected PasswordAuthentication getPasswordAuthentication() {
                       return new PasswordAuthentication(username1, password1);
               }
            });        
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(task.getFrom()));        
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(task.getDest()));
            message.setSubject(task.getSubject());
            Multipart multipart = new MimeMultipart();
            if (!StringUtils.isEmpty(task.getContent())){
            	log.info("邮件包含正文。"); 
                BodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(task.getContent(), "text/html;charset=UTF-8");
                multipart.addBodyPart(htmlPart);
            }
            if (task.getAttachments()!=null&&task.getAttachments().size()>0){
            	log.info("邮件包含附件，附件数量为：{}",task.getAttachments().size()); 
            	List atts = task.getAttachments();
                // 逐个加入附件  
                for (int i = 0; i< atts.size(); i++) {
                	String filePath= (String)atts.get(i);
                	filePath = SchedulerUtils.parseParamValue(filePath, paramVals);
                	BodyPart attchmentPart = new MimeBodyPart(); 
                	FileDataSource filedatasource = new FileDataSource(filePath);  
                	attchmentPart.setDataHandler(new DataHandler(filedatasource));  
                    try {  
                    	attchmentPart.setFileName(MimeUtility.encodeText(filedatasource.getName()));  
                    } catch (Exception e) {  
                        e.printStackTrace();  
                    }  
                    multipart.addBodyPart(attchmentPart);  
                }  
            }
            message.setContent(multipart);
            Transport.send(message);
        	flag=1;
        }catch(Exception e){
        	log.error(e.toString());
        	flag = 9;
        }
        return flag;
    }
}
