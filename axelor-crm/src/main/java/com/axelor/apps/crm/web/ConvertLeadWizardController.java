/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.AppBase;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.apps.crm.service.ConvertLeadWizardService;
import com.axelor.apps.crm.service.LeadService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.util.Map;

@Singleton
public class ConvertLeadWizardController {

  @SuppressWarnings("unchecked")
  public void convertLead(ActionRequest request, ActionResponse response) {

    try {
      Context context = request.getContext();

      Map<String, Object> leadContext = (Map<String, Object>) context.get("_lead");

      Lead lead =
          Beans.get(LeadRepository.class).find(((Integer) leadContext.get("id")).longValue());

      Partner partner = createPartnerData(context);
      Partner contactPartner = null;

      if (partner != null) {
        contactPartner = createContactData(context, partner);
      }

      try {
        lead = Beans.get(LeadService.class).convertLead(lead, partner, contactPartner);
      } catch (Exception e) {
        TraceBackService.trace(e);
      }

      if (lead.getPartner() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.CONVERT_LEAD_ERROR));
      }

      openPartner(response, lead);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  private Partner createPartnerData(Context context) throws AxelorException {

    Integer leadToPartnerSelect = (Integer) context.get("leadToPartnerSelect");
    ConvertLeadWizardService convertLeadWizardService = Beans.get(ConvertLeadWizardService.class);
    Partner partner = null;

    if (leadToPartnerSelect == LeadRepository.CONVERT_LEAD_CREATE_PARTNER) {
      Address primaryAddress = convertLeadWizardService.createPrimaryAddress(context);
      if (primaryAddress != null
          && (primaryAddress.getAddressL6() == null
              || primaryAddress.getAddressL7Country() == null)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(IExceptionMessage.LEAD_PARTNER_MISSING_ADDRESS));
      }
      partner =
          convertLeadWizardService.createPartner(
              (Map<String, Object>) context.get("partner"), primaryAddress);
      // TODO check all required fields...
    } else if (leadToPartnerSelect == LeadRepository.CONVERT_LEAD_SELECT_PARTNER) {
      Map<String, Object> selectPartnerContext = (Map<String, Object>) context.get("selectPartner");
      partner =
          Beans.get(PartnerRepository.class)
              .find(((Integer) selectPartnerContext.get("id")).longValue());
      if (!partner.getIsCustomer()) {
        partner.setIsProspect(true);
      }
    }

