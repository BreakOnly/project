package com.jrmf.common;

public enum ResponseCodeMapping {

  SUCCESS("成功！", 1),
  ERR_500("服务器开小差了!", 500),
  ERR_501("已经提交审核,不可重复修改", 501),
  ERR_502("数据更新失败,请重试", 502),
  ERR_503("您绑定的身份证件与商家导入不一致", 503),
  ERR_504("此证件已经被别的用户绑定,请联系管理员", 504),
  ERR_505("此用户数据存在异常情况,请联系管理员", 505),
  ERR_506("请求服务接口失败,请重试", 506),
  ERR_507("请先完成身份认证", 507),
  ERR_508("请求服务接口失败,认证后的数据为空", 508),
  ERR_509("银行卡识别失败", 509),
  ERR_510("实名数据更新失败,请重试", 510),
  ERR_511("未获取到登陆信息", 300),
  ERR_512("请先完成实名认证", 512),
  ERR_513("视频信息获取失败,请重新上传", 513),
  ERR_514("请先完成活体认证", 514),
  ERR_515("签名识别失败,请重新上传", 515),
  ERR_516("签名保存失败,请重新上传", 516),
  ERR_517("请先完成手签等前序流程", 517),
  ERR_518("获取的个体户编号为空", 518),
  ERR_519("您已经完成实名认证请勿重复提交", 519),
  PARAMETER_ILLEGAL("参数可能为空或者不合法,请检查参数", 50000),
  ERR_5003("文件太大,只支持100M以下!", 5003),
  ERR_5008("用户未登录!", 306),
  ERR_5009("未获取到相关用户信息!", 5009),
  ERR_5010("该用户已经注册,请前去一键登录!", 5010),
  ERR_5011("该用户已经注册,请前去授权登录!", 5011),
  ERR_5012("用户未进行小程序授权获取手机号，请先进行授权获取手机号!", 5012),
  ERR_5015("未登录", 5015),
  ERR_520("请输入您的短信验证码", 520),
  ERR_521("短信验证码无效", 521),
  ERR_522("暂无协议请联系管理员", 522),
  ERR_523("暂无审核信息", 523),
  ERR_525("未查询到相关发包方信息", 525),
  ERR_526("用户还未完成全部认证,不能进行审批", 526),
  ERR_527("用户已经被审批", 527),
  ERR_528("此商户是白名单用户,不允许修改商户", 528),
  ERR_529("暂无权限", 529),
  ERR_530("您选中的数据不属于同一个商户,请重新确认", 530),
  ERR_531("发送失败", 531),
  ERR_532("数据不能为空", 532),
  ERR_533("交易类型不能为空", 533),
  ERR_534("交易金额不能为空", 534),
  ERR_535("交易流水号不能为空", 535),
  ERR_536("所交易的商户数据不能为空", 536),
  ERR_537("此商户记账户数据存在异常,请联系管理员", 537),
  ERR_538("系统内记账户为失效,请联系客户经理", 538),
  ERR_539("充值金额不可以<=0", 539),
  ERR_540("交易操作失败,请确认记账户余额与状态", 540),
  ERR_541("此记账户不存在", 541),
  ERR_542("此服务公司不存在", 542),
  ERR_543("系统内记账户余额不足,请联系客户经理", 543),
  ERR_544("此记账户已存在,请勿重复添加", 544),
  ERR_545("数据添加失败,请重试", 545),
  ERR_546("请选择失败节点", 546),
  ERR_547("错误节点添加失败", 547),
  ERR_551("未找到相关充值记录", 551),
  ERR_552("已经上传充值确认函，请刷新页面", 552),
  ERR_553("未找到相关的电子签名模板", 553),
  ERR_555("备注信息不符合规范", 555),
  ERR_556("备注金额与实际提交金额不符", 556),
  ERR_557("所选数据发包商存在异常", 557),
  ERR_558("此发包商对应商户名称存在多条", 558),
  ERR_559("批量开票,包含有已开票数据！请正确选择需要申请开票的查询条件", 559),
  ERR_560("批量开票,包含未有回单的数据！请正确选择需要申请开票的查询条件", 560),
  ERR_561("请先创建项目", 561),
  ERR_562("所选数据包含多家发包商", 562),
  ERR_563("请先创建发包商", 563),
  ERR_564("数据更新失败或数据包含已开票数据,请刷新页面重试", 564);
  private String message;
  private Integer code;

  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  ResponseCodeMapping(String message, Integer code) {
    this.message = message;
    this.code = code;
  }

  public String getMessage() {
    return this.message;
  }

  public static ResponseCodeMapping getProperType(String name) {
    ResponseCodeMapping[] codeMappings = ResponseCodeMapping
        .values();
    for (ResponseCodeMapping responseCodeMapping : codeMappings) {
      if (responseCodeMapping.name().equals(name)) {
        return responseCodeMapping;
      }
    }
    return null;
  }

}
