/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.crm.web;

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.apps.crm.service.EmailDomainToolService;
import com.axelor.apps.crm.service.PartnerCrmService;
import com.axelor.apps.crm.service.PartnerEmailDomainToolService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.HashMap;
import java.util.Map;

public class PartnerCrmController {

  // using provider for the injection of a parameterized service
  @Inject private Provider<EmailDomainToolService<Partner>> emailDomainToolServiceProvider;

  public void getSubsidiaryPartnersCount(ActionRequest request, ActionResponse response) {

    try {
      Partner partner = request.getContext().asType(Partner.class);
      partner = Beans.get(PartnerRepository.class).find(partner.getId());
      long count =
          Beans.get(PartnerRepository.class)
              .all()
              .filter("self.parentPartner = :id")
              .bind("id", partner.getId())
              .count();
      response.setValue("$subsidiaryCount", count);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void losePartner(ActionRequest request, ActionResponse response) {
    try {
      Partner partner = request.getContext().asType(Partner.class);
      Beans.get(PartnerCrmService.class)
          .losePartner(
              Beans.get(PartnerRepository.class).find(partner.getId()),
              partner.getLostReason(),
              partner.getLostReasonStr());
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /** Called from partner contact form view, when loading related contact panel. */
  public void viewRelatedContacts(ActionRequest request, ActionResponse response) {
    Partner partner = request.getContext().asType(Partner.class);
    String domain;
    Map<String, Object> params;
    if (partner == null || partner.getId() == null) {
      domain = "";
      params = new HashMap<>();
    } else {
      domain = Beans.get(PartnerEmailDomainToolService.class).computeFilterEmailOnDomain(partner);
      params =
          emailDomainToolServiceProvider
              .get()
              .computeParameterForFilter(partner, partner.getEmailAddress());
    }
    ActionView.ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get("Contacts"));
    actionViewBuilder.model(Partner.class.getName());
    actionViewBuilder.add("grid", "partner-related-contact-grid");
    actionViewBuilder.add("form", "partner-contact-form");
    actionViewBuilder.domain(domain);
    for (Map.Entry<String, Object> entry : params.entrySet()) {
      actionViewBuilder.context(entry.getKey(), entry.getValue());
    }
    response.setView(actionViewBuilder.map());
  }

  public void kanbanPartnerOnMove(ActionRequest request, ActionResponse response) {
    Partner partner = request.getContext().asType(Partner.class);
    try {
      Beans.get(PartnerCrmService.class).kanbanPartnerOnMove(partner);
    } catch (Exception e) {
      if (e.getMessage().equals(I18n.get(CrmExceptionMessage.PROSPECT_CLOSE_WIN_KANBAN))) {
        TraceBackService.trace(response, e, ResponseMessageType.INFORMATION);
        response.setReload(true);
        return;
      }
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
