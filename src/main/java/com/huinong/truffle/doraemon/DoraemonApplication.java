package com.huinong.truffle.doraemon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.huinong.framework.autoconfigure.web.BaseResult;
import com.huinong.truffle.doraemon.codegen.HnCodeGenerator;
import com.huinong.truffle.doraemon.codegen.HnJavaClientCodegen;
import com.huinong.truffle.doraemon.domain.eureka.AllEurekaServices;
import com.huinong.truffle.doraemon.domain.eureka.EurekaServiceInfo;
import com.huinong.truffle.doraemon.domain.eureka.EurekaServiceInfo.EurekaInstanceInfo;
import com.huinong.truffle.doraemon.service.EurekaService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import joptsimple.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.CodegenConstants;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@Slf4j
@SpringBootApplication
public class DoraemonApplication implements CommandLineRunner {

  @Resource
  private EurekaService eurekaService;

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(DoraemonApplication.class);
    application.run(args);
  }


  @Override
  public void run(String... args) throws Exception {
    FileUtils.deleteDirectory(new File("src/main/java/com/huinong/truffle/doraemon/api"));

    int argsLength = args.length;

    log.info("start doraemon project, {}", LocalDateTime.now());

    BaseResult<AllEurekaServices> baseResult = eurekaService.getAllEurekaServiceInfo();
    List<EurekaServiceInfo> eurekaServiceInfoList = baseResult.getData().getApplications().getApplication();

    eurekaServiceInfoList.forEach(info -> {
      OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
      EurekaInstanceInfo instanceInfo = info.getInstance().stream().findFirst().orElse(null);

      if (instanceInfo == null) {
        return;
      }

      if (!instanceInfo.getMetadata().containsKey("framework-version") ||
          !instanceInfo.getMetadata().get("framework-version").startsWith("2.")) {
        return;
      }

      String serviceId;
      List<String> excludeUrl = Lists.newArrayList();
      if (argsLength > 0 && instanceInfo.getApp().equalsIgnoreCase(args[0])) {
        serviceId = args[0];
        excludeUrl.addAll(getAllRealPath(instanceInfo));

      } else if (argsLength == 0) {
        serviceId = instanceInfo.getApp().toLowerCase();
        excludeUrl.addAll(getAllRealPath(instanceInfo));
      } else {
        return;
      }

      String location = instanceInfo.getHomePageUrl() + "/v3/api-docs";
      OpenAPI openAPI = openAPIV3Parser.read(location);

      ClientOptInput input = new ClientOptInput().config(new HnJavaClientCodegen(serviceId)).openAPI(openAPI);
      HnCodeGenerator apiCodegen = new HnCodeGenerator(serviceId, excludeUrl);
      apiCodegen.setGeneratorPropertyDefault(CodegenConstants.APIS, "true");
      ;
      apiCodegen.setGeneratorPropertyDefault(CodegenConstants.MODELS, "true");
      apiCodegen.opts(input).generate();
    });

    log.info("done doraemon project, {}", LocalDateTime.now());
    System.exit(0);
  }

  private List<String> getAllRealPath(EurekaInstanceInfo instanceInfo) {
    return instanceInfo.getMetadata().values().stream().map(v -> {
      try {
        JsonNode jsonNode = new ObjectMapper().readValue(v, JsonNode.class);
        if(jsonNode.has("realPath")) {
          return jsonNode.get("realPath").asText();
        } else {
          return Strings.EMPTY;
        }
      } catch (Exception e) {
        log.error("value is {} not contain exclude url",v);
        return Strings.EMPTY;
      }
    }).collect(Collectors.toList());
  }
}
