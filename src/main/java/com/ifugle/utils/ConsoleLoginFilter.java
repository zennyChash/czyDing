package com.ifugle.utils;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.ifugle.czyDing.utils.JResponse;

public class ConsoleLoginFilter implements Filter{
    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response,FilterChain fchain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse resp =(HttpServletResponse) response;
        
        // 获得用户请求的URI
        String path = req.getRequestURI();
        //应用管理后台跳转的首个请求不做过滤
        if(path.indexOf("/manage/consoleLogin") > -1) {
        	fchain.doFilter(req, resp);
            return;
        } else {
        	//从session取得免登后从钉钉取得的管理后台的accessToken
        	HttpSession session = req.getSession();
            String accessToken = (String) session.getAttribute("console_accessToken");
            if (StringUtils.isEmpty(accessToken)) {
            	JResponse jr = new JResponse("9","您未登录后台管理系统！",null);
            	response.setContentType("application/json; charset=utf-8");
                response.setCharacterEncoding("UTF-8");
                String errJson = JSON.toJSONString(jr);
                OutputStream out = response.getOutputStream();
                out.write(errJson.getBytes("UTF-8"));
                out.flush();
            } else {
            	fchain.doFilter(req, resp);
            }
        }
    }
    public void init(FilterConfig arg0) throws ServletException {
        // TODO Auto-generated method stub
    }
}
