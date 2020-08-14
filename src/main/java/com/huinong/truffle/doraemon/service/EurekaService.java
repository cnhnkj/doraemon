package com.huinong.truffle.doraemon.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import com.huinong.framework.autoconfigure.okhttp.HnOkHttpBuilder;
import com.huinong.framework.autoconfigure.okhttp.HnOkHttpClient;
import com.huinong.framework.autoconfigure.web.BaseResult;
import com.huinong.truffle.doraemon.domain.eureka.AllEurekaServices;
import java.util.Map;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EurekaService {

  @Resource
  private HnOkHttpClient hnOkHttpClient;

  private final static String eurekaAddress = "http://10.10.3.62:1111/eureka/apps";

  public BaseResult<AllEurekaServices> getAllEurekaServiceInfo() {
    try {
      Map<String, String> headers = Maps.newHashMap();
      headers.put("Accept", "application/json");
      TypeReference<AllEurekaServices> allEurekaServicesTypeReference = new TypeReference<>() {
      };

      HnOkHttpBuilder<AllEurekaServices> okHttpBuilder = new HnOkHttpBuilder<>(){};
      okHttpBuilder.setTypeReference(allEurekaServicesTypeReference);
      okHttpBuilder.setUrl(eurekaAddress);
      okHttpBuilder.setHeaders(headers);

      return hnOkHttpClient.get(okHttpBuilder);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return BaseResult.fail(-1, "请求出错");
    }
  }

}