    return partner;
  }

  @SuppressWarnings("unchecked")
  private Partner createContactData(Context context, Partner partner) throws AxelorException {

    Partner contactPartner = null;
    Integer leadToContactSelect = (Integer) context.get("leadToContactSelect");
    ConvertLeadWizardService convertLeadWizardService = Beans.get(ConvertLeadWizardService.class);

    if (leadToContactSelect == null) {
      return null;
    }

    if (leadToContactSelect == LeadRepository.CONVERT_LEAD_CREATE_CONTACT
        && partner.getPartnerTypeSelect() != PartnerRepository.PARTNER_TYPE_INDIVIDUAL) {
      Address primaryAddress = convertLeadWizardService.createPrimaryAddress(context);
      if (primaryAddress != null
          && (primaryAddress.getAddressL6() == null
              || primaryAddress.getAddressL7Country() == null)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(IExceptionMessage.LEAD_CONTACT_MISSING_ADDRESS));
      }

      contactPartner =
          convertLeadWizardService.createPartner(
              (Map<String, Object>) context.get("contactPartner"), primaryAddress);
      contactPartner.setIsContact(true);
      // TODO check all required fields...
    } else if (leadToContactSelect == LeadRepository.CONVERT_LEAD_SELECT_CONTACT
        && partner.getPartnerTypeSelect() != PartnerRepository.PARTNER_TYPE_INDIVIDUAL) {
      Map<String, Object> selectContactContext = (Map<String, Object>) context.get("selectContact");
      contactPartner =
          Beans.get(PartnerRepository.class)
              .find(((Integer) selectContactContext.get("id")).longValue());
    }

    return contactPartner;
  }

  private void openPartner(ActionResponse response, Lead lead) {

    Partner partner = lead.getPartner();
    String form = "partner-customer-form";
    String grid = "partner-customer-grid";

    if (partner.getIsSupplier() && !partner.getIsCustomer() && !partner.getIsProspect()) {
      form = "partner-supplier-form";
      grid = "partner-supplier-grid";
    }

    response.setFlash(I18n.get(IExceptionMessage.CONVERT_LEAD_1));
    response.setCanClose(true);
    response.setView(
        ActionView.define(I18n.get(IExceptionMessage.CONVERT_LEAD_1))
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
    response.setAttr("fax", "value", lead.getFax());
    response.setAttr("mobilePhone", "value", lead.getMobilePhone());
    response.setAttr("fixedPhone", "value", lead.getFixedPhone());
    response.setAttr("webSite", "value", lead.getWebSite());
    response.setAttr("source", "value", lead.getSource());
    response.setAttr("department", "value", lead.getDepartment());
    response.setAttr("team", "value", lead.getTeam());
    response.setAttr("user", "value", lead.getUser());
    response.setAttr("isProspect", "value", true);
    response.setAttr("partnerTypeSelect", "value", "1");
    response.setAttr("language", "value", appBase.getDefaultPartnerLanguage());
  }

  public void setIndividualPartner(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Lead lead = findLead(request);

    response.setAttr("firstName", "value", lead.getFirstName());
    response.setAttr("name", "value", lead.getName());
  }

  public void setContactDefaults(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Lead lead = findLead(request);

    response.setAttr("firstName", "value", lead.getFirstName());
    response.setAttr("name", "value", lead.getName());
    response.setAttr("titleSelect", "value", lead.getTitleSelect());
    response.setAttr("emailAddress", "value", lead.getEmailAddress());
    response.setAttr("fax", "value", lead.getFax());
    response.setAttr("mobilePhone", "value", lead.getMobilePhone());
    response.setAttr("fixedPhone", "value", lead.getFixedPhone());
    response.setAttr("user", "value", lead.getUser());
    response.setAttr("team", "value", lead.getTeam());
    response.setAttr("jobTitleFunction", "value", lead.getJobTitleFunction());
  }

  public void setConvertLeadIntoOpportunity(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Lead lead = findLead(request);

    AppBase appBase = Beans.get(AppBaseService.class).getAppBase();
    response.setAttr("lead", "value", lead);
    response.setAttr("amount", "value", lead.getEstimatedBudget());
    response.setAttr("customerDescription", "value", lead.getDescription());
    response.setAttr("source", "value", lead.getSource());
    response.setAttr("partner", "value", lead.getPartner());
    response.setAttr("user", "value", lead.getUser());
    response.setAttr("team", "value", lead.getTeam());
    response.setAttr("webSite", "value", lead.getWebSite());
    response.setAttr("source", "value", lead.getSource());
    response.setAttr("department", "value", lead.getDepartment());
    response.setAttr("isCustomer", "value", true);
    response.setAttr("partnerTypeSelect", "value", "1");
    response.setAttr("language", "value", appBase.getDefaultPartnerLanguage());

    Company company = null;
    CompanyRepository companyRepo = Beans.get(CompanyRepository.class);

    if (lead.getUser() != null && lead.getUser().getActiveCompany() != null) {
      company = lead.getUser().getActiveCompany();
    } else if (companyRepo.all().count() == 1) {
      company = companyRepo.all().fetchOne();
    }

    if (company != null) {
      response.setAttr("company", "value", company);
      response.setAttr("currency", "value", company.getCurrency());
    }
  }

  protected Lead findLead(ActionRequest request) throws AxelorException {

    Context context = request.getContext();

    Lead lead = null;

    if (context.getParent() != null
        && context.getParent().get("_model").equals("com.axelor.apps.base.db.Wizard")) {
      context = context.getParent();
    }

    Map leadMap = (Map) context.get("_lead");
    if (leadMap != null && leadMap.get("id") != null) {
      lead = Beans.get(LeadRepository.class).find(Long.parseLong(leadMap.get("id").toString()));
    }

    if (lead == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE, I18n.get(IExceptionMessage.CONVERT_LEAD_MISSING));
    }

    return lead;
  }
}
