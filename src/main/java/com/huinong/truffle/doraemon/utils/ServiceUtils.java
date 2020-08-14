package com.huinong.truffle.doraemon.utils;

public class ServiceUtils {

  public static String serviceId2FeignClient(String serviceId, boolean firstUpper) {
    if(firstUpper) {
      serviceId = serviceId.substring(0, 1).toUpperCase() + serviceId.substring(1);
    }
    int index = serviceId.indexOf("-");
    while (index >= 0) {
      serviceId =
          serviceId.substring(0, index) + serviceId.substring(index + 1, index + 2).toUpperCase() + serviceId
              .substring(index + 2);
      index = serviceId.indexOf("-");
    }

    index = serviceId.indexOf("_");
    while (index >= 0) {
      serviceId =
          serviceId.substring(0, index - 1) + serviceId.substring(index, 1).toUpperCase() + serviceId.substring(index + 1);
      index = serviceId.indexOf("_");
    }
    return serviceId;
  }
}
