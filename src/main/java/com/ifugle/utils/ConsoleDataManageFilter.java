package com.ifugle.utils;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.ifugle.czy.utils.JResponse;

public class ConsoleDataManageFilter implements Filter{
    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response,FilterChain fchain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse resp =(HttpServletResponse) response;
        String ip = getClientIP(req);
        String path = req.getRequestURI();
        if(path.indexOf("/data/testWhiteIPS") > -1) {
        	System.out.println("czyDataManage访问来自IP："+ip);
        }
    	boolean allowed = false;
    	if(ip.indexOf("0:0:0:0:0:0:0:1")>=0||ip.indexOf("127.0.0.1")>=0){//本机始终可以访问
    		fchain.doFilter(req, resp);
    	}else {
    		String whiteIPS = Configuration.getConfig().getString("WHITE_IPS", "");
    		if (!StringUtils.isEmpty(whiteIPS)) {
        		allowed = IPWhiteListUtil.checkLoginIP(ip, whiteIPS);
	        	if(!allowed){
		        	JResponse jr = new JResponse("9","请求来自未经允许的IP，不能访问！",null);
		        	System.out.println("非法访问czyDataManage，来自IP："+ip);
		        	response.setContentType("application/json; charset=utf-8");
		            response.setCharacterEncoding("UTF-8");
		            String errJson = JSON.toJSONString(jr);
		            OutputStream out = response.getOutputStream();
		            out.write(errJson.getBytes("UTF-8"));
		            out.flush();
	        	}else{
	        		fchain.doFilter(req, resp);
	        	}
        	} else {
	        	JResponse jr = new JResponse("9","未配置IP白名单，不能访问！",null);
	        	System.out.println("czyDataManage未配置IP白名单，不能访问。");
	        	response.setContentType("application/json; charset=utf-8");
	            response.setCharacterEncoding("UTF-8");
	            String errJson = JSON.toJSONString(jr);
	            OutputStream out = response.getOutputStream();
	            out.write(errJson.getBytes("UTF-8"));
	            out.flush();
	        }
    	}
    }
    public void init(FilterConfig arg0) throws ServletException {
        // TODO Auto-generated method stub
    }
    
    private String getClientIP(HttpServletRequest request) {
    	String ip = request.getHeader("x-forwarded-for"); 
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {  
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if( ip.indexOf(",")!=-1 ){
                ip = ip.split(",")[0];
            }
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("WL-Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_CLIENT_IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("X-Real-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getRemoteAddr();  
        } 
        return ip;  
    }
}
