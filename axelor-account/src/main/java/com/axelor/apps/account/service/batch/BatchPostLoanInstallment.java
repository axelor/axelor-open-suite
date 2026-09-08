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
package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.LoanLine;
import com.axelor.apps.account.db.repo.LoanLineRepository;
import com.axelor.apps.account.db.repo.LoanRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.loan.LoanLineMoveService;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class BatchPostLoanInstallment extends BatchStrategy {

  protected LoanLineMoveService loanLineMoveService;
  protected LoanLineRepository loanLineRepository;
  protected AppBaseService appBaseService;

  @Inject
  public BatchPostLoanInstallment(
      LoanLineMoveService loanLineMoveService,
      LoanLineRepository loanLineRepository,
      AppBaseService appBaseService) {
    this.loanLineMoveService = loanLineMoveService;
    this.loanLineRepository = loanLineRepository;
    this.appBaseService = appBaseService;
  }

  @Override
  protected void process() {
    LocalDate endDate = batch.getAccountingBatch().getEndDate();
    if (endDate == null) {
      endDate =
          appBaseService.getTodayDate(
              batch.getAccountingBatch().getCompany() != null
                  ? batch.getAccountingBatch().getCompany()
                  : Optional.ofNullable(AuthUtils.getUser())
                      .map(User::getActiveCompany)
                      .orElse(null));
    }

    HashMap<String, Object> queryParameters = new HashMap<>();
    queryParameters.put(
        "companyId",
        batch.getAccountingBatch().getCompany() != null
            ? batch.getAccountingBatch().getCompany().getId()
            : Long.valueOf(0));
    queryParameters.put("endDate", endDate);
    queryParameters.put("statusSelect", LoanRepository.STATUS_VALIDATED);

    List<LoanLine> loanLineList =
        loanLineRepository
            .all()
            .filter(
                "self.accountMove IS NULL AND self.loan.company.id = :companyId"
                    + " AND self.installmentDate <= :endDate"
                    + " AND self.loan.statusSelect >= :statusSelect")
            .bind(queryParameters)
            .order("installmentDate")
            .fetch();

    postLoanLineList(loanLineList);
  }

  protected void postLoanLineList(List<LoanLine> loanLineList) {
    for (LoanLine loanLine : loanLineList) {
      try {
        loanLine = loanLineRepository.find(loanLine.getId());
        loanLineMoveService.postInstallment(loanLine, true);
        incrementDone();
      } catch (Exception e) {
        incrementAnomaly();
        TraceBackService.trace(e, null, this.batch.getId());
      }
      JPA.clear();
    }
  }

  @Override
  protected void stop() {
    StringBuilder sbComment =
        new StringBuilder(
            String.format(
                "\t* %s " + I18n.get(AccountExceptionMessage.BATCH_POSTED_LOAN_INSTALLMENT) + "\n",
                batch.getDone()));
    sbComment.append(
        String.format("\t" + I18n.get(BaseExceptionMessage.BASE_BATCH_3), batch.getAnomaly()));
    addComment(sbComment.toString());
    super.stop();
  }

  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_ACCOUNTING_BATCH);
  }
}
