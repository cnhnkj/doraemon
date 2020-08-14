package com.huinong.truffle.doraemon.enums;

public enum ServiceEnum {

  COINS("COINS", "http://10.10.3.70:16310/v3/api-docs"),

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
