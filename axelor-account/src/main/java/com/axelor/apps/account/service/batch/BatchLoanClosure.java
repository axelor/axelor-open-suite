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

import com.axelor.apps.account.db.Loan;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.LoanRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.loan.LoanClosureService;
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

public class BatchLoanClosure extends BatchStrategy {

  protected LoanClosureService loanClosureService;
  protected LoanRepository loanRepository;
  protected AppBaseService appBaseService;

  @Inject
  public BatchLoanClosure(
      LoanClosureService loanClosureService,
      LoanRepository loanRepository,
      AppBaseService appBaseService) {
    this.loanClosureService = loanClosureService;
    this.loanRepository = loanRepository;
    this.appBaseService = appBaseService;
  }

  @Override
  protected void process() {
    LocalDate closingDate = batch.getAccountingBatch().getMoveDate();
    if (closingDate == null) {
      closingDate =
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
    queryParameters.put("validated", LoanRepository.STATUS_VALIDATED);
    queryParameters.put("ongoing", LoanRepository.STATUS_ONGOING);

    List<Loan> loanList =
        loanRepository
            .all()
            .filter(
                "self.company.id = :companyId" + " AND self.statusSelect IN (:validated, :ongoing)")
            .bind(queryParameters)
            .order("id")
            .fetch();

    closeLoanList(loanList, closingDate);
  }

  protected void closeLoanList(List<Loan> loanList, LocalDate closingDate) {
    for (Loan loan : loanList) {
      try {
        loan = loanRepository.find(loan.getId());
        Move move = loanClosureService.postClosure(loan, closingDate);
        if (move != null) {
          incrementDone();
        }
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
                "\t* %s " + I18n.get(AccountExceptionMessage.BATCH_LOAN_CLOSURE) + "\n",
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
