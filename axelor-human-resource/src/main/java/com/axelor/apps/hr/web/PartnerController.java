package com.axelor.apps.hr.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.hr.service.PartnerEmployeeService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PartnerController {

  public void changeEmployeeType(ActionRequest request, ActionResponse response) {
    Partner partner = request.getContext().asType(Partner.class);
    partner = Beans.get(PartnerRepository.class).find(partner.getId());
    Beans.get(PartnerEmployeeService.class).convertToContactPartner(partner);
    response.setCanClose(true);
    response.setView(
        ActionView.define(I18n.get("Partner"))
            .model(Partner.class.getName())
            .add("form", "partner-contact-form")
            .add("grid", "partner-contact-grid")
            .context("_showRecord", partner.getId())
            .map());
  }
}
