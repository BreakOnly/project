package com.jrmf.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chonglulu
 */

public class RespCode {

  public static final String RESP_STAT = "state";
  public static final String RESP_MSG = "respmsg";
  public static final String RESULT = "result";
  /**
   * 成功
   */
  public static final int success = 1;
  /**
   * 银行返回错误信息
   */
  public static final int error000 = 0;
  /**
   * 参数异常
   */
  public static final int error101 = 101;
  /**
   * 公司不存在
   */
  public static final int error102 = 102;
  /**
   * 用户已存在
   */
  public static final int error103 = 103;
  /**
   * 手机号已存在
   */
  public static final int error104 = 104;

  /**
   * 用户不存在
   */
  public static final int error105 = 105;
  /**
   * 用户不存在
   */
  public static final int USER_NOT_FOUND = 105;
  /**
   * 用户账户异常
   */
  public static final int error106 = 106;
  /**
   * 网络错误
   */
  public static final int error107 = 107;
  /**
   * merchantId错误
   */
  public static final int error108 = 108;
  /**
   * ip错误
   */
  public static final int error109 = 109;

  /**
   * 时间戳超时
   */
  public static final int error110 = 110;
  /**
   * 时间戳非法
   */
  public static final int error111 = 111;
  /**
   * 签名不存在
   */
  public static final int error112 = 112;
  /**
   * sign不存在
   */
  public static final int error113 = 113;
  /**
   * 请求序列号不存在
   */
  public static final int error114 = 114;
  /**
   * 金额过大
   */
  public static final int error115 = 115;
  /**
   * 图片解析异常
   */
  public static final int error116 = 116;
  /**
   * 商户已存在
   */
  public static final int error117 = 117;
  /**
   * 商户审核中
   */
  public static final int error118 = 118;
  /**
   * 用户状态错误
   */
  public static final int error119 = 119;
  /**
   * 公司信息已经通过审核
   */
  public static final int error120 = 120;
  /**
   * 公司信息不存在
   */
  public static final int error121 = 121;
  /**
   * 银行卡不支持
   */
  public static final int error122 = 122;
  /**
   * 密码还没有设置
   */
  public static final int error123 = 123;
  /**
   * 输入金额错误
   */
  public static final int error124 = 124;
  /**
   * 银行卡未绑定
   */
  public static final int error125 = 125;
  /**
   * json数据异常
   */
  public static final int error126 = 126;
  /**
   * 更换账户唯一银行卡需要您的余额账户清零，请您余额提现成功后更换银行卡
   */
  public static final int error127 = 127;
  /**
   * 绑卡失败，已经绑定过银行卡
   */
  public static final int error128 = 128;
  /**
   * 非法登陆
   */
  public static final int error129 = 129;
  /**
   * 密码已存在
   */
  public static final int error130 = 130;
  /**
   * 用户已注册成功，无需重复注册
   */
  public static final int error131 = 131;
  /**
   * 订单号错误，查询不到此订单
   */
  public static final int error132 = 132;
  /**
   * 用户已签约合同
   */
  public static final int error133 = 133;
  /**
   * 用户已提交签约申请
   */
  public static final int error134 = 134;
  /**
   * 用户未签约
   */
  public static final int error135 = 135;
  /**
   * 渠道通知地址不存在
   */
  public static final int error136 = 136;
  /**
   * 手机号校验失败
   */
  public static final int error137 = 137;
  /**
   * 回调地址不能为空
   */
  public static final int error138 = 138;
  /**
   * 身份证验证失败
   */
  public static final int error139 = 139;
  /**
   * 总金额错误
   */
  public static final int error140 = 140;

