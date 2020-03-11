/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

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
    copy.setPfpValidateStatusSelect(PFP_STATUS_AWAITING);
    copy.setDecisionPfpTakenDate(null);
    return copy;
  }

  @Override
  public Invoice save(Invoice invoice) {
    try {
      List<InvoicePayment> invoicePayments = invoice.getInvoicePaymentList();
      if (CollectionUtils.isNotEmpty(invoicePayments)) {
        LocalDate latestPaymentDate =
            invoicePayments
                .stream()
                .filter(
                    invoicePayment ->
                        invoicePayment.getStatusSelect()
                            == InvoicePaymentRepository.STATUS_VALIDATED)
                .map(InvoicePayment::getPaymentDate)
                .max(LocalDate::compareTo)
                .orElse(null);
        invoice.setPaymentDate(latestPaymentDate);
      }

      invoice = super.save(invoice);
      Beans.get(InvoiceService.class).setDraftSequence(invoice);

      return invoice;
    } catch (Exception e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    try {
      if (context.get("_model") != null
          && context.get("_model").toString().contains("SubrogationRelease")) {
        if (context.get("id") != null) {
          long id = (long) context.get("id");
          SubrogationRelease subrogationRelease =
              Beans.get(SubrogationReleaseRepository.class).find(id);
          if (subrogationRelease != null && subrogationRelease.getStatusSelect() != null) {
            json.put("$subrogationStatusSelect", subrogationRelease.getStatusSelect());
          } else {
            json.put("$subrogationStatusSelect", SubrogationReleaseRepository.STATUS_NEW);
          }
        }
      } else {
        json.put("$subrogationStatusSelect", SubrogationReleaseRepository.STATUS_NEW);
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return super.populate(json, context);
  }
}
