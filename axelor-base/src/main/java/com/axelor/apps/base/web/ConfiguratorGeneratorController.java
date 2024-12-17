package com.axelor.apps.base.web;

import com.axelor.apps.base.db.ConfiguratorBinding;
import com.axelor.apps.base.db.ConfiguratorGenerator;
import com.axelor.apps.base.db.repo.ConfiguratorBindingRepository;
import com.axelor.apps.base.service.ConfiguratorGeneratorService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.studio.db.LinkScript;
import com.axelor.studio.ls.LinkScriptResult;
import com.axelor.studio.ls.LinkScriptService;
import java.util.LinkedHashMap;

public class ConfiguratorGeneratorController {

  /**
   * Sunc configurator generator line
   *
   * @param request
   * @param response
   */
  public void syncArcDependencies(ActionRequest request, ActionResponse response) {
    ConfiguratorGenerator generator = request.getContext().asType(ConfiguratorGenerator.class);
    Beans.get(ConfiguratorGeneratorService.class).syncDependencies(generator);
  }

  public void testConfigurator(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    ConfiguratorGenerator generator = context.asType(ConfiguratorGenerator.class);
    ConfiguratorBinding configuratorBinding =
        Beans.get(ConfiguratorBindingRepository.class)
            .all()
            .filter("self.metaModel = :metaModel")
            .bind("metaModel", generator.getMetaModel())
            .fetchOne();
    LinkScript linkScript = configuratorBinding.getBusinessLogicLinkScript();

    // linkedHashMap => from MetaJsonField
    LinkScriptResult result =
        (Beans.get(LinkScriptService.class)).run(linkScript.getName(), new LinkedHashMap<>());

    response.setAlert(result.toString());
  }
}
