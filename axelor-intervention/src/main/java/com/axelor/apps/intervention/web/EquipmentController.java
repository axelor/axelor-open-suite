package com.axelor.apps.intervention.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.EquipmentLine;
import com.axelor.apps.intervention.repo.EquipmentRepository;
import com.axelor.apps.intervention.service.EquipmentService;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EquipmentController {

  public static final String FIELD_PARTNER_ID = "_xPartnerId";
  public static final String FIELD_PARENT_EQUIPMENT_ID = "_xParentEquipmentId";

  public void fillDefaultValues(ActionRequest request, ActionResponse response) {
    try {
      response.setValue(
          "customerWarrantyOnPartEndDate",
          LocalDate.parse("01/01/2020", DateTimeFormatter.ofPattern("dd/MM/yyyy")));
      if (request.getContext().get(FIELD_PARTNER_ID) != null) {
        response.setValue(
            "partner",
            JPA.find(
                Partner.class,
                Long.valueOf(request.getContext().get(FIELD_PARTNER_ID).toString())));
      }
      if (request.getContext().get(FIELD_PARENT_EQUIPMENT_ID) != null) {
        response.setValue(
            "parentEquipment",
            JPA.find(
                Equipment.class,
                Long.valueOf(request.getContext().get(FIELD_PARENT_EQUIPMENT_ID).toString())));
      }
      /* if (request.getContext().getParent() != null
              && request.getContext().getParent().getContextClass().equals(Intervention.class)) {
          response.setValue("partner", request.getContext().getParent().get("deliveredPartner"));
          response.setValue("contract", request.getContext().getParent().get("contract"));
      }*/
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void openNewItemOnTree(ActionRequest request, ActionResponse response) {
    try {
      if (request.getContext().getContextClass().equals(Partner.class)) {
        Long partnerId = Long.valueOf(request.getContext().get("id").toString());
        response.setView(
            ActionView.define("Equipment")
                .model(Equipment.class.getName())
                .add("form", "equipment-form")
                .param("forceEdit", "true")
                .context("_showSingle", true)
                .context(FIELD_PARTNER_ID, partnerId)
                .map());
      } else if (request.getContext().getContextClass().equals(Equipment.class)) {
        Equipment equipment = request.getContext().asType(Equipment.class);
        if (equipment.getTypeSelect().equals(EquipmentRepository.INTERVENTION_TYPE_PLACE)) {
          response.setView(
              ActionView.define("Equipment")
                  .model(Equipment.class.getName())
                  .add("form", "equipment-form")
                  .param("forceEdit", "true")
                  .context("_showSingle", true)
                  .context(FIELD_PARTNER_ID, equipment.getPartner().getId())
                  .context(FIELD_PARENT_EQUIPMENT_ID, equipment.getId())
                  .map());
        } else if (equipment
            .getTypeSelect()
            .equals(EquipmentRepository.INTERVENTION_TYPE_EQUIPMENT)) {
          response.setView(
              ActionView.define("Equipment line")
                  .model(EquipmentLine.class.getName())
                  .add("form", "equipment-line-form")
                  .param("forceEdit", "true")
                  .context("_showSingle", true)
                  .context("_xEquipmentId", equipment.getId())
                  .map());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void removeEquipment(ActionRequest request, ActionResponse response) {
    try {
      if (request.getContext().get("id") != null
          && request
              .getContext()
              .get("_model")
              .toString()
              .equals("com.axelor.apps.intervention.db.Equipment")) {
        EquipmentRepository equipmentRepository = Beans.get(EquipmentRepository.class);
        Beans.get(EquipmentService.class)
            .removeEquipment(
                equipmentRepository.find(Long.valueOf(request.getContext().get("id").toString())));
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
