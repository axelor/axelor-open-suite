package com.axelor.apps.budget.export;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.meta.db.MetaFile;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ExportGlobalBudgetLevelService {

  /**
   * Returned metafile is a excel file which has two sheets: Budgets, PurchaseOrderLines.
   *
   * @param budgetLevel, advancedExportBudget, advancedExportPOLine
   * @return MetaFile
   * @throws AxelorException
   * @throws IOException
   */
  public MetaFile export(
      BudgetLevel budgetLevel,
      AdvancedExport advancedExportBudget,
      AdvancedExport advancedExportPOLine)
      throws AxelorException, IOException;

  /**
   * Merge both files(Excel) into one.
   *
   * @param files
   * @throws IOException
   * @throws AxelorException
   */
  public void mergeFiles(List<File> files) throws IOException, AxelorException;

  /**
   * Generate an export file for each advanced export in the list
   *
   * @param advancedExportList, record
   * @return List File
   * @throws AxelorException
   */
  public List<File> createFiles(
      List<AdvancedExport> advancedExportList, Map<AdvancedExport, List<Long>> record)
      throws AxelorException;
}
