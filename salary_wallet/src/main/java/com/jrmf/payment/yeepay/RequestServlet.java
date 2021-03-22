//package com.jrmf.payment.yeepay;
//
//import java.io.IOException;
//import java.util.Map;
//
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import com.alibaba.fastjson.JSONObject;
//import com.jrmf.domain.YeePayLog;
//import com.jrmf.persistence.YeePayDao;
//import com.jrmf.utils.SalaryConfigUtil;
//
//@WebServlet(urlPatterns = "/rest/*")
//public class RequestServlet extends HttpServlet {
//
//	private static final long serialVersionUID = 1445249247093622828L;
//
//	private final static Logger logger = LoggerFactory.getLogger(RequestServlet.class);
//
//	@Autowired
//	YeePayDao yeePayDao;
//	@Autowired
//	private SalaryConfigUtil conf;
//
//	@Override
//	protected void doGet(HttpServletRequest request,
//			HttpServletResponse response) throws ServletException, IOException {
//		super.doPost(request, response);
//	}
//
//	@Override
//	protected void doPost(HttpServletRequest request, HttpServletResponse response){
//
//		String responseMsg = "";
//		try {
//
//			request.setCharacterEncoding(conf.getEncode());
//
//			String baseUri = conf.getYeePayUrl();
//			String requestUri = request.getRequestURI();
//			String appkey = request.getHeader("x-yop-appkey");
//			String requestNo = request.getParameter("requestNo");
//			String requestUrl = baseUri + requestUri;
//			Map<String, String[]> parameterMap = request.getParameterMap();
//			String requestMsg = JSONObject.toJSONString(parameterMap);
//			logger.info("---start--forward---appkey:"
//					+ appkey
//					+ "--requestUri:"
//					+ requestUri
//					+ "--requestNo:"
//					+ requestNo
//					+ "--requestMsg:"
//					+ requestMsg);
//
//			YeePayLog yeePayLog = new YeePayLog(appkey,
//					requestNo,
//					requestUri,
//					requestMsg);
//
//			yeePayDao.saveYeePayRequestLog(yeePayLog);
//			responseMsg = HttpClientSendUtil.sendHttpClientPost(request, requestUrl, conf.getEncode());
//			logger.info("---end----forward---:" + responseMsg);
//			if(!requestUri.contains("accountingCheck/checkFileDownload")){
//				yeePayLog.setResponseMsg(responseMsg);
//				yeePayDao.updateYeePayResponseLog(yeePayLog);
//			}
//
//			response.setHeader("Content-type", "text/html;charset=UTF-8");
//			response.setCharacterEncoding(conf.getEncode());
//			response.getWriter().write(responseMsg);
//			response.getWriter().flush();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.info("---end----forward--exception-:" + e.getMessage());
//		}
//
//	}
//
//}
