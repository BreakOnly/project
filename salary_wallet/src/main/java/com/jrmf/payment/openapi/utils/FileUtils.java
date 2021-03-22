/**
 * 
 */
package com.jrmf.payment.openapi.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Napoleon.Chen
 * @date 2018年12月17日
 */
public class FileUtils {

	public static byte [] getByteByFile(String filePath) {
        return getByteByFile(new File(filePath));
	}

    public static byte [] getByteByFile(File file) {
        FileInputStream fis = null;
        ByteArrayOutputStream swapStream = null;
        try {
            fis = new FileInputStream(file);
            swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[100];
            int rc = 0;
            while ((rc = fis.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            swapStream.flush();
            return swapStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        } finally {
            try {
                if (swapStream != null) {
                    swapStream.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
            }
        }
    }
	
}
