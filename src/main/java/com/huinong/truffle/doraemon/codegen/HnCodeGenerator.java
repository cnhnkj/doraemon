package com.huinong.truffle.doraemon.codegen;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huinong.truffle.doraemon.utils.PathUtils;
import io.swagger.v3.oas.models.media.Schema;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.utils.ModelUtils;
import org.springframework.util.CollectionUtils;

@Slf4j
public class HnCodeGenerator extends DefaultGenerator {

  public String serviceId;

  public HnCodeGenerator(String serviceId) {
    this.serviceId = serviceId;
  }

  @Override
  public List<File> generate() {

    createModel();
    createFeignApi();

    return null;
  }


  private void createModel() {

    final Map<String, Schema> schemas = ModelUtils.getSchemas(this.openAPI);

    Optional.ofNullable(schemas).ifPresent(s -> {
      Set<String> modelKeys = s.keySet();
      List<Map<String, Object>> objects = Lists.newArrayList();

      for (String name : modelKeys) {
        Schema schema = schemas.get(name);
        Map<String, Schema> schemaMap = new HashMap<>();
        schemaMap.put(name, schema);
        Map<String, Object> model = processModels(config, schemaMap);
        if(!CollectionUtils.isEmpty(model)) {
          objects.add(model);
        }
      }

      objects.forEach(c -> {
        if (c.get("className") != null) {
          String beanOutputFilePath = PathUtils
              .combinePath("src", "main", "java", "com", "huinong", "truffle", "doraemon", "api",
                  "bean",
                  c.get("className") + ".java");
        try {
          super.processTemplateToFile(c, "bean.mustache", beanOutputFilePath);


        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      });
    });


  }

  private Map<String, Object> processModels(CodegenConfig config, Map<String, Schema> definitions) {
    Map<String, Object> objs = new HashMap<>();
    objs.put("modelPackage", config.modelPackage());

    for (String key : definitions.keySet()) {
      if(key.startsWith("BaseResult")) {
        continue;
      }

      Schema schema = definitions.get(key);
      if (schema == null) {
        throw new RuntimeException("schema cannot be null in processModels");
      }
      CodegenModel cm = config.fromModel(key, schema);

      objs.put("className", key);

      Optional.ofNullable(cm.getVars()).ifPresent(c -> {
        List<Map<String, String>> fieldList = Lists.newArrayList();
        Set<String> allImports = new LinkedHashSet<>();

        c.forEach(v -> {
          HashMap<String, String> fieldMap = Maps.newHashMap();
          fieldMap.put("type", v.dataType);
          fieldMap.put("field", v.baseName);
          fieldMap.put("description", Optional.ofNullable(v.description).orElse(""));
          allImports.addAll(cm.imports);
          fieldList.add(fieldMap);
        });

        Set<String> importSet = new TreeSet<>();
        for (String nextImport : allImports) {
          String mapping = config.importMapping().get(nextImport);
          if (mapping == null) {
            mapping = config.toModelImport(nextImport);
          }
          if (mapping != null && !config.defaultIncludes().contains(mapping)) {
            importSet.add(mapping);
          }
          // add instantiation types
          mapping = config.instantiationTypes().get(nextImport);
          if (mapping != null && !config.defaultIncludes().contains(mapping)) {
            importSet.add(mapping);
          }
        }
        List<Map<String, String>> imports = new ArrayList<>();
        for (String s : importSet) {
          Map<String, String> item = new HashMap<>();
          item.put("import", s);
          imports.add(item);
        }

        objs.put("importList", imports);
        objs.put("fieldList", fieldList);

      });
    }

    config.postProcessModels(objs);
    return objs;
  }

  private void createFeignApi() {
    Map<String, List<CodegenOperation>> paths = processPaths(this.openAPI.getPaths());
    Map<String, Object> result = Maps.newHashMap();

    String feignClientName = serviceId2FeignClient(serviceId);
    result.put("serviceName", serviceId);
    result.put("apiPackage", config.apiPackage());
    result.put("feignClientName", feignClientName);

    List<Map<String, String>> imports = new ArrayList<>();
    List<Map<String, Object>> methods = new ArrayList<>();

    for (String tag : paths.keySet()) {
      List<CodegenOperation> ops = paths.get(tag);

      for (CodegenOperation codegenOperation : ops) {

        Map<String, Object> operation = Maps.newHashMap();

        operation.put("operation", codegenOperation);

        String returnObject = codegenOperation.returnType;

        if(returnObject.startsWith("BaseResultList")) {
          returnObject = returnObject.replace("BaseResultList", "");
          operation.put("returnObject", returnObject);
          operation.put("returnObjectList", "1");
        }


        if(returnObject.startsWith("BaseResult")) {
          returnObject = returnObject.replace("BaseResult", "");
          operation.put("returnObject", returnObject);
        }




        //获取需要import的包
        Set<String> allImports = codegenOperation.imports;
        allImports.addAll(codegenOperation.imports);

        Set<String> mappingSet = new TreeSet<>();
        for (String nextImport : allImports) {
          Map<String, String> im = new LinkedHashMap<>();
          if(nextImport.startsWith("BaseResultList")) {
            nextImport = nextImport.replace("BaseResultList", "");
          }

          if(nextImport.startsWith("BaseResult")) {
            nextImport = nextImport.replace("BaseResult", "");
          }

          if(nextImport.equals("Integer") || nextImport.equals("Long") || nextImport.equals("Void")) {
            continue;
          }

          String mapping = config.importMapping().get(nextImport);
          if (mapping == null) {
            mapping = config.toModelImport(nextImport);
          }

          if (mapping != null && !mappingSet
              .contains(mapping)) { // ensure import (mapping) is unique
            mappingSet.add(mapping);

            im.put("import", mapping);
            im.put("classname", nextImport);
            if (!imports.contains(im)) { // avoid duplicates
              imports.add(im);
            }
          }
        }
        methods.add(operation);
      }
      result.put("importList", imports);
      result.put("operations", methods);
    }
    String feignOutputFilePath = PathUtils
        .combinePath("src", "main", "java", "com", "huinong", "truffle", "doraemon", "api",
            "feign",
            feignClientName + "Feign.java");

    String feignFallbackFactoryOutputFilePath = PathUtils
        .combinePath("src", "main", "java", "com", "huinong", "truffle", "doraemon", "api",
            "feign",
            feignClientName + "FeignFallbackFactory" + ".java");
    try {
      super.processTemplateToFile(result, "feign.mustache", feignOutputFilePath);
      super.processTemplateToFile(result, "feignFallbackFactory.mustache", feignFallbackFactoryOutputFilePath);
    } catch (IOException e) {
      e.printStackTrace();
      log.info("load file error message is {}", e.getMessage());
    }

  }

  private String serviceId2FeignClient(String serviceId) {
    serviceId = serviceId.substring(0, 1).toUpperCase() + serviceId.substring(1);
    int index = serviceId.indexOf("-");
    while(index >= 0) {
      serviceId =
          serviceId.substring(0, index) + serviceId.substring(index + 1, index + 2).toUpperCase() + serviceId.substring(index + 2);
      index = serviceId.indexOf("-");
    }

    index = serviceId.indexOf("_");
    while(index >= 0) {
      serviceId =
          serviceId.substring(0, index - 1) + serviceId.substring(index, 1).toUpperCase() + serviceId.substring(index + 1);
      index = serviceId.indexOf("_");
    }
    return serviceId;
  }


}
