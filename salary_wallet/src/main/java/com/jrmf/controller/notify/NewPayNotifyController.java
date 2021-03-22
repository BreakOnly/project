package com.jrmf.controller.notify;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NewPayNotifyController {

  @RequestMapping("/newPayNotify")
  public String newPayNotify(HttpServletRequest request) {
    return "200";
  }

}
