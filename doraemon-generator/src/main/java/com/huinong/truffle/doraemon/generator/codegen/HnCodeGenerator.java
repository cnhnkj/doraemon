package com.huinong.truffle.doraemon.generator.codegen;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huinong.truffle.doraemon.generator.utils.PathUtils;
import com.huinong.truffle.doraemon.generator.utils.ServiceUtils;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import joptsimple.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.utils.ModelUtils;
import org.springframework.util.CollectionUtils;

@Slf4j
public class HnCodeGenerator extends DefaultGenerator {

  private static final List<String> SKIP_URL_PREFIX = Lists.newArrayList("/api", "/hsww", "/openapi");

  private static final List<String> SKIP_IMPORT_COLLECTION_TYPE = Lists.newArrayList("List", "Set", "Map");

  private static final List<String> SKIP_IMPORT_BASIC_TYPE = Lists.newArrayList("Integer", "Long", "Void", "Boolean", "Double", "String", "Object");

  private String serviceId;

  private List<String> excludeUrl = Lists.newArrayList();

  public HnCodeGenerator(String serviceId) {
    this.serviceId = serviceId;
  }

  public HnCodeGenerator(String serviceId, List<String> excludeUrl) {
    this.serviceId = serviceId;
    this.excludeUrl = excludeUrl;
  }


  @Override
  public List<File> generate() {
    createFeignApi();
    return null;
  }


  private void createModelByName(String modelName) {
    createModelByName(modelName, false);
  }

  private void createModelByName(String modelName, boolean responseModel) {
    List<Map<String, Object>> objects = Lists.newArrayList();
    Map<String, Schema> schemaMap = new HashMap<>();

    Schema schema = ModelUtils.getSchema(this.openAPI, modelName);
    if (schema == null && SKIP_IMPORT_BASIC_TYPE.stream().noneMatch(type -> type.equalsIgnoreCase(modelName))) {
      if(responseModel) {
        schema = ModelUtils.getSchema(this.openAPI, "BaseResult" + modelName);
        if(schema.getType().equalsIgnoreCase("object") && !CollectionUtils.isEmpty(schema.getProperties())) {
          Object dataSchema = schema.getProperties().get("data");
          if(dataSchema instanceof StringSchema) {
            List list = ((StringSchema)dataSchema).getEnum();
            createEnum(list, modelName);
          }
        }
      }
      return;
    }

    schemaMap.put(modelName, schema);
    Map<String, Object> model = processModels(config, schemaMap);
    if (!CollectionUtils.isEmpty(model)) {
      objects.add(model);
    }

    objects.forEach(c -> {
      if (c.get("className") != null) {
        List<String> dir = Lists.newArrayList(PathUtils.BEAN_BASE_DIR);
        dir.add(ServiceUtils.serviceId2FeignClient(this.serviceId, false));
        dir.add(c.get("className") + ".java");
        String beanOutputFilePath = PathUtils.combinePath(dir);
        try {
          super.processTemplateToFile(c, "bean.mustache", beanOutputFilePath);
        } catch (IOException e) {
          log.error(e.getMessage(), e);
        }
      }
    });
  }

