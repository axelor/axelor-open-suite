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
package com.axelor.apps.intervention.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.EquipmentLine;
import com.axelor.apps.intervention.service.EquipmentLineService;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.db.Wizard;
import com.axelor.utils.helpers.MapHelper;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class EquipmentLineController {

  public static final String FIELD_TRACKING_NUMBER = "trackingNumber";

  public void createTrackingNumber(ActionRequest request, ActionResponse response) {
    try {
      EquipmentLine equipmentLine = request.getContext().asType(EquipmentLine.class);
      if (equipmentLine == null) {
        return;
      }
      Product product = equipmentLine.getProduct();
      if (product == null
          || product.getTrackingNumberConfiguration() == null
          || !Boolean.TRUE.equals(
              product.getTrackingNumberConfiguration().getGenerateSaleAutoTrackingNbr())) {
        response.setValue(FIELD_TRACKING_NUMBER, null);
        return;
      }
      TrackingNumber trackingNumber =
          Beans.get(EquipmentLineService.class).createTrackingNumber(equipmentLine);
      response.setValue(FIELD_TRACKING_NUMBER, trackingNumber);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setTrackingNumberAttrs(ActionRequest request, ActionResponse response) {
    try {
      EquipmentLine equipmentLine = request.getContext().asType(EquipmentLine.class);
      Product product = equipmentLine.getProduct();
      if (product == null || product.getTrackingNumberConfiguration() == null) {
        response.setAttr("hidden", FIELD_TRACKING_NUMBER, true);
        return;
      }
      if (product.getTrackingNumberConfiguration() != null
          && Boolean.TRUE.equals(
              product.getTrackingNumberConfiguration().getGenerateSaleAutoTrackingNbr())) {
        response.setAttr(FIELD_TRACKING_NUMBER, "readonly", true);
        response.setAttr(FIELD_TRACKING_NUMBER, "hidden", false);
        return;
      }
      if (product.getTrackingNumberConfiguration() != null
          && Boolean.TRUE.equals(
              product.getTrackingNumberConfiguration().getIsPurchaseTrackingManaged())) {
        response.setAttr(FIELD_TRACKING_NUMBER, "required", true);
        response.setAttr(FIELD_TRACKING_NUMBER, "hidden", false);
        response.setAttr(FIELD_TRACKING_NUMBER, "readonly", false);
        return;
      }
      response.setAttr(FIELD_TRACKING_NUMBER, "hidden", true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void changeEquipment(ActionRequest request, ActionResponse response) {
    try {
      List<EquipmentLine> equipmentLineList =
          MapHelper.getSelectedObjects(EquipmentLine.class, request.getContext());
      Equipment equipment = MapHelper.get(request.getContext(), Equipment.class, "_xEquipment");

      if (CollectionUtils.isNotEmpty(equipmentLineList)) {
        if (equipmentLineList.stream()
                .map(EquipmentLine::getEquipment)
                .map(Equipment::getPartner)
                .map(Partner::getId)
                .distinct()
                .count()
            > 1) {
          throw new IllegalArgumentException(
              I18n.get("All articles must belong to the same partner"));
        }
        response.setView(
            ActionView.define(I18n.get("Change equipment"))
                .model(Wizard.class.getName())
                .add("form", "equipment-line-change-equipment-form")
                .param("show-toolbar", "false")
                .param("show-confirm", "true")
                .param("popup-save", "true")
                .param("forceEdit", "true")
                .param("popup", "reload")
                .context(
                    "_ctxPartnerId",
                    equipmentLineList.stream()
                        .map(EquipmentLine::getEquipment)
                        .map(Equipment::getPartner)
                        .map(Partner::getId)
                        .findFirst()
                        .orElse(null))
                .context("_ctxEquipmentLines", equipmentLineList)
                .map());
        return;
      }

      if (equipment != null) {
        equipmentLineList =
            MapHelper.getCollection(
                request.getContext(), EquipmentLine.class, "_ctxEquipmentLines");
        Beans.get(EquipmentLineService.class).changeEquipment(equipmentLineList, equipment);
        response.setCanClose(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
