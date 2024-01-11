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
package com.axelor.apps.account.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.DepositSlip;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherConfirmService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.db.Query;
import com.axelor.dms.db.DMSFile;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.utils.QueryBuilder;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DepositSlipServiceImpl implements DepositSlipService {

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  protected InvoicePaymentRepository invoicePaymentRepository;
  protected PaymentVoucherRepository paymentVoucherRepository;

  @Inject
  public DepositSlipServiceImpl(
      InvoicePaymentRepository invoicePaymentRepository,
      PaymentVoucherRepository paymentVoucherRepository) {
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.paymentVoucherRepository = paymentVoucherRepository;
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

    paymentVouchers.stream()
        .forEach(
            paymentVoucher ->
                updateInvoicePayment(
                    paymentVoucher.getChequeDate(),
                    depositSlip.getDepositNumber(),
                    paymentVoucher.getGeneratedMove()));

    Set<BankDetails> bankDetailsCollection =
        paymentVouchers.stream()
            .map(PaymentVoucher::getCompanyBankDetails)
            .collect(Collectors.toSet());

    for (BankDetails bankDetails : bankDetailsCollection) {
      publish(depositSlip, bankDetails);
    }

    LocalDate date = Beans.get(AppBaseService.class).getTodayDate(depositSlip.getCompany());
    depositSlip.setPublicationDate(date);

    return date;
  }

  protected void publish(DepositSlip depositSlip, BankDetails bankDetails) throws AxelorException {

    String filename = getFilename(depositSlip, bankDetails);

    deleteExistingPublishDmsFile(depositSlip, filename);

    ReportSettings settings = ReportFactory.createReport(getReportName(depositSlip), filename);
    settings.addParam("DepositSlipId", depositSlip.getId());
    settings.addParam("BankDetailsId", bankDetails.getId());
    settings.addParam("Locale", ReportSettings.getPrintingLocale(null));
    settings.addParam(
        "Timezone",
        depositSlip.getCompany() != null ? depositSlip.getCompany().getTimezone() : null);
    settings.addFormat("pdf");
    settings.toAttach(depositSlip).generate();
  }

  protected void deleteExistingPublishDmsFile(DepositSlip depositSlip, String filename) {

    MetaFiles metaFiles = Beans.get(MetaFiles.class);

    Query.of(DMSFile.class)
        .filter("self.relatedId = :id AND self.relatedModel = :model and self.fileName = :filename")
        .bind("id", depositSlip.getId())
        .bind("model", depositSlip.getClass().getName())
        .bind("filename", filename + ".pdf")
        .fetchStream()
        .forEach(dmsFile -> metaFiles.delete(dmsFile));
  }

  public String getFilename(DepositSlip depositSlip, BankDetails bankDetails)
      throws AxelorException {

    StringBuilder stringBuilder = new StringBuilder(depositSlip.getDepositNumber());
    stringBuilder = stringBuilder.append('-').append(bankDetails.getBankCode());
    stringBuilder = stringBuilder.append('-').append(bankDetails.getAccountNbr());

    return stringBuilder.toString();
  }

  protected String getReportName(DepositSlip depositSlip) throws AxelorException {
    switch (depositSlip.getPaymentModeTypeSelect()) {
      case PaymentModeRepository.TYPE_CHEQUE:
        return IReport.CHEQUE_DEPOSIT_SLIP;
      case PaymentModeRepository.TYPE_CASH:
        return IReport.CASH_DEPOSIT_SLIP;
      default:
        throw new AxelorException(
            depositSlip,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            AccountExceptionMessage.DEPOSIT_SLIP_UNSUPPORTED_PAYMENT_MODE_TYPE);
    }
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
          "self.companyBankDetails = :companyBankDetails AND ( self.bankEntryGenWithoutValEntryCollectionOk is true OR self.paymentMode.accountingTriggerSelect = :accountingTriggerSelect)");
      queryBuilder.bind("companyBankDetails", depositSlip.getCompanyBankDetails());
      queryBuilder.bind(
          "accountingTriggerSelect", PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE);
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
  public List<Integer> getSelectedPaymentVoucherDueIdList(
      List<Map<String, Object>> paymentVoucherDueList) {
    return paymentVoucherDueList.stream()
        .filter(o -> (Boolean) o.get("selected"))
        .map(o -> (Integer) o.get("id"))
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
