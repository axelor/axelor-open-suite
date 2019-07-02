/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.production.db.UnitCostCalculation;
import com.axelor.apps.production.db.repo.UnitCostCalculationRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.costsheet.UnitCostCalculationService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;

@Singleton
public class UnitCostCalculationController {

  @Inject protected Provider<UnitCostCalculationRepository> unitCostCalculationRepoProvider;

  @Inject protected Provider<UnitCostCalculationService> unitCostCalculationServiceProvider;

  public void runUnitCostCalc(ActionRequest request, ActionResponse response)
      throws AxelorException {

    try {
      UnitCostCalculation unitCostCalculation =
          request.getContext().asType(UnitCostCalculation.class);
      unitCostCalculation = unitCostCalculationRepoProvider.get().find(unitCostCalculation.getId());
      unitCostCalculationServiceProvider.get().runUnitCostCalc(unitCostCalculation);

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
      unitCostCalculation = unitCostCalculationRepoProvider.get().find(unitCostCalculation.getId());
      unitCostCalculationServiceProvider.get().updateUnitCosts(unitCostCalculation);

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
      unitCostCalculation = unitCostCalculationRepoProvider.get().find(unitCostCalculation.getId());
      String fileName =
          unitCostCalculation.getUnitCostCalcSeq()
              + "-"
              + Beans.get(AppProductionService.class)
                  .getTodayDateTime()
                  .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
              + ".csv";
      MetaFile metaFile =
          unitCostCalculationServiceProvider
              .get()
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
          unitCostCalculationRepoProvider.get().find(unitCostCalculationId);

      if (Files.getFileExtension(csvFile.getName()).equals("csv")) {
        unitCostCalculationServiceProvider.get().importUnitCostCalc(dataFile, unitCostCalculation);
        response.setCanClose(true);
      } else {
        response.setError(IExceptionMessage.UNIT_COST_CALCULATION_IMPORT_CSV_ERROR);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
