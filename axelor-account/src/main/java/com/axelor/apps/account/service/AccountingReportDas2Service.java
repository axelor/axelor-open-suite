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
import com.axelor.meta.db.MetaFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface AccountingReportDas2Service {

  String printPreparatoryProcessDeclaration(AccountingReport accountingReport)
      throws AxelorException;

  MetaFile exportN4DSFile(AccountingReport accountingReport) throws AxelorException, IOException;

  boolean isThereAlreadyDas2ExportInPeriod(AccountingReport accountingReport);

  List<Long> getAccountingReportDas2Pieces(AccountingReport accountingReport);

  AccountingReport getAssociatedDas2Export(AccountingReport accountingReport);

  BigDecimal getDebitBalance(String query);

  BigDecimal getCreditBalance(String query);
}
