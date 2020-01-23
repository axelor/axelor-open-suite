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
package com.axelor.apps.production.web;

import com.axelor.apps.production.db.RawMaterialRequirement;
import com.axelor.apps.production.db.repo.RawMaterialRequirementRepository;
import com.axelor.apps.production.service.RawMaterialRequirementService;
import com.axelor.apps.production.service.RawMaterialRequirementServiceImpl;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class RawMaterialRequirementController {

  /**
   * Called from the raw material requirement view, on clicking "print" button. Call {@link
   * RawMaterialRequirementService#print(RawMaterialRequirement)} and then show the printed report.
   *
   * @param request
   * @param response
   */
  public void print(ActionRequest request, ActionResponse response) {
    try {
      RawMaterialRequirement rawMaterialRequirement =
          request.getContext().asType(RawMaterialRequirement.class);
      rawMaterialRequirement =
          Beans.get(RawMaterialRequirementRepository.class).find(rawMaterialRequirement.getId());
      String fileLink =
          Beans.get(RawMaterialRequirementService.class).print(rawMaterialRequirement);
      response.setView(
          ActionView.define(I18n.get(RawMaterialRequirementServiceImpl.RAW_MATERIAL_REPORT_TITLE))
              .add("html", fileLink)
              .map());
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }
}
