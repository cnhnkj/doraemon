package com.huinong.truffle.doraemon;

import com.huinong.framework.autoconfigure.web.BaseResult;
import com.huinong.truffle.doraemon.codegen.HnCodeGenerator;
import com.huinong.truffle.doraemon.codegen.HnJavaClientCodegen;
import com.huinong.truffle.doraemon.domain.eureka.AllEurekaServices;
import com.huinong.truffle.doraemon.domain.eureka.EurekaServiceInfo;
import com.huinong.truffle.doraemon.domain.eureka.EurekaServiceInfo.EurekaInstanceInfo;
import com.huinong.truffle.doraemon.service.EurekaService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.util.List;
import javax.annotation.Resource;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.CodegenConstants;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class DoraemonApplication implements CommandLineRunner {

  @Resource
  private EurekaService eurekaService;

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(DoraemonApplication.class);
    application.run(args);
  }


  @Override
  public void run(String... args) {
    System.out.println("start doraemon project ");

    BaseResult<AllEurekaServices> baseResult = eurekaService.getAllEurekaServiceInfo();
    List<EurekaServiceInfo> eurekaServiceInfoList = baseResult.getData().getApplications().getApplication();

    eurekaServiceInfoList.forEach(info -> {
      OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
      EurekaInstanceInfo instanceInfo = info.getInstance().stream().findFirst().orElse(null);

      if(instanceInfo == null) {
        return ;
      }

      if(!instanceInfo.getMetadata().containsKey("framework-version") ||
          !instanceInfo.getMetadata().get("framework-version").startsWith("2.")) {
        return ;
      }

      String location = instanceInfo.getHomePageUrl() + "/v3/api-docs";
      OpenAPI openAPI = openAPIV3Parser.read(location);

      ClientOptInput input = new ClientOptInput().config(new HnJavaClientCodegen()).openAPI(openAPI);
      HnCodeGenerator apiCodegen = new HnCodeGenerator();
      apiCodegen.setGeneratorPropertyDefault(CodegenConstants.APIS, "true");;
      apiCodegen.setGeneratorPropertyDefault(CodegenConstants.MODELS, "true");
      apiCodegen.opts(input).generate();
    });


    System.out.println("done doraemon project ");
  }
}
