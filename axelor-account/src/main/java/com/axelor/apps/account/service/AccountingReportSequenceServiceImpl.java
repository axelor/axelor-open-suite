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
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.util.AccountingReportUtilsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class AccountingReportSequenceServiceImpl implements AccountingReportSequenceService {

  protected SequenceService sequenceService;
  protected AccountingReportUtilsService accountingReportUtilsService;

  @Inject
  public AccountingReportSequenceServiceImpl(
      SequenceService sequenceService, AccountingReportUtilsService accountingReportUtilsService) {
    this.sequenceService = sequenceService;
    this.accountingReportUtilsService = accountingReportUtilsService;
  }

  @Override
  public String getSequence(AccountingReport accountingReport) throws AxelorException {
    accountingReportUtilsService.checkReportType(accountingReport);

    int accountingReportTypeSelect = accountingReport.getReportType().getTypeSelect();

    if (accountingReportTypeSelect >= 0 && accountingReportTypeSelect < 1000
        || accountingReportTypeSelect == 3000) {
      String seq =
          sequenceService.getSequenceNumber(
              SequenceRepository.ACCOUNTING_REPORT,
              accountingReport.getCompany(),
              AccountingReport.class,
              "ref",
              accountingReport);
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_1),
            I18n.get(BaseExceptionMessage.EXCEPTION),
            accountingReport.getCompany().getName());
      }
      return seq;
    } else if (accountingReportTypeSelect >= 1000 && accountingReportTypeSelect < 2000) {
      String seq =
          sequenceService.getSequenceNumber(
              SequenceRepository.MOVE_LINE_EXPORT,
              accountingReport.getCompany(),
              AccountingReport.class,
              "ref",
              accountingReport);
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_2),
            I18n.get(BaseExceptionMessage.EXCEPTION),
            accountingReport.getCompany().getName());
      }
      return seq;
    } else if (accountingReportTypeSelect >= 2000 && accountingReportTypeSelect < 3000) {
      String seq =
          sequenceService.getSequenceNumber(
              SequenceRepository.ANALYTIC_REPORT,
              accountingReport.getCompany(),
              AccountingReport.class,
              "ref",
              accountingReport);
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_ANALYTIC_REPORT),
            I18n.get(BaseExceptionMessage.EXCEPTION),
            accountingReport.getCompany().getName());
      }
      return seq;
    }
    throw new AxelorException(
        accountingReport,
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_UNKNOWN_ACCOUNTING_REPORT_TYPE),
        accountingReport.getReportType().getTypeSelect());
  }

  @Override
  public void setSequence(AccountingReport accountingReport, String sequence) {
    accountingReport.setRef(sequence);
  }
}
