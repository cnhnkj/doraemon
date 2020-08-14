package com.huinong.truffle.doraemon;

import com.huinong.truffle.doraemon.codegen.HnCodeGenerator;
import com.huinong.truffle.doraemon.codegen.HnJavaClientCodegen;
import com.huinong.truffle.doraemon.enums.ServiceEnum;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.CodegenConstants;

public class Gen {

  public static void main(String[] args) {
    //openApiV3 解析器
    OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();

//    ServiceEnum serviceEnum = ServiceEnum.COINS;
    ServiceEnum serviceEnum = ServiceEnum.DEMETER;

    OpenAPI openAPI = openAPIV3Parser.read(serviceEnum.getUrl());

    String serviceId = serviceEnum.getServiceId().toLowerCase();
    ClientOptInput input = new ClientOptInput().config(new HnJavaClientCodegen(serviceId)).openAPI(openAPI);
    HnCodeGenerator apiCodegen = new HnCodeGenerator(serviceId);
    apiCodegen.setGeneratorPropertyDefault(CodegenConstants.APIS, "true");;
    apiCodegen.setGeneratorPropertyDefault(CodegenConstants.MODELS, "true");
    apiCodegen.opts(input).generate();
  }


}
