import com.jrmf.taxsettlement.api.security.sign.RSA2SignWorker;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class RSA2SignWorkerTest {


    @Test
    public void testSignAndVerify() throws Exception {

        String[] keyPairs = GengerateKeyPairTest.generateKeyString(2048);
        String publicKey = keyPairs[0];
        String privateKey = keyPairs[1];

        String content = "测试RSA2签名验签";
        RSA2SignWorker signWorker = new RSA2SignWorker();
        String sign = signWorker.generateSign(content.getBytes(StandardCharsets.UTF_8), privateKey);

        boolean verifyResult = signWorker.verifySign(content.getBytes(StandardCharsets.UTF_8), publicKey, sign);
        Assert.assertTrue(verifyResult);

    }
}
