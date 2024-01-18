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
package com.axelor.apps.bankpayment.service.batch;

import com.axelor.apps.bankpayment.db.BankPaymentBatch;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.apps.bankpayment.db.repo.EbicsPartnerRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsUserRepository;
import com.axelor.apps.bankpayment.ebics.service.EbicsPartnerService;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchBankStatement extends AbstractBatch {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private int bankStatementCount;

  @Inject protected EbicsPartnerRepository ebicsPartnerRepository;

  @Inject protected EbicsPartnerService ebicsPartnerService;

  @Inject protected BankStatementService bankStatementService;

  @Override
  protected void process() {
    BankPaymentBatch bankPaymentBatch = batch.getBankPaymentBatch();
    Collection<EbicsPartner> ebicsPartners = bankPaymentBatch.getEbicsPartnerSet();

    // Retrieve all active EBICS partners if there is no configured EBICS partners
    // on the batch.
    if (ebicsPartners == null || ebicsPartners.isEmpty()) {
      ebicsPartners = getAllActiveEbicsPartners();
    }

    for (EbicsPartner ebicsPartner : ebicsPartners) {
      try {
        List<BankStatement> bankStatementList =
            ebicsPartnerService.getBankStatements(
                ebicsPartnerRepository.find(ebicsPartner.getId()),
                bankPaymentBatch.getBankStatementFileFormatSet());

        bankStatementCount += bankStatementList.size();

        for (BankStatement bankStatement : bankStatementList) {

          try {
            bankStatementService.runImport(bankStatement, false);
          } catch (AxelorException e) {
            processError(e, e.getCategory(), ebicsPartner);
          } finally {
            JPA.clear();
          }
        }

        incrementDone();

      } catch (AxelorException e) {
        processError(e, e.getCategory(), ebicsPartner);
      } catch (Exception e) {
        processError(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, ebicsPartner);
      }
    }
  }

  protected void processError(Exception cause, int category, EbicsPartner ebicsPartner) {
    log.error(cause.getMessage(), cause);
    incrementAnomaly();
    AxelorException exception =
        new AxelorException(
            cause,
            ebicsPartner,
            category,
            BankPaymentExceptionMessage.BANK_STATEMENT_EBICS_PARTNER,
            ebicsPartner.getPartnerId(),
            cause.getMessage());
    TraceBackService.trace(exception, ExceptionOriginRepository.BANK_STATEMENT, batch.getId());
  }

  @Override
  protected void stop() {
    StringBuilder sb = new StringBuilder();
    sb.append(I18n.get(BaseExceptionMessage.ABSTRACT_BATCH_REPORT));
    sb.append(" ");
    sb.append(
        String.format(
            I18n.get(
                BaseExceptionMessage.ABSTRACT_BATCH_DONE_SINGULAR,
                BaseExceptionMessage.ABSTRACT_BATCH_DONE_PLURAL,
                batch.getDone()),
            batch.getDone()));
    sb.append(" ");
    sb.append(
        String.format(
            I18n.get(
                BaseExceptionMessage.ABSTRACT_BATCH_ANOMALY_SINGULAR,
                BaseExceptionMessage.ABSTRACT_BATCH_ANOMALY_PLURAL,
                batch.getAnomaly()),
            batch.getAnomaly()));
    sb.append("\n");
    sb.append(
        String.format(
            I18n.get(
                BankPaymentExceptionMessage.BATCH_BANK_STATEMENT_RETRIEVED_BANK_STATEMENT_COUNT),
            bankStatementCount));
    addComment(sb.toString());
    super.stop();
  }

  private Collection<EbicsPartner> getAllActiveEbicsPartners() {
    return ebicsPartnerRepository
        .all()
        .filter("self.transportEbicsUser.statusSelect = :statusSelect")
        .bind("statusSelect", EbicsUserRepository.STATUS_ACTIVE_CONNECTION)
        .fetch();
  }

  public Batch bankStatement(BankPaymentBatch bankPaymentBatch) {
    return Beans.get(BatchBankStatement.class).run(bankPaymentBatch);
  }

  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_BANK_PAYMENT_BATCH);
  }
}
