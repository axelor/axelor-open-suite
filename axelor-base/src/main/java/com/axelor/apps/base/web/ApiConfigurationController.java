package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ApiConfiguration;
import com.axelor.apps.base.db.repo.ApiConfigurationRepository;
import com.axelor.apps.base.service.apiconfiguration.ApiConfigurationService;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.LinkedHashMap;

public class ApiConfigurationController {

  @ErrorException
  public void fetchData(ActionRequest request, ActionResponse response) throws AxelorException {
    String siret = request.getContext().get("siret number").toString();
    Object apiConfigContext = request.getContext().get("apiConfiguration");

    Long apiConfigId =
        apiConfigContext != null
            ? Long.valueOf(((LinkedHashMap<String, Object>) apiConfigContext).get("id").toString())
            : null;

    ApiConfiguration apiConfiguration =
        Beans.get(ApiConfigurationRepository.class).find(apiConfigId);

    String result = Beans.get(ApiConfigurationService.class).fetchData(apiConfiguration, siret);

    if (!StringUtils.isEmpty(result)) {
      response.setInfo(result);
      response.setCanClose(true);
    }
  }
}
