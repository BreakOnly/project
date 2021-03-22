package com.jrmf.taxsettlement.api.security.sign;


import org.springframework.util.Base64Utils;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * 李松
 * RSA2(SHA256WithRSA)算法 签名/验签
 */
public class RSA2SignWorker extends AbstractSignWorker {

    private static final String SIGN_NAME = "SHA256WithRSA";

    @Override
    protected boolean verifySign(String sortKVStr, String verifyKey, String sign) throws Exception {
        return doCheck(sortKVStr.getBytes(StandardCharsets.UTF_8), sign, getPublicKey(verifyKey));
    }

    @Override
    public boolean verifySign(byte[] byteArrayData, String verificationKey, String sign) throws Exception {
        return doCheck(byteArrayData, sign, getPublicKey(verificationKey));
    }

    @Override
    protected String generateSign(String sortKVStr, String signGenerationKey) throws Exception {
        return sign(sortKVStr.getBytes(StandardCharsets.UTF_8), getPrivateKey(signGenerationKey));
    }

    @Override
    public String generateSign(byte[] byteArrayData, String generationKey) throws Exception {
        return sign(byteArrayData, getPrivateKey(generationKey));
    }

    private static boolean doCheck(byte[] content, String sign, PublicKey publicKey) throws Exception {

        Signature signature = Signature.getInstance(SIGN_NAME);

        signature.initVerify(publicKey);
        signature.update(content);
        byte[] tmp = Base64Utils.decodeFromString(sign);

        boolean bverify = signature.verify(tmp);
        return bverify;
    }

    private static PublicKey getPublicKey(String publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException {

        byte[] keyBytes = Base64Utils.decodeFromString(publicKey);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        return pubKey;
    }

    private static PrivateKey getPrivateKey(String privateKey) throws Exception {

        byte[] keyBytes = Base64Utils.decodeFromString(privateKey);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        PrivateKey priKey = keyFactory.generatePrivate(keySpec);
        return priKey;
    }

    private static String sign(byte[] content, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, UnsupportedEncodingException, InvalidKeyException {
        Signature signature = Signature.getInstance(SIGN_NAME);
        signature.initSign(privateKey);
        signature.update(content);
        byte[] signed = signature.sign();
        return Base64Utils.encodeToString(signed);
    }
}