  /**
   * 验证码错误
   */
  public static final int error141 = 141;
  /**
   * 验证码错误
   */
  public static final int CODE_VILID_ERROR = 141;
  /**
   * 薪资下发错误
   */
  public static final int error142 = 142;
  /**
   * 签约提交失败
   */
  public static final int error143 = 143;
  /**
   * 用户未实名开户
   */
  public static final int error144 = 144;
  /**
   * 用户未上传身份证
   */
  public static final int error145 = 145;
  /**
   * 图片过大
   */
  public static final int error146 = 146;
  /**
   * 商户薪资下发信息未配置
   */
  public static final int error201 = 201;
  /**
   * 批次信息不符合要求
   */
  public static final int error202 = 202;
  /**
   * 预存款金额不足
   */
  public static final int error203 = 203;
  /**
   * 爱员工用户信息授权失败
   */
  public static final int error204 = 204;

  public static final int error205 = 205;

  public static final int error301 = 301;
  public static final int error302 = 302;
  public static final int error303 = 303;
  public static final int error304 = 304;
  public static final int error305 = 305;
  public static final int error306 = 306;
  public static final int error600 = 600;
  public static final int error601 = 601;
  public static final int error602 = 602;
  public static final int SESSION_DESTROYED = 306;

  public static final int IMPORT_NUMBER_ERROR = 307;

  public static final String UNSUPPORT_LINE_NUM = "文件行数超出支持范围";

  public static final int ParamNotFound = 309;

  public static final int AGREEMENT_NOT_FOUND = 310;

  public static final int FILE_IOEXCEPTION = 311;

  public static final int USERNO_ALREADY_EXIST = 312;

  public static final int CHANNEL_RELATED_NOT_FOUND = 313;
  /**
   * 有下发记录或者  签约记录 不等于  创建或者失败
   */
  public static final int FOUND_USERCOMMISSION_OR_USERAGREEMENT = 314;
  /**
   * 您合作的商户目前尚未上传您的信息，请联系商户
   */
  public static final int ORIGIN_NOT_FOUND = 315;
  /**
   * 服务公司不存在
   */
  public static final int COMPANY_NOT_FOUND = 316;
  /**
   * 模板不存在
   */
  public static final int AGREEMENT_TEMPLATE_NOT_FOUND = 317;

  public static final int UNSUBSCRIBE = 318;

  /**
   * 商户下发限额配置不存在
   */
  public static final int PAYMENT_LIMITATION_NOT_FOUND = 319;
  /**
   * 商户下发限额配置存在
   */
  public static final int PAYMENT_LIMITATION_EXIST = 320;
  /**
   * 部分批次已经被审核或驳回
   */
  public static final int NOT_ALL_BATCHES_ARE_PENDING_APPROVAL = 321;
  /**
   * 没有符合权限
   */
  public static final int DO_NOT_HAVE_APPROVAL_RIGHT = 322;

  /**
   * 商户模板已存在
   */
  public static final int CUSTOM_TEMP_EXISTS = 325;

  /**
   * 没有符合条件的结果
   */
  public static final int DO_NOT_HAVE_MATCHING_RESULTS = 323;
  /**
   * 协议导出异常
   */
  public static final int AGREEMENT_EXPORT_ERROR = 324;

  /**
   * 此模板商户正使用
   */
  public static final int CUSTOM_TEMP_USE = 326;

  /**
   * 重发异常
   */
  public static final int PAY_EXCEPTION = 327;

  /**
   * 状态不支持
   */
  public static final int NOT_SUPPORT_STATUS = 328;

  /**
   * 查询失败
   */
  public static final int QUERY_FAIL = 329;

  /**
   * 联动重发前笔步骤订单不为成功
   */
  public static final int PRE_STATUS_ERROR = 330;

  /**
   * 同步状态异常
   */
  public static final int SURE_STATUS = 331;
  /**
   * 压缩包不存在
   */
  public static final int ZIP_FILE_NOT_FOUND = 332;

  /**
   * 业务状态不满足
   */
  public static final int BUSINESS_TYPE_NOT = 333;
  /**
   * 导入数据超过500条
   */
  public static final int IMPORT_NUMBER_ERROR_500 = 334;

  /**
   * 未勾选开票充值流水号
   */
  public static final int NOT_CHECK_RECHARGENO = 335;

  /**
   * 待开票余额应大于0
   */
  public static final int INVOICEAMOUNT_SHOULD_GT_ZERO = 336;

