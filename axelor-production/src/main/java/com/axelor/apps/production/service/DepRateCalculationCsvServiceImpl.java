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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.UnitCostCalcLine;
import com.axelor.apps.production.db.UnitCostCalculation;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.data.csv.CSVImporter;
import com.axelor.dms.db.DMSFile;
import com.axelor.file.temp.TempFiles;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.helpers.file.CsvHelper;
import jakarta.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;

public class DepRateCalculationCsvServiceImpl implements DepRateCalculationCsvService {

  protected final MetaFiles metaFiles;

  @Inject
  public DepRateCalculationCsvServiceImpl(MetaFiles metaFiles) {
    this.metaFiles = metaFiles;
  }

  @Override
  public MetaFile exportDepRateCalc(UnitCostCalculation unitCostCalculation, String fileName)
      throws IOException {

    List<String[]> list = new ArrayList<>();
    List<UnitCostCalcLine> unitCostCalcLineList = unitCostCalculation.getUnitCostCalcLineList();

    unitCostCalcLineList.sort(
        Comparator.comparing(unitCostCalcLine -> unitCostCalcLine.getProduct().getCode()));

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

    File file = TempFiles.createTempFile(fileName, ".csv").toFile();

    String[] headers = {"Product_code", "Product_code", "Computed_cost", "Cost_to_apply"};

    CsvHelper.csvWriter(file.getParent(), file.getName(), ';', '"', headers, list);

    try (InputStream is = new FileInputStream(file)) {
      DMSFile dmsFile = metaFiles.attach(is, file.getName(), unitCostCalculation);
      return dmsFile.getMetaFile();
    }
  }

  @Override
  public void importDepRateCalc(MetaFile dataFile, UnitCostCalculation unitCostCalculation)
      throws IOException, AxelorException {

    File tempDir = TempFiles.createTempDir(null).toFile();
    File csvFile = new File(tempDir, "depratecalc.csv");
    Files.copy(MetaFiles.getPath(dataFile), csvFile.toPath());
    File configXmlFile = this.getDepRateCalcConfigXmlFile();

    CSVImporter csvImporter =
        new CSVImporter(configXmlFile.getAbsolutePath(), tempDir.getAbsolutePath());

    Map<String, Object> context = new HashMap<>();
    context.put("_unitCostCalculation", unitCostCalculation.getId());
    csvImporter.setContext(context);
    csvImporter.run();
  }

  protected File getDepRateCalcConfigXmlFile() throws AxelorException {
    File configFile = null;
    try {
      configFile = TempFiles.createTempFile("input-config", ".xml").toFile();
      copyBindFile(configFile);

    } catch (IOException e) {
      TraceBackService.trace(e);
    }

    return configFile;
  }

  protected void copyBindFile(File configFile) throws AxelorException {
    InputStream bindFileInputStream =
        this.getClass().getResourceAsStream("/import-configs/" + "dep-rate-calc-csv-config.xml");

    if (bindFileInputStream == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.UNIT_COST_CALCULATION_IMPORT_FAIL_ERROR));
    }

    try (FileOutputStream outputStream = new FileOutputStream(configFile)) {
      IOUtils.copy(bindFileInputStream, outputStream);
    } catch (IOException e) {
      TraceBackService.trace(e);
    }
  }
}
