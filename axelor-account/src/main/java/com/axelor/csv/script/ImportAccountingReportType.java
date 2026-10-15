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
package com.axelor.csv.script;

import com.axelor.apps.account.db.repo.AccountingReportTypeRepository;
import com.axelor.meta.MetaStore;
import com.axelor.meta.schema.views.Selection;
import com.axelor.utils.helpers.ImportHelper;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;

public class ImportAccountingReportType {

  public static final String REPORT_TYPE_SELECTION = "accounting.report.type.select";
  public static final String EXPORT_TYPE_SELECTION = "accounting.report.type.select.export";

  /**
   * Checks the imported typeSelect against the selection matching the report/export type, as report
   * and export types are stored in the same field but defined in two selections. When the CSV does
   * not provide reportExportTypeSelect, the value is accepted from either selection.
   */
  public static boolean checkTypeSelect(Object reportExportTypeSelect, Object typeSelect) {
    if (!ImportHelper.isValuePresent(typeSelect)) {
      return false;
    }
    if (!ImportHelper.isValuePresent(reportExportTypeSelect)) {
      return isInSelection(REPORT_TYPE_SELECTION, typeSelect)
          || isInSelection(EXPORT_TYPE_SELECTION, typeSelect);
    }
    String selectionName =
        String.valueOf(AccountingReportTypeRepository.EXPORT)
                .equals(String.valueOf(reportExportTypeSelect).trim())
            ? EXPORT_TYPE_SELECTION
            : REPORT_TYPE_SELECTION;
    return isInSelection(selectionName, typeSelect);
  }

  protected static boolean isInSelection(String selectionName, Object typeSelect) {
    List<Selection.Option> options = MetaStore.getSelectionList(selectionName);
    if (CollectionUtils.isEmpty(options)) {
      return false;
    }
    String value = String.valueOf(typeSelect).trim();
    return options.stream().map(Selection.Option::getValue).anyMatch(v -> Objects.equals(v, value));
  }
}
