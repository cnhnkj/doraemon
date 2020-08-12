package com.huinong.truffle.doraemon.domain.eureka;

import java.util.List;
import lombok.Data;

@Data
public class AllEurekaServices {

  private Applications applications;

  @Data
  public static class Applications {
    private List<EurekaServiceInfo> application;
  }
}
