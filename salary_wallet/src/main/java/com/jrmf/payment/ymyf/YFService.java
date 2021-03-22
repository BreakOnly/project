package com.jrmf.payment.ymyf;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.jrmf.utils.FtpTool;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jrmf.payment.ympay.YmService;
import com.jrmf.payment.ymyf.entity.RequestMessage;
import com.jrmf.payment.ymyf.entity.ResultMessage;
import com.jrmf.payment.ymyf.util.Base64Utils;
import com.jrmf.payment.ymyf.util.DESUtils;
import com.jrmf.payment.ymyf.util.FileTool;
import com.jrmf.payment.ymyf.util.HttpClientHelper;
import com.jrmf.payment.ymyf.util.JsonUtils;
import com.jrmf.payment.ymyf.util.RSAUtils;
import com.jrmf.signContract.channel.AYGChannel;
import com.jrmf.utils.exception.NoBatchException;
import com.jrmf.utils.exception.YmyfHasSignException;
import com.jrmf.utils.exception.YmyfNormalExcepion;
import com.jrmf.utils.exception.YmyfVerfyException;

public class YFService {

	private static Logger logger = LoggerFactory.getLogger(YFService.class);

	//商户私钥
	private String privateKey;
	//商银信公钥
	private String publicKey;
	//商户代码
	private String merchant_id;
	//请求url
	private String url;
	//签名key
	private String md5Key;
	//请求参数
	private String reqInfo;

	private String charset ="UTF-8";


	public YFService() {
		super();
	}

	public YFService(String url, String merchant_id, String privateKey,
			String publicKey, String md5Key, String reqInfo,
			String charset) {
		super();
		this.url = url;
		this.merchant_id = merchant_id;
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.md5Key = md5Key;
		this.reqInfo = reqInfo;
		this.charset = charset;
	}

	/**
	 * 用户签约
	 * @throws Exception
	 */
	public String signContract(Integer uploadFlag,String backPicUrl,String frontPicUrl) throws Exception{
		String respInfo=reqYMYFSevice(reqInfo,YFConfig.SIGN_CONTRACT,backPicUrl,frontPicUrl,uploadFlag);
		return respInfo;
	}

	/**
	 * 用户签约查询
	 */
	public String signContractQuery()throws Exception{
		String respInfo=reqYMYFSevice(reqInfo,YFConfig.SIGN_CONTRACT_QUERY,"","",0);
		return respInfo;
	}

	/**
	 * 短信确认付款申请
	 */
	public String paymentConfirm()throws Exception{
		this.setUrl("http://api.youfupingtai.com/serverPay/common.do");
		String respInfo=reqYMYFSevice(reqInfo,YFConfig.PAYMENT_CONFIRM,"","",0);
		return respInfo;
	}

	/**
	 * 短信付款
	 * @throws Exception
	 */
	public String paymentSms() throws Exception{
		this.setUrl("http://api.youfupingtai.com/serverPay/confirmPay.do");
		String respInfo=reqYMYFSevice(reqInfo,YFConfig.PAYMENT_SMS,"","",0);
		return respInfo;
	}

	/**
	 * 直接付款
	 * @throws Exception
	 */
	public String payment() throws Exception{
		String respInfo=reqYMYFSevice(reqInfo,YFConfig.PAYMENT,"","",0);
		return respInfo;
	}

	/**
	 * 余额查询
	 * @throws Exception
	 */
	public String balanceQuery() throws Exception{
		String respInfo=reqYMYFSevice(reqInfo,YFConfig.BALANCE_QUERY,"","",0);
		return respInfo;
	}

	/**
	 * 可用额度查询
	 * @throws Exception
	 */
	public String accountQuery() throws Exception{
		String respInfo=reqYMYFSevice(reqInfo,YFConfig.ACCOUNT_QUERY,"","",0);
		return respInfo;
	}

	/**
	 * 付款结果查询
	 * @throws UnsupportedEncodingException
	 * @throws Exception
	 */
	public String paymentQuery() throws Exception {
		String respInfo=reqYMYFSevice(reqInfo,YFConfig.PAYMENT_QUERY,"","",0);
		return respInfo;
	}




