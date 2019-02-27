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
package com.axelor.apps.production.service;

import com.axelor.app.AppSettings;
import com.axelor.apps.production.db.UnitCostCalcLine;
import com.axelor.apps.production.db.UnitCostCalculation;
import com.axelor.apps.production.db.repo.UnitCostCalculationRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.tool.file.CsvTool;
import com.axelor.data.csv.CSVImporter;
import com.axelor.dms.db.DMSFile;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.io.Files;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.ValidationException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitCostCalculationServiceImpl implements UnitCostCalculationService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public MetaFile exportUnitCostCalc(UnitCostCalculation unitCostCalculation, String fileName)
      throws IOException {
    List<String[]> list = new ArrayList<>();
    List<UnitCostCalcLine> unitCostCalcLineList = unitCostCalculation.getUnitCostCalcLineList();

    for (UnitCostCalcLine unitCostCalcLine : unitCostCalcLineList) {
      String[] item = new String[4];
      item[0] =
          unitCostCalcLine.getProduct() == null ? "" : unitCostCalcLine.getProduct().getCode();
      item[1] =
          unitCostCalcLine.getProduct() == null ? "" : unitCostCalcLine.getProduct().getName();
      item[2] = unitCostCalcLine.getComputedCost().toString();
      item[3] = unitCostCalcLine.getCostToApply().toString();

      list.add(item);
    }

    String filePath = AppSettings.get().get("file.upload.dir");
    Path path = Paths.get(filePath, fileName);
    File file = path.toFile();

    log.debug("File located at: {}", path);

    String[] headers = {
      I18n.get("Product_code"),
      I18n.get("Product_name"),
      I18n.get("Computed_cost"),
      I18n.get("Cost_to_apply")
    };

    CsvTool.csvWriter(filePath, fileName, ';', '"', headers, list);

    try (InputStream is = new FileInputStream(file)) {
      DMSFile dmsFile = Beans.get(MetaFiles.class).attach(is, fileName, unitCostCalculation);
      return dmsFile.getMetaFile();
    }
  }

  @Override
  public void importUnitCostCalc(MetaFile dataFile, UnitCostCalculation unitCostCalculation)
      throws IOException {
    File tempDir = Files.createTempDir();
    File csvFile = new File(tempDir, "unitcostcalc.csv");
    Files.copy(MetaFiles.getPath(dataFile).toFile(), csvFile);
    File configXmlFile = this.getConfigXmlFile();
    CSVImporter csvImporter =
        new CSVImporter(configXmlFile.getAbsolutePath(), tempDir.getAbsolutePath());
    Map<String, Object> context = new HashMap<>();
    context.put("_unitCostCalculation", unitCostCalculation.getId());
    csvImporter.setContext(context);
    csvImporter.run();
  }

  private File getConfigXmlFile() {
    File configFile = null;
    try {
      configFile = File.createTempFile("input-config", ".xml");
      InputStream bindFileInputStream =
          this.getClass().getResourceAsStream("/import-configs/" + "csv-config.xml");
      if (bindFileInputStream == null) {
        throw new ValidationException(IExceptionMessage.UNIT_COST_CALCULATION_IMPORT_FAIL_ERROR);
      }
      FileOutputStream outputStream = new FileOutputStream(configFile);
      IOUtils.copy(bindFileInputStream, outputStream);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return configFile;
  }

  @Override
  @Transactional
  public void runUnitCostCalc(UnitCostCalculation unitCostCalculation) {
    unitCostCalculation.setExecutionDate(Beans.get(AppProductionService.class).getTodayDate());
    Beans.get(UnitCostCalculationRepository.class).save(unitCostCalculation);
  }
}
