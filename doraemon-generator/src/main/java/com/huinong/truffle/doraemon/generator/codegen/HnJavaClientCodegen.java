package com.huinong.truffle.doraemon.generator.codegen;

import com.huinong.truffle.doraemon.generator.utils.ServiceUtils;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.languages.SpringCodegen;

public class HnJavaClientCodegen extends SpringCodegen {

  /**
   * 初始化配置
   */
  public HnJavaClientCodegen(String serviceId) {
    super();
    modelPackage = "com.huinong.truffle.doraemon.api.bean." + ServiceUtils.serviceId2FeignClient(serviceId, false);
    apiPackage = "com.huinong.truffle.doraemon.api.feign." + ServiceUtils.serviceId2FeignClient(serviceId, false);
    apiTemplateFiles.put("feign.mustache", ".java");
    apiTemplateFiles.put("bean.mustache", ".java");
    apiTemplateFiles.put("enum.mustache", ".java");
    modelTemplateFiles.put("bean.mustache", ".java");
    templateDir = "templates";
    embeddedTemplateDir = "templates";
    library = "templates";
  }

  /**
   * 处理选项 可以自定义
   */
  @Override
  public void processOpts() {
    super.processOpts();
  }

  /**
   * 用来创造codegenModel里面包含了各种解析好的数据 可以自定义
   */
  @Override
  public CodegenModel fromModel(String name, Schema model) {
    return super.fromModel(name, model);
  }

  /**
   * 最后处理model 这里处理了import包 还有变量规范 可以自定义
   */
  @Override
  public Map<String, Object> postProcessModels(Map<String, Object> objs) {
    return super.postProcessModels(objs);
  }

  @Override
  public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
  }
}