  /**
   * 多笔复合开票金额需待开票金额一致
   */
  public static final int INVOICE_AMOUNT_DIFFERENT = 337;

  /**
   * 开票金额超出待开票金额范围或待开票金额未大与0
   */
  public static final int INVOICE_AMOUNT_WRONG = 338;

  /**
   * 该笔订单已完成开票
   */
  public static final int INVOICE_COMPLETE = 339;

  /**
   * 商户开票申请异常
   */
  public static final int INVOICE_EXCEPTION = 340;

  /**
   * 该开票申请已受理
   */
  public static final int INVOICE_HANDLE = 341;

  /**
   * 部分开票，开票类目必须一致
   */
  public static final int INVOICE_ClASS_DIFFERENT = 342;

  /**
   * 多充值流水合并开票申请的必须是商户+下发公司全部一致
   */
  public static final int INVOICE_MERANDCOMPANY_DIFFERENT = 343;

  /**
   * 开票记录不存在，无法确认
   */
  public static final int INVOICE_RECORD_NOTEXIST = 344;

  /**
   * 删除图片异常
   */
  public static final int DELETE_PIC_EXCEPTION = 345;

  /**
   * 修改失败
   */
  public static final int UPDATE_FAIL = 346;

  /**
   * 添加失败
   */
  public static final int INSERT_FAIL = 347;

  /**
   * 删除失败
   */
  public static final int DELETE_FAIL = 348;

  /**
   * 批量导入全部失败
   */
  public static final int IMPORT_ALL_FAIL = 349;

  /**
   * 导入部分成功
   */
  public static final int IMPORT_PART_SUCCESS = 350;

  /**
   * EXCEL无内容
   */
  public static final int EXCEL_NO_INFO = 351;

  /**
   * 商户交易黑名单导入模板格式错误
   */
  public static final int BLACK_TEMPTYPE_ERROR = 352;

  /**
   * 导入数据异常
   */
  public static final int EXCEL_IMPORT_EXCEPTION = 353;

  /**
   * 商户在一种支付方式下只能配置一个充值账户
   */
  public static final int MERCHNT_ONLYONE_ACCOUNT = 354;

  /**
   * 商户充值账户配置操作异常
   */
  public static final int INSERT_ACCOUNT_EXCEPTION = 355;

  /**
   * 添加商户充值账户配置信息失败
   */
  public static final int INSERT_ACCOUNT_FAIL = 356;

  /**
   * 商户充值账户记录不存在，无法删除
   */
  public static final int NO_ACCOUNT_INFO = 357;

  /**
   * 开票申请中包含已完成开票的记录
   */
  public static final int INVOICE_CONTAIN_SUCEESS = 358;

  public static final int REQUIRED_PARAMS_ISNULL = 359;

  /**
   * 状态已落地，拒绝操作
   */
  public static final int CURRENT_STATUS_REFUSE = 360;

  /**
   * 发生异常
   */
  public static final int HAPPEND_EXCEPTION = 361;

  /**
   * 清结算数据月份不能为空
   */
  public static final int MONTH_IS_NULL = 362;

  /**
   * 商户编号标识为空
   */
  public static final int MERCHANTID_IS_NULL = 363;

  /**
   * 清结算数据无记录
   */
  public static final int CLEARACCOUNTS_IS_NULL = 364;

  /**
   * 不能删除预开票
   */
  public static final int NO_DELETE_PREPINVOICE = 365;

  /**
   * 不能删除预开票
   */
  public static final int PRE_PAY_FAIL = 366;

  public static final int YMYF_NO_BATCH_EXCEPTION = 367;

  public static final int YMYF_NORMAL_EXCEPTION = 368;

  public static final int YMYF_VERFY_FAIL = 369;

  public static final int LINK_CONFIG_USE = 370;

  public static final int LINK_CONFIG_USERONE = 371;

  public static final int ACCOUNT_NAME_DIFFERENT = 372;

  public static final int EXISTS_ACCOUNT_RECORDS = 373;

