/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.PartnerStatus;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.crm.db.repo.PartnerStatusRepository;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.apps.crm.service.ConvertLeadWizardService;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.auth.AuthUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.studio.db.AppBase;
import com.axelor.studio.db.AppCrm;
import com.axelor.utils.service.BinaryConversionService;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ConvertLeadWizardController {

  @SuppressWarnings("unchecked")
  public void convertLead(ActionRequest request, ActionResponse response) {

    try {
      Context context = request.getContext();
      AppCrm appCrm = Beans.get(AppCrmService.class).getAppCrm();
      boolean crmProcessOnPartner = appCrm.getCrmProcessOnPartner();
      Map<String, Object> leadMap = (Map<String, Object>) context.get("_lead");
      Map<String, Object> partnerMap = null;
      Map<String, Object> contactPartnerMap = null;

      Lead lead = Beans.get(LeadRepository.class).find(((Integer) leadMap.get("id")).longValue());
      Integer leadToPartnerSelect =
          (Integer) Optional.ofNullable(context.get("leadToPartnerSelect")).orElse(1);
      Integer leadToContactSelect =
          (Integer) Optional.ofNullable(context.get("leadToContactSelect")).orElse(0);

      Partner partner = null;
      PartnerStatus partnerStatus = null;
      List<Partner> contactPartnerList = new ArrayList<>();

      PartnerRepository partnerRepository = Beans.get(PartnerRepository.class);

      if (leadToPartnerSelect == LeadRepository.CONVERT_LEAD_CREATE_PARTNER) {
        partnerMap = this.getPartnerMap(leadToContactSelect != 0, request, response);
      } else if (leadToPartnerSelect == LeadRepository.CONVERT_LEAD_SELECT_PARTNER) {
        Map<String, Object> selectPartnerContext =
            (Map<String, Object>) context.get("selectPartner");
        partner = partnerRepository.find(((Integer) selectPartnerContext.get("id")).longValue());
      }

      if (leadToContactSelect == LeadRepository.CONVERT_LEAD_CREATE_CONTACT) {
        contactPartnerMap = this.getContactMap(request, response);
      } else if (leadToContactSelect == LeadRepository.CONVERT_LEAD_SELECT_CONTACT) {
        List<HashMap<String, Object>> selectContactContextList =
            (List<HashMap<String, Object>>) context.get("selectContactSet");
        for (HashMap<String, Object> selectContactContext : selectContactContextList) {
          contactPartnerList.add(
              partnerRepository.find(((Integer) selectContactContext.get("id")).longValue()));
        }
      }

      if (crmProcessOnPartner) {
        Map<String, Object> partnerStatusMap = (Map<String, Object>) context.get("partnerStatus");
        partnerStatus =
            Beans.get(PartnerStatusRepository.class)
                .find(((Integer) partnerStatusMap.get("id")).longValue());
        partner =
            Beans.get(ConvertLeadWizardService.class)
                .generateDataAndConvertLead(
                    lead,
                    leadToPartnerSelect,
                    leadToContactSelect,
                    partner,
                    partnerMap,
                    partnerStatus,
                    contactPartnerList,
                    contactPartnerMap);
      }
      openPartner(
          response,
          partner,
          partnerMap,
          crmProcessOnPartner,
          leadToPartnerSelect,
          lead,
          partnerStatus,
          contactPartnerList,
          contactPartnerMap);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected void openPartner(
      ActionResponse response,
      Partner partner,
      Map<String, Object> partnerMap,
      boolean crmProcessOnPartner,
      Integer leadToPartnerSelect,
      Lead lead,
      PartnerStatus partnerStatus,
      List<Partner> contactPartnerList,
      Map<String, Object> contactPartnerMap) {

    String form = "partner-form";
    String grid = "partner-grid";

    response.setCanClose(true);
    if (crmProcessOnPartner) {
      response.setView(
          ActionView.define(I18n.get(CrmExceptionMessage.CONVERT_LEAD_1))
              .model(Partner.class.getName())
              .add("form", form)
              .add("grid", grid)
              .param("search-filters", "partner-filters")
              .context("_showRecord", partner.getId())
              .context("_isFromCrm", true)
              .map());
    } else {
      if (leadToPartnerSelect == LeadRepository.CONVERT_LEAD_CREATE_PARTNER) {
        response.setView(
            ActionView.define(I18n.get(CrmExceptionMessage.CONVERT_LEAD_1))
                .model(Partner.class.getName())
                .add("form", form)
                .add("grid", grid)
                .param("search-filters", "partner-filters")
                .context("_isInConversionFromLead", true)
                .context("_lead", lead)
                .context("_isFromCrm", true)
                .context("_partnerMap", partnerMap)
                .context("_contactPartnerList", contactPartnerList)
                .context("_contactPartnerMap", contactPartnerMap)
                .map());
      } else if (leadToPartnerSelect == LeadRepository.CONVERT_LEAD_SELECT_PARTNER) {
        response.setView(
            ActionView.define(I18n.get(CrmExceptionMessage.CONVERT_LEAD_1))
                .model(Partner.class.getName())
                .add("form", form)
                .add("grid", grid)
                .param("search-filters", "partner-filters")
                .context("_isInConversionFromLead", true)
                .context("_showRecord", partner.getId())
                .context("_lead", lead)
                .context("_isFromCrm", true)
                .context("_contactPartnerList", contactPartnerList)
                .context("_contactPartnerMap", contactPartnerMap)
                .map());
      }
    }
  }

  public Map<String, Object> getPartnerMap(
      boolean isCompany, ActionRequest request, ActionResponse response) throws AxelorException {
    Lead lead = findLead(request);
    AppBase appBase = Beans.get(AppBaseService.class).getAppBase();
    Map<String, Object> partnerMap = new HashMap<String, Object>();
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
        response.setAttr("isProspect", "value", true);
      }
    } else {
      partnerMap.put("isProspect", true);
    }

    if (!isCompany || StringUtils.isEmpty(lead.getEnterpriseName())) {
      partnerMap.put("firstName", lead.getFirstName());
      partnerMap.put("name", lead.getName());
      partnerMap.put("titleSelect", lead.getTitleSelect());
      partnerMap.put("partnerTypeSelect", 2);

    } else {
      partnerMap.put("partnerTypeSelect", 1);
      partnerMap.put("name", lead.getEnterpriseName());
    }
    partnerMap.put("localization", appBase.getDefaultPartnerLocalization());
    return partnerMap;
  }

  public Map<String, Object> getContactMap(ActionRequest request, ActionResponse response)
      throws AxelorException, IOException {
    Map<String, Object> contactMap = new HashMap<String, Object>();
    Lead lead = findLead(request);
    if (lead.getPicture() != null) {
      MetaFile picture = Beans.get(BinaryConversionService.class).toMetaFile(lead.getPicture());
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

  public void setFieldsForConversionFromLeads(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Partner partner = context.asType(Partner.class);

      if (partner.getId() == null) {
        Map<String, Object> partnerMap = (Map<String, Object>) context.get("_partnerMap");
        for (Map.Entry partnerField : partnerMap.entrySet()) {
          response.setValue((String) partnerField.getKey(), partnerField.getValue());
        }
      }
      List<PartnerAddress> partnerAddressList = this.generateAddress(request, partner);
      response.setValue("partnerAddressList", partnerAddressList);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected List<PartnerAddress> generateAddress(ActionRequest request, Partner partner)
      throws AxelorException {
    Lead lead = this.findLead(request);
    Address address = lead.getAddress();
    if (address != null) {
      Beans.get(PartnerService.class).addPartnerAddress(partner, address, true, true, true);
    }
    return partner.getPartnerAddressList();
  }

  public void setFieldsForOpportunityGenerationFromPartner(
      ActionRequest request, ActionResponse response) {
    try {
      Map<String, Object> opportunityMap = this.getOpportunityMap(request);
      for (Map.Entry opportunityField : opportunityMap.entrySet()) {
        response.setValue((String) opportunityField.getKey(), opportunityField.getValue());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void convertLeadFromPartner(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Partner partner = context.asType(Partner.class);
      partner = Beans.get(PartnerRepository.class).find(partner.getId());
      Lead lead = this.findLead(request);

      ConvertLeadWizardService convertLeadWizardService = Beans.get(ConvertLeadWizardService.class);

      List<Map<String, Object>> partnerList =
          (List<Map<String, Object>>) context.get("_contactPartnerList");

      List<Partner> contactList = convertLeadWizardService.convertMapListToPartnerList(partnerList);

      Map<String, Object> contactPartnerMap =
          (Map<String, Object>) context.get("_contactPartnerMap");

      convertLeadWizardService.generateDataAndConvertLead(
          lead,
          LeadRepository.CONVERT_LEAD_SELECT_PARTNER,
          LeadRepository.CONVERT_LEAD_SELECT_CONTACT,
          partner,
          null,
          null,
          contactList,
          contactPartnerMap);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void openOpportunity(ActionRequest request, ActionResponse response) {

    try {

      Context context = request.getContext();
      Partner partner = context.asType(Partner.class);
      partner = Beans.get(PartnerRepository.class).find(partner.getId());

      Lead lead;
      Map<String, Object> leadContext = (Map<String, Object>) context.get("_lead");
      lead = Beans.get(LeadRepository.class).find(((Integer) leadContext.get("id")).longValue());

      String form = "opportunity-form";
      String grid = "opportunity-grid";
      response.setCanClose(true);
      response.setView(
          ActionView.define(I18n.get(CrmExceptionMessage.CONVERT_LEAD_1))
              .model(Opportunity.class.getName())
              .add("form", form)
              .add("grid", grid)
              .param("search-filters", "opportunity-filters")
              .context("_isGeneratedFromPartner", true)
              .context("_partner", partner)
              .context("_lead", lead)
              .context("_internalUserId", AuthUtils.getUser().getId())
              .context("_myActiveTeam", Beans.get(UserService.class).getUserActiveTeam())
              .context("todayDate", Beans.get(AppBaseService.class).getTodayDate(null))
              .map());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected Map<String, Object> getOpportunityMap(ActionRequest request) throws AxelorException {
    Map<String, Object> opportunityMap = new HashMap<String, Object>();
    Lead lead = findLead(request);
    opportunityMap.put("source", lead.getSource());
    opportunityMap.put("user", lead.getUser());
    return opportunityMap;
  }
}
