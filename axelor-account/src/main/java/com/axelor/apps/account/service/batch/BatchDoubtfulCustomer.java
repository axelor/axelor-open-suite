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
package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountingService;
import com.axelor.apps.account.service.debtrecovery.DoubtfulCustomerService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchDoubtfulCustomer extends PreviewBatch {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected boolean end = false;

  protected String updateCustomerAccountLog = "";

  protected AccountRepository accountRepo;

  @Inject
  public BatchDoubtfulCustomer(
      DoubtfulCustomerService doubtfulCustomerService,
      BatchAccountCustomer batchAccountCustomer,
      MoveLineRepository moveLineRepo,
      AccountRepository accountRepo) {

    super(doubtfulCustomerService, batchAccountCustomer, moveLineRepo);

    this.accountRepo = accountRepo;

    AccountingService.setUpdateCustomerAccount(false);
  }

  @Override
  protected void start() throws IllegalAccessException {

    super.start();

    Company company = batch.getAccountingBatch().getCompany();

    try {

      doubtfulCustomerService.testCompanyField(company);

    } catch (AxelorException e) {

      TraceBackService.trace(
          new AxelorException(e, e.getCategory(), ""),
          ExceptionOriginRepository.DOUBTFUL_CUSTOMER,
          batch.getId());
      incrementAnomaly();
      end = true;
    }

    checkPoint();
  }

  @Override
  protected void process() {
    if (!end) {
      super.process();
    }
  }

  @Override
  protected void _processByQuery(AccountingBatch accountingBatch) {
    Company company = batch.getAccountingBatch().getCompany();

    AccountConfig accountConfig = company.getAccountConfig();

    Account doubtfulCustomerAccount = accountConfig.getDoubtfulCustomerAccount();
    int sixMonthDebtMonthNumber = accountConfig.getSixMonthDebtMonthNumber();
    int threeMonthDebtMonthNumber = accountConfig.getThreeMonthDebtMontsNumber();

    List<Pair<Integer, Boolean>> pairList =
        Arrays.asList(
            Pair.of(sixMonthDebtMonthNumber, false),
            Pair.of(threeMonthDebtMonthNumber, true),
            Pair.of(sixMonthDebtMonthNumber, true),
            Pair.of(threeMonthDebtMonthNumber, false));

    for (Pair<Integer, Boolean> pair : pairList) {
      List<Long> moveLineIds =
          doubtfulCustomerService.getMoveLineIds(
              company, doubtfulCustomerAccount, pair.getLeft(), pair.getRight());

      String debtPassReason =
          pair.getLeft().equals(sixMonthDebtMonthNumber)
              ? accountConfig.getSixMonthDebtPassReason()
              : accountConfig.getThreeMonthDebtPassReason();

      this._processMoveLines(moveLineIds, doubtfulCustomerAccount, debtPassReason, pair.getRight());
    }

    updateCustomerAccountLog +=
        batchAccountCustomer.updateAccountingSituationMarked(companyRepo.find(company.getId()));
  }

  @Override
  protected void _processByIds(AccountingBatch accountingBatch) {}

  protected void _processMoveLines(
      List<Long> moveLineIdList,
      Account doubtfulCustomerAccount,
      String debtPassReason,
      boolean isReject) {
    int i = 0;

    for (Long id : moveLineIdList) {
      MoveLine moveLine = moveLineRepo.find(id);
      Move move = moveLine.getMove();
      Invoice invoice = isReject ? moveLine.getInvoiceReject() : move.getInvoice();
      String invoiceId =
          Optional.ofNullable(invoice).map(Invoice::getInvoiceId).orElse(move.getOrigin());

      try {

        if (isReject) {
          doubtfulCustomerService.createDoubtFulCustomerRejectMove(
              moveLine, doubtfulCustomerAccount, debtPassReason);

          this.updateInvoice(invoice);
        } else {
          doubtfulCustomerService.createDoubtFulCustomerMove(
              move, doubtfulCustomerAccount, debtPassReason);

          if (invoice != null) {
            this.updateInvoice(invoice);
          } else {
            this.updateAccountMove(move, true);
          }
        }

        i++;
      } catch (AxelorException e) {
        TraceBackService.trace(
            new AxelorException(
                e, e.getCategory(), String.format("%s %s", I18n.get("Invoice"), invoiceId)),
            ExceptionOriginRepository.DOUBTFUL_CUSTOMER,
            batch.getId());

        incrementAnomaly();
      } catch (Exception e) {
        TraceBackService.trace(
            new Exception(String.format("%s %s", I18n.get("Invoice"), invoiceId), e),
            ExceptionOriginRepository.DOUBTFUL_CUSTOMER,
            batch.getId());

        incrementAnomaly();

        log.error(
            "Anomaly generated for the invoice {}", moveLine.getInvoiceReject().getInvoiceId());
      } finally {
        if (i % 10 == 0) {
          JPA.clear();

          doubtfulCustomerAccount = accountRepo.find(doubtfulCustomerAccount.getId());
        }
      }
    }
  }

  /**
   * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the
   * entity in the persistant context. Warning : {@code batch} entity have to be saved before.
   */
  @Override
  protected void stop() {

    AccountingService.setUpdateCustomerAccount(true);

    String comment = I18n.get(AccountExceptionMessage.BATCH_DOUBTFUL_1) + " :\n";
    comment +=
        String.format(
            "\t" + I18n.get(AccountExceptionMessage.BATCH_DOUBTFUL_2) + "\n", batch.getDone());
    comment +=
        String.format("\t" + I18n.get(BaseExceptionMessage.BASE_BATCH_3), batch.getAnomaly());

    comment += String.format("\t* ------------------------------- \n");
    comment += String.format("\t* %s ", updateCustomerAccountLog);

    super.stop();
    addComment(comment);
  }
}