	/**
	 * 请求溢美优付
	 * @param serviceType
	 * @return
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
	public String reqYMYFSevice(String reqInfo, String serviceType,String backPicUrl,String frontPicUrl,Integer uploadCert)
			throws Exception{
		RequestMessage rm = new RequestMessage();
		rm.setReqId("reqId" + System.currentTimeMillis());
		rm.setFunCode(serviceType);
		logger.info("溢美优付请求方法类型："+getServiceName(serviceType));
		logger.info("溢美优付请求地址："+url);
		logger.info("溢美优付商编："+merchant_id);
		rm.setMerId(merchant_id);
		rm.setVersion("V1.0");
		if(uploadCert==1){
			//需要上传证件图片信息
			if (StringUtil.isEmpty(backPicUrl) || StringUtil.isEmpty(frontPicUrl)){
				throw new YmyfNormalExcepion("需要上传证件照片");
			}
			String fileStringOn = FileTool.bytesToHexString(FtpTool.downloadFtpFile(backPicUrl.substring(0, backPicUrl.lastIndexOf("/")), backPicUrl.substring(backPicUrl.lastIndexOf("/") + 1)));//身份证正面
			String fileStringBack = FileTool.bytesToHexString(FtpTool.downloadFtpFile(frontPicUrl.substring(0, frontPicUrl.lastIndexOf("/")), frontPicUrl.substring(frontPicUrl.lastIndexOf("/") + 1)));//身份证反面
			rm.setRemark1(fileStringOn);
			rm.setRemark2(fileStringBack);
		}
		logger.info("溢美优付请求内容信息为："+reqInfo);
		byte[] bs = DESUtils.encrypt(reqInfo.getBytes(charset), md5Key);
		String reqDataEncrypt = Base64Utils.encode(bs);
		rm.setReqData(reqDataEncrypt);
		rm.setSign(RSAUtils.sign(reqDataEncrypt, privateKey));
		String reqStr=JsonUtils.toJson(rm);
		logger.info("溢美优付请求信息："+reqStr);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("reqJson", reqStr);
		String resData = HttpClientHelper.httpClientPost(url, map, charset);
		logger.info("溢美优付响应信息："+resData);
		ResultMessage resultMessage = JsonUtils.fromJson(resData,ResultMessage.class);
		byte[] base64bs = Base64Utils.decode(resultMessage.getResData());
		byte[] debs = DESUtils.decrypt(base64bs, md5Key);
		String detailData = new String(debs,charset);
		logger.info("溢美优付响应解密数据为："+detailData);
		if(resultMessage.getResCode().equals("0000")){
			boolean verifySign = RSAUtils.verify(resultMessage.getResData(), resultMessage.getSign(),this.publicKey);
			if(!verifySign){
//				throw new YmyfVerfyException("响应签名验证失败");
				logger.error("响应签名验证失败");
			}
		}else if(resultMessage.getResCode().equals("6032")){
			throw new NoBatchException("批次查询结果失败，原因："+resultMessage.getResMsg());
		}else if(resultMessage.getResCode().equals("6016")){
			throw new YmyfHasSignException("该用户信息已经做过签约");
		}else{
			throw new YmyfNormalExcepion("请求服务失败,原因："+resultMessage.getResMsg());
		}
		return detailData;
	}

	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public String getMerchant_id() {
		return merchant_id;
	}
	public void setMerchant_id(String merchant_id) {
		this.merchant_id = merchant_id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMd5Key() {
		return md5Key;
	}
	public void setMd5Key(String md5Key) {
		this.md5Key = md5Key;
	}
	public String reqInfo() {
		return reqInfo;
	}
	public void setParamsMap(String reqInfo) {
		this.reqInfo = reqInfo;
	}

	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getServiceName(String serviceType){
		String serviceName = "";
		if(serviceType.equals("6010")){
			serviceName = "签约";
		}else if(serviceType.equals("6011")){
			serviceName = "签约查询";
		}else if(serviceType.equals("6001")){
			serviceName = "短信付款确认";
		}else if(serviceType.equals("6001")){
			serviceName = "短信付款";
		}else if(serviceType.equals("6002")){
			serviceName = "付款结果查询";
		}else if(serviceType.equals("6001")){
			serviceName = "直接付款";
		}else if(serviceType.equals("6005")){
			serviceName = "余额查询";
		}else if(serviceType.equals("6003")){
			serviceName = "可用额度查询";
		}
		return serviceName;
	}

	public static void main(String[] args) {
		//1.初始化服务
		YFService yfService = new YFService("请求地址", "商编", "rsa私钥", "溢美优付公钥", "md5Key", "请求信息", "UTF-8");
		//调用相关功能
		try {
			//举例调用签约功能
			String respInfo = yfService.signContractQuery();
			//响应信息
			logger.info(respInfo);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

}
