package com.huinong.truffle.doraemon;

import com.huinong.truffle.doraemon.codegen.HnCodeGenerator;
import com.huinong.truffle.doraemon.codegen.HnJavaClientCodegen;
import com.huinong.truffle.doraemon.enums.ServiceEnum;
import com.huinong.truffle.doraemon.utils.ServiceUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.CodegenConstants;

public class Gen {

  public static void main(String[] args) throws Exception {

    //openApiV3 解析器
    OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();

    ServiceEnum serviceEnum = ServiceEnum.COINS;
    FileUtils.deleteDirectory(new File("src/main/java/com/huinong/truffle/doraemon/api/bean/" + ServiceUtils
        .serviceId2FeignClient(serviceEnum.getServiceId(), false)));
    FileUtils.deleteDirectory(new File("src/main/java/com/huinong/truffle/doraemon/api/feign/" + ServiceUtils
        .serviceId2FeignClient(serviceEnum.getServiceId(), false)));

    OpenAPI openAPI = openAPIV3Parser.read(serviceEnum.getUrl());

    String serviceId = serviceEnum.getServiceId().toLowerCase();
    ClientOptInput input = new ClientOptInput().config(new HnJavaClientCodegen(serviceId)).openAPI(openAPI);
    HnCodeGenerator apiCodegen = new HnCodeGenerator(serviceId);
    apiCodegen.setGeneratorPropertyDefault(CodegenConstants.APIS, "true");
    ;
    apiCodegen.setGeneratorPropertyDefault(CodegenConstants.MODELS, "true");
    apiCodegen.opts(input).generate();
  }


}
