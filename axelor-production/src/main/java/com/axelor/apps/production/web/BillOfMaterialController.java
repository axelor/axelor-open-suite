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

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.CostSheet;
import com.axelor.apps.production.db.TempBomTree;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.ProdProcessService;
import com.axelor.apps.production.service.costsheet.CostSheetService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BillOfMaterialController {

  private static final Logger LOG = LoggerFactory.getLogger(BillOfMaterialController.class);

  public void computeCostPrice(ActionRequest request, ActionResponse response)
      throws AxelorException {

    BillOfMaterial billOfMaterial = request.getContext().asType(BillOfMaterial.class);

    CostSheet costSheet =
        Beans.get(CostSheetService.class)
            .computeCostPrice(
                Beans.get(BillOfMaterialRepository.class).find(billOfMaterial.getId()),
                CostSheetService.ORIGIN_BILL_OF_MATERIAL,
                null);

    response.setView(
        ActionView.define(String.format(I18n.get("Cost sheet - %s"), billOfMaterial.getName()))
            .model(CostSheet.class.getName())
            .param("popup", "true")
            .param("show-toolbar", "false")
            .param("show-confirm", "false")
            .param("popup-save", "false")
            .add("grid", "cost-sheet-bill-of-material-grid")
            .add("form", "cost-sheet-bill-of-material-form")
            .context("_showRecord", String.valueOf(costSheet.getId()))
            .map());

    response.setReload(true);
  }

  public void updateProductCostPrice(ActionRequest request, ActionResponse response)
      throws AxelorException {

    BillOfMaterial billOfMaterial = request.getContext().asType(BillOfMaterial.class);

    Beans.get(BillOfMaterialService.class)
        .updateProductCostPrice(
            Beans.get(BillOfMaterialRepository.class).find(billOfMaterial.getId()));

    response.setReload(true);
  }

  public void checkOriginalBillOfMaterial(ActionRequest request, ActionResponse response) {

    BillOfMaterialRepository billOfMaterialRepository = Beans.get(BillOfMaterialRepository.class);
    BillOfMaterial billOfMaterial =
        billOfMaterialRepository.find(request.getContext().asType(BillOfMaterial.class).getId());

    List<BillOfMaterial> BillOfMaterialSet = Lists.newArrayList();
    BillOfMaterialSet =
        billOfMaterialRepository
            .all()
            .filter("self.originalBillOfMaterial = :origin")
            .bind("origin", billOfMaterial)
            .fetch();
    String message;

    if (!BillOfMaterialSet.isEmpty()) {

      String existingVersions = "";
      for (BillOfMaterial billOfMaterialVersion : BillOfMaterialSet) {
        existingVersions += "<li>" + billOfMaterialVersion.getFullName() + "</li>";
      }
      message =
          String.format(
              I18n.get(
                  "This bill of material already has the following versions : <br/><ul> %s </ul>And these versions may also have ones. Do you still wish to create a new one ?"),
              existingVersions);
    } else {
      message = I18n.get("Do you really wish to create a new version of this bill of material ?");
    }

    response.setAlert(message);
  }

  public void generateNewVersion(ActionRequest request, ActionResponse response) {

    BillOfMaterial billOfMaterial =
        Beans.get(BillOfMaterialRepository.class)
            .find(request.getContext().asType(BillOfMaterial.class).getId());

    BillOfMaterial copy = Beans.get(BillOfMaterialService.class).generateNewVersion(billOfMaterial);

    response.setView(
        ActionView.define("Bill of material")
            .model(BillOfMaterial.class.getName())
            .add("form", "bill-of-material-form")
            .add("grid", "bill-of-material-grid")
            .domain("self.defineSubBillOfMaterial = true AND self.personalized = false")
            .context("_showRecord", String.valueOf(copy.getId()))
            .map());
  }

  public void validateProdProcess(ActionRequest request, ActionResponse response) {
    BillOfMaterial billOfMaterial = request.getContext().asType(BillOfMaterial.class);
    if (billOfMaterial != null && billOfMaterial.getProdProcess() != null) {
      if (billOfMaterial.getProdProcess().getIsConsProOnOperation()) {
        try {
          Beans.get(ProdProcessService.class)
              .validateProdProcess(billOfMaterial.getProdProcess(), billOfMaterial);
        } catch (AxelorException e) {
          TraceBackService.trace(response, e, ResponseMessageType.ERROR);
        }
      }
    }
  }

  public void print(ActionRequest request, ActionResponse response) throws AxelorException {

    BillOfMaterial billOfMaterial = request.getContext().asType(BillOfMaterial.class);
    BillOfMaterialService billOfMaterialService = Beans.get(BillOfMaterialService.class);
    String language = ReportSettings.getPrintingLocale(null);

    String name = billOfMaterialService.getFileName(billOfMaterial);

    String fileLink =
        billOfMaterialService.getReportLink(
            billOfMaterial, name, language, ReportSettings.FORMAT_PDF);

    LOG.debug("Printing " + name);

    response.setView(ActionView.define(name).add("html", fileLink).map());
  }

  public void openBomTree(ActionRequest request, ActionResponse response) {

    BillOfMaterial billOfMaterial = request.getContext().asType(BillOfMaterial.class);
    billOfMaterial = Beans.get(BillOfMaterialRepository.class).find(billOfMaterial.getId());

    TempBomTree tempBomTree = Beans.get(BillOfMaterialService.class).generateTree(billOfMaterial);

    response.setView(
        ActionView.define(I18n.get("Bill of material"))
            .model(TempBomTree.class.getName())
            .add("tree", "bill-of-material-tree")
            .context("_tempBomTreeId", tempBomTree.getId())
            .map());
  }

  public void setBillOfMaterialAsDefault(ActionRequest request, ActionResponse response) {
    try {
      BillOfMaterial billOfMaterial = request.getContext().asType(BillOfMaterial.class);
      billOfMaterial = Beans.get(BillOfMaterialRepository.class).find(billOfMaterial.getId());

      Beans.get(BillOfMaterialService.class).setBillOfMaterialAsDefault(billOfMaterial);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void computeName(ActionRequest request, ActionResponse response) {
    BillOfMaterial billOfMaterial = request.getContext().asType(BillOfMaterial.class);

    if (billOfMaterial.getName() == null) {
      response.setValue("name", Beans.get(BillOfMaterialService.class).computeName(billOfMaterial));
    }
  }

  public void addRawMaterials(ActionRequest request, ActionResponse response) {
    try {
      BillOfMaterial billOfMaterial = request.getContext().asType(BillOfMaterial.class);
      @SuppressWarnings("unchecked")
      ArrayList<LinkedHashMap<String, Object>> rawMaterials =
          (ArrayList<LinkedHashMap<String, Object>>) request.getContext().get("rawMaterials");

      if (rawMaterials != null && !rawMaterials.isEmpty()) {
        Beans.get(BillOfMaterialService.class)
            .addRawMaterials(billOfMaterial.getId(), rawMaterials);

        response.setReload(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
