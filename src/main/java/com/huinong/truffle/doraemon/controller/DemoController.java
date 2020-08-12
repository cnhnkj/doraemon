package com.huinong.truffle.doraemon.controller;

import com.huinong.framework.autoconfigure.web.BaseResult;
import com.huinong.truffle.doraemon.domain.eureka.AllEurekaServices;
import com.huinong.truffle.doraemon.service.EurekaService;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

  @Resource
  EurekaService eurekaService;


  @GetMapping("/getAllEurekaServiceInfo")
  public BaseResult<AllEurekaServices> getAllEurekaServiceInfo(){
    return eurekaService.getAllEurekaServiceInfo();
  }



}
