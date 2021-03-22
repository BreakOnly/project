package com.jrmf.utils.ocr;

import com.baidu.aip.ocr.AipOcr;
import com.baidu.aip.util.Util;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author linsong
 * @date 2019/6/12
 */
public class OCRUtil {
    /**
     * 设置APPID/AK/SK
     */
    private static final String APP_ID = "16895381";
    private static final String API_KEY = "YOS8NeW4xRutBPjcaYrBB6Zw";
    private static final String SECRET_KEY = "ZnAt76822VBEzoeFWgN3wN0vTdrhFMiG";
    public static final String IDCARD_SIDE_FRONT = "front";
    public static final String IDCARD_SIDE_BACK = "back";


    public static Map<String, Object> getIdCardResult(byte[] image, String idCardSide) {
        Map<String, Object> resultMap = new HashMap<>(4);
        if(image == null){
            resultMap.put("error_msg", "图片不存在");
            return resultMap;
        }
        if(image.length == 0){
            resultMap.put("error_msg", "图片解析失败");
            return resultMap;
        }
        AipOcr client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
        HashMap<String, String> options = new HashMap<>();
        options.put("detect_direction", "true");
        options.put("accuracy", "high");

        JSONObject res = client.idcard(image, idCardSide, options);


        if (res.has("error_code")) {
            resultMap = res.toMap();
            return resultMap;
        }

        //识别结果数,表示words_result的元素个数
        Integer wordsResultNum = (Integer) res.get("words_result_num");
        if (wordsResultNum == null || wordsResultNum < 3) {
//            resultMap.put("error_code", "");
            resultMap.put("error_msg", "请提供正确的身份证照片");
            return resultMap;
        }

        JSONObject wordsResult = res.getJSONObject("words_result");
        for (String key : wordsResult.keySet()) {
            JSONObject result = wordsResult.getJSONObject(key);
            String info = result.getString("words");
            switch (key) {
                case "姓名":
                    resultMap.put("name", info);
                    break;
                case "性别":
                    resultMap.put("sex", info);
                    break;
                case "民族":
                    resultMap.put("nation", info);
                    break;
                case "出生":
                    resultMap.put("birthday", info);
                    break;
                case "住址":
                    resultMap.put("address", info);
                    break;
                case "公民身份号码":
                    resultMap.put("idNumber", info);
                    break;
                case "签发机关":
                    resultMap.put("issuedOrganization", info);
                    break;
                case "签发日期":
                    resultMap.put("issuedAt", info);
                    break;
                case "失效日期":
                    resultMap.put("expiredAt", info);
                    break;
                default:
            }
        }
        return resultMap;
    }


    public static void main(String[] args) throws IOException {
        String url = "D:\\test\\1.jpg";
        String url2 = "D:\\test\\2.jpg";

        System.out.println(getIdCardResult(Util.readFileByBytes(url), IDCARD_SIDE_FRONT));
        System.out.println(getIdCardResult(Util.readFileByBytes(url2), IDCARD_SIDE_BACK));
    }

}
