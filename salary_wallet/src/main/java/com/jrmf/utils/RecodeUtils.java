package com.jrmf.utils;

import java.util.HashMap;
import java.util.Map;

public class RecodeUtils {
	
	public static final String SUCC_0000 = "0000";
	public static final String RESP_STAT = "respstat";
	public static final String RESP_MSG = "respmsg";
	public static final String RESP_OPENUID = "openuid";
	public static final String U_0001 = "U0001";
	public static final String U_0002 = "U0002";
	public static final String U_0003 = "U0003";
	public static final String U_0004 = "U0004";
	public static final String U_0005 = "U0005";
	public static final String U_0006 = "U0006";
	public static final String U_0007 = "U0007";
	public static final String U_0008 = "U0008";
	public static final String U_0009 = "U0009";
	public static final String U_0010 = "U0010";
	public static final String U_0011 = "U0011";
	public static final String U_0014 = "U0014";
	public static final String U_0015 = "U0015";
	public static final String U_0016 = "U0016";
	public static final String U_0017 = "U0017";
 
	public static final String U_0018 = "U0018";
 
	public static final String U_0019 = "U0019";
	public static final String U_0020 = "U0020";
	public static final String U_0021 = "U0021";
	public static final String U_0022 = "U0022";
	public static final String U_0023 = "U0023";
	public static final String U_0024 = "U0024";
	public static final String U_0025 = "U0025";
	public static final String U_0026 = "U0026";
	public static final String U_0027 = "U0027";
	public static final String U_0028 = "U0028";
	public static final String U_0029 = "U0029";
	public static final String U_0030 = "U0030";
	public static final String U_0031 = "U0031";
	public static final String U_0032 = "U0032";
	public static final String U_0033 = "U0033";
	public static final String U_0035 = "U0035";
	public static final String U_0036 = "U0036";
	public static final String U_0037 = "U0037";
	public static final String U_0038 = "U0038";
	public static final String U_0039 = "U0039";
	public static final String U_0040 = "U0040";
	public static final String U_0041 = "U0041";
	public static final String U_0042 = "U0042";
	public static final String U_0043 = "U0043";
	public static final String U_0044 = "U0044";
	public static final String U_0045 = "U0045";
	public static final String U_0046 = "U0046";
	public static final String U_0047 = "U0047";
	public static final String U_0048 = "U0048";
	
	
	public static final String A_0001 = "A0001";
	public static final String A_0002 = "A0002";
	public static final String A_0003 = "A0003";
	public static final String A_0004 = "A0004";

	public static final String B_C001 = "BC001";
	public static final String B_C002 = "BC002";
	public static final String C_K001 = "CK001";
	public static final String U_B001 = "UB001";
	public static final String U_B002 = "UB002";
	public static final String U_B003 = "UB003";
	public static final String U_B005 = "UB005";
	public static final String U_B006 = "UB006";

 
	public static final String T_P001 = "TP001";
	public static final String T_P002 = "R0012";
	public static final String T_P003 = "TP003";
	public static final String T_P004 = "TP004";
	public static final String T_P005 = "TP005";
	public static final String T_P006 = "TP006";
	public static final String T_P007 = "TP007";
	public static final String T_P008 = "TP008";
	public static final String T_P009 = "TP009";
	public static final String T_P010 = "TP010";
	public static final String T_P011 = "TP011";
	public static final String T_P012 = "TP012";
	public static final String T_P013 = "TP013";
	public static final String T_P105 = "TP105";

	
	public static final String T_0001 = "T0001";
	public static final String T_0002 = "T0002";
	public static final String T_0003 = "T0003";
	
	public static final String T_0009 = "T0009";
	public static final String T_0010 = "T0010";
	public static final String T_0011 = "T0011";
	public static final String T_0012 = "T0012";
	public static final String T_0013 = "T0013";
	public static final String T_0014 = "T0014";
	public static final String T_0015 = "T0015";
	public static final String T_0016 = "T0016";
	public static final String T_0017 = "T0017";
	public static final String T_0018 = "T0018";
	public static final String T_0019 = "T0019";
	public static final String T_0020 = "T0020";
	public static final String T_0021 = "T0021";
	public static final String T_0022 = "T0022";
	public static final String T_0023 = "T0023";
	public static final String T_0024 = "T0024";
	public static final String T_0025 = "T0025";
	public static final String T_0026 = "T0026";
	public static final String T_0027 = "T0027";
	public static final String T_0099 = "T0099";// 返回错误信息可变
	
	
	public static final String C_0001 = "C0001";
	public static final String S_0001 = "S0001";
	public static final String S_0002 = "S0002";
	public static final String S_0003 = "S0003";
	public static final String S_0004 = "S0004";
	public static final String S_0005 = "S0005";
	public static final String S_0006 = "S0006";
	public static final String S_0007 = "S0007";
	public static final String S_0008 = "S0008";
	public static final String S_0009 = "S0009";
	public static final String S_0010 = "S0010";
	public static final String S_0011 = "S0011";
	public static final String S_0012 = "S0012";
	public static final String S_0099 = "S0099";
	
