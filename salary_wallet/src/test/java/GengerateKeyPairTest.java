import org.springframework.util.Base64Utils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;


public class GengerateKeyPairTest {

    public static void main(String[] args) {
//        String[] keys = generateKeyStringPEMFormat(2048);
//        System.out.println("==================>  public key");
//        System.out.println(keys[0]);
//        System.out.println("==================>  private key");
//        System.out.println(keys[1]);

        System.out.println(formatToPemPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhzWqkGyILc1pz/tCgi7ZKdpv0dVJfUs9olxJFu2PbX2CT4UcaIjwwQSV3J6gqgz6irbZrAJ9FYAUbXpN5Vr3JfV4blzTCru4VgIg+iR976/68C0dpdSLf6GmQ7D5YKlUabU2kT9XPA3Sfg94DuFR9pMyLzDgUVvQdoNbSfzDUHEussYuKNZhe4iSIpeZl18ilMnZxm62ce1efhuNsIaS8l1fqZGLhfUKzXQvgJZkM3nDBvZZVIY7FlReE78Z1dmTolnIb8K8902Qfaft49pZG85kd0c6zgaOtfChYtaoKl06Y2Yjenp9iO+G9ZXomTaqchlz+RlJAKJQSqZblH2HRwIDAQAB"));
    }

    public static String[] generateKeyStringPEMFormat(int keySize) {
        String[] keyPairs = generateKeyString(keySize);

        return new String[]{formatToPemPublicKey(keyPairs[0]), formatToPemPrivateKey(keyPairs[1])};
    }

    private static String formatToPemPublicKey(String publicKey) {
        return "-----BEGIN PUBLIC KEY-----\\n" + splitToPemParts(publicKey) + "-----END PUBLIC KEY-----";
    }

    private static String formatToPemPrivateKey(String privateKey) {
        return "-----BEGIN RSA PRIVATE KEY-----\\n" + splitToPemParts(privateKey) + "-----END RSA PRIVATE KEY-----";
    }


    private static String splitToPemParts(String key) {
        String result = "";
        int partLen = 64;
        int parts = key.length() / partLen;
        for (int i = 0; i < parts; i++) {
            result = result + key.substring(i * partLen, i * partLen + partLen) + "\\n";
        }
        if (key.length() % partLen > 0) {
            result = result + key.substring(parts * partLen) + "\\n";
        }
        return result;
    }

    /**
     * 生成RSA密钥对，获得公私钥的base64字符串
     *
     * @param keySize 密钥位数，1024 2048 3072 4096，默认2048
     * @return ["公钥","私钥"]
     */
    public static String[] generateKeyString(int keySize) {
        if (keySize != 1024 && keySize != 2048 && keySize != 3072 && keySize != 4096)
            keySize = 2048;

        try {
            KeyPair keyPair = generateKeyPair(keySize);

            String publicKey = Base64Utils.encodeToString(keyPair.getPublic().getEncoded());
            String privateKey = Base64Utils.encodeToString(keyPair.getPrivate().getEncoded());
            return new String[]{publicKey, privateKey};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生成RSA密钥对，获得KeyPair对象
     *
     * @param keySize 1024,2048,3072,4096，默认2048
     * @return
     */
    public static KeyPair generateKeyPair(int keySize) {
        if (keySize != 1024 && keySize != 2048 && keySize != 3072 && keySize != 4096)
            keySize = 2048;
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(keySize, new SecureRandom());
            KeyPair keyPair = keyPairGen.generateKeyPair();
            keyPair.getPublic().getEncoded();
            return keyPair;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
