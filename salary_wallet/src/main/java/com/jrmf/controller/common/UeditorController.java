package com.jrmf.controller.common;


import com.jrmf.common.APIResponse;
import com.jrmf.common.ResponseCodeMapping;
import com.jrmf.controller.BaseController;

import com.jrmf.ueditor.ActionEnter;
import com.jrmf.utils.FtpTool;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/ueditor")
public class UeditorController extends BaseController {

  private Logger logger = LoggerFactory.getLogger(UeditorController.class);

  @Value("${ftppath}")
  private String basePath;

  @RequestMapping(value = "/exec")
  @ResponseBody
  public String exec(HttpServletRequest request, HttpServletResponse response,
      MultipartFile upfile) {

    String exec = null;
//        response.setContentType("application/json");
    try {
      String actionType = request.getParameter("action");
      if ("uploadimage".equals(actionType) && !upfile.isEmpty()) {
        // 做图片上传操作
        exec = upload(upfile);
      } else if ("uploadfile".equals(actionType) && !upfile.isEmpty()) {
        // 做文件上传操作
        exec = upload(upfile);
      } else {
        String rootPath = request.getSession().getServletContext().getRealPath("/");
        request.setCharacterEncoding("utf-8");
        exec = new ActionEnter(request, rootPath).exec();
      }
//            PrintWriter writer = response.getWriter();
//            writer.write(exec);
//            writer.flush();
//            writer.close();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }

    return exec;
  }

  /**
   * ueditor文件上传
   */
//    @PostMapping(value = "/ueditor/upload")
//    public Map<String, Object> editorUpload(MultipartFile upfile) {
//        HashMap<String, Object> result = new HashMap<>();
//
//        try {
//
//            if (!upfile.isEmpty()) {
//
//                long size = upfile.getSize();  // 文件大小
//                String fileName = UUID.randomUUID().toString() + FilenameUtils.getExtension(upfile.getOriginalFilename());
//
//                String uploadPath = "/ueditor/upload/";
//                boolean state = FtpTool.uploadFile(uploadPath, fileName, upfile.getInputStream());
//
//                if (state) {
//                    result.put("original", upfile.getOriginalFilename());//原来的文件名
//                    result.put("size", size); // 文件大小
//                    result.put("title", upfile.getOriginalFilename()); // 鼠标经过图片时显示的文字
//                    result.put("type", FilenameUtils.getExtension(upfile.getOriginalFilename())); // 文件后缀名
//                    result.put("url", basePath + uploadPath + fileName);// 上传后文件的完整地址（http://ip:端口/***/***/***.jpg）
//
//                    return returnSuccess(result);
//                }
//            }
//
//            return returnFail(RespCode.error101, RespCode.UPLOAD_ERROR);
//
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            return returnFail(RespCode.error101, RespCode.UPLOAD_ERROR);
//        }
//    }


  /**
   * 文件上传
   */
  private String upload(MultipartFile upfile) {
    JSONObject result = new JSONObject(resultMap("文件上传失败", "", 0, "", "", ""));
    try {
      if (!upfile.isEmpty()) {

        long size = upfile.getSize();  // 文件大小
        String fileName = UUID.randomUUID().toString() + "." + FilenameUtils
            .getExtension(upfile.getOriginalFilename());

        String uploadPath = "/ueditor/upload/";
        boolean state = FtpTool.uploadFile(uploadPath, fileName, upfile.getInputStream());

        if (state) {
          result = new JSONObject(resultMap("SUCCESS", basePath + uploadPath + fileName, size,
              upfile.getOriginalFilename(), upfile.getOriginalFilename(),
              FilenameUtils.getExtension(upfile.getOriginalFilename())));
        }
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }

    return result.toString();
  }

  /**
   * 文件上传
   */
  private APIResponse commonUpload(MultipartFile upfile,
      @RequestParam(required = false) String bizPath) {
    try {
      if (!upfile.isEmpty()) {

        long size = upfile.getSize();  // 文件大小
        String fileName = UUID.randomUUID().toString() + "." + FilenameUtils
            .getExtension(upfile.getOriginalFilename());
        String uploadPath = "/common/upload/";
        if (bizPath != null && !"".equals(bizPath)) {
          uploadPath = bizPath;
        }
        boolean state = FtpTool.uploadFile(uploadPath, fileName, upfile.getInputStream());

        if (state) {
          JSONObject jsonObject = new JSONObject(
              resultMap("SUCCESS", basePath + uploadPath + fileName, size,
                  upfile.getOriginalFilename(), upfile.getOriginalFilename(),
                  FilenameUtils.getExtension(upfile.getOriginalFilename())));
          return APIResponse.successResponse(jsonObject);
        }
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }

    return APIResponse.errorResponse(ResponseCodeMapping.ERR_500);
  }

  private Map<String, Object> resultMap(String state, String url, long size, String title,
      String original, String type) {
    Map<String, Object> result = new HashMap<>();
    result.put("state", state);
    result.put("original", original);
    result.put("size", size);
    result.put("title", title);
    result.put("type", type);
    result.put("url", url);
    return result;
  }


}
