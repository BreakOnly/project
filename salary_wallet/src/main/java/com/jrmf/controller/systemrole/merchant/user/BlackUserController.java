package com.jrmf.controller.systemrole.merchant.user;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.CertType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ExcelHead;
import com.jrmf.domain.Page;
import com.jrmf.domain.QbBlackUsers;
import com.jrmf.service.QbBlackUsersService;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.ImportExcel;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.SalaryConfigUtil;
import com.jrmf.utils.StringUtil;

@Controller
@RequestMapping("/merchant/blackUsers")
public class BlackUserController extends BaseController{
	
	private static Logger logger = LoggerFactory.getLogger(BlackUserController.class);

	@Autowired
	private QbBlackUsersService qbBlackUsersService;
	@Autowired
	private SalaryConfigUtil conf;

	/**
	 * 黑名单用户列表
	 * @param request
	 * @return
	 */
	@RequestMapping("/list")
	@ResponseBody
	public Map<String, Object> list(HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		//获取登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if(!StringUtil.isEmpty(page.getParams().get("customkey"))){
			page.getParams().put("loginCustomer", page.getParams().get("customkey"));
		}else{
			if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
				page.getParams().put("loginCustomer", customLogin.getMasterCustom());
			}else{
				page.getParams().put("loginCustomer", customLogin.getCustomkey());
			}
		}
		//查询黑名单用户总条数
		int total = qbBlackUsersService.queryBlackUsersCount(page);
		//查询黑名单用户集合
		List<Map<String, Object>> relationList = qbBlackUsersService.queryBlackUsers(page);
		result.put("total", total);
		result.put("relationList", relationList);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * 保存黑名单用户
	 * @param qBlackUsers
	 * @return
	 */
	@RequestMapping("/saveBlackUsers")
	@ResponseBody
	public Map<String, Object> saveBlackUsers(QbBlackUsers qBlackUsers,HttpServletRequest request,String customkey){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		//获取登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if(StringUtil.isEmpty(customkey)){		
			if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
				qBlackUsers.setCustomkey(customLogin.getMasterCustom());
			}else{
				qBlackUsers.setCustomkey(customLogin.getCustomkey());
			}
		}else{
			qBlackUsers.setCustomkey(customkey);
		}
		if(qBlackUsers.getId()!=null){
			//修改
			qBlackUsers.setUpdateTime(DateUtils.getNowDate());
			int respFlag = qbBlackUsersService.updateBlackUserById(qBlackUsers);
			if(respFlag>0){
				//修改成功
				result.put(RespCode.RESP_STAT, respstat);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			}else{
				//修改失败
				result.put(RespCode.RESP_STAT, RespCode.UPDATE_FAIL);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.UPDATE_FAIL));
			}
		}else{
			//新增
			int count = qbBlackUsersService.checkIsExists(qBlackUsers);
			if(count>0){
				//此用户已存在
				result.put(RespCode.RESP_STAT, RespCode.error103);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error103));
			}else{
				qBlackUsers.setAddUser(customLogin.getUsername());
				qBlackUsers.setCreateTime(DateUtils.getNowDate());
				int respFlag = qbBlackUsersService.insert(qBlackUsers);
				if(respFlag>0){
					result.put(RespCode.RESP_STAT, respstat);
					result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
				}else{
					result.put(RespCode.RESP_STAT, RespCode.INSERT_FAIL);
					result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INSERT_FAIL));
				}
			}
		}
		return result;
	}

	/**
	 * 删除黑名单用户
	 * @param qBlackUsers
	 * @param request
	 * @return
	 */
	@RequestMapping("/deleteBlackUser")
	@ResponseBody
	public Map<String, Object> deleteBlackUser(QbBlackUsers qBlackUsers,HttpServletRequest request,String customkey){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		//获取登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if(StringUtil.isEmpty(customkey)){		
			if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
				qBlackUsers.setCustomkey(customLogin.getMasterCustom());
			}else{
				qBlackUsers.setCustomkey(customLogin.getCustomkey());
			}
		}else{
			qBlackUsers.setCustomkey(customkey);
		}
		//逻辑删除黑名单用户
		int respFlag = qbBlackUsersService.updateStatusById(qBlackUsers);
		if(respFlag>0){
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		}else{
			result.put(RespCode.RESP_STAT, RespCode.DELETE_FAIL);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.DELETE_FAIL));
		}
		return result;
	}

	/**
	 * 批量导入黑名单用户信息
	 * @param file
	 * @param request
	 * @return
	 */
	@RequestMapping("/importBlackUsers")
	@ResponseBody
	public Map<String, Object> importBlackUsers(MultipartFile file,HttpServletRequest request,String customkey){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		//获取登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if(StringUtil.isEmpty(customkey)){			
			if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
				customkey=customLogin.getMasterCustom();
			}else{
				customkey=customLogin.getCustomkey();
			}
		}
		try{
			//校验黑名单模板格式
			boolean checkFileHead = checkExcelHead(file.getInputStream(), file.getOriginalFilename());
			if(checkFileHead){
				//设置excel标题和javaBean属性对应
				List<ExcelHead> excelHeads = new ArrayList<ExcelHead>();
				ExcelHead excelHead = new ExcelHead("姓名（必填）", "userName");
				ExcelHead excelHead1 = new ExcelHead("证件类型（必填）", "documentName");
				ExcelHead excelHead2 = new ExcelHead("身份证号（必填）", "certId");
				excelHeads.add(excelHead);
				excelHeads.add(excelHead1);
				excelHeads.add(excelHead2);
				int successCount =0;
				int fail = 0;
				//导入失败信息
				List<QbBlackUsers> failBlackList = new ArrayList<QbBlackUsers>();
				List<QbBlackUsers> blackUsers = ImportExcel.readExcelToEntity(QbBlackUsers.class, file.getInputStream(), file.getOriginalFilename(),excelHeads,1,2);
				if(blackUsers.size()>0){
					if(blackUsers.size()<=2000){
						for (QbBlackUsers qbBlackUser:blackUsers) {
							Integer documentType = returnDocumentType(qbBlackUser.getDocumentName());
							qbBlackUser.setCreateTime(DateUtils.getNowDate());
							qbBlackUser.setDocumentType(documentType);
							qbBlackUser.setAddUser(customLogin.getUsername());
							qbBlackUser.setCustomkey(customkey);
//							System.out.println("用户名为："+qbBlackUser.getUserName()+"证件号为："+qbBlackUser.getCertId());
							int count = qbBlackUsersService.checkIsExists(qbBlackUser);
							if(count>0){
								//计入失败重入list
								fail++;
								qbBlackUser.setLineNum(blackUsers.indexOf(qbBlackUser)+3);
								qbBlackUser.setErrorMsg("此种证件类型的证件号信息已存在");
								failBlackList.add(qbBlackUser);
							}else{
								//执行新增
								int respFlag = qbBlackUsersService.insert(qbBlackUser);
								if(respFlag>0){
									//新增成功
									successCount++;
								}else{
									//新增失败
									fail++;
									qbBlackUser.setLineNum(blackUsers.indexOf(qbBlackUser)+3);
									qbBlackUser.setErrorMsg("数据库添加失败");
									failBlackList.add(qbBlackUser);
								}
							}
						}
						if(successCount==blackUsers.size()){
							result.put("successCount",successCount);
							result.put(RespCode.RESP_STAT, respstat);
							result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
							result.put("hasError", false);
						}else if(fail==blackUsers.size()){
							result.put(RespCode.RESP_STAT, RespCode.IMPORT_ALL_FAIL);
							result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.IMPORT_ALL_FAIL));
						}else{
							result.put(RespCode.RESP_STAT, respstat);
							result.put("hasError", true);
							result.put("successCount",successCount);
							result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.IMPORT_PART_SUCCESS));
							result.put("failBlackList",failBlackList);
						}
					}else{
						result.put(RespCode.RESP_STAT, RespCode.IMPORT_NUMBER_ERROR);
						result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.IMPORT_NUMBER_ERROR));
					}
				}else{
					result.put(RespCode.RESP_STAT, RespCode.EXCEL_NO_INFO);
					result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.EXCEL_NO_INFO));
				}
			}else{
				result.put(RespCode.RESP_STAT, RespCode.BLACK_TEMPTYPE_ERROR);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.BLACK_TEMPTYPE_ERROR));
			}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			result.put(RespCode.RESP_STAT, RespCode.EXCEL_IMPORT_EXCEPTION);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.EXCEL_IMPORT_EXCEPTION));
		}
		return result;
	}

	/**
	 * 黑名单用户列表导出
	 * @param request
	 * @return
	 */
	@RequestMapping("/export")
	@ResponseBody
	public void export(HttpServletRequest request,HttpServletResponse response){
		// 标题
		String[] headers = new String[] {"用户名称", "证件类型","证件号","用户创建时间"}; 
		String filename = "商户黑名单用户信息"; 
		Page page = new Page(request);
		//获取登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if(!StringUtil.isEmpty(page.getParams().get("customkey"))){
			page.getParams().put("loginCustomer", page.getParams().get("customkey"));
		}else{
			if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
				page.getParams().put("loginCustomer", customLogin.getMasterCustom());
			}else{
				page.getParams().put("loginCustomer", customLogin.getCustomkey());
			}
		}
		//查询黑名单用户集合
		List<Map<String, Object>> relationList = qbBlackUsersService.queryBlackUsersNoPage(page);
		List<Map<String, Object>> data = new ArrayList<>();
		for (Map<String, Object> invioceBase : relationList) {
			Map<String, Object> dataMap = new HashMap<>(20);
			dataMap.put("1", invioceBase.get("userName"));
			dataMap.put("2", CertType.codeOf(Integer.parseInt(String.valueOf(invioceBase.get("documentType")))).getDesc());
			dataMap.put("3", invioceBase.get("certId"));
			dataMap.put("4",invioceBase.get("createTime"));

			data.add(sortMapByKey(dataMap));
		}
		ExcelFileGenerator.ExcelExport(response, headers, filename, data);
	}


	/**
	 * 模板下载
	 * @param qBlackUsers
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/downloadTemp")
	public ResponseEntity<byte[]>  downloadTemp(QbBlackUsers qBlackUsers,HttpServletRequest request) throws Exception{
		String fileName = "商户交易黑名单导入模板V1.0.xlsx";
		String filePath = "/blackTempFile/";
        byte[] bytes = FtpTool.downloadFtpFile(filePath,fileName);
        HttpHeaders headers = new HttpHeaders(); 
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);    
		fileName = new String(fileName.getBytes("gbk"), "iso8859-1");// 防止中文乱码
		headers.add("Content-Disposition", "attachment;filename=" + fileName);
		headers.setContentDispositionFormData("attachment", fileName);    
		return new ResponseEntity<byte[]>(bytes,    
				headers, HttpStatus.OK);  
	}

	/**
	 * 返回相应证件类型
	 * @param documentName
	 * @return
	 */
	public Integer returnDocumentType(String documentName){
		Integer documentType;
		if(documentName.equals("身份证")){
			documentType=1;
		}else if(documentName.equals("港澳台通行证")){
			documentType=2;
		}else if(documentName.equals("护照")){
			documentType=3;
		}else if(documentName.equals("军官证")){
			documentType=4;
		}else{
			documentType=1;
		}
		return documentType;
	}

	/**
	 * 校验黑名单excel头部是否合规
	 * @param in
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public boolean checkExcelHead(InputStream in, String fileName) throws IOException{
		boolean flag = false;
		Workbook workbook = ImportExcel.getWorkBoot(in,fileName);
		Sheet sheet = workbook.getSheetAt(0);
		XSSFRow row = (XSSFRow) sheet.getRow(1);
		if (row != null) {
			String alipayTemple = StringUtil.getXSSFCell(row.getCell(0))+ StringUtil.getXSSFCell(row.getCell(1)) + StringUtil.getXSSFCell(row.getCell(2));
			String confAliTemple = conf.getBlackUsersTempFormat();
			if (alipayTemple.equals(confAliTemple)) {
				flag=true;
			}
		}
		return flag;
	}


}
