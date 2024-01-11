/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.apps.crm.service.ConvertLeadWizardService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.studio.db.AppBase;
import com.axelor.utils.service.ConvertBinaryToMetafileService;
import com.google.inject.Singleton;
import java.util.Map;

@Singleton
public class ConvertLeadWizardController {

  @SuppressWarnings("unchecked")
  public void convertLead(ActionRequest request, ActionResponse response) {

    try {
      Context context = request.getContext();

      Map<String, Object> leadMap = (Map<String, Object>) context.get("_lead");
      Map<String, Object> opportunityMap = null;
      Map<String, Object> partnerMap = null;
      Map<String, Object> contactPartnerMap = null;

      Lead lead = Beans.get(LeadRepository.class).find(((Integer) leadMap.get("id")).longValue());
      Integer leadToPartnerSelect = (Integer) context.get("leadToPartnerSelect");
      Integer leadToContactSelect = (Integer) context.get("leadToContactSelect");

      if (context.containsKey("isCreateOpportunity")
          && (Boolean) context.get("isCreateOpportunity")) {
        opportunityMap = (Map<String, Object>) context.get("opportunity");
      }

      Partner partner = null;
      Partner contactPartner = null;

      if (leadToPartnerSelect == LeadRepository.CONVERT_LEAD_CREATE_PARTNER) {
        partnerMap = (Map<String, Object>) context.get("partner");
      } else if (leadToPartnerSelect == LeadRepository.CONVERT_LEAD_SELECT_PARTNER) {
        Map<String, Object> selectPartnerContext =
            (Map<String, Object>) context.get("selectPartner");
        partner =
            Beans.get(PartnerRepository.class)
                .find(((Integer) selectPartnerContext.get("id")).longValue());
      }

      if (leadToContactSelect == LeadRepository.CONVERT_LEAD_CREATE_CONTACT) {
        contactPartnerMap = (Map<String, Object>) context.get("contactPartner");
      } else if (leadToContactSelect == LeadRepository.CONVERT_LEAD_SELECT_CONTACT) {
        Map<String, Object> selectContactContext =
            (Map<String, Object>) context.get("selectContact");
        contactPartner =
            Beans.get(PartnerRepository.class)
                .find(((Integer) selectContactContext.get("id")).longValue());
      }

      lead =
          Beans.get(ConvertLeadWizardService.class)
              .generateDataAndConvertLeadAndGenerateOpportunity(
                  lead,
                  leadToPartnerSelect,
                  leadToContactSelect,
                  partner,
                  partnerMap,
                  contactPartner,
                  contactPartnerMap,
                  opportunityMap);

      openPartner(response, lead);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected void openPartner(ActionResponse response, Lead lead) {

    Partner partner = lead.getPartner();
    String form = "partner-customer-form";
    String grid = "partner-customer-grid";

    if (partner.getIsSupplier() && !partner.getIsCustomer() && !partner.getIsProspect()) {
      form = "partner-supplier-form";
      grid = "partner-supplier-grid";
    }

    response.setInfo(I18n.get(CrmExceptionMessage.CONVERT_LEAD_1));
    response.setCanClose(true);
    response.setView(
        ActionView.define(I18n.get(CrmExceptionMessage.CONVERT_LEAD_1))
            .model(Partner.class.getName())
            .add("form", form)
            .add("grid", grid)
            .param("search-filters", "partner-filters")
            .context("_showRecord", partner.getId())
            .map());
  }

  public void setDefaults(ActionRequest request, ActionResponse response) throws AxelorException {

    Lead lead = findLead(request);

    response.setAttr("$partner.$primaryAddress", "value", lead.getPrimaryAddress());
    response.setAttr("$partner.$primaryCity", "value", lead.getPrimaryCity());
    response.setAttr("$partner.$primaryState", "value", lead.getPrimaryState());
    response.setAttr("$partner.$primaryPostalCode", "value", lead.getPrimaryPostalCode());
    response.setAttr("$partner.$primaryCountry", "value", lead.getPrimaryCountry());
    response.setAttr("$contactAddress", "value", lead.getPrimaryAddress());
    response.setAttr("$contactCity", "value", lead.getPrimaryCity());
    response.setAttr("$contactState", "value", lead.getPrimaryState());
    response.setAttr("$contactPostalCode", "value", lead.getPrimaryPostalCode());
    response.setAttr("$contactCountry", "value", lead.getPrimaryCountry());
    response.setAttr("$leadToPartnerSelect", "value", 1);
    response.setAttr("$leadToContactSelect", "value", 1);
  }

  public void setPartnerDefaults(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Lead lead = findLead(request);

    AppBase appBase = Beans.get(AppBaseService.class).getAppBase();
    response.setAttr("name", "value", lead.getEnterpriseName());
    response.setAttr("industrySector", "value", lead.getIndustrySector());
    response.setAttr("titleSelect", "value", lead.getTitleSelect());
    response.setAttr("emailAddress", "value", lead.getEmailAddress());
    response.setAttr("mobilePhone", "value", lead.getMobilePhone());
    response.setAttr("fixedPhone", "value", lead.getFixedPhone());
    response.setAttr("webSite", "value", lead.getWebSite());
    response.setAttr("source", "value", lead.getSource());
    response.setAttr("department", "value", lead.getDepartment());
    response.setAttr("team", "value", lead.getTeam());
    response.setAttr("user", "value", lead.getUser());
    response.setAttr("isKeyAccount", "value", lead.getIsKeyAccount());
    response.setAttr("leadScoringSelect", "value", lead.getLeadScoringSelect());
    response.setAttr("partnerCategory", "value", lead.getType());
    response.setAttr("sizeSelect", "value", lead.getSizeSelect());
    response.setAttr("isNurturing", "value", lead.getIsNurturing());
    response.setAttr("agency", "value", lead.getAgency());
    if (lead.getUser() != null && lead.getUser().getActiveCompany() != null) {
      if (lead.getUser().getActiveCompany().getDefaultPartnerCategorySelect()
          == CompanyRepository.CATEGORY_CUSTOMER) {
        response.setAttr("isCustomer", "value", true);
      } else if (lead.getUser().getActiveCompany().getDefaultPartnerCategorySelect()
          == CompanyRepository.CATEGORY_SUPPLIER) {
        response.setAttr("isSupplier", "value", true);
      } else {
        response.setAttr("isProspect", "value", true);
      }
    } else {
      response.setAttr("isProspect", "value", true);
    }
    response.setAttr("partnerTypeSelect", "value", "1");
    response.setAttr("language", "value", appBase.getDefaultPartnerLanguage());
  }

  public void setIndividualPartner(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Lead lead = findLead(request);

    if (request.getContext().get("partnerTypeSelect").toString().equals("2")) {
      response.setAttr("firstName", "value", lead.getFirstName());
      response.setAttr("name", "value", lead.getName());

    } else {
      response.setAttr("name", "value", lead.getEnterpriseName());
    }
  }

  public void setContactDefaults(ActionRequest request, ActionResponse response) {
    try {
      Lead lead = findLead(request);

      if (lead.getPicture() != null) {
        MetaFile picture =
            Beans.get(ConvertBinaryToMetafileService.class)
                .convertByteTabPictureInMetafile(lead.getPicture());
        response.setAttr("picture", "value", picture);
      }
      response.setAttr("firstName", "value", lead.getFirstName());
      response.setAttr("name", "value", lead.getName());
      response.setAttr("titleSelect", "value", lead.getTitleSelect());
      response.setAttr("emailAddress", "value", lead.getEmailAddress());
      response.setAttr("mobilePhone", "value", lead.getMobilePhone());
      response.setAttr("fixedPhone", "value", lead.getFixedPhone());
      response.setAttr("user", "value", lead.getUser());
      response.setAttr("team", "value", lead.getTeam());
      response.setAttr("jobTitleFunction", "value", lead.getJobTitleFunction());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setOpportunityDeafults(ActionRequest request, ActionResponse response) {
    try {
      Lead lead = findLead(request);
      response.setAttr("source", "value", lead.getSource());
      response.setAttr("user", "value", lead.getUser());
    } catch (AxelorException e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  protected Lead findLead(ActionRequest request) throws AxelorException {

    Context context = request.getContext();

    Lead lead = null;

    if (context.getParent() != null
        && context.getParent().get("_model").equals("com.axelor.utils.db.Wizard")) {
      context = context.getParent();
    }

    Map leadMap = (Map) context.get("_lead");
    if (leadMap != null && leadMap.get("id") != null) {
      lead = Beans.get(LeadRepository.class).find(Long.parseLong(leadMap.get("id").toString()));
    }

    if (lead == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(CrmExceptionMessage.CONVERT_LEAD_MISSING));
    }

    return lead;
  }
}