	public static final String E_R001 = "ER001";
	public static final String E_R002 = "ER002";
	public static final String E_R003 = "ER003";
	public static final String E_R004 = "ER004";
	public static final String E_R005 = "ER005";
	public static final String E_R006 = "ER006";
	public static final String E_R007 = "ER007";
	public static final String E_R008 = "ER008";
	public static final String E_R009 = "ER009";
	public static final String E_R010 = "ER010";
	public static final String E_R011 = "ER011";
	public static final String E_R012 = "ER012";
	public static final String E_R013 = "ER013";
	public static final String E_R014 = "ER014";
	public static final String E_R015 = "ER015";
	public static final String E_R016 = "ER016";
	public static final String E_R017 = "ER017";
	public static final String E_R018 = "ER018";
	public static final String E_R019 = "ER019";
	public static final String E_R020 = "ER020";
	public static final String E_R021 = "ER021";
	public static final String E_R022 = "ER022";
	public static final String E_R023 = "ER023";
	public static final String E_R024 = "ER024";
	public static final String E_R025 = "ER025";
	public static final String E_R026 = "ER026";
	public static final String E_R027 = "ER027";
	public static final String R_0015 = "R0015";
	public static final String R_0016 = "R0016";
	public static final String R_0017 = "R0017";
	public static final String R_0018 = "R0018";
	public static final String R_0019 = "R0019";
	public static final String R_0020 = "R0020";
	public static final String R_0021 = "R0021";
	public static final String R_S011 = "RS011";
	
	public static final String Y_0001 = "Y0001";
	public static final String Y_0002 = "Y0002";
	public static final String Y_0003 = "Y0003";
	public static final String Y_0004 = "Y0004";
	
