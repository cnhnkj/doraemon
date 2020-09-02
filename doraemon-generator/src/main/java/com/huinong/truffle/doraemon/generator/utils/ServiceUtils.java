package com.huinong.truffle.doraemon.generator.utils;

public class ServiceUtils {

  public static String serviceId2FeignClient(String serviceId, boolean firstUpper) {
    if(firstUpper) {
      serviceId = serviceId.substring(0, 1).toUpperCase() + serviceId.substring(1);
    }
    serviceId = replaceChar(serviceId, "-");
    serviceId = replaceChar(serviceId, "_");
    return serviceId;
  }

  public static String pathToFunctionName(String path) {
    path = path.replace("{", "").replace("}", "");
    path = replaceChar(path, "-");
    path = replaceChar(path, "_");
    path = replaceChar(path, "/");
    path = path.substring(0, 1).toLowerCase() + path.substring(1);
    return path;
  }

  private static String replaceChar(String str, String c) {
    int index = str.indexOf(c);
    while (index >= 0) {
      str =
          str.substring(0, index) + str.substring(index + 1, index + 2).toUpperCase() + str
              .substring(index + 2);
      index = str.indexOf(c);
    }

    return str;
  }
}
