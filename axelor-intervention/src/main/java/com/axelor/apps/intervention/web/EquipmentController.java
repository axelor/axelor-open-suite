package com.axelor.apps.intervention.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.EquipmentLine;
import com.axelor.apps.intervention.repo.EquipmentRepository;
import com.axelor.apps.intervention.service.EquipmentService;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.helpers.MapHelper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EquipmentController {

  public void fillDefaultValues(ActionRequest request, ActionResponse response) {
    try {
      response.setValue(
          "customerWarrantyOnPartEndDate",
          LocalDate.parse("01/01/2020", DateTimeFormatter.ofPattern("dd/MM/yyyy")));
      if (request.getContext().get("_xPartnerId") != null) {
        response.setValue(
            "partner",
            JPA.find(
                Partner.class, Long.valueOf(request.getContext().get("_xPartnerId").toString())));
      }
      if (request.getContext().get("_xParentEquipmentId") != null) {
        response.setValue(
            "parentEquipment",
            JPA.find(
                Equipment.class,
                Long.valueOf(request.getContext().get("_xParentEquipmentId").toString())));
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
                .context("_xPartnerId", partnerId)
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
                  .context("_xPartnerId", equipment.getPartner().getId())
                  .context("_xParentEquipmentId", equipment.getId())
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

  public void importEquipments(ActionRequest request, ActionResponse response) {
    try {
      Partner partner = MapHelper.get(request.getContext(), Partner.class, "_xPartner");
      MetaFile metaFile = MapHelper.get(request.getContext(), MetaFile.class, "_xFile");
      response.setExportFile(
          Beans.get(EquipmentService.class).importEquipments(partner.getId(), metaFile).toString());
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void loadFormatFile(ActionRequest request, ActionResponse response) {
    response.setValue("$_xEmptyImportFormat", Beans.get(EquipmentService.class).loadFormatFile());
  }
}
