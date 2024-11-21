/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.UnitCostCalculation;
import com.axelor.apps.production.db.repo.UnitCostCalculationRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.costsheet.UnitCostCalculationService;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.io.Files;
import com.google.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;

@Singleton
public class UnitCostCalculationController {

  public void runUnitCostCalc(ActionRequest request, ActionResponse response)
      throws AxelorException {

    try {
      UnitCostCalculation unitCostCalculation =
          request.getContext().asType(UnitCostCalculation.class);
      unitCostCalculation =
          Beans.get(UnitCostCalculationRepository.class).find(unitCostCalculation.getId());
      Beans.get(UnitCostCalculationService.class).runUnitCostCalc(unitCostCalculation);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateUnitCosts(ActionRequest request, ActionResponse response)
      throws AxelorException {

    try {
      UnitCostCalculation unitCostCalculation =
          request.getContext().asType(UnitCostCalculation.class);
      unitCostCalculation =
          Beans.get(UnitCostCalculationRepository.class).find(unitCostCalculation.getId());
      Beans.get(UnitCostCalculationService.class).updateUnitCosts(unitCostCalculation);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void exportUnitCostCalc(ActionRequest request, ActionResponse response)
      throws IOException {

    try {
      UnitCostCalculation unitCostCalculation =
          request.getContext().asType(UnitCostCalculation.class);
      unitCostCalculation =
          Beans.get(UnitCostCalculationRepository.class).find(unitCostCalculation.getId());
      String fileName =
          unitCostCalculation.getUnitCostCalcSeq()
              + "-"
              + Beans.get(AppProductionService.class)
                  .getTodayDateTime()
                  .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
      MetaFile metaFile =
          Beans.get(UnitCostCalculationService.class)
              .exportUnitCostCalc(unitCostCalculation, fileName);

      response.setView(
          ActionView.define(fileName)
              .add(
                  "html",
                  "ws/rest/com.axelor.meta.db.MetaFile/"
                      + metaFile.getId()
                      + "/content/download?v="
                      + metaFile.getVersion())
              .map());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void importUnitCostCalc(ActionRequest request, ActionResponse response)
      throws IOException {

    try {
      LinkedHashMap<String, Object> map =
          (LinkedHashMap<String, Object>) request.getContext().get("metaFile");
      MetaFile dataFile =
          Beans.get(MetaFileRepository.class).find(((Integer) map.get("id")).longValue());
      File csvFile = MetaFiles.getPath(dataFile).toFile();
      Long unitCostCalculationId = Long.valueOf(request.getContext().get("_id").toString());
      UnitCostCalculation unitCostCalculation =
          Beans.get(UnitCostCalculationRepository.class).find(unitCostCalculationId);

      if (Files.getFileExtension(csvFile.getName()).equals("csv")) {
        Beans.get(UnitCostCalculationService.class)
            .importUnitCostCalc(dataFile, unitCostCalculation);
        response.setCanClose(true);
      } else {
        response.setError(ProductionExceptionMessage.UNIT_COST_CALCULATION_IMPORT_CSV_ERROR);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void fillProductSetDomain(ActionRequest request, ActionResponse response) {
    try {
      UnitCostCalculation unitCostCalculation =
          request.getContext().asType(UnitCostCalculation.class);

      UnitCostCalculationService unitCostCalculationService =
          Beans.get(UnitCostCalculationService.class);

      Company company = null;

      if (unitCostCalculationService.hasDefaultBOMSelected()) {
        LinkedHashMap<String, Object> companyMap =
            (LinkedHashMap<String, Object>) request.getContext().get("company");
        if (companyMap != null) {
          company =
              Beans.get(CompanyRepository.class).find(((Integer) companyMap.get("id")).longValue());
        }
      }

      String domain =
          unitCostCalculationService.createProductSetDomain(unitCostCalculation, company);
      response.setAttr("productSet", "domain", domain);

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  @SuppressWarnings("unchecked")
  public void fillCompanySet(ActionRequest request, ActionResponse response) {
    try {
      UnitCostCalculationService unitCostCalculationService =
          Beans.get(UnitCostCalculationService.class);

      if (unitCostCalculationService.hasDefaultBOMSelected()) {
        UnitCostCalculation unitCostCalculation =
            request.getContext().asType(UnitCostCalculation.class);

        unitCostCalculation =
            Beans.get(UnitCostCalculationRepository.class).find(unitCostCalculation.getId());

        LinkedHashMap<String, Object> companyMap =
            (LinkedHashMap<String, Object>) request.getContext().get("company");

        if (companyMap == null) {
          return;
        }

        Company company =
            Beans.get(CompanyRepository.class).find(((Integer) companyMap.get("id")).longValue());

        unitCostCalculationService.fillCompanySet(unitCostCalculation, company);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillCompany(ActionRequest request, ActionResponse response) {
    try {
      UnitCostCalculationService unitCostCalculationService =
          Beans.get(UnitCostCalculationService.class);

      if (unitCostCalculationService.hasDefaultBOMSelected()) {
        UnitCostCalculation unitCostCalculation =
            request.getContext().asType(UnitCostCalculation.class);

        unitCostCalculation =
            Beans.get(UnitCostCalculationRepository.class).find(unitCostCalculation.getId());

        Company company = unitCostCalculationService.getSingleCompany(unitCostCalculation);

        response.setValue("$company", company);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
