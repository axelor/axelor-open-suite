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
