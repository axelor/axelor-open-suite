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
package com.axelor.apps.bankpayment.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.BankPaymentConfig;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.InvoiceTermFilterBankPaymentService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderValidationService;
import com.axelor.apps.bankpayment.service.config.BankPaymentConfigService;
import com.axelor.apps.bankpayment.service.invoice.payment.InvoicePaymentCreateServiceBankPay;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BatchBankOrderGenerationBillOfExchangeInvoiceTerm extends AbstractBatch {

  protected AppAccountService appAccountService;
  protected BankPaymentConfigService bankPaymentConfigService;
  protected InvoicePaymentCreateServiceBankPay invoicePaymentCreateService;
  protected BankOrderCreateService bankOrderCreateService;
  protected BankOrderLineService bankOrderLineService;
  protected BankOrderRepository bankOrderRepository;
  protected BankOrderValidationService bankOrderValidationService;
  protected InvoiceTermFilterBankPaymentService invoiceTermFilterBankPaymentService;
  protected BillOfExchangeInvoiceTermQueryService billOfExchangeInvoiceTermQueryService;
  private boolean end = false;

  @Inject
  public BatchBankOrderGenerationBillOfExchangeInvoiceTerm(
      AppAccountService appAccountService,
      BankPaymentConfigService bankPaymentConfigService,
      InvoicePaymentCreateServiceBankPay invoicePaymentCreateService,
      BankOrderCreateService bankOrderCreateService,
      BankOrderLineService bankOrderLineService,
      BankOrderRepository bankOrderRepository,
      BankOrderValidationService bankOrderValidationService,
      InvoiceTermFilterBankPaymentService invoiceTermFilterBankPaymentService,
      BillOfExchangeInvoiceTermQueryService billOfExchangeInvoiceTermQueryService) {
    super();
    this.appAccountService = appAccountService;
    this.bankPaymentConfigService = bankPaymentConfigService;
    this.invoicePaymentCreateService = invoicePaymentCreateService;
    this.bankOrderCreateService = bankOrderCreateService;
    this.bankOrderLineService = bankOrderLineService;
    this.bankOrderRepository = bankOrderRepository;
    this.bankOrderValidationService = bankOrderValidationService;
    this.invoiceTermFilterBankPaymentService = invoiceTermFilterBankPaymentService;
    this.billOfExchangeInvoiceTermQueryService = billOfExchangeInvoiceTermQueryService;
  }

  @Override
  protected void start() throws IllegalAccessException {
    super.start();
    try {
      BankPaymentConfig bankPaymentConfig =
          bankPaymentConfigService.getBankPaymentConfig(batch.getAccountingBatch().getCompany());
      if (bankPaymentConfig.getBillOfExchangeSequence() == null) {
        throw new AxelorException(
            bankPaymentConfig,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BankPaymentExceptionMessage.ACCOUNT_CONFIG_SEQUENCE_12),
            I18n.get(BaseExceptionMessage.EXCEPTION),
            bankPaymentConfig.getCompany().getName());
      }
      if (!isBankOrderPaymentMode(batch.getAccountingBatch().getPaymentMode())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(
                BankPaymentExceptionMessage
                    .BATCH_BILL_OF_EXCHANGE_BANK_ORDER_PAYMENT_MODE_INVALID));
      }
    } catch (Exception e) {
      TraceBackService.trace(
          e, "Batch bill of exchange bank order generation invoice term", batch.getId());
      incrementAnomaly();
      end = true;
    }
  }

  @Override
  protected void process() {
    if (end) {
      return;
    }
    AccountingBatch accountingBatch = batch.getAccountingBatch();

    try {
      List<InvoiceTerm> invoiceTermList =
          billOfExchangeInvoiceTermQueryService
              .buildOrderedQueryFetchLcrAccountedInvoiceTerms(accountingBatch)
              .fetch();
      invoiceTermList = getInvoiceTermsWithoutAwaitingBankOrder(invoiceTermList);
      if (invoiceTermList.isEmpty()) {
        return;
      }
      Currency bankOrderCurrency = getBankOrderCurrency(accountingBatch, invoiceTermList);
      BankOrder bankOrder =
          buildAndSaveBankOrderIfNeeded(accountingBatch, bankOrderCurrency, invoiceTermList);
      if (bankOrder != null) {
        confirmBankOrderIfNeeded(bankOrder.getId(), true);
      }
    } catch (Exception e) {
      incrementAnomaly();
      TraceBackService.trace(
          e, "billOfExchangeInvoiceTermBatch: bank order generation", batch.getId());
    }
  }

  protected boolean isBankOrderPaymentMode(PaymentMode paymentMode) {
    return paymentMode != null && Boolean.TRUE.equals(paymentMode.getGenerateBankOrder());
  }

  protected BankDetails getAccountingBankDetails(AccountingBatch accountingBatch) {
    return accountingBatch.getBankDetails() != null
        ? accountingBatch.getBankDetails()
        : accountingBatch.getCompany().getDefaultBankDetails();
  }

  protected List<InvoiceTerm> getInvoiceTermsWithoutAwaitingBankOrder(
      List<InvoiceTerm> invoiceTermList) {
    return invoiceTermList.stream()
        .filter(
            invoiceTerm ->
                invoiceTermFilterBankPaymentService.getAwaitingBankOrderLineOrigin(invoiceTerm)
                    == null)
        .collect(Collectors.toList());
  }

  protected Currency getBankOrderCurrency(
      AccountingBatch accountingBatch, List<InvoiceTerm> invoiceTermList) throws AxelorException {
    if (accountingBatch.getPaymentMode().getBankOrderFileFormat().getIsMultiCurrency()) {
      return accountingBatch.getCurrency() != null
          ? accountingBatch.getCurrency()
          : accountingBatch.getCompany().getCurrency();
    }

    Currency bankOrderCurrency = null;
    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      Currency invoiceTermCurrency = invoiceTerm.getCurrency();
      if (invoiceTermCurrency == null
          || (bankOrderCurrency != null
              && !Objects.equals(bankOrderCurrency.getId(), invoiceTermCurrency.getId()))) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BankPaymentExceptionMessage.BANK_ORDER_MERGE_SAME_CURRENCY));
      }
      bankOrderCurrency = invoiceTermCurrency;
    }

    return bankOrderCurrency;
  }

  protected BankOrder buildBankOrder(AccountingBatch accountingBatch, Currency bankOrderCurrency)
      throws AxelorException {
    PaymentMode paymentMode = accountingBatch.getPaymentMode();
    BankDetails senderBankDetails = getAccountingBankDetails(accountingBatch);
    LocalDate bankOrderDate =
        accountingBatch.getDueDate() != null
            ? accountingBatch.getDueDate()
            : appAccountService.getTodayDate(accountingBatch.getCompany());

    return bankOrderCreateService.createBankOrder(
        paymentMode,
        BankOrderRepository.PARTNER_TYPE_CUSTOMER,
        bankOrderDate,
        accountingBatch.getCompany(),
        senderBankDetails,
        bankOrderCurrency,
        null,
        I18n.get("Bill of exchange"),
        BankOrderRepository.TECHNICAL_ORIGIN_AUTOMATIC,
        BankOrderRepository.FUNCTIONAL_ORIGIN_LCR,
        paymentMode.getAccountingTriggerSelect());
  }

  @Transactional(rollbackOn = {Exception.class})
  protected BankOrder buildAndSaveBankOrderIfNeeded(
      AccountingBatch accountingBatch,
      Currency bankOrderCurrency,
      List<InvoiceTerm> invoiceTermList)
      throws AxelorException {
    BankOrder bankOrder = null;
    List<InvoiceTerm> linkedTerms = new ArrayList<>();

    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      try {
        if (invoiceTermFilterBankPaymentService.getAwaitingBankOrderLineOrigin(invoiceTerm)
            != null) {
          continue;
        }

        if (bankOrder == null) {
          bankOrder = buildBankOrder(accountingBatch, bankOrderCurrency);
        }

        LocalDate lineDate =
            bankOrder.getBankOrderFileFormat().getIsMultiDate() ? invoiceTerm.getDueDate() : null;

        BankOrderLine bankOrderLine =
            bankOrderLineService.createBankOrderLine(
                bankOrder.getBankOrderFileFormat(),
                null,
                invoiceTerm.getPartner(),
                billOfExchangeInvoiceTermQueryService.getReceiverBankDetails(invoiceTerm),
                invoiceTerm.getAmountRemaining(),
                invoiceTerm.getCurrency(),
                lineDate,
                invoiceTerm.getName(),
                invoiceTerm.getName(),
                invoiceTerm);

        bankOrder.addBankOrderLineListItem(bankOrderLine);
        linkedTerms.add(invoiceTerm);
        incrementDone();
      } catch (Exception e) {
        incrementAnomaly();
        TraceBackService.trace(
            e, "billOfExchangeInvoiceTermBatch: create bank order line", batch.getId());
      }
    }

    if (bankOrder == null) {
      return null;
    }

    bankOrder = bankOrderRepository.save(bankOrder);

    for (InvoiceTerm invoiceTerm : linkedTerms) {
      if (invoiceTerm.getInvoice() != null) {
        // InvoicePayment requires an Invoice; standalone terms get only the BankOrderLine above.
        createInvoicePaymentForTraceability(invoiceTerm, accountingBatch, bankOrder);
      }
    }

    return bankOrder;
  }

  protected void createInvoicePaymentForTraceability(
      InvoiceTerm invoiceTerm, AccountingBatch accountingBatch, BankOrder bankOrder)
      throws AxelorException {
    LocalDate paymentDate =
        accountingBatch.getDueDate() != null
            ? accountingBatch.getDueDate()
            : appAccountService.getTodayDate(accountingBatch.getCompany());
    invoicePaymentCreateService.createInvoicePaymentForBankOrder(
        invoiceTerm.getInvoice(),
        invoiceTerm,
        accountingBatch.getPaymentMode(),
        getAccountingBankDetails(accountingBatch),
        paymentDate,
        bankOrder);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void confirmBankOrderIfNeeded(Long bankOrderId, boolean hasBankOrderLine)
      throws AxelorException {
    if (!hasBankOrderLine) {
      return;
    }

    BankOrder bankOrder = bankOrderRepository.find(bankOrderId);
    if (bankOrder.getPaymentMode().getAutoConfirmBankOrder()
        && bankOrder.getStatusSelect() == BankOrderRepository.STATUS_DRAFT) {
      bankOrderValidationService.confirm(bankOrder);
    }
  }

  @Override
  protected void stop() {
    StringBuilder sb = new StringBuilder();
    sb.append(I18n.get(BaseExceptionMessage.ABSTRACT_BATCH_REPORT)).append(" ");
    sb.append(
        String.format(
            I18n.get(
                    BaseExceptionMessage.ABSTRACT_BATCH_DONE_SINGULAR,
                    BaseExceptionMessage.ABSTRACT_BATCH_DONE_PLURAL,
                    batch.getDone())
                + " ",
            batch.getDone()));
    sb.append(
        String.format(
            I18n.get(
                BaseExceptionMessage.ABSTRACT_BATCH_ANOMALY_SINGULAR,
                BaseExceptionMessage.ABSTRACT_BATCH_ANOMALY_PLURAL,
                batch.getAnomaly()),
            batch.getAnomaly()));
    addComment(sb.toString());
    super.stop();
  }

  @Override
  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_BANK_PAYMENT_BATCH);
  }
}
