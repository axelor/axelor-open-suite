/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.common.ObjectUtils;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdvancePaymentRefundServiceImpl implements AdvancePaymentRefundService {

  protected InvoiceRepository invoiceRepository;
  protected InvoicePaymentCreateService invoicePaymentCreateService;
  protected InvoiceTermPaymentService invoiceTermPaymentService;
  protected InvoicePaymentRepository invoicePaymentRepository;
  protected InvoiceTermService invoiceTermService;
  protected InvoicePaymentToolService invoicePaymentToolService;

  @Inject
  public AdvancePaymentRefundServiceImpl(
      InvoiceRepository invoiceRepository,
      InvoicePaymentCreateService invoicePaymentCreateService,
      InvoiceTermPaymentService invoiceTermPaymentService,
      InvoicePaymentRepository invoicePaymentRepository,
      InvoiceTermService invoiceTermService,
      InvoicePaymentToolService invoicePaymentToolService) {
    this.invoiceRepository = invoiceRepository;
    this.invoicePaymentCreateService = invoicePaymentCreateService;
    this.invoiceTermPaymentService = invoiceTermPaymentService;
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.invoiceTermService = invoiceTermService;
    this.invoicePaymentToolService = invoicePaymentToolService;
  }

  @Override
  public void updateAdvancePaymentAmounts(Invoice refund) throws AxelorException {
    List<Invoice> advancePaymentList = getAdvancePaymentList(refund);

    if (!ObjectUtils.isEmpty(advancePaymentList)) {
      BigDecimal amountRemaining = refund.getAmountRemaining();

      for (Invoice advancePayment : advancePaymentList) {
        if (amountRemaining.compareTo(BigDecimal.ZERO) > 0
            && advancePayment.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0) {
          BigDecimal paymentAmount = amountRemaining.min(advancePayment.getAmountRemaining());

          processInvoicePaymentImputation(paymentAmount, refund, advancePayment);
          amountRemaining = amountRemaining.subtract(paymentAmount);
        }
      }

      invoicePaymentToolService.updateAmountPaid(refund);
    }
  }

  @Override
  public String createAdvancePaymentInvoiceSetDomain(Invoice refund) throws AxelorException {
    Set<Invoice> invoices = getDefaultAdvancePaymentInvoice(refund);
    String domain = "self.id IN (" + StringHelper.getIdListString(invoices) + ")";

    return domain;
  }

  @Override
  public Set<Invoice> getDefaultAdvancePaymentInvoice(Invoice refund) throws AxelorException {
    Set<Invoice> advancePaymentInvoices;

    Company company = refund.getCompany();
    Currency currency = refund.getCurrency();
    Partner partner = refund.getPartner();
    if (company == null
        || currency == null
        || partner == null
        || refund.getStatusSelect() != InvoiceRepository.STATUS_DRAFT) {
      return new HashSet<>();
    }
    String filter = writeGeneralFilterForAdvancePayment();
    filter +=
        " AND self.partner = :_partner "
            + "AND self.currency = :_currency "
            + "AND self.operationTypeSelect = :_operationTypeSelect "
            + "AND self.internalReference IS NULL "
            + "AND self.amountRemaining > 0";

    Integer operationTypeSelect =
        refund.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND
            ? InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
            : InvoiceRepository.OPERATION_TYPE_CLIENT_SALE;

    advancePaymentInvoices =
        new HashSet<>(
            invoiceRepository
                .all()
                .filter(filter)
                .bind("_status", InvoiceRepository.STATUS_VALIDATED)
                .bind("_operationSubType", InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE)
                .bind("_operationTypeSelect", operationTypeSelect)
                .bind("_partner", partner)
                .bind("_currency", currency)
                .fetch());
    return advancePaymentInvoices;
  }

  protected String writeGeneralFilterForAdvancePayment() {
    return "self.statusSelect = :_status" + " AND self.operationSubTypeSelect = :_operationSubType";
  }

  protected List<Invoice> getAdvancePaymentList(Invoice refund) {
    List<Invoice> advancePaymentList = new ArrayList<>();

    if (refund == null) {
      return advancePaymentList;
    }

    if (refund.getOriginalInvoice() != null
        && refund.getOriginalInvoice().getAmountRemaining().compareTo(BigDecimal.ZERO) > 0
        && refund.getOriginalInvoice().getStatusSelect() == InvoiceRepository.STATUS_VALIDATED) {
      advancePaymentList.add(refund.getOriginalInvoice());
    }
    List<Invoice> refundAdvancePaymentList = new ArrayList<>(refund.getAdvancePaymentInvoiceSet());
    if (!ObjectUtils.isEmpty(refundAdvancePaymentList)) {
      advancePaymentList.addAll(refundAdvancePaymentList);
    }

    Integer operationTypeSelect =
        refund.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND
            ? InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
            : InvoiceRepository.OPERATION_TYPE_CLIENT_SALE;

    String filter =
        "self.operationSubTypeSelect = :operationSubTypeSelect AND self.operationTypeSelect = :operationTypeSelect AND self.originalInvoice = :originalInvoice AND self.amountRemaining > 0 AND self.statusSelect = :statusSelect";
    Map<String, Object> params = new HashMap<>();
    params.put("operationSubTypeSelect", InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE);
    params.put("operationTypeSelect", operationTypeSelect);
    params.put("originalInvoice", refund);
    params.put("statusSelect", InvoiceRepository.STATUS_VALIDATED);

    advancePaymentList.addAll(invoiceRepository.all().filter(filter).bind(params).fetch());

    return advancePaymentList;
  }

  protected InvoicePayment createInvoicePaymentWithImputation(
      Invoice invoice, BigDecimal amount, int typeSelect) throws AxelorException {
    InvoicePayment imputationPayment =
        invoicePaymentCreateService.createInvoicePayment(
            invoice,
            amount,
            invoice.getDueDate(),
            invoice.getCurrency(),
            invoice.getPaymentMode(),
            typeSelect);
    imputationPayment.setCompanyBankDetails(invoice.getCompanyBankDetails());
    invoice.addInvoicePaymentListItem(imputationPayment);
    invoiceTermPaymentService.createInvoicePaymentTerms(imputationPayment, null);
    if (!ObjectUtils.isEmpty(imputationPayment.getInvoiceTermPaymentList())) {
      for (InvoiceTermPayment invoiceTermPayment : imputationPayment.getInvoiceTermPaymentList()) {
        invoiceTermService.updateInvoiceTermsPaidAmount(
            imputationPayment, invoiceTermPayment.getInvoiceTerm(), invoiceTermPayment, null);
      }
    }

    return imputationPayment;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void processInvoicePaymentImputation(
      BigDecimal paymentAmount, Invoice refund, Invoice advancePayment) throws AxelorException {
    InvoicePayment refundPayment =
        createInvoicePaymentWithImputation(
            refund, paymentAmount, InvoicePaymentRepository.TYPE_ADV_PAYMENT_IMPUTATION);
    InvoicePayment invoicePayment =
        createInvoicePaymentWithImputation(
            advancePayment, paymentAmount, InvoicePaymentRepository.TYPE_REFUND_IMPUTATION);
    refundPayment.setDescription(advancePayment.getInvoiceId());
    refundPayment.setImputedBy(invoicePayment);
    refundPayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
    invoicePayment.setImputedBy(refundPayment);
    invoicePayment.setDescription(refund.getInvoiceId());
    invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
    invoicePaymentRepository.save(refundPayment);
    invoicePaymentRepository.save(invoicePayment);
    invoicePaymentToolService.updateAmountPaid(advancePayment);
  }
}
