package com.jrmf.test;


/**
 * 描 述: 平常练习测试类 <br/>
 * 创 建：2019年09⽉23⽇<br/>
 */
public class ServcieCompanyControllerTest {
/*
  private static StopWatch s = new StopWatch();

  @Test
  public void test() throws UnknownHostException {
    boolean x = true;
    boolean y = false;
    short z = 42;
    if ((z++==42) && (y=true)) {
      z++;
    }
    if ((x=false) || (++z==45)) {
      z++;
    }
    System.out.println(z);
  }

  @Test
  public void testStopWatch() throws InterruptedException {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    Thread.sleep(100);
    stopWatch.stop();
    System.out.println(stopWatch.getTotalTimeMillis());
    System.out.println(stopWatch.prettyPrint());
  }

  @Test
  public void testWholeStopWatch() {
    s.start();
    new Thread(new Runnable() {
      @SneakyThrows
      @Override
      public void run() {
          s.start();
          Thread.sleep(300);
          s.stop();
          System.out.println(s.getTotalTimeMillis());
      }
    });
    s.stop();
    System.out.println(s.getTotalTimeMillis());
  }

  @Test
  public void testCount() {
    int[] i = new int[]{1,2,3,4,5,6,7,8,9,10};

    int maxValue = i[0];
    for (int j = 0; j < i.length; j++) {
      if (maxValue < i[j]) {
        maxValue = i[j];
      }
    }
    System.out.println(maxValue);

  }

  @Test
  public void testSign() throws Exception {
    String privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAIjQsI0pnVwJzhEsM+B9xwDlR2bL1wYUPCSnC8gm3YVeXTzZSWXslGb/h13v4oHXFnvgX54JNmBvrMKV6gJGVyOlZ+rfovvz3rx5q+OeyHDxuuqF09aV6YdfEIvRLjoas34f2dG9FnLFnx+OrkigrYkv33QOYdfkR3CMXRIFDaDdAgMBAAECgYEAhKX5l1PyusrS3XmC7rRkHq0t9i/KUZ2K1mqTlMKB4o4kThvJ+yVOmTJE18H+Va7poL9hVYNPEl8UHDFnij8SuhPw6N6KXQPE5A10zy+ovrfuwrJ1/mCkzrBFHs3GclNUMr5CIXFeEnadFKvPGx602Qp+RJDayAz5APj6+2VaSiECQQDJ5yuzVt4NG98LFbwRdTxds72PFEhGGsL5X7qQRWz21hI8PAU5ylbBrN/3XbXYtKkF5F9Cuu7y9c4P8mXD7HkJAkEArXkMXm6U7jgUDODbDbS+T7M4EATMEW+Ey/ofcl8BMb8TVQnRm4A2N3IJJij8WNTKM3vUDwmy5WGMj5H7NRKCNQJBAIOHi5qLbqDPlWAdSqV84FSSsVyyc9rnuDjIujseXzv2fW6sEyNww0/slQMLE1oXZz0lZ0GLkfrJUvJkkRMvzDECQGC0GDcUQiXCUTsUpkivt1/KsrgLVI9rsYXcO2eQmqPWtrozLQwYnyCti3ggZPxIMygcIYz4hSfmB2uJn5ZoaPkCQCrddIBzgzLn5fB6pQJrOi/Z/dE8wSDr+a+0oCOM+f4l6uN+zERI/TgA+F9wmJaMsmdsfbZUFlqqZlzsCaXuSoc=";
    String payPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCXFtJNcCv9X+pfOz+jy4WO+7KC0tdtDGfW/XrJOFn6mTR/TpJrsLzwssTyj0hDfUla1xDCbAWlUEkzb64jsPKk9WACupLOOrnEy7NuG0O5jg36UXle+m5/uDm8B/KGV9BQmEt+EOIbl2IzNYS4GflkbCTtkbzMc+DXetl3IHM1+wIDAQAB";
    String md5Key = "3505F7C85E1CA969249474AB7C647246";
    String url = "http://api.youfupingtai.com/clientBusiness/common.do";
    String appId = "89900000125115783648";
    Integer uploadFlag = 0;
    String backPicUrl = "";
    String frontPicUrl = "";

    ContractModle cm = new ContractModle();
    cm.setName("刘洪林");
    cm.setCardNo("6212261702000071985");
    // 身份证号
    cm.setIdCard("412723199006126598");
    cm.setMobile("17810638142");
    //签约类型 0：接口签约，1：公众号签约,2：签约接口
    cm.setSignType(2);
    cm.setLevyId(Long.parseLong("33094"));//税优地通道ID
    cm.setOtherParam("123456");
    String json= JsonUtils.toJson(cm);
    YFService yfService = new YFService(url, appId, privateKey, payPublicKey, md5Key, json, "UTF-8");
    //响应信息
    String respInfo = yfService.signContract(uploadFlag, backPicUrl, frontPicUrl);
    Map<String, String> respUpInfo = JSONObject.parseObject(respInfo, Map.class);
    System.out.println(respUpInfo);
  }

  @Test
  public void testSignQuery() {
    String privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAIjQsI0pnVwJzhEsM+B9xwDlR2bL1wYUPCSnC8gm3YVeXTzZSWXslGb/h13v4oHXFnvgX54JNmBvrMKV6gJGVyOlZ+rfovvz3rx5q+OeyHDxuuqF09aV6YdfEIvRLjoas34f2dG9FnLFnx+OrkigrYkv33QOYdfkR3CMXRIFDaDdAgMBAAECgYEAhKX5l1PyusrS3XmC7rRkHq0t9i/KUZ2K1mqTlMKB4o4kThvJ+yVOmTJE18H+Va7poL9hVYNPEl8UHDFnij8SuhPw6N6KXQPE5A10zy+ovrfuwrJ1/mCkzrBFHs3GclNUMr5CIXFeEnadFKvPGx602Qp+RJDayAz5APj6+2VaSiECQQDJ5yuzVt4NG98LFbwRdTxds72PFEhGGsL5X7qQRWz21hI8PAU5ylbBrN/3XbXYtKkF5F9Cuu7y9c4P8mXD7HkJAkEArXkMXm6U7jgUDODbDbS+T7M4EATMEW+Ey/ofcl8BMb8TVQnRm4A2N3IJJij8WNTKM3vUDwmy5WGMj5H7NRKCNQJBAIOHi5qLbqDPlWAdSqV84FSSsVyyc9rnuDjIujseXzv2fW6sEyNww0/slQMLE1oXZz0lZ0GLkfrJUvJkkRMvzDECQGC0GDcUQiXCUTsUpkivt1/KsrgLVI9rsYXcO2eQmqPWtrozLQwYnyCti3ggZPxIMygcIYz4hSfmB2uJn5ZoaPkCQCrddIBzgzLn5fB6pQJrOi/Z/dE8wSDr+a+0oCOM+f4l6uN+zERI/TgA+F9wmJaMsmdsfbZUFlqqZlzsCaXuSoc=";
    String payPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCXFtJNcCv9X+pfOz+jy4WO+7KC0tdtDGfW/XrJOFn6mTR/TpJrsLzwssTyj0hDfUla1xDCbAWlUEkzb64jsPKk9WACupLOOrnEy7NuG0O5jg36UXle+m5/uDm8B/KGV9BQmEt+EOIbl2IzNYS4GflkbCTtkbzMc+DXetl3IHM1+wIDAQAB";
    String md5Key = "3505F7C85E1CA969249474AB7C647246";
    String url = "http://api.youfupingtai.com/clientBusiness/common.do";
    String appId = "89900000125115783648";
      Map<String, String> respMap = new HashMap<String, String>();
      try {
        ContractModle cm = new ContractModle();
        cm.setLevyId(Long.parseLong("33094"));//税优地通道ID
        cm.setIdCard("412723199006126598");
        cm.setName("刘洪林");
        String json=JsonUtils.toJson(cm);
        YFService yfService = new YFService(url, appId, privateKey, payPublicKey, md5Key, json, "UTF-8");
        //响应信息
        String respInfo = yfService.signContractQuery();
        Map<String, String> respUpInfo = JSONObject.parseObject(respInfo, Map.class);
        System.out.println(respUpInfo);
        //封装响应结果
        if(Integer.parseInt(String.valueOf(respUpInfo.get("state")))==1){
          //签约成功
          respMap.put("code", "0000");
          respMap.put("state", "1");
        }else if(Integer.parseInt(String.valueOf(respUpInfo.get("state")))==4){
          //签约失败
          respMap.put("code", "0000");
          respMap.put("state", "2");
          respMap.put("msg", respUpInfo.get("retMsg"));
        }else if(Integer.parseInt(String.valueOf(respUpInfo.get("state")))==0){
          //未申请签约
          respMap.put("code", "0000");
          respMap.put("state", "0");
        }else{
          //签约处理中
          respMap.put("code", "0000");
          respMap.put("state", "3");
        }
        //封装响应结果
      }catch (YmyfHasSignException e) {
        //该用户信息已经做过签约
        respMap.put("code", "1014");
        respMap.put("msg", e.getMessage());
      } catch (Exception e) {
        respMap.put("code", "1009");
        respMap.put("msg", e.getMessage());
      }
    System.out.println(respMap);
  }

  @Test
  public void testList() throws ParseException {

    System.out.println(isInteger("00001"));
  }

  public static boolean isInteger(String str) {
    Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
    return pattern.matcher(str).matches();
  }

  public static boolean isDate(String value,String format){

    SimpleDateFormat sdf = null;
    ParsePosition pos = new ParsePosition(0);//指定从所传字符串的首位开始解析

    try {
      sdf = new SimpleDateFormat(format);
      sdf.setLenient(false);
      Date date = sdf.parse(value,pos);
      if(date == null){
        return false;
      }else{
        System.out.println("-------->pos : " + pos.getIndex());
        System.out.println("-------->date : " + sdf.format(date));
        //更为严谨的日期,如2011-03-024认为是不合法的
        if(pos.getIndex() > sdf.format(date).length()){
          return false;
        }
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public static boolean isValidDate(String strDate) {
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    try {
      // 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2018-02-29会被接受，并转换成2018-03-01

      format.setLenient(false);
      Date date = format.parse(strDate);

      //判断传入的yyyy年-MM月-dd日 字符串是否为数字
      String[] sArray = strDate.split("-");
      for (String s : sArray) {
        boolean isNum = s.matches("[0-9]+");
        //+表示1个或多个（如"3"或"225"），*表示0个或多个（[0-9]*）（如""或"1"或"22"），?表示0个或1个([0-9]?)(如""或"7")
        if (!isNum) {
          return false;
        }
      }
    } catch (Exception e) {
      // e.printStackTrace();
      // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
      return false;
    }

    return true;
  }

  public static boolean checkValidDate(String pDateObj) {
    boolean ret = true;
    if (pDateObj == null || pDateObj.length() != 8) {
      ret = false;
    }
    try {
      int year = new Integer(pDateObj.substring(0, 4)).intValue();
      int month = new Integer(pDateObj.substring(4, 6)).intValue();
      int day = new Integer(pDateObj.substring(6)).intValue();
      Calendar cal = Calendar.getInstance();
      cal.setLenient(false);   //允许严格检查日期格式
      cal.set(year, month - 1, day);
      cal.getTime();//该方法调用就会抛出异常
    } catch (Exception e) {
      ret = false;
    }
    return ret;
  }

  public static Boolean DateCompare(Date time1,Date time2,int numYear) {
    Date time3 = add(time1, Calendar.YEAR,numYear);
    if(time3.getTime()<time2.getTime()){
      return true;
    }
    return false;
  }

  public static Date add(final Date date, final int calendarField, final int amount) {
    if (date == null) {
      return null;
    }
    final Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(calendarField, amount);
    return c.getTime();
  }

  @Test
  public void test2() throws IOException {

    File file = new File("D:\\图片\\人像面.jpg");
    FileInputStream input = new FileInputStream(file);
    MultipartFile multipartFile =new MockMultipartFile("file", file.getName(), "text/plain", IOUtils
        .toByteArray(input));
    byte[] bytes = multipartFile.getBytes();
    String s = HexStringUtil.bytesToHexString(bytes);

    try {
      FileOutputStream fos = new FileOutputStream("C:\\Users\\16699\\Desktop\\a.txt");
      fos.write(s.getBytes());
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }


  @Test
  public void signAgreement() throws Exception {
    File file = new File("D:\\图片\\人像面.jpg");
    FileInputStream input = new FileInputStream(file);
    MultipartFile multipartFile =new MockMultipartFile("file", file.getName(), "text/plain", IOUtils
        .toByteArray(input));

    SignAgreementServiceParams params = new SignAgreementServiceParams();
    params.setMerchantId("huZ6NOkt65V3643Iye63");
    params.setPartnerId("JRMF");
    params.setThirdSerialNumber("123456789987456");
    params.setName("王胜");
    params.setTransferCorpId("10147218");
    params.setCertificateNo("142727199706270013");
    params.setCertificateType("1");
    params.setMobileNo("18519237983");
    params.setSignAgreementType("1");
    params.setNotifyUrl("www.baidu.com");
    params.setSerialNo("123456654132");
    params.setCertificateImage(FileTool.file2Hex(multipartFile));
    String url = "http://wallet-st.jrmf360.com";
    String signType = "SHA256";
    String signGenerationKey = "5facf37ecda54fb78909d6d852ef6be5";

    APIServiceResult<SignAgreementServiceData> result = null;
    try {
      ITPAPIClient client = ITPAPIClient.getInstance(signGenerationKey, signGenerationKey);
      client.setItpRootURL(url);
      client.setSignType(signType);
      result = client.signAgreement(params);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println(result);
  }

  @Test
  public void splitTest() throws ParseException {
    String s = "2021-01-05 15:25:28";
    Date parse = new SimpleDateFormat("yyyy-MM-dd").parse(s);
    String format = new SimpleDateFormat("yyyy-MM-dd").format(parse);
    System.out.println(format);
  }

  @Test
  public void arithmeticTest() {
    String var1 = "鬼鬼谷 尚硅谷你尚硅 尚硅谷你尚硅谷你尚硅谷你好";
    String var2 = "尚硅谷你尚硅谷你";
    // 判断var1 是否含有 var2， 如果存在，就返回第一次出现的位置，如果没有，则返回-1
    // KMP算法 《部分匹配表》
    String[][] arr = new String[1][];
    // 汉诺塔
    // 《分治算法》

    // 八皇后
    // 《回溯算法》

    // 《马踏棋盘算法》
    // 深度优化遍历算法（DFS） + 贪心算法优化

    // 修路问题 =》 最小生成树（普利姆算法）

    // 最短路径问题 => 《弗洛伊德算法？



  }


  @Test
  public void SparseArray() {
    int chessArray[][] = new int[11][11];
    chessArray[1][2] = 1;
    chessArray[2][3] = 2;

    for (int[] row: chessArray) {
      for (int data: row) {
        System.out.printf("%d\t", data);
      }
      System.out.println();
    }

    int sum = 0;
    for (int i = 0; i < chessArray.length; i++) {
      for (int j = 0; j < chessArray.length; j++) {
        if (chessArray[i][j] != 0) {
          sum++;
        }
      }
    }
    System.out.println("sum=" + sum);

    int[][] sparseArray = new int[sum + 1][3];
    sparseArray[0][0] = 11;
    sparseArray[0][1] = 11;
    sparseArray[0][2] = sum;

    int count = 0;
    for (int i = 0; i < chessArray.length; i++) {
      for (int j = 0; j < chessArray.length; j++) {
        if (chessArray[i][j] != 0) {
          count++;
          sparseArray[count][0] = i;
          sparseArray[count][1] = j;
          sparseArray[count][2] = chessArray[i][j];
        }
      }
    }

    System.out.println("得到的稀疏数组为：");
    for (int i = 0; i < sparseArray.length; i++) {
      System.out.printf("%d\t%d\t%d\t\n", sparseArray[i][0], sparseArray[i][1], sparseArray[i][2]);
    }

    *//**
     * 稀疏数组恢复成原始的二维数组
     *
     * 1、 先读取稀疏数组的第一行，根据第一行的数据，创建原始的二维数组
     * 2、在读取稀疏数组后几行的数据，并赋给原始的二维数组即可。
     *//*

    // 1、 先读取稀疏数组的第一行，根据第一行的数据，创建原始的二维数组，
    int chessArrayTwo[][] = new int[sparseArray[0][0]][sparseArray[0][1]];

    // 2、在读取稀疏数组后几行的数据（从第二行开始），并赋给原始的二维数组即可。

    for (int i = 1; i < sparseArray.length; i++) {
      chessArrayTwo[sparseArray[i][0]][sparseArray[i][1]] = sparseArray[i][2];
    }

    // 3、输出恢复后的二维数组
    System.out.println("恢复后的二维数组");
    for (int[] row: chessArrayTwo) {
      for (int data: row) {
        System.out.printf("%d\t", data);
      }
      System.out.println();
    }

    // io 输出到本地文件 待开发

  }

  @Test
  public void test3() {
   *//* BigDecimal bd = new BigDecimal("9.0");
    bd = bd.setScale(2, RoundingMode.HALF_UP);
    System.out.println(bd.toString());*//*
    *//*String beforeDayStrByNYR = DateUtils.getBeforeDayStrByNYR(1);
    String receiptTime = DateUtils.getBeforeDayStrShort(1);
    System.out.println("receiptTime = " + receiptTime);
    System.out.println("beforeDayStrByNYR = " + beforeDayStrByNYR);*//*
    String receiptUrl = "";
    Optional.ofNullable(receiptUrl)
        .ifPresent(url -> {
          System.out.println("=========================");
        });
    System.out.println("11111111111111111111");
  }

  @Test
  public void count() {
    LinkedList list = new LinkedList();
    HeroNode head1 = new HeroNode(1, "哈哈");
    HeroNode head2 = new HeroNode(2, "嘿嘿");

    SingleLinkedList singleLinkedList = new SingleLinkedList();
    singleLinkedList.add(head1);
    singleLinkedList.add(head2);
//    singleLinkedList.list();

    // 通过栈反转输出
    singleLinkedList.reversalStack(singleLinkedList.getHead());

//    singleLinkedList.list();


  }
*/
}

