package com.jrmf.sign;


import org.springframework.stereotype.Component;

/**
 * TestClient调用服务发生熔断时执行的快速失败逻辑
 *
 * @param
 * @author linsong
 */
@Component
public class TestClientHystrix implements TestClient {

    @Override
    public String oem(String domain) {
        return "oem个毛，熔断了，完蛋了！";
    }

}
