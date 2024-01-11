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
package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchDoubtfulCustomer extends BatchStrategy {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected boolean end = false;

  protected String updateCustomerAccountLog = "";

  protected AccountRepository accountRepo;

  @Inject
  public BatchDoubtfulCustomer(
      DoubtfulCustomerService doubtfulCustomerService,
      BatchAccountCustomer batchAccountCustomer,
      AccountRepository accountRepo) {

    super(doubtfulCustomerService, batchAccountCustomer);

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

  @SuppressWarnings("unchecked")
  @Override
  protected void process() {

    if (!end) {
      Company company = batch.getAccountingBatch().getCompany();

      AccountConfig accountConfig = company.getAccountConfig();

      Account doubtfulCustomerAccount = accountConfig.getDoubtfulCustomerAccount();
      String sixMonthDebtPassReason = accountConfig.getSixMonthDebtPassReason();
      String threeMonthDebtPassReason = accountConfig.getThreeMonthDebtPassReason();

      // FACTURES
      List<Move> moveList = doubtfulCustomerService.getMove(0, doubtfulCustomerAccount, company);
      log.debug("Number of move lines (Debt more than 6 months) in 411 : {}", moveList.size());
      this.createDoubtFulCustomerMove(moveList, doubtfulCustomerAccount, sixMonthDebtPassReason);

      moveList = doubtfulCustomerService.getMove(1, doubtfulCustomerAccount, company);
      log.debug("Number of move lines (Debt more than 3 months) in 411 : {}", moveList.size());
      this.createDoubtFulCustomerMove(moveList, doubtfulCustomerAccount, threeMonthDebtPassReason);

      // FACTURES REJETES
      List<MoveLine> moveLineList =
          (List<MoveLine>)
              doubtfulCustomerService.getRejectMoveLine(0, doubtfulCustomerAccount, company);
      log.debug(
          "Number of rejected move lines (Debt more than 6 months) in 411 : {}",
          moveLineList.size());
      this.createDoubtFulCustomerRejectMove(
          moveLineList, doubtfulCustomerAccount, sixMonthDebtPassReason);

      moveLineList =
          (List<MoveLine>)
              doubtfulCustomerService.getRejectMoveLine(1, doubtfulCustomerAccount, company);
      log.debug(
          "Number of rejected move lines (Debt more than 3 months) in 411 : {}",
          moveLineList.size());
      this.createDoubtFulCustomerRejectMove(
          moveLineList, doubtfulCustomerAccount, threeMonthDebtPassReason);

      updateCustomerAccountLog +=
          batchAccountCustomer.updateAccountingSituationMarked(companyRepo.find(company.getId()));
    }
  }

  /**
   * Procédure permettant de créer les écritures de passage en client douteux pour chaque écriture
   * de facture
   *
   * @param moveLineList Une liste d'écritures de facture
   * @param doubtfulCustomerAccount Un compte client douteux
   * @param debtPassReason Un motif de passage en client douteux
   * @throws AxelorException
   */
  public void createDoubtFulCustomerMove(
      List<Move> moveList, Account doubtfulCustomerAccount, String debtPassReason) {
    for (int i = 0; i < moveList.size(); i++) {
      Move move = moveList.get(i);

      try {
        doubtfulCustomerService.createDoubtFulCustomerMove(
            moveRepo.find(move.getId()),
            accountRepo.find(doubtfulCustomerAccount.getId()),
            debtPassReason);

        Move myMove = moveRepo.find(move.getId());

        if (myMove.getInvoice() != null) {
          updateInvoice(myMove.getInvoice());
        } else {
          updateAccountMove(myMove, true);
        }

      } catch (AxelorException e) {
        TraceBackService.trace(
            new AxelorException(e, e.getCategory(), I18n.get("Invoice") + " %s", move.getOrigin()),
            ExceptionOriginRepository.DOUBTFUL_CUSTOMER,
            batch.getId());

        incrementAnomaly();
      } catch (Exception e) {
        TraceBackService.trace(
            new Exception(String.format(I18n.get("Invoice") + " %s", move.getOrigin()), e),
            ExceptionOriginRepository.DOUBTFUL_CUSTOMER,
            batch.getId());

        incrementAnomaly();

        log.error(
            "Anomaly generated for the invoice {}",
            moveRepo.find(move.getId()).getInvoice().getInvoiceId());
      } finally {
        if (i % 10 == 0) {
          JPA.clear();
        }
      }
    }
  }

  /**
   * Procédure permettant de créer les écritures de passage en client douteux pour chaque ligne
   * d'écriture de rejet de facture
   *
   * @param moveLineList Une liste de lignes d'écritures de rejet de facture
   * @param doubtfulCustomerAccount Un compte client douteux
   * @param debtPassReason Un motif de passage en client douteux
   * @throws AxelorException
   */
  public void createDoubtFulCustomerRejectMove(
      List<MoveLine> moveLineList, Account doubtfulCustomerAccount, String debtPassReason) {

    int i = 0;

    for (MoveLine moveLine : moveLineList) {

      try {

        doubtfulCustomerService.createDoubtFulCustomerRejectMove(
            moveLineRepo.find(moveLine.getId()),
            accountRepo.find(doubtfulCustomerAccount.getId()),
            debtPassReason);
        updateInvoice(moveLineRepo.find(moveLine.getId()).getInvoiceReject());
        i++;

      } catch (AxelorException e) {

        TraceBackService.trace(
            new AxelorException(
                e,
                e.getCategory(),
                I18n.get("Invoice") + " %s",
                moveLine.getInvoiceReject().getInvoiceId()),
            ExceptionOriginRepository.DOUBTFUL_CUSTOMER,
            batch.getId());
        incrementAnomaly();

      } catch (Exception e) {

        TraceBackService.trace(
            new Exception(
                String.format(
                    I18n.get("Invoice") + " %s", moveLine.getInvoiceReject().getInvoiceId()),
                e),
            ExceptionOriginRepository.DOUBTFUL_CUSTOMER,
            batch.getId());

        incrementAnomaly();

        log.error(
            "Anomaly generated for the invoice {}",
            moveLineRepo.find(moveLine.getId()).getInvoiceReject().getInvoiceId());

      } finally {

        if (i % 10 == 0) {
          JPA.clear();
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
        String.format(
            "\t" + I18n.get(BaseExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());

    comment += String.format("\t* ------------------------------- \n");
    comment += String.format("\t* %s ", updateCustomerAccountLog);

    super.stop();
    addComment(comment);
  }
}