	public static final String Pay_0002 = "Pay0002";// //可以自定义异常
	public static final String I_0001 = "I0001";
	//
	public static final Map<String, String> codeMaps = new HashMap<String, String>();
	static {
		codeMaps.put(SUCC_0000, "成功");
		codeMaps.put(U_0001, "用户不存在");
		codeMaps.put(U_0002, "用户未完善身份信息");
		codeMaps.put(U_0003, "姓名信息未验证通过");
		codeMaps.put(U_0004, "身份证信息未验证通过");
		codeMaps.put(U_0005, "手机号未验证通过");
		codeMaps.put(U_0006, "用户真实姓名不能为空");
		codeMaps.put(U_0007, "身份证信息已经被占用");
		codeMaps.put(U_0008, "用户开户成功");
		codeMaps.put(U_0009, "已经完成实名认证");
		codeMaps.put(U_0010, "手机号已占用");
		codeMaps.put(U_0011, "请输入正确的手机号");
		codeMaps.put(U_0014, "短信验证码不能为空");
		codeMaps.put(U_0015, "验证码有误");
		codeMaps.put(U_0016, "验证码过期");
		codeMaps.put(U_0017, "用户已存在");
		codeMaps.put(U_0021, "短信发送频繁，请稍后再试");
		codeMaps.put(U_0022, "短信发送异常，请稍后再试");
		codeMaps.put(U_0023, "今天此手机号码已接受十次短信校验码,请更换手机号码或明天再试。");
		codeMaps.put(U_0024, "用户登录已失效");
		codeMaps.put(U_0025, "已完成身份校验不能修改个人信息");
		codeMaps.put(U_0026, "省信息不能为空");
		codeMaps.put(U_0027, "城市信息不能为空");
		codeMaps.put(U_0028, "当前渠道单日提现次数已超上限");
		codeMaps.put(U_0029, "支行不信息能为空");
		codeMaps.put(U_0030, "图形验证码不能为空");
		codeMaps.put(U_0031, "图形验证码不正确");
		codeMaps.put(U_0032, "账户异常，账户类型不允许此操作");
		codeMaps.put(U_0037, "付款账户异常，不存在的账户");
		codeMaps.put(U_0038, "收款账户异常，不存在的账户");
		codeMaps.put(U_0036, "该银行卡已绑定，请勿重复操作");
		codeMaps.put(U_0039, "不支持账户类型");
		codeMaps.put(U_0035, "不存在的渠道账户");
		codeMaps.put(U_0047, "收款账户未激活请先激活账户");
		codeMaps.put(U_0048, "付款账户未激活请先激活账户");
 
		codeMaps.put(U_0018, "登录密码错误");
		codeMaps.put(U_0019, "用户信息不匹配");
		codeMaps.put(U_0020, "预留手机号码不正确");
		codeMaps.put(U_0033, "单笔限额50000元");
		codeMaps.put(U_0040, "该银行卡已绑定，请勿重复操作");
		codeMaps.put(U_0041, "渠道信息为空");
		codeMaps.put(U_0042, "返回值信息异常");
		codeMaps.put(U_0043, "接收账户唯一标识不能为空");
		codeMaps.put(U_0044, "未完成实名认证");
		codeMaps.put(U_0045, "银行卡绑定失败，请重新尝试或换一张卡");
		codeMaps.put(U_0046, "激活账户需要设置交易密码");
		
		codeMaps.put(A_0001, "令牌无效");
		codeMaps.put(A_0002, "令牌不能为空");
		codeMaps.put(A_0003, "手机令牌无效");
		codeMaps.put(A_0004, "手机令牌不能为空");
		
		codeMaps.put(B_C001, "暂不支持信用卡");
		codeMaps.put(B_C002, "暂不支持此类银行卡");
		
		codeMaps.put(C_K001, "渠道用户唯一标识不为空");
		
		codeMaps.put(U_B001, "银行卡信息不能为空");
		codeMaps.put(U_B002, "银行卡信息未验证通过");
		codeMaps.put(U_B003, "请使用自己银行卡");
		codeMaps.put(U_B005, "当前银行卡已被占用");
		codeMaps.put(U_B006, "暂无支持银行");

		codeMaps.put(T_P001, "支付密码不能为空");
		codeMaps.put(T_P002, "支付密码错误");
		codeMaps.put(T_P003, "密码格式错误");
		codeMaps.put(T_P004, "还未绑卡,不可设置支付密码");
		codeMaps.put(T_P005, "密码已经设置成功，不可重复设置");
		codeMaps.put(T_P006, "用户未设置支付密码");
		codeMaps.put(T_P007, "支付密码为6为数字");
		codeMaps.put(T_P008, "原支付密码错误");
		codeMaps.put(T_P009, "原支付密码不能为空");
		codeMaps.put(T_P010, "支付密码输入不正确，已错误3次，请点击重置密码进行找回或3小时后重试");
		codeMaps.put(T_P011, "退款中,请稍后");
		codeMaps.put(T_P012, "支付密码输入不正确，已错误5次，请点击重置密码进行找回或24小时后重试");
		codeMaps.put(T_P013, "每日提现次数为3次，已提现3次，请24小时后重试");
		codeMaps.put(T_P105, "订单号重复");

		codeMaps.put(C_0001, "暂无数据");
		
		
		codeMaps.put(T_0001, "您今日提现3次，已达上限");
		codeMaps.put(T_0002, "单笔最小提现额度为10元");
		
		
		//可变的超过提款上限提示。在封装的返回信息中会包含已提款金额。
		codeMaps.put(T_0003, "单日提现上限5000元");
		
		//可变的余额不足提示信息。在封装的返回信息中会包含余额。
		codeMaps.put(T_0009, "余额不足");
		codeMaps.put(T_0010, "不允许此操作");
		codeMaps.put(T_0011, "已经超过退款最大期限，不允许此操作");
		codeMaps.put(T_0012, "订单金额不符，不允许此操作");
		codeMaps.put(T_0013, "订单状态不符，不允许此操作");
		codeMaps.put(T_0014, "订单交易渠道不符，不允许此操作");
		codeMaps.put(T_0015, "收付款不可以是同一人");
		codeMaps.put(T_0016, "用户类型不允许此操作");
		codeMaps.put(T_0017, "退款用户不是订单用户不允许此操作");
		codeMaps.put(T_0018, "单个红包金额超过最大限额");
		codeMaps.put(T_0019, "微信支付异常请使用其他支付方式尝试");
		codeMaps.put(T_0020, "单笔最小充值金额为N元");
		codeMaps.put(T_0021, "充值失败");
		codeMaps.put(T_0022, "单笔最小提现额度为N");
		codeMaps.put(T_0023, "单日最大提现额度为N元");
		codeMaps.put(T_0024, "您今日提现M元，单日提现上限N元");
		codeMaps.put(T_0025, "可提现额度不足,最多可提现N元");
		codeMaps.put(T_0026, "当日无手续费提现额度已满，请使用普通提现");
		codeMaps.put(T_0027, "当前批次号已经做过退款操作，请通过退款查询接口查询信息");
		codeMaps.put(T_0099, "交易异常");
		
		
		
		codeMaps.put(S_0001, "请求验证不通过");
		codeMaps.put(S_0002, "非法请求");
		codeMaps.put(S_0003, "请求参数不全");
		codeMaps.put(S_0004, "请求参数异常");
		codeMaps.put(S_0005, "请输入正确金额");
		codeMaps.put(S_0006, "渠道账户异常");
		codeMaps.put(S_0007, "不支持业务类型");
		codeMaps.put(S_0008, "金额计算笔例有误");
		codeMaps.put(S_0009, "当前渠道未开通此功能");
		codeMaps.put(S_0010, "单笔最大金额不超过20000");
		codeMaps.put(S_0011, "验证不通过");
		codeMaps.put(S_0012, "商户余额存量不足！");
		codeMaps.put(S_0099, "请求异常，请稍后重试");
		

		codeMaps.put(E_R001, "单个红包金额不可低于0.01元");
		codeMaps.put(E_R002, "红包类型不能为空");
		codeMaps.put(E_R003, "红包数量不正确");
		codeMaps.put(E_R004, "支付方式不能为空");
		codeMaps.put(E_R005, "银行卡信息不能为空");
		codeMaps.put(E_R006, "红包唯一标识不能为空");
		codeMaps.put(E_R007, "账户余额不足");
		codeMaps.put(E_R008, "红包触发条件不能为空");
		codeMaps.put(E_R009, "设备唯一标识不能为空");
		codeMaps.put(E_R010, "红包已经被领完");
		codeMaps.put(E_R011, "发送红包异常");
		codeMaps.put(E_R012, "账户异常");
		codeMaps.put(E_R013, "该红包已支付");
		codeMaps.put(E_R014, "订单重复");
		codeMaps.put(E_R015, "订单号不能为空");
		codeMaps.put(E_R016, "当前订单已成功支付");
		codeMaps.put(E_R017, "订单不存在");
		codeMaps.put(E_R018, "订单异常");
		codeMaps.put(E_R019, "不支持支付方式");
		codeMaps.put(E_R020, "订单名称不能为空");
		codeMaps.put(E_R021, "收款人姓名不能为空");
		codeMaps.put(E_R022, "红包已失效");
		codeMaps.put(E_R023, "抢红包异常");
		codeMaps.put(E_R024, "银行卡验卡失败，请您尝试其他银行卡，或者添加新银行卡进行提现，如有疑问请拔打电话010-88312877-0000");
		codeMaps.put(E_R025, "红包发送失败");
		codeMaps.put(E_R026, "不支持设备类型");
		codeMaps.put(E_R027, "获取银行信息失败，请点击重试");
		codeMaps.put(R_0015, "您尚未绑定银行卡，累计支付金额已达上限，请绑定银行卡后再操作");
		codeMaps.put(R_0016, "当前银行卡未绑定成功，无法继续完成支付");
		codeMaps.put(R_0017, "单个红包金额不可超过200元");
		codeMaps.put(R_0018, "单个红包金额不可低于0.01元");
		codeMaps.put(R_0019, "当日发送红包次数已超限额，请联系管理员");
		codeMaps.put(R_0020, "单日累计发送上限M元，您还可以发送N元。");
		codeMaps.put(R_0021, "您今日此卡已发送N元红包，单张银行卡每天最多可发送M元");
		codeMaps.put(R_S011, "重复提交");
		codeMaps.put(Pay_0002, "chinaPay交易异常！");
		codeMaps.put(I_0001, "参数异常");
		
		codeMaps.put(Y_0001, "父渠道验证不通过");
		codeMaps.put(Y_0002, "子渠道未接入");
		codeMaps.put(Y_0003, "验签不通过");
		codeMaps.put(Y_0004, "请求参数不全");
	}
	
	public static Map<String, Object> getErrMap(String errorCode){
		Map<String, Object> errMap = new HashMap<String, Object>();
		errMap.put(RESP_STAT, errorCode);
		errMap.put(RESP_STAT, codeMaps.get(errorCode));
		return errMap;
	}
	
}
