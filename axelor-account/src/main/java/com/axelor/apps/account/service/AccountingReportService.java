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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.base.AxelorException;
import com.axelor.meta.db.MetaFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface AccountingReportService {

  String print(AccountingReport accountingReport) throws AxelorException, IOException;

  MetaFile export(AccountingReport accountingReport) throws AxelorException, IOException;

  public String getMoveLineList(AccountingReport accountingReport) throws AxelorException;

  public String buildQuery(AccountingReport accountingReport) throws AxelorException;

  public String addParams(String paramQuery, Object param);

  public String addParams(String paramQuery);

  public void setSequence(AccountingReport accountingReport, String sequence);

  public String getSequence(AccountingReport accountingReport) throws AxelorException;

  public Account getAccount(AccountingReport accountingReport);

  public void setStatus(AccountingReport accountingReport);

  /**
   * @param queryFilter
   * @return
   */
  public BigDecimal getDebitBalance();

  /**
   * @param queryFilter
   * @return
   */
  public BigDecimal getCreditBalance();

  public BigDecimal getDebitBalanceType4();

  public BigDecimal getCreditBalance(AccountingReport accountingReport, String queryFilter);

  public BigDecimal getCreditBalanceType4();

  public boolean isThereTooManyLines(AccountingReport accountingReport) throws AxelorException;

  public void testReportedDateField(LocalDate reportedDate) throws AxelorException;

  public AccountingReport createAccountingExportFromReport(
      AccountingReport accountingReport, int exportTypeSelect, boolean isComplementary)
      throws AxelorException;

  /**
   * Method to get fields from ReportTypeModelAccountingReport
   *
   * @param accountingReport the accouting report linked to the ReportTypeModelAccountingReport
   * @return map if ReportTypeModelAccountingReport is found else null
   * @throws AxelorException
   */
  public Map<String, Object> getFieldsFromReportTypeModelAccountingReport(
      AccountingReport accountingReport) throws AxelorException;

  void checkReportType(AccountingReport accountingReport) throws AxelorException;
}
