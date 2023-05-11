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

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountingService;
import com.axelor.apps.account.service.ReimbursementImportService;
import com.axelor.apps.account.service.RejectImportService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.studio.app.service.AppService;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchReimbursementImport extends BatchStrategy {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected boolean end = false;

  protected BigDecimal totalAmount = BigDecimal.ZERO;

  protected String updateCustomerAccountLog = "";

  @Inject
  public BatchReimbursementImport(
      ReimbursementImportService reimbursementImportService,
      RejectImportService rejectImportService,
      BatchAccountCustomer batchAccountCustomer) {

    super(reimbursementImportService, rejectImportService, batchAccountCustomer);

    AccountingService.setUpdateCustomerAccount(false);
  }

  @Override
  protected void start() throws IllegalAccessException {

    super.start();

    Company company = batch.getAccountingBatch().getCompany();

    company = companyRepo.find(company.getId());

    try {
      reimbursementImportService.testCompanyField(company);
    } catch (AxelorException e) {
      TraceBackService.trace(
          new AxelorException(e, e.getCategory(), ""),
          ExceptionOriginRepository.REIMBURSEMENT,
          batch.getId());
      incrementAnomaly();
      end = true;
    }
    checkPoint();
  }

  @Override
  protected void process() {
    if (!end) {

      Company company = batch.getAccountingBatch().getCompany();

      company = companyRepo.find(company.getId());

      AccountConfig accountConfig = company.getAccountConfig();

      Map<List<String[]>, String> data = null;

      try {

        String dataImportDir = AppService.getFileUploadDir();

        String reimbursementImportFolderPathCFONB =
            accountConfig.getReimbursementImportFolderPathCFONB() == null
                ? null
                : dataImportDir + accountConfig.getReimbursementImportFolderPathCFONB();

        rejectImportService.createFilePath(reimbursementImportFolderPathCFONB);

        data =
            rejectImportService.getCFONBFileByLot(
                reimbursementImportFolderPathCFONB,
                accountConfig.getTempReimbImportFolderPathCFONB() == null
                    ? null
                    : dataImportDir + accountConfig.getTempReimbImportFolderPathCFONB(),
                company,
                0);

      } catch (AxelorException e) {

        TraceBackService.trace(
            new AxelorException(
                e,
                e.getCategory(),
                I18n.get(AccountExceptionMessage.BATCH_REIMBURSEMENT_6),
                batch.getId()),
            ExceptionOriginRepository.REIMBURSEMENT,
            batch.getId());
        incrementAnomaly();

        stop();

      } catch (Exception e) {

        TraceBackService.trace(
            new Exception(
                String.format(
                    I18n.get(AccountExceptionMessage.BATCH_REIMBURSEMENT_6), batch.getId()),
                e),
            ExceptionOriginRepository.REIMBURSEMENT,
            batch.getId());

        incrementAnomaly();

        log.error(
            "Bug(Anomalie) généré(e) pour le batch d'import des remboursements {}", batch.getId());

        stop();
      }

      int seq = 1;

      int i = 0;

      for (Entry<List<String[]>, String> entry : data.entrySet()) {

        LocalDate rejectDate = rejectImportService.createRejectDate(entry.getValue());

        Move move = this.createMove(company, rejectDate);

        for (String[] reject : entry.getKey()) {

          try {

            Reimbursement reimbursement =
                reimbursementImportService.createReimbursementRejectMoveLine(
                    reject,
                    companyRepo.find(company.getId()),
                    seq,
                    moveRepo.find(move.getId()),
                    rejectDate);
            if (reimbursement != null) {
              log.debug("Reimbursement n° {} processed", reimbursement.getRef());
              seq++;
              i++;
              updateReimbursement(reimbursement);
            }
          } catch (AxelorException e) {

            TraceBackService.trace(
                new AxelorException(
                    e,
                    e.getCategory(),
                    I18n.get(AccountExceptionMessage.BATCH_REIMBURSEMENT_7),
                    reject[1]),
                ExceptionOriginRepository.REIMBURSEMENT,
                batch.getId());
            incrementAnomaly();

          } catch (Exception e) {

            TraceBackService.trace(
                new Exception(
                    String.format(
                        I18n.get(AccountExceptionMessage.BATCH_REIMBURSEMENT_7), reject[1]),
                    e),
                ExceptionOriginRepository.REIMBURSEMENT,
                batch.getId());

            incrementAnomaly();

            log.error("Bug(Anomalie) généré(e) pour le rejet de remboursement {}", reject[1]);

          } finally {

            if (i % 10 == 0) {
              JPA.clear();
            }
          }
        }

        this.validateMove(move, rejectDate, seq);
      }

      updateCustomerAccountLog += batchAccountCustomer.updateAccountingSituationMarked(company);
    }
  }

  public Move createMove(Company company, LocalDate rejectDate) {

    Move move = null;

    try {
      move =
          reimbursementImportService.createMoveReject(
              companyRepo.find(company.getId()), rejectDate);

    } catch (AxelorException e) {

      TraceBackService.trace(
          new AxelorException(
              e,
              e.getCategory(),
              I18n.get(AccountExceptionMessage.BATCH_REIMBURSEMENT_6),
              batch.getId()),
          ExceptionOriginRepository.REIMBURSEMENT,
          batch.getId());
      incrementAnomaly();

      stop();

    } catch (Exception e) {

      TraceBackService.trace(
          new Exception(
              String.format(I18n.get(AccountExceptionMessage.BATCH_REIMBURSEMENT_6), batch.getId()),
              e),
          ExceptionOriginRepository.REIMBURSEMENT,
          batch.getId());

      incrementAnomaly();

      log.error(
          "Bug(Anomalie) généré(e) pour le batch d'import des remboursements {}", batch.getId());

      stop();
    }

    return move;
  }

  public void validateMove(Move move, LocalDate rejectDate, int seq) {
    try {
      if (seq != 1) {
        MoveLine oppositeMoveLine =
            reimbursementImportService.createOppositeRejectMoveLine(
                moveRepo.find(move.getId()), seq, rejectDate);
        reimbursementImportService.validateMove(moveRepo.find(move.getId()));
        this.totalAmount =
            this.totalAmount.add(moveLineRepo.find(oppositeMoveLine.getId()).getDebit());
      } else {
        reimbursementImportService.deleteMove(moveRepo.find(move.getId()));
      }
    } catch (AxelorException e) {

      TraceBackService.trace(
          new AxelorException(
              e,
              e.getCategory(),
              I18n.get(AccountExceptionMessage.BATCH_REIMBURSEMENT_6),
              batch.getId()),
          ExceptionOriginRepository.REIMBURSEMENT,
          batch.getId());
      incrementAnomaly();

    } catch (Exception e) {

      TraceBackService.trace(
          new Exception(
              String.format(I18n.get(AccountExceptionMessage.BATCH_REIMBURSEMENT_6), batch.getId()),
              e),
          ExceptionOriginRepository.REIMBURSEMENT,
          batch.getId());

      incrementAnomaly();

      log.error(
          "Bug(Anomalie) généré(e) pour le batch d'import des remboursements {}", batch.getId());
    }
  }

  /**
   * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the
   * entity in the persistant context. Warning : {@code batch} entity have to be saved before.
   */
  @Override
  protected void stop() {

    AccountingService.setUpdateCustomerAccount(true);

    String comment = "";
    comment = I18n.get(AccountExceptionMessage.BATCH_REIMBURSEMENT_8) + "\n";
    comment +=
        String.format(
            "\t* %s " + I18n.get(AccountExceptionMessage.BATCH_REIMBURSEMENT_9) + "\n",
            batch.getDone());
    comment +=
        String.format(
            "\t* " + I18n.get(AccountExceptionMessage.BATCH_REIMBURSEMENT_10) + " : %s \n",
            this.totalAmount);
    comment +=
        String.format(
            "\t" + I18n.get(BaseExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());

    comment += String.format("\t* ------------------------------- \n");
    comment += String.format("\t* %s ", updateCustomerAccountLog);

    super.stop();
    addComment(comment);
  }
}
