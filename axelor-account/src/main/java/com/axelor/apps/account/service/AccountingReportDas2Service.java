/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public interface AccountingReportDas2Service {

  String printPreparatoryProcessDeclaration(AccountingReport accountingReport)
      throws AxelorException;

  MetaFile exportN4DSFile(AccountingReport accountingReport) throws AxelorException, IOException;

  boolean isThereAlreadyDas2ExportInPeriod(AccountingReport accountingReport);

  List<BigInteger> getAccountingReportDas2Pieces(AccountingReport accountingReport);

  AccountingReport getAssociatedDas2Export(AccountingReport accountingReport);
}
