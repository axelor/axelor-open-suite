package com.axelor.apps.base.web;

import com.axelor.apps.base.db.ProductFamily;
import com.axelor.apps.base.service.ProductFamilyService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProductFamilyController {
  public void avoidCompanyDuplicates(ActionRequest req, ActionResponse resp) {
    try {
      ProductFamily family = req.getContext().asType(ProductFamily.class);
      Beans.get(ProductFamilyService.class).avoidCompanyDuplicates(family);
      resp.setValue("accountManagementList", family.getAccountManagementList());
    } catch (Exception e) {
      resp.setError(I18n.get(e.getMessage()));
    }
  }
}
