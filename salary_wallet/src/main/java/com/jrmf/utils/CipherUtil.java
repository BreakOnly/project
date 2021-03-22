/**
 *
 */
package com.jrmf.utils;

import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;

/**
 * @author zhangpeng
 * @version 创建时间：2013-9-17 上午11:54:56
 *
 *          对密码进行加密和验证的类
 */
public class CipherUtil {

    private static Md5PasswordEncoder encoder = new Md5PasswordEncoder();

    /** * 把inputString加密 */
    public static String generatePassword(String inputString, String userName) {

        return encoder.encodePassword(inputString, userName);
    }

    // public static String generatePassword(String inputString) {
    // return encoder.encodePassword(inputString, null);
    // }

    /**
     * 验证输入的密码是否正确
     *
     * @param password
     *            加密后的密码
     * @param inputString
     *            输入的字符串
     * @return 验证结果，TRUE:正确 FALSE:错误
     */
    public static boolean validatePassword(String password, String inputString,
                                           String userName) {

        return encoder.isPasswordValid(password, inputString, userName);

    }

    /** 对字符串进行MD5加密 */
    @SuppressWarnings("unused")
    private static String encodeByMD5(String originString, String userName) {
        if (originString != null) {
            try {
                String password = DigestUtils.md5Hex(originString);
                password = encrypt(password, userName);
                return password;

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    private static String encrypt(String data, String salt) {
        return DigestUtils.md5Hex(data + "{" + salt + "}");
    }

    public static void main(String ar[]) throws IOException {
        System.out.println(CipherUtil.encoder.encodePassword("123456",
                "10147219"));
        String hex = DigestUtils.md5Hex("123456" + "{sZlt5mE2XPGnYnr220T6}");
        System.out.println(hex);
    }

}
