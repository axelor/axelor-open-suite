/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import javax.persistence.PersistenceException;

public class InvoiceManagementRepository extends InvoiceRepository {
  @Override
  public Invoice copy(Invoice entity, boolean deep) {

    Invoice copy = super.copy(entity, deep);

    copy.setStatusSelect(STATUS_DRAFT);
    copy.setInvoiceId(null);
    copy.setInvoiceDate(null);
    copy.setDueDate(null);
    copy.setValidatedByUser(null);
    copy.setMove(null);
    copy.setOriginalInvoice(null);
    copy.setCompanyInTaxTotalRemaining(BigDecimal.ZERO);
    copy.setAmountPaid(BigDecimal.ZERO);
    copy.setIrrecoverableStatusSelect(IRRECOVERABLE_STATUS_NOT_IRRECOUVRABLE);
    copy.setAmountRejected(BigDecimal.ZERO);
    copy.clearBatchSet();
    copy.setDebitNumber(null);
    copy.setDirectDebitManagement(null);
    copy.setDoubtfulCustomerOk(false);
    copy.setMove(null);
    copy.setInterbankCodeLine(null);
    copy.setPaymentMove(null);
    copy.clearRefundInvoiceList();
    copy.setRejectDate(null);
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
    copy.setValidatedDate(null);
    copy.setVentilatedByUser(null);
    copy.setVentilatedDate(null);
    return copy;
  }

  @Override
  public Invoice save(Invoice invoice) {
    try {
      invoice = super.save(invoice);
      Beans.get(InvoiceService.class).setDraftSequence(invoice);

      return invoice;
    } catch (Exception e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }
}
