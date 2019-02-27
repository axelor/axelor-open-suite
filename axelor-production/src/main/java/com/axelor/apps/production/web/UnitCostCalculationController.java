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

import com.axelor.apps.production.db.UnitCostCalcLine;
import com.axelor.apps.production.db.UnitCostCalculation;
import com.axelor.apps.production.db.repo.UnitCostCalculationRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.UnitCostCalculationService;
import com.axelor.apps.production.service.app.AppProductionService;
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
import java.util.Map;
import javax.validation.ValidationException;

@Singleton
public class UnitCostCalculationController {

  public void runUnitCostCalc(ActionRequest request, ActionResponse response) {
    UnitCostCalculation unitCostCalculation =
        request.getContext().asType(UnitCostCalculation.class);
    unitCostCalculation =
        Beans.get(UnitCostCalculationRepository.class).find(unitCostCalculation.getId());
    Beans.get(UnitCostCalculationService.class).runUnitCostCalc(unitCostCalculation);

    response.setReload(true);
  }

  public void exportUnitCostCalc(ActionRequest request, ActionResponse response)
      throws IOException {
    UnitCostCalculation unitCostCalculation =
        request.getContext().asType(UnitCostCalculation.class);
    unitCostCalculation =
        Beans.get(UnitCostCalculationRepository.class).find(unitCostCalculation.getId());
    if (unitCostCalculation.getUnitCostCalcLineList() != null
        && !unitCostCalculation.getUnitCostCalcLineList().isEmpty()) {
      String fileName =
          unitCostCalculation.getUnitCostCalcSeq()
              + "-"
              + Beans.get(AppProductionService.class)
                  .getTodayDate()
                  .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
              + ".csv";
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
    }
  }

  @SuppressWarnings("unchecked")
  public void importUnitCostCalc(ActionRequest request, ActionResponse response)
      throws IOException {
    LinkedHashMap<String, Object> map =
        (LinkedHashMap<String, Object>) request.getContext().get("metaFile");
    MetaFile dataFile =
        Beans.get(MetaFileRepository.class).find(((Integer) map.get("id")).longValue());
    File csvFile = MetaFiles.getPath(dataFile).toFile();
    Long unitCostCalculationId = Long.valueOf(request.getContext().get("_id").toString());
    UnitCostCalculation unitCostCalculation =
        Beans.get(UnitCostCalculationRepository.class).find(unitCostCalculationId);

    if (Files.getFileExtension(csvFile.getName()).equals("csv")) {
      Beans.get(UnitCostCalculationService.class).importUnitCostCalc(dataFile, unitCostCalculation);
      response.setCanClose(true);
    } else {
      response.setError(IExceptionMessage.UNIT_COST_CALCULATION_IMPORT_CSV_ERROR);
    }
  }

  public Object importUnitCostCalc(Object bean, Map<String, Object> context)
      throws ValidationException {
    UnitCostCalculation unitCostCalculation =
        (UnitCostCalculation) context.get("_unitCostCalculation");
    assert bean instanceof UnitCostCalcLine;
    UnitCostCalcLine unitCostCalcLine = (UnitCostCalcLine) bean;
    unitCostCalcLine.setUnitCostCalculation(unitCostCalculation);

    return bean;
  }
}
