package com.ifugle.czy.servlet;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.open.client.api.model.corp.CorpUserDetail;
import com.ifugle.czy.service.AuthDao;
import com.ifugle.czy.system.bean.User;

import java.io.IOException;
public class DingUserServlet extends HttpServlet{
	 protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String action = request.getParameter("doType");
		String destination = "/index.jsp";
		RequestDispatcher dispatcher;
		String msg = null;
		
		if("getDingConfig".equals(action)){
			try {
				String config = AuthDao.getConfig(request);
				StringBuffer ub = new StringBuffer("{\"retCode\":\"0\",\"retMsg\":\"\",\"retData\":");
				ub.append(config).append("}");
				System.out.println("DINGDING_CONFIG:" + ub.toString());
				response.setContentType("application/json;charset=utf-8");
				response.getWriter().append(ub.toString());
			}catch(Exception e) {
				StringBuffer emg = new StringBuffer("{\"retData\":null,retCode:\"1\",retMsg:");
				emg.append("\"获取钉钉配置信息时发生错误！\"}");
				e.printStackTrace();
	            response.getWriter().append(emg.toString());
	        }
		}else if("authUser".equals(action)){
			try {
				// 获取免登授权码
		        String code = request.getParameter("code");
		        String corpId = request.getParameter("corpid");
		        System.out.println("authCode:" + code + " corpid:" + corpId);
	            String accessToken = AuthDao.getAccessToken();
	            System.out.println("access token:" + accessToken);
	            //CorpUserDetail dUser = AuthDao.getUser(accessToken, AuthDao.getUserBaseInfo(accessToken, code).getUserid());
	            User user = AuthDao.getUserCzyConfig(accessToken,code);
	            //String userJson = JSON.toJSONString(user);
	            System.out.println("user.config:" + user.getConfig());
	            
	            JSONArray jmenu = JSON.parseObject(user.getConfig())==null?null:JSON.parseObject(user.getConfig()).getJSONArray("menus");
	            String menus = jmenu==null?"[]":jmenu.toJSONString();
	            System.out.println("menus from json:" + menus);
	            
	            StringBuffer ub = new StringBuffer("{\"retCode\":\"0\",\"retMsg\":\"\",\"retData\":{\"userid\":\"");
	            ub.append(user.getUserid()).append("\",\"username\":\"").append(user.getDingname()).append("\",\"menus\":").append(menus).append("}}");
	            String userJson = ub.toString();
	            
	            response.setContentType("application/json;charset=utf-8");
	            response.getWriter().append(userJson);
	            System.out.println("USERJSON:" + userJson);
			}catch(Exception e) {
				StringBuffer emg = new StringBuffer("{retData:null,retCode:\"1\",retMsg:");
				emg.append("\"钉钉免登验证时发生错误！\"}");
				e.printStackTrace();
	            response.getWriter().append(emg.toString());
	        }
		}else if("getChartData".equals(action)){
			try {
		        String chartID = request.getParameter("chartID");
	            JSONArray dataset = AuthDao.getChartDataSet(chartID);
	            String rps = AuthDao.buidResponsJson("0","",dataset);
	            response.setContentType("application/json;charset=utf-8");
	            response.getWriter().append(rps);
	            System.out.println("CHART DATASET:" + dataset);
			}catch(Exception e) {
				e.printStackTrace();
				String rps = AuthDao.buidResponsJson("1","获取图表数据时发生错误！",new JSONArray());
	            response.setContentType("application/json;charset=utf-8");
	            response.getWriter().append(rps);
	        }
		}
	 }
}
