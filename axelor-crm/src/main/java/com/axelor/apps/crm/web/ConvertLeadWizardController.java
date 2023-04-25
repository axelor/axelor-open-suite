/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.crm.web;

import com.axelor.apps.base.AxelorException;
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
import com.axelor.common.ObjectUtils;
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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class ConvertLeadWizardController {

  @SuppressWarnings("unchecked")
  public void convertLead(ActionRequest request, ActionResponse response) {

    try {
      Context context = request.getContext();

      Map<String, Object> leadMap = (Map<String, Object>) context.get("_lead");
      Map<String, Object> partnerMap = null;
      Map<String, Object> contactPartnerMap = null;

      Lead lead = Beans.get(LeadRepository.class).find(((Integer) leadMap.get("id")).longValue());
      Integer leadToPartnerSelect = (Integer) context.get("leadToPartnerSelect");
      Integer leadToContactSelect = (Integer) context.get("leadToContactSelect");

      Partner partner = null;
      Partner contactPartner = null;

      if (leadToPartnerSelect == LeadRepository.CONVERT_LEAD_CREATE_PARTNER) {
        partnerMap = this.getPartnerMap(request, response);
      } else if (leadToPartnerSelect == LeadRepository.CONVERT_LEAD_SELECT_PARTNER) {
        Map<String, Object> selectPartnerContext =
            (Map<String, Object>) context.get("selectPartner");
        partner =
            Beans.get(PartnerRepository.class)
                .find(((Integer) selectPartnerContext.get("id")).longValue());
      }

      if (leadToContactSelect == LeadRepository.CONVERT_LEAD_CREATE_CONTACT) {
        contactPartnerMap = this.getContactMap(request, response);
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
                  contactPartnerMap);

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

  public Map<String, Object> getPartnerMap(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Lead lead = findLead(request);
    AppBase appBase = Beans.get(AppBaseService.class).getAppBase();
    Map<String, Object> partnerMap = new HashMap<String, Object>();
    partnerMap.put("primaryAddress", lead.getPrimaryAddress());
    partnerMap.put("primaryCity", lead.getPrimaryCity());
    partnerMap.put("primaryState", lead.getPrimaryState());
    partnerMap.put("primaryPostalCode", lead.getPrimaryPostalCode());
    partnerMap.put("primaryCountry", lead.getPrimaryCountry());
    partnerMap.put("industrySector", lead.getIndustrySector());
    partnerMap.put("emailAddress", lead.getEmailAddress());
    partnerMap.put("mobilePhone", lead.getMobilePhone());
    partnerMap.put("fixedPhone", lead.getFixedPhone());
    partnerMap.put("webSite", lead.getWebSite());
    partnerMap.put("source", lead.getSource());
    partnerMap.put("department", lead.getDepartment());
    partnerMap.put("team", lead.getTeam());
    partnerMap.put("user", lead.getUser());
    partnerMap.put("isKeyAccount", lead.getIsKeyAccount());
    partnerMap.put("leadScoringSelect", lead.getLeadScoringSelect());
    partnerMap.put("partnerCategory", lead.getType());
    partnerMap.put("sizeSelect", lead.getSizeSelect());
    partnerMap.put("isNurturing", lead.getIsNurturing());
    partnerMap.put("agency", lead.getAgency());
    if (lead.getUser() != null && lead.getUser().getActiveCompany() != null) {
      if (lead.getUser().getActiveCompany().getDefaultPartnerCategorySelect()
          == CompanyRepository.CATEGORY_CUSTOMER) {
        partnerMap.put("isCustomer", true);
      } else if (lead.getUser().getActiveCompany().getDefaultPartnerCategorySelect()
          == CompanyRepository.CATEGORY_SUPPLIER) {
        partnerMap.put("isSupplier", true);
      } else {
        partnerMap.put("primaryAddress", lead.getPrimaryAddress());
        response.setAttr("isProspect", "value", true);
      }
    } else {
      partnerMap.put("isProspect", true);
    }

    if (ObjectUtils.isEmpty(lead.getEnterpriseName())) {
      partnerMap.put("firstName", lead.getFirstName());
      partnerMap.put("name", lead.getName());
      partnerMap.put("titleSelect", lead.getTitleSelect());
      partnerMap.put("partnerTypeSelect", 2);

    } else {
      partnerMap.put("partnerTypeSelect", 1);
      partnerMap.put("name", lead.getEnterpriseName());
    }
    partnerMap.put("language", appBase.getDefaultPartnerLanguage());
    return partnerMap;
  }

  public Map<String, Object> getContactMap(ActionRequest request, ActionResponse response)
      throws AxelorException, IOException {
    Map<String, Object> contactMap = new HashMap<String, Object>();
    Lead lead = findLead(request);
    if (lead.getPicture() != null) {
      MetaFile picture =
          Beans.get(ConvertBinaryToMetafileService.class)
              .convertByteTabPictureInMetafile(lead.getPicture());
      contactMap.put("picture", picture);
    }
    contactMap.put("firstName", lead.getFirstName());

    contactMap.put("name", lead.getName());

    contactMap.put("titleSelect", lead.getTitleSelect());

    contactMap.put("emailAddress", lead.getEmailAddress());

    contactMap.put("mobilePhone", lead.getMobilePhone());

    contactMap.put("fixedPhone", lead.getFixedPhone());

    contactMap.put("user", lead.getUser());

    contactMap.put("team", lead.getTeam());

    contactMap.put("jobTitleFunction", lead.getJobTitleFunction());

    return contactMap;
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
