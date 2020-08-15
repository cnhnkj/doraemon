package com.huinong.truffle.doraemon.generator.utils;

import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import org.springframework.util.CollectionUtils;

public final class PathUtils {

  public static final List<String> BEAN_BASE_DIR = Lists
      .newArrayList("doraemon-api", "src", "main", "java", "com", "huinong", "truffle", "doraemon", "api", "bean");
  public static final List<String> FEIGN_BASE_DIR = Lists
      .newArrayList("doraemon-api", "src", "main", "java", "com", "huinong", "truffle", "doraemon", "api", "feign");


  public static String combinePath(List<String> args) {
    if (CollectionUtils.isEmpty(args)) {
      throw new IllegalArgumentException("path is empty.");
    }
    return String.join(File.separator, args);
  }
}
