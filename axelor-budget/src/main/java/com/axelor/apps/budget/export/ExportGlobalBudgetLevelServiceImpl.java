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
package com.axelor.apps.budget.export;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.base.db.repo.AdvancedExportRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.advancedExport.AdvancedExportService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.BudgetManagementRepository;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExportGlobalBudgetLevelServiceImpl implements ExportGlobalBudgetLevelService {

  protected AdvancedExportRepository advancedExportRepo;
  protected AdvancedExportService advancedExportService;
  protected File exportFile;

  @Inject
  public ExportGlobalBudgetLevelServiceImpl(
      AdvancedExportRepository advancedExportRepo, AdvancedExportService advancedExportService) {
    this.advancedExportRepo = advancedExportRepo;
    this.advancedExportService = advancedExportService;
  }

  @Override
  public MetaFile export(
      BudgetLevel budgetLevel,
      AdvancedExport budgetAdvanceExport,
      AdvancedExport purchaseOrderLineAdvanceExport)
      throws AxelorException, IOException {

    if (!budgetLevel
        .getLevelTypeSelect()
        .equals(BudgetLevelRepository.BUDGET_LEVEL_LEVEL_TYPE_SELECT_GLOBAL)) {
      budgetLevel = budgetLevel.getParentBudgetLevel();
      budgetLevel =
          budgetLevel.getParentBudgetLevel() != null
              ? budgetLevel.getParentBudgetLevel()
              : budgetLevel;
    }

    List<Long> budgetRecordIds =
        Beans.get(BudgetManagementRepository.class).all()
            .filter(
                "self.budgetLevel.parentBudgetLevel.parentBudgetLevel.id = ?1", budgetLevel.getId())
            .fetch().stream()
            .map(Budget::getId)
            .collect(Collectors.toList());

    List<Long> purchaseOrderLinesRecordIds =
        Beans.get(PurchaseOrderLineRepository.class).all()
            .filter("self.purchaseOrder.budgetLevel.id = ?1", budgetLevel.getId()).fetch().stream()
            .map(PurchaseOrderLine::getId)
            .collect(Collectors.toList());

    if (budgetRecordIds.isEmpty()) {
      budgetRecordIds = null;
    }

    if (purchaseOrderLinesRecordIds.isEmpty()) {
      purchaseOrderLinesRecordIds = null;
    }

    if (purchaseOrderLineAdvanceExport == null || budgetAdvanceExport == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          BudgetExceptionMessage.MISSING_ADVANCED_EXPORT);
    }

    List<AdvancedExport> advancedExportList =
        new ArrayList<>(Arrays.asList(budgetAdvanceExport, purchaseOrderLineAdvanceExport));

    // Map to use correct recordIds each advanceExport
    Map<AdvancedExport, List<Long>> record = new HashMap<>();
    record.put(budgetAdvanceExport, budgetRecordIds);
    record.put(purchaseOrderLineAdvanceExport, purchaseOrderLinesRecordIds);

    // Getting excels for each advanceExport
    List<File> files = createFiles(advancedExportList, record);

    // Merging all the excels in a single excel
    this.mergeFiles(files);

    // Creating metaFile
    if (exportFile != null) {
      FileInputStream inStream = new FileInputStream(exportFile);
      MetaFile metaExportFile =
          Beans.get(MetaFiles.class)
              .upload(
                  inStream,
                  budgetLevel.getName()
                      + "-"
                      + LocalTime.now().format(DateTimeFormatter.ofPattern("HH-mm-ss"))
                      + ".xlsx");
      inStream.close();
      exportFile.delete();
      return metaExportFile;
    }
    return null;
  }

  @Override
  public void mergeFiles(List<File> files) throws IOException, AxelorException {
    if (files == null || files.isEmpty())
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get("Files are not created"));

    Iterator<File> iterator = files.iterator();

    Workbook budget = new XSSFWorkbook(new FileInputStream(iterator.next()));
    Workbook poLines = new XSSFWorkbook(new FileInputStream(iterator.next()));

    // Merge two workbook into one
    Beans.get(MergeExcel.class).mergeExcels(poLines, budget);

    // Creating file for the new merged excel
    File file = File.createTempFile(BudgetLevel.class.getSimpleName(), ".xlsx");
    FileOutputStream out = new FileOutputStream(file);
    budget.write(out);
    out.close();

    exportFile = file;
  }

  @Override
  public List<File> createFiles(
      List<AdvancedExport> advancedExportList, Map<AdvancedExport, List<Long>> record)
      throws AxelorException {

    List<File> files = new ArrayList<>();
    for (AdvancedExport advancedExport : advancedExportList) {

      if (!advancedExport.getAdvancedExportLineList().isEmpty()) {

        File file =
            advancedExportService.export(
                advancedExport, record.get(advancedExport), AdvancedExportService.EXCEL);

        if (advancedExportService.getIsReachMaxExportLimit()) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(BaseExceptionMessage.ADVANCED_EXPORT_3));
        }

        files.add(file);
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(BaseExceptionMessage.ADVANCED_EXPORT_1));
      }
    }
    return files;
  }
}
