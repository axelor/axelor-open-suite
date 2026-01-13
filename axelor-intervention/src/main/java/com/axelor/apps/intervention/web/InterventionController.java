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
package com.axelor.apps.intervention.web;

import com.axelor.apps.base.db.Duration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerLinkTypeRepository;
import com.axelor.apps.base.service.PartnerLinkService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.intervention.db.InterventionType;
import com.axelor.apps.intervention.db.repo.InterventionRepository;
import com.axelor.apps.intervention.exception.InterventionExceptionMessage;
import com.axelor.apps.intervention.repo.EquipmentRepository;
import com.axelor.apps.intervention.service.InterventionService;
import com.axelor.apps.intervention.service.helper.InterventionHelper;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.auth.db.User;
import com.axelor.db.JpaRepository;
import com.axelor.db.mapper.Adapter;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.helpers.MapHelper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class InterventionController {

  public void fillDefault(ActionRequest request, ActionResponse response) {
    try {
      Intervention intervention = request.getContext().asType(Intervention.class);
      if (request.getContext().getParent() != null
          && request.getContext().getParent().getContextClass().equals(Intervention.class)) {
        Intervention interventionParent =
            request.getContext().getParent().asType(Intervention.class);
        InterventionHelper.fillDefaultFromParent(intervention, interventionParent);
      } else {
        InterventionHelper.fillDefault(intervention);
      }
      response.setValues(intervention);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeUnderContractEquipmentsNbr(ActionRequest request, ActionResponse response) {
    try {
      Intervention intervention = request.getContext().asType(Intervention.class);
      if (intervention == null) {
        return;
      }
      intervention = Beans.get(InterventionRepository.class).find(intervention.getId());
      response.setValue(
          "$_xUnderContractEquipmentsNbr",
          InterventionHelper.computeUnderContractEquipmentsNbr(intervention));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void start(ActionRequest request, ActionResponse response) {
    try {
      Intervention intervention = request.getContext().asType(Intervention.class);
      intervention = Beans.get(InterventionRepository.class).find(intervention.getId());
      Beans.get(InterventionService.class).start(intervention, LocalDateTime.now());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void reschedule(ActionRequest request, ActionResponse response) {
    try {
      Intervention intervention = request.getContext().asType(Intervention.class);
      intervention = Beans.get(InterventionRepository.class).find(intervention.getId());
      Beans.get(InterventionService.class).reschedule(intervention);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void suspend(ActionRequest request, ActionResponse response) {
    try {
      Intervention intervention = request.getContext().asType(Intervention.class);
      intervention = Beans.get(InterventionRepository.class).find(intervention.getId());
      Beans.get(InterventionService.class).suspend(intervention, LocalDateTime.now());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void cancel(ActionRequest request, ActionResponse response) {
    try {
      Intervention intervention = request.getContext().asType(Intervention.class);
      intervention = Beans.get(InterventionRepository.class).find(intervention.getId());
      Beans.get(InterventionService.class).cancel(intervention);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void finish(ActionRequest request, ActionResponse response) {
    try {
      Intervention intervention = request.getContext().asType(Intervention.class);
      intervention = Beans.get(InterventionRepository.class).find(intervention.getId());
      Beans.get(InterventionService.class).finish(intervention, LocalDateTime.now());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void fillInterventionType(ActionRequest request, ActionResponse response) {
    try {
      Intervention intervention = request.getContext().asType(Intervention.class);

      List<InterventionType> interventionTypes =
          Optional.ofNullable(
                  JpaRepository.of(InterventionType.class)
                      .all()
                      .filter(InterventionHelper.computeInterventionTypeDomain(intervention))
                      .fetch())
              .orElse(Collections.emptyList());
      response.setAttr(
          "interventionType",
          "value",
          interventionTypes.size() == 1 ? interventionTypes.get(0) : null);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void selectInterventionType(ActionRequest request, ActionResponse response) {
    try {
      Intervention intervention = request.getContext().asType(Intervention.class);
      response.setAttr(
          "interventionType",
          "domain",
          InterventionHelper.computeInterventionTypeDomain(intervention));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillFromContract(ActionRequest request, ActionResponse response) {
    try {
      Intervention intervention = request.getContext().asType(Intervention.class);
      Contract contract = intervention.getContract();
      if (intervention == null || contract == null) {
        return;
      }

      if (intervention.getEquipmentSet() != null) {
        intervention.getEquipmentSet().clear();
      }
      intervention.setEquipmentSet(
          new HashSet<>(Beans.get(EquipmentRepository.class).findByContract(contract.getId())));
      intervention.setPlanningPreferenceSelect(contract.getPlanningPreferenceSelect());
      response.setValues(intervention);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillFromRequest(ActionRequest request, ActionResponse response) {
    try {
      Intervention intervention = request.getContext().asType(Intervention.class);
      if (intervention == null || intervention.getCustomerRequest() == null) {
        return;
      }
      Beans.get(InterventionService.class)
          .fillFromRequest(intervention, intervention.getCustomerRequest());
      response.setValues(intervention);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeEstimatedEndDateTime(ActionRequest request, ActionResponse response) {
    try {
      LocalDateTime planificationDateTime =
          (LocalDateTime)
              Adapter.adapt(
                  request.getContext().get("_xPlanificationDateTime"),
                  LocalDateTime.class,
                  LocalDateTime.class,
                  null);
      Duration plannedInterventionDuration =
          MapHelper.get(request.getContext(), Duration.class, "_xPlannedInterventionDuration");
      if (planificationDateTime == null || plannedInterventionDuration == null) {
        return;
      }
      response.setValue(
          "_xEstimatedEndDateTime",
          Beans.get(InterventionService.class)
              .computeEstimatedEndDateTime(planificationDateTime, plannedInterventionDuration));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void plan(ActionRequest request, ActionResponse response) {
    try {
      Intervention intervention = request.getContext().asType(Intervention.class);
      intervention = Beans.get(InterventionRepository.class).find(intervention.getId());
      TraceBackService traceBackService = Beans.get(TraceBackService.class);
      long tracebackCount = traceBackService.countMessageTraceBack(intervention);
      User technicianUser = MapHelper.get(request.getContext(), User.class, "_xTechnicianUser");
      LocalDateTime planificationDateTime =
          (LocalDateTime)
              Adapter.adapt(
                  request.getContext().get("_xPlanificationDateTime"),
                  LocalDateTime.class,
                  LocalDateTime.class,
                  null);
      LocalDateTime estimatedEndDateTime =
          (LocalDateTime)
              Adapter.adapt(
                  request.getContext().get("_xEstimatedEndDateTime"),
                  LocalDateTime.class,
                  LocalDateTime.class,
                  null);
      if (technicianUser == null || planificationDateTime == null || estimatedEndDateTime == null) {
        return;
      }
      Beans.get(InterventionService.class)
          .plan(
              intervention, response, technicianUser, planificationDateTime, estimatedEndDateTime);
      if (traceBackService.countMessageTraceBack(intervention) > tracebackCount) {
        traceBackService
            .findLastMessageTraceBack(intervention)
            .ifPresent(
                traceback ->
                    response.setError(
                        String.format(
                            "<b>%s :</b><br>%s",
                            I18n.get(InterventionExceptionMessage.MESSAGE_ON_EXCEPTION),
                            traceback.getMessage())));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void setInvoicedPartnerDomain(ActionRequest request, ActionResponse response) {
    try {
      Intervention intervention = request.getContext().asType(Intervention.class);
      Partner client = null;
      if (intervention != null) {
        client = intervention.getDeliveredPartner();
      }

      String strFilter =
          Beans.get(PartnerLinkService.class)
              .computePartnerFilter(client, PartnerLinkTypeRepository.TYPE_SELECT_INVOICED_TO);

      response.setAttr("invoicedPartner", "domain", strFilter);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setDefaultInvoicedPartner(ActionRequest request, ActionResponse response) {
    try {
      Intervention intervention = request.getContext().asType(Intervention.class);
      Partner invoicedPartner =
          Beans.get(InterventionService.class).getDefaultInvoicedPartner(intervention);

      response.setAttr("invoicedPartner", "value", invoicedPartner);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateSaleOrder(ActionRequest request, ActionResponse response) {
    try {
      Intervention intervention = request.getContext().asType(Intervention.class);
      intervention = Beans.get(InterventionRepository.class).find(intervention.getId());

      SaleOrder saleOrder = Beans.get(InterventionService.class).generateSaleOrder(intervention);

      if (saleOrder != null) {
        response.setView(
            ActionView.define(I18n.get("Sale order"))
                .model(SaleOrder.class.getName())
                .add("form", "sale-order-form")
                .context("_showRecord", saleOrder.getId())
                .map());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }
}
