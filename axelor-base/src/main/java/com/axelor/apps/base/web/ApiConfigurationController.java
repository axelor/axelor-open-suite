package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ApiConfiguration;
import com.axelor.apps.base.db.ApiPartner;
import com.axelor.apps.base.db.repo.ApiConfigurationRepository;
import com.axelor.apps.base.service.apiconfiguration.ApiConfigurationService;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
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

    String result = Beans.get(ApiConfigurationService.class).fetchData(apiConfiguration, siret);

    if (!StringUtils.isEmpty(result)) {
      try {
        new JSONObject(result);
        response.setView(
            ActionView.define(I18n.get("API Partner"))
                .model(ApiPartner.class.getName())
                .add("form", "api-partner-form")
                .param("popup", "reload")
                .param("show-toolbar", "false")
                .param("show-confirm", "false")
                .param("popup-save", "true")
                .param("forceEdit", "true")
                .param("popup.maximized", "true")
                .context("_partnerId", request.getContext().get("partner"))
                .context("_apiResult", request.getContext().get("_id"))
                .map());
      } catch (JSONException e) {
        response.setInfo(result);
        response.setCanClose(true);
      }
    }
  }
}
