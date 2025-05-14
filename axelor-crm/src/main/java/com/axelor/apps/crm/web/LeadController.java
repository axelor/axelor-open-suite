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
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.address.AddressAttrsService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.apps.crm.service.CrmActivityService;
import com.axelor.apps.crm.service.EmailDomainToolService;
import com.axelor.apps.crm.service.LeadDuplicateService;
import com.axelor.apps.crm.service.LeadService;
import com.axelor.apps.crm.translation.ITranslation;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class LeadController {

  // using provider for the injection of a parameterized service
  @Inject private Provider<EmailDomainToolService<Lead>> emailDomainToolServiceProvider;

  protected String getTimezone(Lead lead) {
    if (lead.getUser() == null || lead.getUser().getActiveCompany() == null) {
      return null;
    }
    return lead.getUser().getActiveCompany().getTimezone();
  }

  public void showLeadsOnMap(ActionRequest request, ActionResponse response) {
    try {
      response.setView(
          ActionView.define(I18n.get("Leads"))
              .add("html", Beans.get(MapService.class).getMapURI("lead"))
              .map());
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void setSocialNetworkUrl(ActionRequest request, ActionResponse response)
      throws IOException {

    Lead lead = request.getContext().asType(Lead.class);
    Map<String, String> urlMap =
        Beans.get(LeadService.class)
            .getSocialNetworkUrl(lead.getName(), lead.getFirstName(), lead.getEnterpriseName());
    response.setAttr("googleLabel", "title", urlMap.get("google"));
    response.setAttr("linkedinLabel", "title", urlMap.get("linkedin"));
  }

  /**
   * Called from lead view on name change and onLoad. Call {@link
   * LeadService#isThereDuplicateLead(Lead)}
   *
   * @param request
   * @param response
   */
  public void checkLeadName(ActionRequest request, ActionResponse response) {
    Lead lead = request.getContext().asType(Lead.class);
    response.setAttr(
        "duplicateLeadText", "hidden", !Beans.get(LeadService.class).isThereDuplicateLead(lead));
  }

  public void loseLead(ActionRequest request, ActionResponse response) {
    try {
      Lead lead = request.getContext().asType(Lead.class);
      Beans.get(LeadService.class)
          .loseLead(
              Beans.get(LeadRepository.class).find(lead.getId()),
              lead.getLostReason(),
              lead.getLostReasonStr());
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void assignToMeLead(ActionRequest request, ActionResponse response) {
    try {
      Lead lead = request.getContext().asType(Lead.class);
      Beans.get(LeadService.class)
          .assignToMeLead(Beans.get(LeadRepository.class).find(lead.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void assignToMeMultipleLead(ActionRequest request, ActionResponse response) {
    try {
      LeadRepository leadRepo = Beans.get(LeadRepository.class);
      Beans.get(LeadService.class)
          .assignToMeMultipleLead(
              leadRepo.all().filter("id in ?1", request.getContext().get("_ids")).fetch());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void viewLeadWithSameDomainName(ActionRequest request, ActionResponse response) {
    try {
      Lead lead = request.getContext().asType(Lead.class);
      lead = Beans.get(LeadRepository.class).find(lead.getId());

      List<Lead> leadList =
          emailDomainToolServiceProvider
              .get()
              .getEntitiesWithSameEmailAddress(
                  Lead.class, lead.getId(), lead.getEmailAddress(), null);

      if (ObjectUtils.notEmpty(leadList)) {
        response.setView(null);

        response.setView(
            ActionView.define(I18n.get("Lead with same domain name"))
                .model(Lead.class.getName())
                .add("form", "lead-with-same-domain-name-form")
                .param("popup", "true")
                .param("show-toolbar", "false")
                .param("show-confirm", "true")
                .param("popup-save", "false")
                .param("forceEdit", "false")
                .context("_leadList", leadList)
                .context("_showRecord", lead.getId())
                .map());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void kanbanLeadOnMove(ActionRequest request, ActionResponse response) {
    Lead lead = request.getContext().asType(Lead.class);
    try {
      Beans.get(LeadService.class).kanbanLeadOnMove(lead);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void rollbackLeadStatus(ActionRequest request, ActionResponse response) {
    Lead lead = request.getContext().asType(Lead.class);
    lead = Beans.get(LeadRepository.class).find(lead.getId());

    response.setValue("leadStatus", lead.getLeadStatus());
  }

  public void computeIsLost(ActionRequest request, ActionResponse response) {
    try {
      Lead lead = request.getContext().asType(Lead.class);
      response.setValue("$isLost", Beans.get(LeadService.class).computeIsLost(lead));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getLeadActivityData(ActionRequest request, ActionResponse response)
      throws JsonProcessingException, AxelorException {
    List<Map<String, Object>> dataList;
    String id = Optional.ofNullable(request.getData().get("id")).map(Object::toString).orElse("");
    if (StringUtils.isBlank(id)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(CrmExceptionMessage.CRM_LEAD_NOT_FOUND));
    }
    dataList = Beans.get(CrmActivityService.class).getLeadActivityData(Long.valueOf(id));
    response.setData(dataList);
  }

  public void showDuplicateRecordsFullName(ActionRequest request, ActionResponse response) {
    Lead lead = request.getContext().asType(Lead.class);
    if (lead.getId() == null) {
      String fullNamesStr = Beans.get(LeadDuplicateService.class).getDuplicateRecordsFullName(lead);
      if (StringUtils.notBlank(fullNamesStr)) {
        response.setAlert(
            String.format(
                "%s<br/><br/>%s",
                I18n.get(CrmExceptionMessage.CRM_EMAIL_DOMAIN_ALREADY_EXISTS), fullNamesStr),
            I18n.get(ITranslation.CRM_DUPLICATE_RECORDS_FOUND));
      }
    }
  }

  public void resetLead(ActionRequest request, ActionResponse response) {
    try {
      Lead lead = request.getContext().asType(Lead.class);
      Beans.get(LeadService.class).resetLead(Beans.get(LeadRepository.class).find(lead.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getAddressMetaField(ActionRequest request, ActionResponse response) {
    Lead lead = request.getContext().asType(Lead.class);
    Address address = lead.getAddress();
    if (address != null && address.getCountry() != null) {
      Map<String, Map<String, Object>> map =
          Beans.get(AddressAttrsService.class).getCountryAddressMetaFieldOnChangeAttrsMap(address);
      Map<String, Map<String, Object>> attrsMap =
          map.entrySet().stream()
              .collect(Collectors.toMap(entry -> "address." + entry.getKey(), Map.Entry::getValue));

      response.setAttrs(attrsMap);
    }
  }
}
