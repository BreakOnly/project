package com.jrmf.payment.zjpay;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Base64 {
    private static final Base64Encoder encoder = new Base64Encoder();

    public Base64() {
    }

    public static byte[] encode(byte[] data) {
        int len = (data.length + 2) / 3 * 4;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(len);

        try {
            encoder.encode(data, 0, data.length, outputStream);
        } catch (IOException var4) {
            throw new RuntimeException("exception encoding base64 string: " + var4);
        }

        return outputStream.toByteArray();
    }

    public static int encode(byte[] data, OutputStream out) throws IOException {
        return encoder.encode(data, 0, data.length, out);
    }

    public static int encode(byte[] data, int off, int length, OutputStream out) throws IOException {
        return encoder.encode(data, off, length, out);
    }

    public static byte[] decode(byte[] data) {
        int len = data.length / 4 * 3;
        ByteArrayOutputStream bOut = new ByteArrayOutputStream(len);

        try {
            encoder.decode(data, 0, data.length, bOut);
        } catch (IOException var4) {
            throw new RuntimeException("exception decoding base64 string: " + var4);
        }

        return bOut.toByteArray();
    }

    public static byte[] decode(String data) {
        int len = data.length() / 4 * 3;
        ByteArrayOutputStream bOut = new ByteArrayOutputStream(len);

        try {
            encoder.decode(data, bOut);
        } catch (IOException var4) {
            throw new RuntimeException("exception decoding base64 string: " + var4);
        }

        return bOut.toByteArray();
    }

    public static int decode(String data, OutputStream out) throws IOException {
        return encoder.decode(data, out);
    }

    public static String encode(String data, String charSet) throws UnsupportedEncodingException {
        return StringUtil.isEmpty(data) ? "" : new String(encode(data.getBytes(charSet)), charSet);
    }

    public static String decode(String data, String charSet) throws UnsupportedEncodingException {
        return StringUtil.isEmpty(data) ? "" : new String(decode(data.getBytes(charSet)), charSet);
    }

    public static boolean matchBase64(String plaintext) {
        String regex = "[A-Za-z0-9,/,=,+]+";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(plaintext);
        return m.matches();
    }
}
