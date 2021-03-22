package com.jrmf.sign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 远程调用服务测试
 * value=platform-cloud-wallet  表示调用注册名为platform-cloud-wallet的服务  /oem.do就代表访问platform-cloud-wallet的/oem.do
 * fallback为发生熔断时，快速失败的的处理类
 *
 * @author linsong
 */
@FeignClient(value = "platform-cloud-wallet", fallback = TestClientHystrix.class)
public interface TestClient {

    @GetMapping("/oem.do")
    String oem(String domain);

}
