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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;

public class InvoiceResetServiceImpl implements InvoiceResetService {

  /**
   * Method to call after copying an invoice to reset the status. Can be used after JPA.copy to
   * reset invoice status without losing references to other objets.<br>
   * <b>Most of the time you do not want to use this method directly but call {@link
   * InvoiceRepository#save(Invoice)} instead.</b>
   *
   * @param copy a copy of an invoice
   */
  @Override
  public void resetInvoiceStatusOnCopy(Invoice copy) throws AxelorException {
    copy.setStatusSelect(InvoiceRepository.STATUS_DRAFT);
    copy.setInvoiceId(null);
    copy.setInvoiceDate(null);
    copy.setDueDate(null);
    copy.setValidatedByUser(null);
    copy.setMove(null);
    copy.setOriginalInvoice(null);
    copy.setCompanyInTaxTotalRemaining(BigDecimal.ZERO);
    copy.setAmountPaid(BigDecimal.ZERO);
    copy.setAmountRemaining(copy.getInTaxTotal());
    copy.setIrrecoverableStatusSelect(InvoiceRepository.IRRECOVERABLE_STATUS_NOT_IRRECOUVRABLE);
    copy.setAmountRejected(BigDecimal.ZERO);
    copy.setPaymentProgress(0);
    copy.clearBatchSet();
    copy.setDebitNumber(null);
    copy.setDoubtfulCustomerOk(false);
    copy.setMove(null);
    copy.setPaymentMove(null);
    copy.clearRefundInvoiceList();
    copy.setRejectDateTime(null);
    copy.setOriginalInvoice(null);
    copy.setUsherPassageOk(false);
    copy.setAlreadyPrintedOk(false);
    copy.setCanceledPaymentSchedule(null);
    copy.setDirectDebitAmount(BigDecimal.ZERO);
    copy.setImportId(null);
    copy.setPartnerAccount(null);
    copy.setJournal(null);
    copy.clearInvoicePaymentList();
    copy.setPrintedPDF(null);
    copy.setValidatedDateTime(null);
    copy.setVentilatedByUser(null);
    copy.setVentilatedDateTime(null);
    copy.setDecisionPfpTakenDateTime(null);
    copy.setInternalReference(null);
    copy.setExternalReference(null);
    copy.setLcrAccounted(false);
    copy.clearInvoiceTermList();
    copy.setFinancialDiscount(null);
    copy.setFinancialDiscountDeadlineDate(copy.getDueDate());
    copy.setFinancialDiscountRate(BigDecimal.ZERO);
    copy.setFinancialDiscountTotalAmount(BigDecimal.ZERO);
    copy.setRemainingAmountAfterFinDiscount(BigDecimal.ZERO);
    copy.setOldMove(null);
    copy.setBillOfExchangeBlockingOk(false);
    copy.setBillOfExchangeBlockingReason(null);
    copy.setBillOfExchangeBlockingToDate(null);
    copy.setBillOfExchangeBlockingByUser(null);
    copy.setNextDueDate(InvoiceToolService.getNextDueDate(copy));
    InvoiceToolService.setPfpStatus(copy);
    copy.setHasPendingPayments(false);
  }
}
