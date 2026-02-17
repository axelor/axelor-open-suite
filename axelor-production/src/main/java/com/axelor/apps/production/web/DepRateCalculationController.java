/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.production.db.UnitCostCalculation;
import com.axelor.apps.production.db.repo.UnitCostCalculationRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.DepRateCalculationCsvService;
import com.axelor.apps.production.service.DepRateCalculationService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;

public class DepRateCalculationController {

  public void runDepRateCalc(ActionRequest request, ActionResponse response)
      throws AxelorException {

    UnitCostCalculation unitCostCalculation =
        request.getContext().asType(UnitCostCalculation.class);
    unitCostCalculation =
        Beans.get(UnitCostCalculationRepository.class).find(unitCostCalculation.getId());
    Beans.get(DepRateCalculationService.class).runDepRateCalc(unitCostCalculation);

    response.setReload(true);
  }

  public void updateDepRates(ActionRequest request, ActionResponse response)
      throws AxelorException {

    UnitCostCalculation unitCostCalculation =
        request.getContext().asType(UnitCostCalculation.class);
    unitCostCalculation =
        Beans.get(UnitCostCalculationRepository.class).find(unitCostCalculation.getId());

    Beans.get(DepRateCalculationService.class).updateDepRates(unitCostCalculation);

    response.setReload(true);
  }

  public void exportDepRateCalc(ActionRequest request, ActionResponse response) throws IOException {

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
        Beans.get(DepRateCalculationCsvService.class)
            .exportDepRateCalc(unitCostCalculation, fileName);

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
  }

  public void importDepRateCalc(ActionRequest request, ActionResponse response)
      throws IOException, AxelorException {

    LinkedHashMap<String, Object> map =
        (LinkedHashMap<String, Object>) request.getContext().get("metaFile");
    MetaFile dataFile =
        Beans.get(MetaFileRepository.class).find(((Integer) map.get("id")).longValue());
    File csvFile = MetaFiles.getPath(dataFile).toFile();
    Long unitCostCalculationId = Long.valueOf(request.getContext().get("_id").toString());
    UnitCostCalculation unitCostCalculation =
        Beans.get(UnitCostCalculationRepository.class).find(unitCostCalculationId);

    if (Files.getFileExtension(csvFile.getName()).equals("csv")) {
      Beans.get(DepRateCalculationCsvService.class)
          .importDepRateCalc(dataFile, unitCostCalculation);
      response.setCanClose(true);
    } else {
      response.setError(
          I18n.get(ProductionExceptionMessage.UNIT_COST_CALCULATION_IMPORT_CSV_ERROR));
    }
  }
}
