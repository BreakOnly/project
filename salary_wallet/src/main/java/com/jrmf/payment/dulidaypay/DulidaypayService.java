package com.jrmf.payment.dulidaypay;


import com.alibaba.fastjson.JSON;
import com.duliday.openapi.OpenApiClient;
import com.duliday.openapi.param.QueryAmountParam;
import com.duliday.openapi.param.SinglePayResultParam;
import com.duliday.openapi.param.SinglePaymentParam;
import com.duliday.openapi.response.OpenApiBaseResponse;
import com.duliday.openapi.result.QueryAmountResult;
import com.duliday.openapi.result.SinglePayResultResult;
import com.duliday.openapi.result.SinglePaymentResult;
import com.duliday.openapi.utils.ParameterUtils;
import com.duliday.openapi.utils.RsaUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class DulidaypayService {

    static OpenApiClient client;

    public static void main(String[] args) {
        String priKeyString = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCgU5/qSyh7//3/bczDXjsndC+4GloLoJ1e/xZCF2WGCAfoQrZjS2iV2I0DgQPBddDSjdpNfSOVMlFUZwnWIh6Eje66n9LYPxSxkYi9wvmNxxIdOeHkm0oYiS08vy2ZB7PKKpnk0lUTDvV7JYArHCPb/89BPfoD6IVoOKS5EplXJiNk21IoDbDSw2Wnb8fJrIpCwf8FoHsbZEKM2WlE/HEQ2ivkD72afLdMUplXkvmD/0IDAL7HiN0hgqTB15d2yUdizbQuee4eZ+tJSH8ittm9bC+l/vFAHHy3/zNB0HJTf/dXcepJtoBqEnx7lyIipRNy6x5lq4CNIhsl5RcJuFAhAgMBAAECggEACOM53TIXZ3BUc0Fx9W6W1uQQ9SK41SYtMyPu5vEHFrwBPDXeiHiYrp3Xi1cujx3p2pI0hNEoFNA6Q/SGThGMIYsAreXIOWadzg4kR2i0jtB3R5l61UJqhBuun0x1yaLsq4tbBp1GTA0rPI1iooEn171BjvtRjb/pDVL+hqfqSjNjNyY0UJV6xb4l01ONJ+pgSn6LvCDnHodX0lmV/BXmw8NLXFC/G3RU8OpmCsSM5KDGqWE42EWynAnngtMI6DJ0jh8JZSRsk0Ze+LAZf7tB8MhiDpXFQRJtaX5qDzSNY4G6UdA8sPtnkX5Cb07uTBNByRpnU5P8Llgb2kEsTF7BsQKBgQDyIN1hPrlv2Vp3D4JPX07RLmMD+RvKNYd6h0aspXcAm19IlOTrk6PCcaV6NEzEAdqAZ7QzXOljYIoS6jbvt1mA1vfFdRdHeCV7v6s0E4G6pTKY3eEWwea6luDzH75wJoZbAsw/GoSW8FIlUIIiNIOwNP4dRAh2yrNtbVr8KpBUZQKBgQCpgwdZx08VsWZs+X7OWaDcmmhEEuK+m7NSuAhj3DMlgfK9kUGNBvUdWrADgPOBvTd6xzT+T3SkHzeY6G4mkZvVcbjLRH8RfvGYQyTs7gkalP75J0G7SbxiF0PpEJ9R3h3Fg7/GlHJZT6ASkM7YHe/uUJXzHDBogxBP1evxMEH7DQKBgCHZalmP9UinsDf42RTTlCc2Pu1tQ+9O8HO2ubClKS/SiM2S0zYD297xGFsvOJQmVi+hT9XmZVJZSrQ+PhPhJAgZBWZ7ahe5ujPEpeCP4ZEAS0SHsFIIJYNsWGJky7DOEfZjO76OrH6VdkZ1LNwvT0GxBt18h1pkGBVyNkgOg0LpAoGBAJk/dTjMQIo7HH9uvB5v10c5bQQGEuoBt4gUCrnFDJRfPAX3uUx3Osh0AWbZgMwNNYgRaq5zRN9PvaLGlzbVMw4vxsI1xa8ale5y3YI53Q8kHrM/s2RP0DjaEbI2LeMErOOxLYKpGAv4OZ4vPmESB6TrQ/VKydTikK+slacyWqRNAoGAWFkjbNrVrIch0uEUi+DpQGdtECxZUa3KoZi28F7xdwzlaV0VBz9hQ7t8us5+UjQe39l2kuRLYlDY2barW5XDRtRNkqtZ+n54YNoCbprzzcDy+I2DGY+p96V7g0SJ2VpnlTRNwJQ9oRu4h8pS1PwaMaqEf7p1hYnFpJ6i5e537XE=";
        client =
                new OpenApiClient.Builder().appId("bjylmf").privateKey(priKeyString).useSandbox().build();

//        singlePayment();
        singlePayResult("2020021400000171");
//        queryAmount();
    }

    /**
     * 单笔支付接口
     */
    public static void singlePayment() {
        SinglePaymentParam param = new SinglePaymentParam();
        param.setOutOrderNo(System.currentTimeMillis() + "");
        param.setPayeeAccount("6217000010067408917");
        param.setChannel("duliday");
        param.setPhone("13021138882");
        param.setAmount("1");
        param.setIdentity("350321199801178159");
        param.setPayeeRealName("林松");
        param.setRemark("测试");
//        param.setAttach("dulidaytest");
//        param.setAccountType("sub");
//        param.setSubAccountNo("20191217162538983541251");
//        param.setCallbackUrl("http://test.duliday.com/s/test/callback");

        OpenApiBaseResponse<SinglePaymentResult> response = client.execute(param);
        System.out.println(JSON.toJSONString(response, true));
    }

    /**
     * 单笔支付查询接口
     */
    public static void singlePayResult(String orderNo) {
        SinglePayResultParam param = new SinglePayResultParam();
//        param.setAccountType("master");
//        param.setSubAccountNo("20191211185251431161929");
        param.setOutOrderNo(orderNo);
//        param.setReqNo("9a15c030dd1042cb82a69b8ede14126e");

        OpenApiBaseResponse<SinglePayResultResult> response = client.execute(param);
        String sign = response.getSign();
        System.out.println("sign: " + sign);
        System.out.println(JSON.toJSONString(response, true));
//        signCheck(response);
    }

    public static void queryAmount() {
        QueryAmountParam param = new QueryAmountParam();
        OpenApiBaseResponse<QueryAmountResult> response = client.execute(param);
        System.out.println(JSON.toJSONString(response, true));
    }

    private void signCheck(OpenApiBaseResponse<SinglePayResultResult> response) {
        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqUCekClOMBYazG/ZomILY3etCf9ZYGbzx81UFWyrApr8R9v+xH6yC1knsJJfSNYgLrWWNXimbUOhBVKd8sA9MGscJllHaKjY2xwA7YFGmnqDlHG2Vbd3M8CsLfmNErwMfuf336Km2DmOgU7ilRXloej9/rchxSbolP/ecs6SOQYmkues85lQfRZY9ILmDYKwFSHMOrk8PV9pThaHrdrL0U8MjBMg9P+aX2hnHVGeebjNyZJn5BPRcWYEw/esPSEM5dYQ7MW9NyDInVrF8xmj+DNR5wAdCIaGf8RznLVozkfZByI+e1PsyBmmNywxbR9bc4pqxFDWki5kL6WtvRuUXwIDAQAB";
        PublicKey loadPublicKey = RsaUtils.loadPublicKey(publicKey);
        if (loadPublicKey == null) {
            return;
        }
        String content = ParameterUtils.objectToSignContent(response);
        System.out.println("signCheck:" + (StringUtils.isNotEmpty(publicKey)
                && RsaUtils.verifySignature(content, response.getSign(), loadPublicKey)));
    }

    public void testSignCheck() {
        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqUCekClOMBYazG/ZomILY3etCf9ZYGbzx81UFWyrApr8R9v+xH6yC1knsJJfSNYgLrWWNXimbUOhBVKd8sA9MGscJllHaKjY2xwA7YFGmnqDlHG2Vbd3M8CsLfmNErwMfuf336Km2DmOgU7ilRXloej9/rchxSbolP/ecs6SOQYmkues85lQfRZY9ILmDYKwFSHMOrk8PV9pThaHrdrL0U8MjBMg9P+aX2hnHVGeebjNyZJn5BPRcWYEw/esPSEM5dYQ7MW9NyDInVrF8xmj+DNR5wAdCIaGf8RznLVozkfZByI+e1PsyBmmNywxbR9bc4pqxFDWki5kL6WtvRuUXwIDAQAB";
        PublicKey loadPublicKey = RsaUtils.loadPublicKey(publicKey);
        if (loadPublicKey == null) {
            return;
        }
        String content = "{\"appId\":\"dulidaytest\",\"data\":{\"accountType\":\"master\",\"amount\":\"0.44\",\"attach\":\"dulidaytest\",\"channel\":\"duliday\",\"identity\":\"410381199412170519\",\"outOrderNo\":\"1574838889272\",\"payeeAccount\":\"6214180100001664714\",\"payeeRealName\":\"曹宇聪\",\"personalIncomeTax\":\"0.01\",\"phone\":\"17621721115\",\"remark\":\"服务费\"},\"method\":\"pay.singleOrder\",\"nonce\":\"4bc5fdf9ac0e48c982d6c498ad81888d\",\"signType\":\"RSA2\",\"timestamp\":\"2019-12-16 14:37:30\",\"version\":\"1.1\"}";
        String sign = "NkTIs3xq76niuXZi4hW0FMVI8HciP/liDBqplly4n86NTHypuYZaZ5nyfv0ycKCdqLNmmMxh5UOUgB4Ch5l7xfi8h5c76cxAaqyC3giJ1oZpM994dMMdDR4dg6a1zfJXRSN6ffVE1Px/X2EqqpZuqj6/n7SFPwfivZNQQVbr7VZzHI9MEtE0/ga197yRR6ahohExgA4uDsTSnKLKDgItfmyKlk64MbF15wAfNqrA6bx/nJHKpxNR7CIcAZQmGfShQkt9WSpMs9kkTQbLWNdCU1fU+PQftZWSSk8y3wFuVo42L3WK8QhTdwlxdotM5t6SFcoq/Tl8/gk56OOs0jrQUA==";
        System.out.println("signCheck:" + (StringUtils.isNotEmpty(publicKey)
                && RsaUtils.verifySignature(content, sign, loadPublicKey)));
    }

    public void test() {
        String privateKey = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCpQJ6QKU4wFhrMb9miYgtjd60J/1lgZvPHzVQVbKsCmvxH2/7EfrILWSewkl9I1iAutZY1eKZtQ6EFUp3ywD0waxwmWUdoqNjbHADtgUaaeoOUcbZVt3czwKwt+Y0SvAx+5/ffoqbYOY6BTuKVFeWh6P3+tyHFJuiU/95yzpI5BiaS56zzmVB9Flj0guYNgrAVIcw6uTw9X2lOFoet2svRTwyMEyD0/5pfaGcdUZ55uM3JkmfkE9FxZgTD96w9IQzl1hDsxb03IMidWsXzGaP4M1HnAB0IhoZ/xHOctWjOR9kHIj57U+zIGaY3LDFtH1tzimrEUNaSLmQvpa29G5RfAgMBAAECggEBAIe5zS4YIZ5IK6djzqf8bduPHhGVVswK41WHn/UqVpzZQpQxpEVYwFh/X2emiYi0RcyPS+yHWxbmE9tb3FPNu4P3cPSDxdL1DCnxdAECGWdJ9fh1k917Kbkzuh+ILowmhvLB/LG3Eor05QVnCCa8D5DeccjwfMFIKV1LLszsQLA+oRrpIyeJtuvjizlZ4XqcTeqasZMrzc9g8fH0J4YCItZA6QPKch7ZP6B8D2P9ocu9ztRZZtW4pVwGvyO4b6jR+zvP4uBSDM7OsWEMoUgg9a2MaY5dwPxzT74VAYz+wsaR7QotOZ5XEHnOw2oiuiRqsyPgHkT4P6L0gL2J9BIWLkkCgYEA6y9/3J2+zMxsDGXyOMf5192UlT6VthlQzJQqeY+Wz4/olqRHWUACOdTQom9sESH468WmCBGTcBGGiUTqeLRlbwhkY/pXGt/3vV2V3VvWvkGQNP7yLRfJSQcIFE3Yh4ITFw701Cpaac+lDmHPdUwKeKfCUi7wnSAg032XtCwBbosCgYEAuDtM+UsFWnfQAC8CLcr5Y3UdRD6jEtEkgtPCAq4cRausiiL0AEiCQ0+wNQaanInrNpnasjxQU6J/y0DSFD2yDfCj8lvmvCwrqKeYqvBNuggrQmF9OG0RQXWJaGv+yfBvriYDVv/r2p3bXzjffPXTvL7CjBJZ0OL6jgpjbOY7n/0CgYEAv1q/cvZaRvHQu+m6wl5bNzsPaLsqPqasajfns06USz9w5Bi4ALMm3Hz+YBu+VtZIXTVMmOYMCzQHPer3x8wnd8bhP8NxWel+/fGGPTw7JkPcHHKPAweGe8ef2D1afy7jb3B43aO0OsF7L49/p5V+M5tzbUDJ/14jt1BJvYvMgm8CgYBY9dmfb0hwfPZlgE2PCzoNepndQFnCVldJDTPb03M3eURhl6s3D8xG8I3nrP6vpDF+NiDSfClVmVwYK37F41y9OnwQJaxvJO0YDYW81TjTIeqxrR2oR5JTh4wIbk2j+YQUFar7Ma0T7bHmczIHxzStKXgL5ziTk2ble8Ky7SHRiQKBgQDBlfDVid0jCqsQZWt+pGOY0Y3OotAjz55j3F1b21jz4UUKuXNnRIop4fZWGdpZMqCwhyJLt/Tpy5KUg1jEqgWdNU8A4DvIKqDu3HT5N3a4Q73+uNoGJWHzk4sd9HKS+uE3rPE94CB5mrM/BKdGE3q661SaX92nB0Hhe4nQtAdU1A==";
        PrivateKey privateKey1 = RsaUtils.loadPrivateKey(privateKey);
        String content = "appId=dulidaytest&data={accountType=sub&amount=2&attach=dulidaytest&callbackUrl=http://test.duliday.com/s/test/callback&channel=duliday&identity=410381199412170519&outOrderNo=1576650059&outUserId=2&payeeAccount=6214180100001664714&payeeRealName=name&payerShowName=dulidaytest&payeeType=ALIPAY_USERID&personalIncomeTax=1&phone=17621721115&remark=ServiceCharge&subAccountNo=114}&method=pay.singleOrder&nonce=6zu2dujprqmi50od7way9qftjk50jknc&timestamp=2019-12-18 14:21:29&version=1.1";
        String signature = RsaUtils.signature(privateKey1, content);
        System.out.println(signature);
    }

}
