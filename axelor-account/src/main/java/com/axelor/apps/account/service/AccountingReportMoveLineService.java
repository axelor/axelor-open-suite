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
import com.axelor.apps.account.db.AccountingReportMoveLine;
import com.axelor.apps.account.db.PaymentMoveLineDistribution;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public interface AccountingReportMoveLineService {

  public void createAccountingReportMoveLines(
      List<BigInteger> paymentMoveLineDistributionIds, AccountingReport accountingReport);

  public void createAccountingReportMoveLine(
      PaymentMoveLineDistribution paymentMoveLineDistribution, AccountingReport accountingReport);

  public void processExportMoveLine(
      AccountingReportMoveLine reportMoveLine, AccountingReport accountingExport);

  public List<Partner> getDasToDeclarePartnersFromAccountingExport(
      AccountingReport accountingExport);

  public MetaFile generateN4DSFile(AccountingReport accountingExport, String fileName)
      throws AxelorException, IOException;

  public List<AccountingReportMoveLine> getDasToDeclareLinesFromAccountingExport(
      AccountingReport accountingExport);

  public List<String> generateN4DSLines(AccountingReport accountingExport) throws AxelorException;

  public List<Object[]> getN4DSDeclaredPartnersData(AccountingReport accountingExport);

  public String computeNic(String registrationCode, String countryAlpha2Code);

  public String computeSiren(String registrationCode, String countryAlpha2Code);

  public String setN4DSLine(String heading, String value);

  public void updateN4DSExportStatus(AccountingReport accountingExport);
}
