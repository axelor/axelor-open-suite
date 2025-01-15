package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PartnerApiConfiguration;
import com.axelor.apps.base.db.repo.PartnerApiConfigurationRepository;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.partner.api.PartnerApiFetchService;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.LinkedHashMap;

public class PartnerApiConfigurationController {

  @ErrorException
  public void fetchData(ActionRequest request, ActionResponse response) throws AxelorException {
    String siret = request.getContext().get("siret number").toString();
    Object apiConfigContext = request.getContext().get("partnerApiConfiguration");

    Long apiConfigId =
        apiConfigContext != null
            ? Long.valueOf(((LinkedHashMap<String, Object>) apiConfigContext).get("id").toString())
            : null;

    PartnerApiConfiguration partnerApiConfiguration =
        Beans.get(PartnerApiConfigurationRepository.class).find(apiConfigId);

    String result = Beans.get(PartnerApiFetchService.class).fetch(partnerApiConfiguration, siret);

    if (!StringUtils.isEmpty(result)) {
      response.setInfo(result);
      response.setCanClose(true);
    }
  }
}
