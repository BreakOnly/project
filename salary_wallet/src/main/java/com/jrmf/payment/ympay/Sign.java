package com.jrmf.payment.ympay;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.security.PublicKey;

/**
 * 溢美签名
 * @author 孙春辉
 *
 */
public class Sign {

    private static Logger logger = LogManager.getLogger(Sign.class);


    public static PublicKey publicKey = null;

    public static String signDate = null;

    public Sign() {}

    /**
     * RSA签名
     * @param srcMessage
     * @param privateKey
     * @return
     */
	public static String signByPriKey(String srcMessage,String privateKey){
		String b64SignMsg =null;
		try {
			b64SignMsg =RsaUtil.sign(srcMessage.getBytes("GBK"), privateKey,"SHA1withRSA");
			logger.info("签名结果为："+b64SignMsg);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return b64SignMsg;
	}
	
	/**
	 * RSA验证签名
	 * @param srcMessage
	 * @param resMessage
	 * @param publicKey
	 * @return
	 */
	public static boolean verfySignByPubKey(String srcMessage,String resMessage,String publicKey){
		try {
			boolean verify  =RsaUtil.verify(srcMessage.getBytes("GBK"), publicKey, "SHA1withRSA", resMessage);
			if (!verify) {
				return false;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return false;
		}
		return true;
	}

}