/*
class SingleLinkedList{

  private HeroNode head = new HeroNode(0, "");

  public HeroNode getHead() {
    return head;
  }

  public void reversalStack(HeroNode heroNode) {
    if (heroNode.next == null || heroNode.next.next == null) {
      System.out.println("链表为空");
      return ;
    }

    HeroNode cur = heroNode.next;
    Stack<HeroNode> stack = new Stack<>();
    while (cur != null) {
      stack.push(cur);
      cur = cur.next;
    }

    while (stack.size() > 0) {
      System.out.println(stack.pop());
    }
  }


  public void reversal(HeroNode heroNode) {
    if (heroNode == null || heroNode.next == null) {
      System.out.println("链表为空");
      return;
    }

    HeroNode current = heroNode.next;
    HeroNode next = null;
    HeroNode reverse = new HeroNode();
    while (current != null) {
      next = current.next;
      current.next = reverse.next;
      reverse.next = current;
      current = next;
    }
    heroNode.next = reverse.next;

  }



  */
/**
   *1、编写一个方法 接收head节点，同时接收一个index
   *2、index表示倒数第index个节点
   *3、先把链表从头到尾遍历一遍，获取链表的长度
   * 4、得到长度size后，我们从链表第一个开始遍历（size-index）个就可以得到
   *//*

  public void findIndex(HeroNode heroNode, Integer index) {
    if (heroNode.next == null) {
      System.out.println("链表为空");
      return;
    }

    HeroNode temp = heroNode;
    Integer length = 0;
    while (true) {
      if (temp.next == null) {
        break;
      }
      length++;
      temp = temp.next;
    }

    Integer length2 = 0;
    Integer index2 = length - index;
    HeroNode temp2 = heroNode;
    while (true) {
      if (temp2.next == null) {
        break;
      }
      length2++;
      if (length2 == index2) {
        temp2 = temp2.next;
      }
    }

  }

  */
