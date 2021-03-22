package com.jrmf.controller.notify;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MybankNotifyController {
	
	private static Logger logger = LoggerFactory.getLogger(MybankNotifyController.class);
	
	@RequestMapping("/myBankNotify")
	public String zxNotify(HttpServletRequest request){
        return "success";
	}

}
