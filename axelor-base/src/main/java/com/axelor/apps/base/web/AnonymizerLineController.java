package com.axelor.apps.base.web;

import com.axelor.apps.base.service.AnonymizerLineService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.Objects;

public class AnonymizerLineController {

  public void getFakerApiFieldDomain(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    MetaField metaField = (MetaField) context.get("metaField");

    if (Objects.isNull(metaField)) {
      return;
    }

    MetaJsonField metaJsonField = (MetaJsonField) context.get("metaJsonField");
    String domain =
        Beans.get(AnonymizerLineService.class).getFakerApiFieldDomain(metaField, metaJsonField);

    response.setAttr("fakerApiField", "domain", domain);
  }
}
