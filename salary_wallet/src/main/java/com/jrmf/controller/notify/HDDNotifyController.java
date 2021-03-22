package com.jrmf.controller.notify;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HDDNotifyController {
	
	private static Logger logger = LoggerFactory.getLogger(HDDNotifyController.class);
	
	@RequestMapping("/hddNotify")
	public String zxNotify(HttpServletRequest request){
        return "success";
	}

}
