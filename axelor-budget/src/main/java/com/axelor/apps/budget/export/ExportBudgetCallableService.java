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
package com.axelor.apps.budget.export;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.base.db.repo.AdvancedExportRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.advancedExport.AdvancedExportService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetToolsService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletScopes;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExportBudgetCallableService implements Callable<MetaFile> {

  private GlobalBudget globalBudget;
  private AdvancedExport advancedExportGlobalBudget;
  private AdvancedExport advancedExportBudgetLevel;
  private AdvancedExport advancedExportBudget;
  private AdvancedExport advancedExportBudgetLine;
  protected File exportFile;
  protected AdvancedExportRepository advancedExportRepository;
  protected GlobalBudgetRepository globalBudgetRepository;
  protected GlobalBudgetToolsService globalBudgetToolsService;
  protected MetaFiles metaFiles;
  protected AdvancedExportService advancedExportService;

  @Inject
  public ExportBudgetCallableService(
      AdvancedExportRepository advancedExportRepository,
      GlobalBudgetRepository globalBudgetRepository,
      GlobalBudgetToolsService globalBudgetToolsService,
      MetaFiles metaFiles,
      AdvancedExportService advancedExportService) {
    this.advancedExportRepository = advancedExportRepository;
    this.globalBudgetRepository = globalBudgetRepository;
    this.globalBudgetToolsService = globalBudgetToolsService;
    this.metaFiles = metaFiles;
    this.advancedExportService = advancedExportService;
  }

  public void initialize(
      GlobalBudget globalBudget,
      AdvancedExport advancedExportGlobalBudget,
      AdvancedExport advancedExportBudgetLevel,
      AdvancedExport advancedExportBudget,
      AdvancedExport advancedExportBudgetLine) {
    this.globalBudget = globalBudget;
    this.advancedExportGlobalBudget = advancedExportGlobalBudget;
    this.advancedExportBudgetLevel = advancedExportBudgetLevel;
    this.advancedExportBudget = advancedExportBudget;
    this.advancedExportBudgetLine = advancedExportBudgetLine;
  }

  @Override
  public MetaFile call() throws Exception {
    final RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());
    try (RequestScoper.CloseableScope ignored = scope.open()) {

      globalBudget = globalBudgetRepository.find(globalBudget.getId());
      advancedExportGlobalBudget =
          advancedExportRepository.find(advancedExportGlobalBudget.getId());
      advancedExportBudgetLevel = advancedExportRepository.find(advancedExportBudgetLevel.getId());
      advancedExportBudget = advancedExportRepository.find(advancedExportBudget.getId());
      advancedExportBudgetLine = advancedExportRepository.find(advancedExportBudgetLine.getId());
      return export();
    } catch (Exception e) {
      TraceBackService.trace(e);
      throw e;
    }
  }

  /**
   * Returned metafile is a excel file which has two sheets: GlobalBudget, BudgetLevel, Budget,
   * BudgetLine.
   *
   * @return MetaFile
   * @throws AxelorException
   * @throws IOException
   */
  public MetaFile export() throws AxelorException, IOException {

    List<Long> budgetLevelRecordIds = globalBudgetToolsService.getAllBudgetLevelIds(globalBudget);

    List<Long> budgetRecordIds = globalBudgetToolsService.getAllBudgetIds(globalBudget);

    List<Long> budgetLineRecordIds = globalBudgetToolsService.getAllBudgetLineIds(globalBudget);

    if (ObjectUtils.isEmpty(advancedExportGlobalBudget)
        || ObjectUtils.isEmpty(advancedExportBudgetLevel)
        || ObjectUtils.isEmpty(advancedExportBudget)
        || ObjectUtils.isEmpty(advancedExportBudgetLine)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          BudgetExceptionMessage.MISSING_ADVANCED_EXPORT);
    }

    List<AdvancedExport> advancedExportList =
        new ArrayList<>(
            Arrays.asList(
                advancedExportGlobalBudget,
                advancedExportBudgetLevel,
                advancedExportBudget,
                advancedExportBudgetLine));

    // Map to use correct recordIds each advanceExport
    Map<AdvancedExport, List<Long>> record = new HashMap<>();
    record.put(advancedExportGlobalBudget, Collections.singletonList(globalBudget.getId()));
    record.put(advancedExportBudgetLevel, budgetLevelRecordIds);
    record.put(advancedExportBudget, budgetRecordIds);
    record.put(advancedExportBudgetLine, budgetLineRecordIds);

    // Getting excels for each advanceExport
    List<File> files = createFiles(advancedExportList, record);

    // Merging all the excels in a single excel
    this.mergeFiles(files);

    // Creating metaFile
    if (exportFile != null) {
      FileInputStream inStream = new FileInputStream(exportFile);
      MetaFile metaExportFile =
          metaFiles.upload(inStream, String.format("%s.xlsx", globalBudget.getName()));
      inStream.close();
      exportFile.delete();
      return metaExportFile;
    }
    return null;
  }

  /**
   * Merge both files(Excel) into one.
   *
   * @param files
   * @throws IOException
   * @throws AxelorException
   */
  public void mergeFiles(List<File> files) throws IOException, AxelorException {
    if (files == null || files.isEmpty())
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get("Files are not created"));

    Iterator<File> iterator = files.iterator();

    Workbook globalBudget = new XSSFWorkbook(new FileInputStream(iterator.next()));
    Workbook budgetLevel = new XSSFWorkbook(new FileInputStream(iterator.next()));
    Workbook budget = new XSSFWorkbook(new FileInputStream(iterator.next()));
    Workbook budgetLine = new XSSFWorkbook(new FileInputStream(iterator.next()));

    // Merge workbooks into one
    new MergeExcel(budgetLevel, globalBudget).merge();
    new MergeExcel(budget, globalBudget).merge();
    new MergeExcel(budgetLine, globalBudget).merge();

    // Creating file for the new merged excel
    File file = File.createTempFile(GlobalBudget.class.getSimpleName(), ".xlsx");
    FileOutputStream out = new FileOutputStream(file);
    globalBudget.write(out);
    out.close();

    exportFile = file;
  }

  /**
   * Generate an export file for each advanced export in the list
   *
   * @param advancedExportList, record
   * @return List File
   * @throws AxelorException
   */
  public List<File> createFiles(
      List<AdvancedExport> advancedExportList, Map<AdvancedExport, List<Long>> record)
      throws AxelorException {

    List<File> files = new ArrayList<>();
    for (AdvancedExport advancedExport : advancedExportList) {

      if (advancedExport != null
          && !ObjectUtils.isEmpty(advancedExport.getAdvancedExportLineList())) {

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
