package com.jrmf.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

/**
 * 图片转化成base64字符串
 */
@SuppressWarnings("restriction")
public class Base64Img {

    private static Logger logger = LoggerFactory.getLogger(Base64Img.class);

	// MultipartFile转化成图片
	public static String multipartImage(String userName,String password,String ftpURL, byte[] fileBytes, String pathName, String time, String picName) {
		String uploadFile = "error";
		InputStream in = new ByteArrayInputStream(fileBytes);
		String path = "/companyImg/" + DateUtils.getNowMonth() + "/" + pathName + "/" + time + "/";
        uploadFile = FtpTool.uploadFile(ftpURL, 21, path, picName, in, userName, password);

		// 生成图片
		if ("success".equals(uploadFile)) {
			return path + picName;
		} else {
			return "";
		}
	}

	/**
	 * 清空文件夹
	 *
	 * @param path
	 * @return
	 */
	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
                boolean delete = temp.delete();
                logger.info("删除文件："+delete);
            }
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				// delFolder(path + "/" + tempList[i]);// 再删除空文件夹
				flag = true;
			}
		}
		return flag;
	}
}
