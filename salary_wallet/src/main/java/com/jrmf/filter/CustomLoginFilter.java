package com.jrmf.filter;

import com.alibaba.fastjson.JSON;
import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.OperateRecord;
import com.jrmf.domain.OperateURL;
import com.jrmf.interceptor.UserThreadLocal;
import com.jrmf.service.OperateRecordService;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.ParamSignTool;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.threadpool.ThreadUtil;
import java.util.Objects;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Author Nicholas-zehui Description //TODO 系统session拦截器 Date 16:39 2018/11/28
 * Param return
 * @author jrmf
 **/
@Component
@WebFilter(urlPatterns = {"/*"}, filterName = "customLoginFilter")
public class CustomLoginFilter implements Filter {

    private final static String PROCESS = "process";

    @Autowired
    private ApplicationArguments applicationArguments;
    @Autowired
    private OperateRecordService operateRecordService;

    private static Logger logger = LoggerFactory.getLogger(CustomLoginFilter.class);


    @Override
    public void destroy() {
    }

    private AntPathMatcher urlMatcher = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
        throws IOException, ServletException {


        HttpServletRequest request = (HttpServletRequest) arg0;
        HttpServletResponse response = (HttpServletResponse) arg1;


        MDC.put(PROCESS, java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());

        String uri = request.getRequestURI();
        String path = uri.replaceFirst(request.getContextPath(), "");
        if(path.startsWith("/swagger-ui.html") || path.startsWith("/webjars/springfox-swagger-ui/") || path.startsWith("/swagger-resources") || path.equals("/v2/api-docs") || path.equals("/csrf")){
            arg2.doFilter(arg0, arg1);
            return;
        }
        if (path.startsWith("/merchant/account/rechargeletter/esign")){
            arg2.doFilter(arg0, arg1);
            return;
        }

        if(path.startsWith("/api/individual")){
            arg2.doFilter(new EbHttpServletWrapper(request), response);
            return;
        }

        // 设置request和response的字符集，防止乱码p
        response.setHeader("P3P", "CP='IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT'");
        String paramStr = ParamSignTool.getReqParams2JsonStr(request);
        ThreadUtil.pdfThreadPool.execute(new Thread(() -> {
            OperateURL operateURL = operateRecordService.getOperateURL(path);
            if (operateURL != null) {
                OperateRecord operateRecord = new OperateRecord();
                operateRecord.setMethodName(operateURL.getName());
                operateRecord.setUrl(operateURL.getUrl());
                Object obj = request.getSession().getAttribute("customLogin");
                if (obj != null) {
                    operateRecord.setOperator(((ChannelCustom) obj).getUsername());
                    operateRecord.setParameter(paramStr);
                    operateRecordService.addOperateRecord(operateRecord);
                }
            }
        }));

        boolean timeCheck = path.contains("/settlement/") || path.contains("/summaryInfoByMerchant") || path.contains("/exportSummaryMerchant");
        if(path.contains("businessmanager/monthStatistics") || path.contains("/month")) {
            timeCheck = false;
        }
        if (timeCheck) {
            request.getParameter("startTime");
            long day = DateUtils.dateDiffByDay(request.getParameter("startTime"), request.getParameter("endTime"), "yyyy-MM-dd HH:mm:ss");
            if (day > 100) {
                returnRes(request,response, RespCode.MORE_THAN_100_DAY, new HashMap<>(8));
                return;
            }
        }


        boolean allowedPath = path.contains("/rest/") || path.contains("/api/") || path.contains("/batch/")
            || path.contains("/util/") || path.contains("/download.do")|| path.contains("/yuncr/upload/");
        if (allowedPath) {

            boolean checkFlag = applicationArguments.containsOption("checking");
            if (checkFlag) {
                logger.error("请求被拦截---checkFlag:" + checkFlag);

                Map<String, Object> model = new HashMap<>(10);
                returnRes(request,response, RespCode.error107, model);
                return;
            }

            request.getSession().setAttribute("currentPath", path);
            arg2.doFilter(arg0, arg1);
        } else {
            logger.info("请求开始：当前请求url：" + path + ",当前请求参数" + paramStr);
            Map<String, Object> model = new HashMap<>(10);
            if (!urlMatcher.match("/innerLogin.do", path) && !urlMatcher.match("/checkLogin.do", path)
                && !urlMatcher.match("/test/websocket", path) && !urlMatcher.match("/wechat/public/*", path)
                && !urlMatcher.match("/user/userLoginOut.do", path) && !urlMatcher.match("/code.do", path)
                && !urlMatcher.match("/user/login.do", path)
                && !urlMatcher.match("/custom/add.do", path)
                && !urlMatcher.match("/wallet/subscriber/bestsign/login.do", path)
                && !urlMatcher.match("/wallet/subscriber/bestsign/login", path)
                && !urlMatcher.match("/wallet/subscriber/bestsign/signSuccess.do", path)
                && !urlMatcher.match("/wallet/subscriber/bestsign/receive/picture_check/result.do", path)
                && !urlMatcher.match("/sign/agreement/login.do", path)
                && !urlMatcher.match("/sign/agreement/login", path)
                && !urlMatcher.match("/MP_verify_pL49clnh9NCIvCjJ.txt", path)
                && !urlMatcher.match("/userLogin.do", path) && !urlMatcher.match("/channel/payment/*", path)
                && !urlMatcher.match("/wechat/*", path)
                && !urlMatcher.match("/websocket/*", path)
                && !urlMatcher.match("/oem.do", path)
                && !urlMatcher.match("/oem/cache.do", path)
                && !urlMatcher.match("/umf/redownload.do", path)
                && !urlMatcher.match("/receipt/receiptAsyncNotify.do", path)
                && !urlMatcher.match("/domanual/receipt/*", path)
                && !urlMatcher.match("/task/autogenerateTask.do", path)
                && !urlMatcher.match("/pingansub/receiveTransferRecord.do", path)
                && !urlMatcher.match("/mybanksub/receiveTransferRecord.do", path)
                && !urlMatcher.match("/user/forgetPassword.do", path)
                && !urlMatcher.match("/user/sendCode2.do", path)
                && !urlMatcher.match("/littleBee/user/two-elements-check.do", path)
                && !urlMatcher.match("/user/validMobileCodeAndForgetPassword.do", path)
                && !urlMatcher.match("/userRegisterRecord/insertUserRecord.do", path)
                && !urlMatcher.match("/userRegisterRecord/pageVisitRecord.do", path)
                && !urlMatcher.match("/zxNotify.do", path)
                && !urlMatcher.match("/hddNotify.do", path)
                && !urlMatcher.match("/newPayNotify.do", path)
                && !urlMatcher.match("/ymshNotify.do", path)
                && !urlMatcher.match("/manage/invoice/list.do", path)
                && !urlMatcher.match("/manage/invoice/list/export.do", path)
                && !urlMatcher.match("/manage/invoice/upload.do", path)
                && !urlMatcher.match("/manage/invoice/set/fail.do", path)
                && !urlMatcher.match("/user/queryCurrentLoginStatus.do", path)
                && !urlMatcher.match("//swagger-ui.html", path)
                && !urlMatcher.match("/favicon.ico", path)) {
//            	ChannelCustom channelCustom = new ChannelCustom();
//            	channelCustom.setCustomkey("mfkj");
//            	channelCustom.setUsername("mfkj");
//            	channelCustom.setCustomType(1);
//            	request.getSession().setAttribute("customLogin", channelCustom);
                Object obj = request.getSession().getAttribute("customLogin");

                if (null == obj) {
                    returnRes(request,response, RespCode.error306, model);
                    return;
                }

                UserThreadLocal.setLocalUser(obj);
            }

            // 商户配置等重要权限，只有MFKJ账号可以操作
//            if (urlMatcher.match("/wallet/config/custom/**", path) || urlMatcher.match("/wallet/config/review/**", path)
//                || urlMatcher.match("/wallet/config/review/**", path)) {
//                String customKey = (String) request.getSession().getAttribute("customkey");
//
//                if (!CommonString.ROOT.equals(customKey) || CustomType.PLATFORM.getCode() != UserThreadLocal.getLocalUser().getCustomType()) {
//                    returnRes(request,response, RespCode.error129, model);
//                    return;
//                }
//            }
            request.getSession().setAttribute("currentPath", path);
            arg2.doFilter(arg0, arg1);
        }

    }

    /**
     * 说明:拦截器修改response,返回错误信息。
     * @throws IOException:
     */
    private void returnRes(HttpServletRequest request, HttpServletResponse response, int state, Map<String, Object> model) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        model.put(RespCode.RESP_STAT, state);
        model.put(RespCode.RESP_MSG, RespCode.codeMaps.get(state));
        JSONObject jsonMap = JSONObject.fromObject(model);
        response.getWriter().println(jsonMap.toString());
        logger.info("请求错误：请求被拦截"+request.getRequestURI());
        MDC.remove(PROCESS);
    }
}
