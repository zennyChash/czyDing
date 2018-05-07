package com.ifugle.czy.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ifugle.czy.service.ReportDataService;
import com.ifugle.czy.utils.JResponse;

@Controller
@RequestMapping("/queryData")
public class QueryDataController {
	@Autowired
	private ReportDataService rptDataService;

	@RequestMapping("/dzjk")
	@ResponseBody
	public JResponse getDzjk(@RequestParam("rptParams") String params){
		Map data = rptDataService.getData("dzjk",params);
		JResponse jr = new JResponse("0","",data);
		return jr;
	}
}
