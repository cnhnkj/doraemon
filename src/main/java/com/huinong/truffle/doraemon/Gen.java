package com.huinong.truffle.doraemon;

import com.huinong.truffle.doraemon.codegen.HnCodeGenerator;
import com.huinong.truffle.doraemon.codegen.HnJavaClientCodegen;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.IOException;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.CodegenConstants;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.languages.SpringCodegen;
import org.openapitools.codegen.languages.features.CXFServerFeatures;

public class Gen {

  public static void main(String[] args) throws IOException {
    //openApiV3 解析器
    OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();

    OpenAPI openAPI = openAPIV3Parser.read("http://10.10.3.70:16310//v3/api-docs");

    ClientOptInput input = new ClientOptInput().config(new HnJavaClientCodegen("coins")).openAPI(openAPI);
    HnCodeGenerator apiCodegen = new HnCodeGenerator("coins");
    apiCodegen.setGeneratorPropertyDefault(CodegenConstants.APIS, "true");;
    apiCodegen.setGeneratorPropertyDefault(CodegenConstants.MODELS, "true");
    apiCodegen.opts(input).generate();
  }


}
