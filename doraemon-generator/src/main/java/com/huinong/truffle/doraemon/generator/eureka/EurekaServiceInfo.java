package com.huinong.truffle.doraemon.generator.eureka;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class EurekaServiceInfo {
    private String name;
    private List<EurekaInstanceInfo> instance;

    @Data
    public static class EurekaInstanceInfo {
        private String instanceId;
        private String app;
        private String ipAddr;
        private String status;
        private String homePageUrl;
        private String statusPageUrl;
        private Map<String, String> metadata;
    }

}


