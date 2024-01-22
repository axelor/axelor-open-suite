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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.meta.db.MetaFile;
import java.io.IOException;
import java.time.LocalDate;

public interface MoveLineExportService {

  public MetaFile exportMoveLine(AccountingReport accountingReport)
      throws AxelorException, IOException;

  public void replayExportMoveLine(AccountingReport accountingReport)
      throws AxelorException, IOException;

  public AccountingReport createAccountingReport(
      Company company, int exportTypeSelect, LocalDate startDate, LocalDate endDate)
      throws AxelorException;

  /**
   * Export general balance to CSV file.
   *
   * @param accountingReport
   * @return
   * @throws AxelorException
   * @throws IOException
   */
  void exportMoveLineTypeSelect1010(AccountingReport accountingReport)
      throws AxelorException, IOException;
}
