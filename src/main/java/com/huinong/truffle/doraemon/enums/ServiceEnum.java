package com.huinong.truffle.doraemon.enums;

public enum ServiceEnum {

  COINS("COINS", "http://10.10.3.70:16310/v3/api-docs"),
  DEMETER("DEMETER", "http://10.10.3.89:16304/v3/api-docs"),
  PARADISE("PARADISE", "http://10.10.3.59:7772/v3/api-docs"),
  USER_VIP("USER-VIP", "http://10.10.3.86:15108/v3/api-docs"),

  ;

  private final String serviceId;

  private final String url;

  public String getServiceId() {
    return this.serviceId;
  }

  public String getUrl() {
    return this.url;
  }

  private ServiceEnum(String serviceId, String url) {
    this.serviceId = serviceId;
    this.url = url;
  }
}