  /**
   * 图片文件必须上传
   */
  public static final int FILE_NOT_FOUND = 374;
  /**
   * 超过100天
   */
  public static final int MORE_THAN_100_DAY = 375;

  public static final int EXISTS_INVOICE_RESERVE = 376;

  public static final int INVOICE_NUM_WRONG = 377;

  public static final int IS_USE_WRONG = 378;

  public static final int INVOICENUM_NOT_ENOUGH = 379;

  public static final int NO_CREATE_RESERVE = 380;

  public static final int GET_INVOICE_FAIL = 381;

  public static final int COMMISSION_INVOICE_PROCESSING = 382;

  public static final int PLATFORM_NOT_MERCHANT = 386;

  public static final int PLATFORM_NOT_EXIST = 384;

  /**
   * 服务公司key 未配置
   */
  public static final int COMPANY_KEY_NOT_SETTING = 383;

  /**
   * 未配置协议使用规则
   */
  public static final int COMPANY_AGREEMENT_NOT_SETTING = 385;

  public static final int YUNCR_PUSH_PROJECT_FAIL = 600;
  public static final int BATCH_AMOUNT_CHANGE = 401;

  public static final String UNKNOW_PAYTYPE = "未知支付方式";
  public static final String ROLE_ERROR = "角色错误，请登陆服务公司账号。";
  public static final String UNSUPPORT_PAYTYPE = "暂不支持此支付方式";
  public static final String COMPANY_NOT_EXISTS = "请联系管理员配置薪资服务公司配置信息！";
  public static final String EXPORT_SUCCESS = "导入成功";
  public static final String EXPORT_FAILIURE = "系统异常，导入失败！";
  public static final String RELATIONSHIP_DOES_NOT_EXIST = "关联关系不存在或请登陆后重试！";
  public static final String PASSWORD_DOES_NOT_EXIST = "未输入密码";
  public static final String PASSWORD_DOES_NOT_SET = "未输入或未设置密码";
  public static final String PASSWORD_ERROR = "密码错误";
  public static final String REFUND_FAILED = "(退款失败！关联关系不存在)";
  public static final String OPERATING_FAILED = "操作失败！请联系管理员";
  public static final String UPDATE_FAILED = "修改失败！请联系管理员";
  public static final String DELETE_FAILED = "删除失败！请联系管理员";
  public static final String UPLOAD_FAILED = "上传失败！";
  public static final String PARAMS_ERROR = "参数异常";
  public static final String CONNECTION_ERROR = "网络异常";
  public static final String REVIEW_SHOW_ONLY = "您的权限不能复核下级机构批次数据";
  public static final String PERMISSIONERROR = "权限错误，仅超管可访问该接口。";
  public static final String FEERULETYPEREPEATERROR = "服务费计算规则仅能配置一个";
  public static final String TITLENOTNULL = "标题不能为空！";
  public static final String CHEACK_REPEAT_WARN = "系统风控校验拦截，原因：两分钟内有相同收款交易，请稍后再试！";
  public static final String AMOUNT_ERROR = "充值金额错误";
  public static final String CHANNEL_ROUTE_NOT_CONFIGURED = "通道路由未配置";
  public static final String PERMISSION_ERROR = "权限错误";
  public static final String SUBACCOUNT_NOT_OPEN = "服务公司未开通子账户模式";
  public static final String SUBACCOUNT_NOT_EXIST = "商户子账户不存在";
  public static final String SUBACCOUNT_UNSUPPORT_SYNC = "暂不支持该子账号同步余额";
  public static final String SUBACCOUNT_BALANCE_EQUAL = "系统余额和通道余额一致，无需同步操作";
  public static final String SUBACCOUNT_BALANCE_CHANGE = "通道余额发生变化,请刷新界面重试";
  public static final String CUSTOM_BALANCE_CHANGE = "系统当前余额发生变化,请刷新界面重试";
  public static final String EXIST_SYNC_RECORD = "存在未落地的余额同步记录,请稍后再试";
  public static final String EXIST_PAY_RECORD = "该商户存在未落地的下发,请等待批次落地后再进行操作";
  public static final String RECHARGE_RECORD_ERROR = "该充值信息不存在或状态已终态,请勿重复操作";
  public static final String TRANSFER_RECORD_ERROR = "该转账流水不存在或已核销";
  public static final String SUBACCOUNT_NAME_ERROR = "子账户名称和对方账户名不一致,不能确认入账";
  public static final String RECHARGE_RECORD_EXCEPTION = "异常充值记录";
  public static final String RECHARGE_AMOUNT_ERROR = "提交充值金额和确定到账金额偏差大,不能确认入账";
  public static final String EXIST_RECHARGE_RECORD = "该商户存在待确认的充值,请等待充值终态后再进行余额同步操作";
  public static final String RECHARGE_RECORD_REFUND_ERROR = "只有未开票状态且非补服务费的充值允许线上操作退款";
  public static final String CUSTOM_BALANCE_REFUND_ERROR = "商户当前余额小于充值记录可用金额,无法进行退款操作";
  public static final String RECHARGE_REFUND_AMOUNT_ERROR = "充值记录实际到账金额与退款金额不一致,无法进行退款操作";
  public static final String RECHARGE_REFUND_AMOUNT_EQUAL = "充值记录实际到账金额不能小于充值记录可用余额,请联系技术人员协助处理";
  public static final String CUSTOM_BALANCE_REFUNDAMOUNT_ERROR = "商户当前余额小于退款金额,无法进行退款操作";
  public static final String CUSTOM_BALANCE_PART_ERROR = "部分退款金额必须小于充值记录可用金额,请更改退款金额重试";
  public static final String COMPANY_NOT_SUPPORT_REFUND = "该服务公司不支持退款";
  public static final String RECHARGE_REFUND_ERROR = "余额退款异常,请联系技术人员协助处理";
  public static final String RATE_OVERLAP = "当前配置的档位范围区间存在重叠";
  public static final String SERVICE_FEE_TYPE_ERROR = "充值预扣收下发档位费率必须一致";
  public static final String UPLOAD_ERROR = "上传失败";
  public static final String BATCH_NOT_EXIST = "批次不存在";
  public static final String UPDATE_BALANCE_EXCEPTION = "扣费失败，余额不足";
  public static final String UNLOCK_ERROR = "已锁定的批次无法进行该操作";
  public static final String NOT_OPEN_LINKAGE = "未开通联动交易";
  public static final String PATH_BALANCE_ERROR = "当前存款机构实时余额不足";
  public static final String IN_ACCOUNT_NAME_ERROR = "收款账号不存在,请检查相关配置";
  public static final String FEE_RULE_TYEP_ERROR = "无效的服务费计算规则";
  public static final String CUSTOM_COMPANY_NOT_RELATED = "未配置服务公司";
  public static final String DEDUCT_BALANCE_ERROR = "余额扣减异常,请联系技术人员协助处理";
  public static final String QUERY_BALANCE_ERROR = "余额查询异常,请联系技术人员协助处理";
  public static final String DEDUCT_BALANCE_CUSTOM_ERROR = "扣款商户与当前登录商户不一致";
  public static final String QUERY_BALANCE_CUSTOM_ERROR = "查询余额商户与当前登录商户不一致";
  public static final String DEDUCT_AMOUNT_ERROR = "结算金额错误";
  public static final String COMPANY_NOT_TAX_RAX = "服务公司 %1$s 未配置个税税率";
  public static final String CUSTOM_THIRD_CONFIG_EXIST = "商户关联平台方通道配置已存在";
  public static final String YUNCR_PUSH_PROJECT_FAIL_MSG = "新建项目异常";
  public static final String FORWARD_COMPANY_NOT_RATE_CONFIG = "转包服务公司未配置费率";
  public static final String COMPANY_NOT_RATE_CONFIG = "服务公司未配置费率";
  public static final String LINKAGESIGN_PROCESSING_ERROR = "系统下发信息认证处理未完成请稍等再付款!";
  public static final String INVOICE_FILE_FORMAT_ERROR = "发票文件格式错误";

