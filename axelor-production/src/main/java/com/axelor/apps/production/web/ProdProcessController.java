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
package com.axelor.apps.production.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.service.ProdProcessComputationService;
import com.axelor.apps.production.service.ProdProcessService;
import com.axelor.apps.production.service.ProdProcessWorkflowService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.helpers.StringHtmlListBuilder;
import com.google.inject.Singleton;
import java.math.BigDecimal;
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
    AppProductionService appProductionService = Beans.get(AppProductionService.class);
    response.setValue("prodProcessLineList", prodProcess.getProdProcessLineList());
    response.setHidden("prodProcessLineList.outsourcing", !prodProcess.getOutsourcing());

    if (!prodProcess.getOutsourcing() && !prodProcess.getOutsourcable()) {
      response.setValue("generatePurchaseOrderOnMoPlanning", false);
      response.setValue("subcontractor", null);
      response.setValue("outsourcingStockLocation", null);
    } else {
      response.setValue(
          "generatePurchaseOrderOnMoPlanning",
          appProductionService.getAppProduction().getManageOutsourcing()
              && appProductionService.getAppProduction().getGeneratePurchaseOrderOnMoPlanning());
    }
  }

  public void checkOriginalProductionProcess(ActionRequest request, ActionResponse response) {

    ProdProcessRepository prodProcessRepository = Beans.get(ProdProcessRepository.class);
    ProdProcess prodProcess =
        prodProcessRepository.find(request.getContext().asType(ProdProcess.class).getId());

    List<ProdProcess> prodProcessSet =
        prodProcessRepository
            .all()
            .filter("self.originalProdProcess = :origin")
            .bind("origin", prodProcess)
            .fetch();
    String message;

    if (!prodProcessSet.isEmpty()) {

      StringHtmlListBuilder builder = new StringHtmlListBuilder();
      prodProcessSet.stream().map(ProdProcess::getFullName).forEach(builder::append);

      message =
          String.format(
              I18n.get(
                  "This production process already has the following versions : <br/> %s And these versions may also have ones. Do you still wish to create a new one ?"),
              builder.toString());
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

  public void setDraftStaus(ActionRequest request, ActionResponse response) throws AxelorException {
    try {
      ProdProcess prodProcess = request.getContext().asType(ProdProcess.class);
      prodProcess = Beans.get(ProdProcessRepository.class).find(prodProcess.getId());
      Beans.get(ProdProcessWorkflowService.class).setDraftStatus(prodProcess);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setValidateStatus(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      ProdProcess prodProcess = request.getContext().asType(ProdProcess.class);
      prodProcess = Beans.get(ProdProcessRepository.class).find(prodProcess.getId());
      Beans.get(ProdProcessWorkflowService.class).setValidateStatus(prodProcess);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setApplicableStatus(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      ProdProcess prodProcess = request.getContext().asType(ProdProcess.class);
      prodProcess = Beans.get(ProdProcessRepository.class).find(prodProcess.getId());
      Beans.get(ProdProcessWorkflowService.class).setApplicableStatus(prodProcess);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setObsoleteStatus(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      ProdProcess prodProcess = request.getContext().asType(ProdProcess.class);
      prodProcess = Beans.get(ProdProcessRepository.class).find(prodProcess.getId());
      Beans.get(ProdProcessWorkflowService.class).setObsoleteStatus(prodProcess);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void recomputeLeadTime(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ProdProcess prodProcess = request.getContext().asType(ProdProcess.class);
    BigDecimal qty =
        prodProcess.getLaunchQty() != null
                && prodProcess.getLaunchQty().compareTo(BigDecimal.ZERO) > 0
            ? prodProcess.getLaunchQty()
            : BigDecimal.ONE;
    if (prodProcess.getCompany() != null) {
      response.setValue(
          "leadTime", Beans.get(ProdProcessComputationService.class).getLeadTime(prodProcess, qty));
    }
  }
}
