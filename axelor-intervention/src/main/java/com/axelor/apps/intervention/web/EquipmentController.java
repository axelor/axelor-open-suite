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

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.EquipmentLine;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.intervention.repo.EquipmentRepository;
import com.axelor.apps.intervention.service.EquipmentService;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class EquipmentController {

  public static final String FIELD_PARTNER_ID = "_xPartnerId";
  public static final String FIELD_PARENT_EQUIPMENT_ID = "_xParentEquipmentId";

  public void fillDefaultValues(ActionRequest request, ActionResponse response) {
    try {
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
      if (request.getContext().getParent() != null
          && request.getContext().getParent().getContextClass().equals(Intervention.class)) {
        response.setValue("partner", request.getContext().getParent().get("deliveredPartner"));
        response.setValue("contract", request.getContext().getParent().get("contract"));
      }
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
