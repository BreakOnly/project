package com.jrmf.sign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RefreshScope
public class RestTestController {

    @Value("${say}")
    private String say;

    @Autowired
    private TestClient testClient;

    /**
     * 远程调用服务测试
     *
     * @author linsong
     */
    @GetMapping("/testFeign")
    public String testFeign(String domain) {
        return testClient.oem(domain);
    }

    /**
     * 远程配置测试
     *
     * @author linsong
     */
    @GetMapping("/testConfig")
    public String testConfig() {
        return say;
    }

}
