package com.ifugle.utils;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.ifugle.czyDing.utils.JResponse;

public class CzyApiFilter  implements Filter{
    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response,FilterChain fchain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse resp =(HttpServletResponse) response;
        
        // 获得用户请求的URI
        String path = req.getRequestURI();
        //应用管理后台跳转的首个请求不做过滤
        if(path.indexOf("/api/getDingConfig")> -1||path.indexOf("/api/canAccessTo")>-1
        		||path.indexOf("/api/getMyMenus")>-1||path.indexOf("portalLogin")>-1) {
        	fchain.doFilter(req, resp);
            return;
        } else {
        	//从session取得免登后从钉钉取得的accessToken
        	HttpSession session = req.getSession();
            String accessToken = (String) session.getAttribute("accessToken");
            if (StringUtils.isEmpty(accessToken)) {
            	//2021-08-23 没有钉钉单点登录的，再检查是否财智云门户单点登录(czyPortalUserid)
            	String czyPortalUserid = (String) session.getAttribute("czyPortalUserid");
            	if(StringUtils.isEmpty(czyPortalUserid)){
	            	JResponse jr = new JResponse("9","您未登录系统！",null);
	            	response.setContentType("application/json; charset=utf-8");
	                response.setCharacterEncoding("UTF-8");
	                String errJson = JSON.toJSONString(jr);
	                OutputStream out = response.getOutputStream();
	                out.write(errJson.getBytes("UTF-8"));
	                out.flush();
            	}
            } else {
            	fchain.doFilter(req, resp);
            }
        }
    }
    public void init(FilterConfig arg0) throws ServletException {
        // TODO Auto-generated method stub
    }
}
