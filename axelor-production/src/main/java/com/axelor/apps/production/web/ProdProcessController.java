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
package com.axelor.apps.production.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.service.ProdProcessService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class ProdProcessController {

  public void validateProdProcess(ActionRequest request, ActionResponse response) {
    ProdProcess prodProcess = request.getContext().asType(ProdProcess.class);
    if (prodProcess.getIsConsProOnOperation()) {
      BillOfMaterial bom = null;
      if (request.getContext().getParent() != null
          && request
              .getContext()
              .getParent()
              .getContextClass()
              .getName()
              .equals(BillOfMaterial.class.getName())) {
        bom = request.getContext().getParent().asType(BillOfMaterial.class);
      } else {
        bom =
            Beans.get(BillOfMaterialRepository.class)
                .all()
                .filter("self.prodProcess.id = ?1", prodProcess.getId())
                .fetchOne();
      }
      if (bom != null) {
        try {
          Beans.get(ProdProcessService.class).validateProdProcess(prodProcess, bom);
        } catch (AxelorException e) {
          TraceBackService.trace(response, e, ResponseMessageType.ERROR);
        }
      }
    }
  }

  public void changeProdProcessListOutsourcing(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ProdProcess prodProcess = request.getContext().asType(ProdProcess.class);
    if (prodProcess.getProdProcessLineList() != null) {
      Beans.get(ProdProcessService.class).changeProdProcessListOutsourcing(prodProcess);
    }
    response.setValue("prodProcessLineList", prodProcess.getProdProcessLineList());
    response.setHidden("prodProcessLineList.outsourcing", !prodProcess.getOutsourcing());

    if (!prodProcess.getOutsourcing()) {
      response.setValue("generatePurchaseOrderOnMoPlanning", false);
      response.setValue("subcontractor", null);
      response.setValue("outsourcingStockLocation", null);
    }
  }

  public void print(ActionRequest request, ActionResponse response) {

    try {
      ProdProcess prodProcess = request.getContext().asType(ProdProcess.class);
      String fileLink = Beans.get(ProdProcessService.class).print(prodProcess);
      response.setView(ActionView.define(prodProcess.getName()).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkOriginalProductionProcess(ActionRequest request, ActionResponse response) {

    ProdProcessRepository prodProcessRepository = Beans.get(ProdProcessRepository.class);
    ProdProcess prodProcess =
        prodProcessRepository.find(request.getContext().asType(ProdProcess.class).getId());

    List<ProdProcess> prodProcessSet = Lists.newArrayList();
    prodProcessSet =
        prodProcessRepository
            .all()
            .filter("self.originalProdProcess = :origin")
            .bind("origin", prodProcess)
            .fetch();
    String message;

    if (!prodProcessSet.isEmpty()) {

      String existingVersions = "";
      for (ProdProcess prodProcessVersion : prodProcessSet) {
        existingVersions += "<li>" + prodProcessVersion.getFullName() + "</li>";
      }
      message =
          String.format(
              I18n.get(
                  "This production process already has the following versions : <br/><ul> %s </ul>And these versions may also have ones. Do you still wish to create a new one ?"),
              existingVersions);
    } else {
      message = I18n.get("Do you really wish to create a new version of this production process ?");
    }

    response.setAlert(message);
  }

  public void generateNewVersion(ActionRequest request, ActionResponse response) {

    ProdProcess prodProcess =
        Beans.get(ProdProcessRepository.class)
            .find(request.getContext().asType(ProdProcess.class).getId());

    ProdProcess copy = Beans.get(ProdProcessService.class).generateNewVersion(prodProcess);

    response.setView(
        ActionView.define(I18n.get("Production process"))
            .model(ProdProcess.class.getName())
            .add("form", "prod-process-form")
            .add("grid", "prod-process-grid")
            .param("search-filters", "prod-process-filters")
            .context("_showRecord", String.valueOf(copy.getId()))
            .map());
  }
}