  private void createEnum(List enumList, String enumClass) {
    if(!CollectionUtils.isEmpty(enumList)) {
      Map<String, Object> map = new HashMap<>();
      List<Map<String, String>> enumListMap = new ArrayList<>();
      enumList.forEach(l -> enumListMap.add(Map.of("enumName", l.toString())));
      map.put("modelPackage", config.modelPackage());
      map.put("enumList", enumListMap);
      map.put("enumClass", enumClass);

      List<String> dir = Lists.newArrayList(PathUtils.BEAN_BASE_DIR);
      dir.add(ServiceUtils.serviceId2FeignClient(this.serviceId, false));
      dir.add(enumClass + ".java");
      String beanOutputFilePath = PathUtils.combinePath(dir);
      try {
        super.processTemplateToFile(map, "enum.mustache", beanOutputFilePath);
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  private Map<String, Object> processModels(CodegenConfig config, Map<String, Schema> definitions) {
    Map<String, Object> objs = new HashMap<>();
    objs.put("modelPackage", config.modelPackage());

    for (String key : definitions.keySet()) {
      if (key.startsWith("BaseResult")) {
        continue;
      }

      Schema schema = definitions.get(key);
      if (schema == null) {
        return new HashMap<>();
      }
      CodegenModel cm = config.fromModel(key, schema);

      cm.imports.remove("ApiModel");

      Set<String> cmImports = cm.getImports();
      if (!CollectionUtils.isEmpty(cmImports)) {
        cm.getImports().stream().filter(i -> SKIP_IMPORT_COLLECTION_TYPE.stream().noneMatch(i::equals)).forEach(this::createModelByName);
      }

      objs.put("className", key);

      Optional.ofNullable(cm.getVars()).ifPresent(vars -> {
        List<Map<String, String>> fieldList = Lists.newArrayList();
        vars.forEach(var -> {
          HashMap<String, String> fieldMap = Maps.newHashMap();
          fieldMap.put("type", var.dataType);
          fieldMap.put("field", var.baseName);
          fieldMap.put("description", Optional.ofNullable(var.description).orElse(""));
          fieldList.add(fieldMap);
        });

        Set<String> importSet = new TreeSet<>();
        for (String nextImport : cmImports) {
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

    String feignClientName = ServiceUtils.serviceId2FeignClient(serviceId, true);
    result.put("serviceName", serviceId);
    result.put("apiPackage", config.apiPackage());
    result.put("feignClientName", feignClientName);

    List<Map<String, String>> imports = new ArrayList<>();
    List<Map<String, Object>> methods = new ArrayList<>();

    for (String tag : paths.keySet()) {
      List<CodegenOperation> ops = paths.get(tag);

      for (CodegenOperation codegenOperation : ops) {
        //判断是否符合内部接口的路径
        if (SKIP_URL_PREFIX.stream().anyMatch(url -> codegenOperation.path.startsWith(url)) || excludeUrl.contains(codegenOperation.path)) {
          continue;
        }

        try {
          //如果是body参数，则定义对象
          if (codegenOperation.bodyParam != null) {
            createModelByName(codegenOperation.bodyParam.baseName);
          }

          Map<String, Object> operation = Maps.newHashMap();
          operation.put("operation", codegenOperation);

          String returnObject = codegenOperation.returnType;

          if(Strings.isNullOrEmpty(returnObject)) {
            log.error("[returnObject type error] returnObject is null or empty, path is {}", codegenOperation.path);
            continue;
          }

          if(!returnObject.equalsIgnoreCase("BaseResult")) {
            operation.put("hasBaseResult", "1");
          }

          if (returnObject.startsWith("BaseResultList")) {
            returnObject = returnObject.replace("BaseResultList", "");
            operation.put("returnObject", returnObject);
            operation.put("returnObjectList", "1");
          } else if (returnObject.startsWith("BaseResult")) {
            returnObject = returnObject.replace("BaseResult", "");
            operation.put("returnObject", returnObject);
          } else {
            log.error("[returnObject type error] returnObject is not BaseResult, returnObject is {}, path is {}", returnObject, codegenOperation.path);
            continue;
          }

          if (returnObject.equalsIgnoreCase("MapStringObject")) {
            log.error("[returnObject type error] returnObject is not BaseResult, returnObject is {}, path is {}", returnObject, codegenOperation.path);
            continue;
          }

          createModelByName(returnObject, true);

          //获取需要import的包
          Set<String> allImports = codegenOperation.imports;
          allImports.addAll(codegenOperation.imports);

          Set<String> mappingSet = new TreeSet<>();
          for (String nextImport : allImports) {
            Map<String, String> im = new LinkedHashMap<>();

            if(nextImport.equalsIgnoreCase("BaseResult")) {
              continue;
            }

            if (nextImport.startsWith("BaseResultList")) {
              nextImport = nextImport.replace("BaseResultList", "");
            }

            if (nextImport.startsWith("BaseResult")) {
              nextImport = nextImport.replace("BaseResult", "");
            }

            boolean skipBasicType = false;
            for (String type : SKIP_IMPORT_BASIC_TYPE) {
              if (nextImport.equals(type)) {
                skipBasicType = true;
                break;
              }
            }

            if (skipBasicType) {
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
        } catch (Exception e) {
          log.error("[returnObject type error] codegenOperation is {}, message is {}", codegenOperation, e.getMessage(), e);
        }
        result.put("importList", imports);
        result.put("operations", methods);
      }
    }

    List<String> feignDir = Lists.newArrayList(PathUtils.FEIGN_BASE_DIR);
    feignDir.add(ServiceUtils.serviceId2FeignClient(this.serviceId, false));
    feignDir.add(feignClientName + "Feign.java");
    String feignOutputFilePath = PathUtils.combinePath(feignDir);

    List<String> feignFallbackDir = Lists.newArrayList(PathUtils.FEIGN_BASE_DIR);
    feignFallbackDir.add(ServiceUtils.serviceId2FeignClient(this.serviceId, false));
    feignFallbackDir.add(feignClientName + "FeignFallbackFactory.java");
    String feignFallbackFactoryOutputFilePath = PathUtils.combinePath(feignFallbackDir);

    try {
      super.processTemplateToFile(result, "feign.mustache", feignOutputFilePath);
      super.processTemplateToFile(result, "feignFallbackFactory.mustache", feignFallbackFactoryOutputFilePath);
    } catch (IOException e) {
      log.error("load file error message is {}", e.getMessage(), e);
    }

  }


}
