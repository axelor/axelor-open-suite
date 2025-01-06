package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ApiConfiguration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.ApiConfigurationRepository;
import com.axelor.apps.base.service.api.ApiConfigurationService;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.LinkedHashMap;
import wslite.json.JSONException;
import wslite.json.JSONObject;

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

    Partner partner =
        JPA.find(Partner.class, Long.parseLong(request.getContext().get("_id").toString()));

    String result = Beans.get(ApiConfigurationService.class).fetchData(apiConfiguration, siret);

    if (!StringUtils.isEmpty(result)) {
      try {
        JSONObject resJson = new JSONObject(result);
        Beans.get(ApiConfigurationService.class).setData(partner, resJson);
        response.setValues(partner);
        response.setCanClose(true);
      } catch (JSONException e) {
        response.setInfo(result);
      }
    }
  }
}
