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
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.crm.db.LostReason;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.crm.service.OpportunityService;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class OpportunityController {

  public void assignToMe(ActionRequest request, ActionResponse response) {

    OpportunityService opportunityService = Beans.get(OpportunityService.class);
    OpportunityRepository opportunityRepository = Beans.get(OpportunityRepository.class);
    if (request.getContext().get("id") != null) {
      Opportunity opportunity = opportunityRepository.find((Long) request.getContext().get("id"));
      opportunity.setUser(AuthUtils.getUser());
      opportunityService.saveOpportunity(opportunity);
    } else if (ObjectUtils.notEmpty(request.getContext().get("_ids"))) {
      for (Opportunity opportunity :
          opportunityRepository
              .all()
              .filter("id in ?1", request.getContext().get("_ids"))
              .fetch()) {
        opportunity.setUser(AuthUtils.getUser());
        opportunityService.saveOpportunity(opportunity);
      }
    } else {
      response.setNotify(I18n.get(BaseExceptionMessage.RECORD_NONE_SELECTED));
      return;
    }

    response.setReload(true);
  }

  public void showOpportunitiesOnMap(ActionRequest request, ActionResponse response) {
    try {
      response.setView(
          ActionView.define(I18n.get("Opportunities"))
              .add("html", Beans.get(MapService.class).getMapURI("opportunity"))
              .map());
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void setStageClosedWon(ActionRequest request, ActionResponse response) {
    try {
      Opportunity opportunity = request.getContext().asType(Opportunity.class);
      opportunity = Beans.get(OpportunityRepository.class).find(opportunity.getId());

      Beans.get(OpportunityService.class).setOpportunityStatusStagedClosedWon(opportunity);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setStageClosedLost(ActionRequest request, ActionResponse response) {
    try {
      Opportunity opportunity = request.getContext().asType(Opportunity.class);
      opportunity = Beans.get(OpportunityRepository.class).find(opportunity.getId());

      Beans.get(OpportunityService.class).setOpportunityStatusStagedClosedLost(opportunity);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setNextStage(ActionRequest request, ActionResponse response) {
    try {
      Opportunity opportunity = request.getContext().asType(Opportunity.class);
      opportunity = Beans.get(OpportunityRepository.class).find(opportunity.getId());

      Beans.get(OpportunityService.class).setOpportunityStatusNextStage(opportunity);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillEndDate(ActionRequest request, ActionResponse response) {
    Opportunity opportunity = request.getContext().asType(Opportunity.class);
    LocalDate recurringStartDate = opportunity.getRecurringStartDate();
    int recurringRevanue = opportunity.getExpectedDurationOfRecurringRevenue();
    if (recurringRevanue != 0 && recurringStartDate != null) {
      LocalDate newDate = recurringStartDate.plusMonths((long) recurringRevanue);
      response.setValue("recurringEndDate", newDate);
    }
  }

  public void winningProcess(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Opportunity opportunity = context.asType(Opportunity.class);
      opportunity = Beans.get(OpportunityRepository.class).find(opportunity.getId());
      Map<String, Boolean> map = new HashMap<>();
      map.put("isCustomer", (Boolean) context.get("isCustomer"));
      map.put("isProspect", (Boolean) context.get("isProspect"));
      map.put("isSupplier", (Boolean) context.get("isSupplier"));
      map.put("isEmployee", (Boolean) context.get("isEmployee"));
      map.put("isContact", (Boolean) context.get("isContact"));
      map.put("isInternal", (Boolean) context.get("isInternal"));
      map.put("isPartner", (Boolean) context.get("isPartner"));

      List<Opportunity> otherOpportunities =
          Beans.get(OpportunityService.class).winningProcess(opportunity, map);

      if (!CollectionUtils.isEmpty(otherOpportunities)) {
        response.setView(
            ActionView.define(I18n.get("Other opportunities"))
                .model(Opportunity.class.getName())
                .add("form", "other-opportunity-found-popup")
                .param("popup", "true")
                .param("show-toolbar", "false")
                .param("show-confirm", "false")
                .param("popup-save", "false")
                .context(
                    "_otherOpportunities",
                    otherOpportunities.stream()
                        .map(Opportunity::getId)
                        .collect(Collectors.toList()))
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void lostProcess(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();

      List<Integer> opportunityIdList =
          (List<Integer>) request.getContext().get("_otherOpportunities");
      List<Opportunity> otherOpportunities = new ArrayList<>();
      OpportunityRepository opportunityRepository = Beans.get(OpportunityRepository.class);
      for (Integer opportunityId : opportunityIdList) {
        otherOpportunities.add(opportunityRepository.find(opportunityId.longValue()));
      }

      LostReason lostReason = (LostReason) context.get("lostReason");
      String lostReasonStr = null;
      if (context.get("lostReasonStr") != null) {
        lostReasonStr = context.get("lostReasonStr").toString();
      }

      Beans.get(OpportunityService.class)
          .lostProcess(otherOpportunities, lostReason, lostReasonStr);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void kanbanOpportunityOnMove(ActionRequest request, ActionResponse response) {
    Opportunity opportunity = request.getContext().asType(Opportunity.class);
    try {
      Beans.get(OpportunityService.class).kanbanOpportunityOnMove(opportunity);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