  public static final Map<Integer, String> codeMaps = new HashMap<Integer, String>();

  static {
    codeMaps.put(success, "成功");
    codeMaps.put(error000, "系统错误");
    codeMaps.put(error101, "参数异常");
    codeMaps.put(error102, "公司不存在");
    codeMaps.put(error103, "用户已存在");
    codeMaps.put(error104, "手机号已存在");
    codeMaps.put(error105, "用户不存在");
    codeMaps.put(USER_NOT_FOUND, "用户不存在");
    codeMaps.put(error106, "用户账户异常");
    codeMaps.put(error107, "网络错误");
    codeMaps.put(error108, "merchantId错误");
    codeMaps.put(error109, "ip错误");
    codeMaps.put(error110, "时间戳超时");
    codeMaps.put(error111, "时间戳非法");
    codeMaps.put(error112, "签名不存在");
    codeMaps.put(error113, "sign错误");
    codeMaps.put(error114, "请求序列号不存在");
    codeMaps.put(error115, "金额过大");
    codeMaps.put(error116, "图片解析异常");
    codeMaps.put(error117, "商户已存在");
    codeMaps.put(error118, "商户审核中");
    codeMaps.put(error119, "用户状态错误");
    codeMaps.put(error120, "公司信息已经通过审核");
    codeMaps.put(error121, "公司信息不存在");
    codeMaps.put(error122, "银行卡不支持");
    codeMaps.put(error123, "密码还没有设置");
    codeMaps.put(error124, "输入金额错误");
    codeMaps.put(error125, "银行卡未绑定");
    codeMaps.put(error126, "json数据异常");
    codeMaps.put(error127, "更换账户唯一银行卡需要您的余额账户清零，请您余额提现成功后更换银行卡");
    codeMaps.put(error128, "绑卡失败，已经绑定过银行卡");
    codeMaps.put(error129, "非法登陆");
    codeMaps.put(error130, "密码已存在");
    codeMaps.put(error131, "用户已注册成功，无需重复注册");
    codeMaps.put(error132, "订单号错误，查询不到此订单");
    codeMaps.put(error133, "用户已签约合同");
    codeMaps.put(error134, "用户已提交签约申请");
    codeMaps.put(error135, "用户未签约");
    codeMaps.put(error136, "渠道通知地址不存在");
    codeMaps.put(error137, "手机号校验失败");
    codeMaps.put(error138, "回调地址不能为空");
    codeMaps.put(error139, "身份证验证失败");
    codeMaps.put(error140, "总金额错误");
    codeMaps.put(error141, "验证码错误");
    codeMaps.put(error142, "批次不存在或者薪资服务公司不存在");
    codeMaps.put(error143, "签约提交失败");
    codeMaps.put(error144, "用户未实名开户");
    codeMaps.put(error145, "用户未上传身份证");
    codeMaps.put(error146, "图片过大");
    codeMaps.put(error201, "商户薪资下发信息未配置！");
    codeMaps.put(error202, "批次信息不符合要求！");
    codeMaps.put(error203, "预存款金额不足！");
    codeMaps.put(error204, "爱员工用户信息授权失败！");
    codeMaps.put(error205, "该时间不允许此操作，请于 6:00 - 17:30 范围内执行 ！");
    codeMaps.put(error301, "验证码错误，请重新输入！");
    codeMaps.put(error302, "用户不存在，请重新输入！");
    codeMaps.put(error303, "用户已被禁用，请联系管理员！");
    codeMaps.put(error304, "密码错误，请重新输入！");
    codeMaps.put(error305, "该时间不允许此操作，请于 6:00 - 17:30 范围内执行 ！");
    codeMaps.put(error306, "登陆超时，请重新登陆！");
    codeMaps.put(error601,"非法的访问用户!");
    codeMaps.put(error602,"密码错误!");
    codeMaps.put(SESSION_DESTROYED, "登陆超时，请重新登陆！");
    codeMaps.put(CODE_VILID_ERROR, "验证码错误");
    codeMaps.put(IMPORT_NUMBER_ERROR, "导入数据不得大于2000条或小于0条，请重新导入！");
    codeMaps.put(ParamNotFound, "参数异常或者为空");
    codeMaps.put(AGREEMENT_NOT_FOUND, "协议没有找到");
    codeMaps.put(FILE_IOEXCEPTION, "文件操作异常");
    codeMaps.put(USERNO_ALREADY_EXIST, "商户用户编号已存在");
    codeMaps.put(CHANNEL_RELATED_NOT_FOUND, "商户服务公司关系未配置");
    codeMaps.put(FOUND_USERCOMMISSION_OR_USERAGREEMENT, "该用户有下发记录或者签约记录，无法删除");
    codeMaps.put(ORIGIN_NOT_FOUND, "您合作的商户目前尚未上传您的信息，请联系商户");
    codeMaps.put(COMPANY_NOT_FOUND, "服务公司不存在");
    codeMaps.put(AGREEMENT_TEMPLATE_NOT_FOUND, "协议模板不存在");
    codeMaps.put(UNSUBSCRIBE, "用户未关注公众号");
    codeMaps.put(PAYMENT_LIMITATION_NOT_FOUND, "商户下发限额配置不存在");
    codeMaps.put(PAYMENT_LIMITATION_EXIST, "商户下发限额配置存在");
    codeMaps.put(NOT_ALL_BATCHES_ARE_PENDING_APPROVAL, "部分批次已经被审核或驳回");
    codeMaps.put(DO_NOT_HAVE_APPROVAL_RIGHT, "没有符合权限");
    codeMaps.put(CUSTOM_TEMP_EXISTS, "商户模板已存在");
    codeMaps.put(DO_NOT_HAVE_MATCHING_RESULTS, "没有符合条件的结果");
    codeMaps.put(AGREEMENT_EXPORT_ERROR, "协议导出异常");
    codeMaps.put(CUSTOM_TEMP_USE, "该模板已有商户使用");
    codeMaps.put(PAY_EXCEPTION, "重发异常");
    codeMaps.put(NOT_SUPPORT_STATUS, "状态不支持");
    codeMaps.put(QUERY_FAIL, "查询失败");
    codeMaps.put(PRE_STATUS_ERROR, "前笔订单不为成功");
    codeMaps.put(SURE_STATUS, "同步状态异常");
    codeMaps.put(BUSINESS_TYPE_NOT, "业务状态不满足");
    codeMaps.put(ZIP_FILE_NOT_FOUND, "压缩包不存在");
    codeMaps.put(IMPORT_NUMBER_ERROR_500, "导入数据超过500条");
    codeMaps.put(NOT_CHECK_RECHARGENO, "未勾选开票充值流水号");
    codeMaps.put(INVOICEAMOUNT_SHOULD_GT_ZERO, "待开票余额应大于0");
    codeMaps.put(INVOICE_AMOUNT_DIFFERENT, "多笔复合开票金额需待开票金额一致");
    codeMaps.put(INVOICE_AMOUNT_WRONG, "开票金额超出待开票金额范围或待开票金额未大与0");
    codeMaps.put(INVOICE_COMPLETE, "该笔订单已完成开票");
    codeMaps.put(INVOICE_EXCEPTION, "开票操作异常");
    codeMaps.put(INVOICE_HANDLE, "该开票申请已受理");
    codeMaps.put(INVOICE_ClASS_DIFFERENT, "部分开票，开票类目必须一致");
    codeMaps.put(INVOICE_MERANDCOMPANY_DIFFERENT, "多充值流水合并开票申请的必须是商户+下发公司全部一致");
    codeMaps.put(INVOICE_RECORD_NOTEXIST, "开票记录不存在，无法确认");
    codeMaps.put(DELETE_PIC_EXCEPTION, "删除图片异常");
    codeMaps.put(UPDATE_FAIL, "修改失败");
    codeMaps.put(INSERT_FAIL, "添加失败");
    codeMaps.put(DELETE_FAIL, "删除失败");
    codeMaps.put(IMPORT_ALL_FAIL, "批量导入全部失败");
    codeMaps.put(IMPORT_PART_SUCCESS, "导入部分成功");
    codeMaps.put(EXCEL_NO_INFO, "EXCEL无内容");
    codeMaps.put(BLACK_TEMPTYPE_ERROR, "商户交易黑名单导入模板格式错误");
    codeMaps.put(EXCEL_IMPORT_EXCEPTION, "导入数据异常");
    codeMaps.put(MERCHNT_ONLYONE_ACCOUNT, "商户在一种支付方式下只能配置一个充值账户");
    codeMaps.put(INSERT_ACCOUNT_EXCEPTION, "商户充值账户配置操作异常");
    codeMaps.put(INSERT_ACCOUNT_FAIL, "添加商户充值账户配置信息失败");
    codeMaps.put(NO_ACCOUNT_INFO, "商户充值账户记录不存在，无法删除");
    codeMaps.put(INVOICE_CONTAIN_SUCEESS, "开票申请中包含已完成开票的记录");
    codeMaps.put(REQUIRED_PARAMS_ISNULL, "必填参数不能为空");
    codeMaps.put(CURRENT_STATUS_REFUSE, "当前状态已落地不能修改");
    codeMaps.put(HAPPEND_EXCEPTION, "发生异常，请联系系统管理员");
    codeMaps.put(MONTH_IS_NULL, "清结算报表重发，月份不能为空");
    codeMaps.put(MERCHANTID_IS_NULL, "清结算报表重发，编号不能为空");
    codeMaps.put(CLEARACCOUNTS_IS_NULL, "清结算数据无记录");
    codeMaps.put(NO_DELETE_PREPINVOICE, "删除开票记录需要在预开票且未核销条件下");
    codeMaps.put(PRE_PAY_FAIL, "预下单付款失败");
    codeMaps.put(YMYF_NO_BATCH_EXCEPTION, "溢美优付批次信息不存在");
    codeMaps.put(YMYF_NORMAL_EXCEPTION, "溢美优付一般性失败");
    codeMaps.put(YMYF_VERFY_FAIL, "溢美优付验证签名失败");
    codeMaps.put(FILE_NOT_FOUND, "该服务公司，图片文件必须上传");
    codeMaps.put(LINK_CONFIG_USE, "操作失败，联动基础配置信息存在使用");
    codeMaps.put(LINK_CONFIG_USERONE, "添加失败，商户同种联动类型下只能关联一个账号");
    codeMaps.put(ACCOUNT_NAME_DIFFERENT, "关联失败，账户名与商户名称不一致");
    codeMaps.put(EXISTS_ACCOUNT_RECORDS, "操作失败，存在账户交易记录");
    codeMaps.put(MORE_THAN_100_DAY, "起止时间超过100天");
    codeMaps.put(EXISTS_INVOICE_RESERVE, "存在该类型的开票量信息数据");
    codeMaps.put(INVOICE_NUM_WRONG, "开票总量需大于已开票数");
    codeMaps.put(IS_USE_WRONG, "存在使用");
    codeMaps.put(INVOICENUM_NOT_ENOUGH, "可开票数量不足");
    codeMaps.put(NO_CREATE_RESERVE, "未创建发票量基础信息");
    codeMaps.put(GET_INVOICE_FAIL, "获取发票信息失败");
    codeMaps.put(COMMISSION_INVOICE_PROCESSING, "实发开票处理中");
    codeMaps.put(PLATFORM_NOT_MERCHANT, "当前选择商户不在该平台下");
    codeMaps.put(PLATFORM_NOT_EXIST, "平台不存在");
    codeMaps.put(YUNCR_PUSH_PROJECT_FAIL, "推送运控项目失败");
    codeMaps.put(COMPANY_KEY_NOT_SETTING, "服务公司key未配置");
    codeMaps.put(COMPANY_AGREEMENT_NOT_SETTING, "服务公司协议路由规则未配置");
  }
}
