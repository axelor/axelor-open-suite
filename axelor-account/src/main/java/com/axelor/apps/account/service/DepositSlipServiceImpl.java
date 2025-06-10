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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.DepositSlip;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherConfirmService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Query;
import com.axelor.dms.db.DMSFile;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.utils.helpers.QueryBuilder;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DepositSlipServiceImpl implements DepositSlipService {

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  protected InvoicePaymentRepository invoicePaymentRepository;
  protected PaymentVoucherRepository paymentVoucherRepository;
  protected AccountConfigService accountConfigService;
  protected PrintingTemplatePrintService printingTemplatePrintService;

  @Inject
  public DepositSlipServiceImpl(
      InvoicePaymentRepository invoicePaymentRepository,
      PaymentVoucherRepository paymentVoucherRepository,
      AccountConfigService accountConfigService,
      PrintingTemplatePrintService printingTemplatePrintService) {
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.paymentVoucherRepository = paymentVoucherRepository;
    this.accountConfigService = accountConfigService;
    this.printingTemplatePrintService = printingTemplatePrintService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public LocalDate publish(DepositSlip depositSlip) throws AxelorException {

    List<PaymentVoucher> paymentVouchers = depositSlip.getPaymentVoucherList();

    if (paymentVouchers.stream()
        .filter(
            paymentVoucher ->
                Strings.isNullOrEmpty(paymentVoucher.getChequeBank())
                    || Strings.isNullOrEmpty(paymentVoucher.getChequeOwner())
                    || Strings.isNullOrEmpty(paymentVoucher.getChequeNumber())
                    || Objects.isNull(paymentVoucher.getChequeDate()))
        .findAny()
        .isPresent()) {
      throw new AxelorException(
          depositSlip,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              AccountExceptionMessage.DEPOSIT_SLIP_CONTAINS_PAYMENT_VOUCHER_WITH_MISSING_INFO));
    }

    List<Pair<LocalDate, BankDetails>> dateByBankDetailsList =
        new ArrayList<Pair<LocalDate, BankDetails>>();
    paymentVouchers.stream()
        .forEach(
            paymentVoucher ->
                updateInvoicePayment(
                    paymentVoucher.getChequeDate(),
                    depositSlip.getDepositNumber(),
                    paymentVoucher.getGeneratedMove()));

    for (PaymentVoucher pv : paymentVouchers) {
      Pair<LocalDate, BankDetails> dateByBankDetails =
          new MutablePair<>(
              pv.getChequeDate(),
              pv.getDepositBankDetails() != null
                  ? pv.getDepositBankDetails()
                  : pv.getCompanyBankDetails());
      if (!dateByBankDetailsList.contains(dateByBankDetails)) {
        dateByBankDetailsList.add(dateByBankDetails);
      }
    }

    if (!CollectionUtils.isEmpty(dateByBankDetailsList)) {
      for (Pair<LocalDate, BankDetails> dateByBankDetails : dateByBankDetailsList) {
        publish(depositSlip, dateByBankDetails.getRight(), dateByBankDetails.getLeft());
      }
    }

    LocalDate date = Beans.get(AppBaseService.class).getTodayDate(depositSlip.getCompany());
    depositSlip.setPublicationDate(date);

    return date;
  }

  protected void publish(DepositSlip depositSlip, BankDetails bankDetails, LocalDate chequeDate)
      throws AxelorException {

    String filename = getFilename(depositSlip, bankDetails, chequeDate);

    deleteExistingPublishDmsFile(depositSlip, filename);

    PrintingTemplate chequeDepositSlipPrintTemplate =
        getChequeDepositSlipPrintTemplate(depositSlip);
    PrintingGenFactoryContext factoryContext = new PrintingGenFactoryContext(depositSlip);
    factoryContext.setContext(
        Map.of("BankDetailsId", bankDetails.getId(), "ChequeDate", chequeDate));
    printingTemplatePrintService.getPrintLink(
        chequeDepositSlipPrintTemplate, factoryContext, filename, true);
  }

  protected void deleteExistingPublishDmsFile(DepositSlip depositSlip, String filename) {

    MetaFiles metaFiles = Beans.get(MetaFiles.class);

    Query.of(DMSFile.class)
        .filter("self.relatedId = :id AND self.relatedModel = :model and self.fileName = :filename")
        .bind("id", depositSlip.getId())
        .bind("model", depositSlip.getClass().getName())
        .bind("filename", filename + ".pdf")
        .fetchStream()
        .forEach(metaFiles::delete);
  }

  public String getFilename(
      DepositSlip depositSlip, BankDetails bankDetails, LocalDate chequeDueDate) {

    StringBuilder stringBuilder = new StringBuilder(depositSlip.getDepositNumber());
    stringBuilder = stringBuilder.append('-').append(bankDetails.getBankCode());
    stringBuilder = stringBuilder.append('-').append(bankDetails.getAccountNbr());
    stringBuilder = stringBuilder.append('-').append(chequeDueDate.toString());

    return stringBuilder.toString();
  }

  protected PrintingTemplate getChequeDepositSlipPrintTemplate(DepositSlip depositSlip)
      throws AxelorException {
    if (depositSlip.getPaymentModeTypeSelect() != PaymentModeRepository.TYPE_CHEQUE) {
      throw new AxelorException(
          depositSlip,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          AccountExceptionMessage.DEPOSIT_SLIP_UNSUPPORTED_PAYMENT_MODE_TYPE);
    }
    PrintingTemplate chequeDepositSlipPrintTemplate =
        accountConfigService
            .getAccountConfig(depositSlip.getCompany())
            .getChequeDepositSlipPrintTemplate();
    if (ObjectUtils.isEmpty(chequeDepositSlipPrintTemplate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.TEMPLATE_CONFIG_NOT_FOUND));
    }
    return chequeDepositSlipPrintTemplate;
  }

  @Override
  public List<PaymentVoucher> fetchPaymentVouchers(DepositSlip depositSlip) {

    QueryBuilder<PaymentVoucher> queryBuilder = QueryBuilder.of(PaymentVoucher.class);

    if (depositSlip.getPaymentModeTypeSelect() != 0) {
      queryBuilder.add("self.paymentMode.typeSelect = :paymentModeTypeSelect");
      queryBuilder.bind("paymentModeTypeSelect", depositSlip.getPaymentModeTypeSelect());
    }

    if (Objects.nonNull(depositSlip.getCompany())) {
      queryBuilder.add("self.company = :company");
      queryBuilder.bind("company", depositSlip.getCompany());
    }

    if (Objects.nonNull(depositSlip.getCurrency())) {
      queryBuilder.add("self.currency = :currency");
      queryBuilder.bind("currency", depositSlip.getCurrency());
    }

    if (Objects.nonNull(depositSlip.getCompanyBankDetails())) {
      queryBuilder.add(
          "(self.depositBankDetails = :companyBankDetails AND self.bankEntryGenWithoutValEntryCollectionOk is false AND self.paymentMode.accountingTriggerSelect = :depositAccountingTriggerSelect) OR (self.companyBankDetails = :companyBankDetails AND (self.bankEntryGenWithoutValEntryCollectionOk is true OR self.paymentMode.accountingTriggerSelect = :immediateAccountingTriggerSelect))");
      queryBuilder.bind("companyBankDetails", depositSlip.getCompanyBankDetails());
      queryBuilder.bind(
          "depositAccountingTriggerSelect",
          PaymentModeRepository.ACCOUNTING_TRIGGER_VALUE_FOR_COLLECTION);
      queryBuilder.bind(
          "immediateAccountingTriggerSelect", PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE);
    }

    if (Objects.nonNull(depositSlip.getValueForCollectionAccount())) {
      queryBuilder.add(
          "self.valueForCollectionAccount = :valueForCollectionAccount AND self.bankEntryGenWithoutValEntryCollectionOk is not true");
      queryBuilder.bind("valueForCollectionAccount", depositSlip.getValueForCollectionAccount());
    }

    if (depositSlip.getFromDate() != null) {
      if (depositSlip.getToDate() != null) {
        queryBuilder.add(
            "self.chequeDate IS NULL OR self.chequeDate BETWEEN :fromDate AND :toDate");
        queryBuilder.bind("fromDate", depositSlip.getFromDate());
        queryBuilder.bind("toDate", depositSlip.getToDate());
      } else {
        queryBuilder.add("self.chequeDate IS NULL OR self.chequeDate >= :fromDate");
        queryBuilder.bind("fromDate", depositSlip.getFromDate());
      }
    } else if (depositSlip.getToDate() != null) {
      queryBuilder.add("self.chequeDate IS NULL OR self.chequeDate <= :toDate");
      queryBuilder.bind("toDate", depositSlip.getToDate());
    }

    queryBuilder.add("self.depositSlip IS NULL");

    queryBuilder.add("self.statusSelect = :statusSelect");
    queryBuilder.bind("statusSelect", PaymentVoucherRepository.STATUS_CONFIRMED);

    List<PaymentVoucher> paymentVouchers = queryBuilder.build().fetch();
    List<PaymentVoucher> paymentVouchersSelected = depositSlip.getPaymentVoucherList();
    if (CollectionUtils.isEmpty(paymentVouchers)
        || CollectionUtils.isEmpty(paymentVouchersSelected)) {
      return paymentVouchers;
    } else {
      for (PaymentVoucher paymentVoucher : paymentVouchersSelected) {
        if (paymentVouchers.contains(paymentVoucher)) {
          paymentVouchers.remove(paymentVoucher);
        }
      }
    }
    return paymentVouchers;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void validate(DepositSlip depositSlip) throws AxelorException {

    if (Objects.isNull(depositSlip.getPaymentVoucherList())) {
      return;
    }

    if (Objects.isNull(depositSlip.getPublicationDate()) || depositSlip.getChequeCount() < 1) {
      throw new AxelorException(
          depositSlip,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.DEPOSIT_SLIP_NOT_PUBLISHED));
    }

    PaymentVoucherConfirmService paymentVoucherConfirmService =
        Beans.get(PaymentVoucherConfirmService.class);
    for (PaymentVoucher paymentVoucher : depositSlip.getPaymentVoucherList()) {
      paymentVoucherConfirmService.valueForCollectionMoveToGeneratedMove(
          paymentVoucher, depositSlip.getDepositDate());
    }

    depositSlip.setIsBankDepositMoveGenerated(true);
  }

  @Override
  public List<PaymentVoucher> getSelectedPaymentVoucherDueList(
      List<Map<String, Object>> paymentVoucherDueList) {
    return paymentVoucherDueList.stream()
        .filter(o -> ObjectUtils.notEmpty(o.get("selected")) && (Boolean) o.get("selected"))
        .map(it -> paymentVoucherRepository.find(((Integer) it.get("id")).longValue()))
        .collect(Collectors.toList());
  }

  @Override
  public BigDecimal getTotalAmount(
      DepositSlip depositSlip, List<Integer> selectedPaymentVoucherDueIdList) {
    return depositSlip
        .getTotalAmount()
        .add(
            selectedPaymentVoucherDueIdList.stream()
                .map(integer -> paymentVoucherRepository.find(integer.longValue()).getPaidAmount())
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO));
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void updateInvoicePayments(DepositSlip depositSlip, LocalDate depositDate) {
    depositSlip.getPaymentVoucherList().stream()
        .forEach(
            paymentVoucher ->
                updateInvoicePayment(
                    depositDate,
                    depositSlip.getDepositNumber(),
                    paymentVoucher.getValueForCollectionMove()));
  }

  protected void updateInvoicePayment(LocalDate depositDate, String depositNumber, Move move) {
    InvoicePayment invoicePayment =
        invoicePaymentRepository
            .all()
            .filter(
                "self.move = :move AND self.paymentMode.typeSelect = :paymentModeTypeSelect AND self.statusSelect = :statusSelect")
            .bind("move", move)
            .bind("paymentModeTypeSelect", PaymentModeRepository.TYPE_CHEQUE)
            .bind("statusSelect", InvoicePaymentRepository.STATUS_VALIDATED)
            .fetchOne();
    if (invoicePayment == null) {
      return;
    }
    invoicePayment.setBankDepositDate(depositDate);
    invoicePayment.setDescription(invoicePayment.getDescription() + ":" + depositNumber);
    invoicePaymentRepository.save(invoicePayment);
  }
}