/**
   * 添加节点到单项链表
   * 思路： 不考虑编号顺序时
   * 1、找到当前列表的最后节点
   * 2、将最后这个节点的next指向新的节点
   *//*

  public void add(HeroNode heroNode) {
    HeroNode temp = head;

    while (true) {
      if (temp.next == null) {
        break;
      }
      temp = temp.next;
    }

    temp.next = heroNode;

  }

  public void list() {

    if (head.next == null) {
      System.out.println("链表为空");
      return;
    }

    // 因为头节点不能动，因此我们需要一个辅助变量来遍历
    HeroNode temp = head.next;
    while (true) {
      // 判断是否到链表最后所以不能用temp.next， 因为最后一个temp.next是空的，用temp.next则不打印最后一个数据
      if (temp == null) {
        break;
      }
      System.out.println(temp);
      temp = temp.next;
    }
  }

  public void addByOrder(HeroNode heroNode) {
    // 因为头结点不能动，因此我们需要一个辅助变量来遍历
    // 因为单链表，因为我们找的temp是位于添加位置的第一个节点，否则插入不了

    // 下面做的不对
*/
/*    if (heroNode.next == null) {
      return;
    }
    boolean flag = false;
    HeroNode temp = head;
    while (temp.next != null) {
      if(temp.next.no > heroNode.no) {
        break;
      } else if (temp.next.no == heroNode.no) {
        flag = true;
      }
      temp = temp.next;
    }

    if (flag) {
      System.out.println("编号已存在");
    } else {

    }*//*

  }

}

class HeroNode{
  public int no;
  public HeroNode next;
  public String name;

  public HeroNode(int no, HeroNode next, String name) {
    this.no = no;
    this.next = next;
    this.name = name;
  }

  public HeroNode(int no, String name) {
    this.no = no;
    this.name = name;
  }

  public HeroNode () {

  }

  @Override
  public String toString() {
    return "HeroNode{" +
        "no=" + no +
        ", name='" + name + '\'' +
        '}';
  }
}*/
