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

import com.axelor.apps.account.db.InterestRateHistoryLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppAccount;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LatePaymentInterestInvoiceServiceImpl implements LatePaymentInterestInvoiceService {

  protected AppAccountService appAccountService;
  protected InvoiceRepository invoiceRepository;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public LatePaymentInterestInvoiceServiceImpl(
      AppAccountService appAccountService,
      InvoiceRepository invoiceRepository,
      CurrencyScaleService currencyScaleService) {
    this.appAccountService = appAccountService;
    this.invoiceRepository = invoiceRepository;
    this.currencyScaleService = currencyScaleService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public Invoice generateLatePaymentInterestInvoice(Invoice invoice) throws AxelorException {

    List<InvoiceTerm> lateInvoiceTerms =
        invoice.getInvoiceTermList().stream()
            .filter(
                invoiceTerm ->
                    invoiceTerm
                            .getDueDate()
                            .isBefore(appAccountService.getTodayDate(invoice.getCompany()))
                        && !invoiceTerm.getIsPaid()
                        && !invoiceTerm.getIsHoldBack())
            .collect(Collectors.toList());

    if (lateInvoiceTerms.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.LATE_PAYMENT_INTEREST_INVOICE_NO_LATE));
    }

    BigDecimal remainingAmount =
        lateInvoiceTerms.stream()
            .map(InvoiceTerm::getAmountRemaining)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (remainingAmount.compareTo(appAccountService.getAppAccount().getThresholdAmount()) < 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.LATE_PAYMENT_INTEREST_BELOW_THRESHOLD));
    }

    InvoiceGenerator invoiceGenerator = getLatePaymentInterestInvoiceGenerator(invoice);

    Invoice latePaymentInvoice = invoiceGenerator.generate();
    latePaymentInvoice.setOperationSubTypeSelect(InvoiceRepository.OPERATION_SUB_TYPE_LATE_PAYMENT);
    latePaymentInvoice.setInvoiceDate(appAccountService.getTodayDate(invoice.getCompany()));
    List<InvoiceLine> invoiceLines = new ArrayList<>();

    invoiceLines.add(createFlatFeeInvoiceLine(invoice));
    invoiceLines.add(createInvoiceLineFromInvoiceTerm(latePaymentInvoice, lateInvoiceTerms));

    invoiceGenerator.populate(latePaymentInvoice, invoiceLines);
    latePaymentInvoice.setLatePaymentInterestSourceInvoice(invoice);
    invoiceRepository.save(latePaymentInvoice);

    return latePaymentInvoice;
  }

  protected InvoiceLine createFlatFeeInvoiceLine(Invoice invoice) throws AxelorException {
    AppAccount appAccount = appAccountService.getAppAccount();
    BigDecimal flatFeeAmount = appAccount.getLatePaymentInterestFlatFee();
    Product flatFeeProduct = appAccount.getFlatFeeProduct();

    if (flatFeeProduct == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.LATE_PAYMENT_INTEREST_FLAT_FEE_NO_PRODUCT));
    }

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            flatFeeProduct,
            flatFeeProduct.getName(),
            flatFeeAmount,
            flatFeeAmount,
            flatFeeAmount,
            null,
            BigDecimal.ONE,
            null,
            null,
            InvoiceLineGenerator.DEFAULT_SEQUENCE,
            BigDecimal.ZERO,
            PriceListLineRepository.AMOUNT_TYPE_NONE,
            flatFeeAmount,
            null,
            false) {
          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();

            List<InvoiceLine> invoiceLines = new ArrayList<>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates().get(0);
  }

  protected InvoiceLine createInvoiceLineFromInvoiceTerm(
      Invoice invoice, List<InvoiceTerm> invoiceTermList) throws AxelorException {

    Product latePaymentInterestProduct =
        appAccountService.getAppAccount().getLatePaymentInterestProduct();

    if (latePaymentInterestProduct == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.LATE_PAYMENT_INTEREST_NO_PRODUCT));
    }

    BigDecimal latePaymentInterest = computeLatePaymentInterest(invoiceTermList);

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            latePaymentInterestProduct,
            latePaymentInterestProduct.getName(),
            latePaymentInterest,
            latePaymentInterest,
            latePaymentInterest,
            null,
            BigDecimal.ONE,
            null,
            null,
            InvoiceLineGenerator.DEFAULT_SEQUENCE,
            BigDecimal.ZERO,
            PriceListLineRepository.AMOUNT_TYPE_NONE,
            latePaymentInterest,
            null,
            false) {
          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();

            List<InvoiceLine> invoiceLines = new ArrayList<>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates().get(0);
  }

  protected InvoiceGenerator getLatePaymentInterestInvoiceGenerator(Invoice invoice)
      throws AxelorException {
    return new InvoiceGenerator(
        InvoiceRepository.OPERATION_TYPE_CLIENT_SALE,
        invoice.getCompany(),
        invoice.getPaymentCondition(),
        invoice.getPaymentMode(),
        invoice.getAddress(),
        invoice.getPartner(),
        invoice.getContactPartner(),
        invoice.getCurrency(),
        invoice.getPriceList(),
        null,
        null,
        null,
        null,
        invoice.getTradingName(),
        null,
        null) {
      @Override
      public Invoice generate() throws AxelorException {
        return super.createInvoiceHeader();
      }
    };
  }

  protected BigDecimal computeLatePaymentInterest(List<InvoiceTerm> invoiceTermList)
      throws AxelorException {
    BigDecimal latePaymentAmount = BigDecimal.ZERO;
    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      latePaymentAmount = latePaymentAmount.add(computeInterestFromInvoiceTerm(invoiceTerm));
    }
    return latePaymentAmount;
  }

  protected BigDecimal computeInterestFromInvoiceTerm(InvoiceTerm invoiceTerm)
      throws AxelorException {
    PaymentMode paymentMode = invoiceTerm.getPaymentMode();
    Invoice invoice = invoiceTerm.getInvoice();
    if (paymentMode == null || paymentMode.getInterestRate() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.LATE_PAYMENT_INTEREST_NO_PAYMENT_MODE_RATE));
    }

    int currencyScale = currencyScaleService.getCurrencyScale(invoiceTerm.getCurrency());
    LocalDate dueDate = invoiceTerm.getDueDate();
    LocalDate startInterestDate = invoiceTerm.getDueDate().plusDays(1);
    BigDecimal interestAmount = BigDecimal.ZERO;

    List<InterestRateHistoryLine> periodHistoryLinesFiltered =
        getPeriodHistoryLine(paymentMode, dueDate);
    BigDecimal latePaymentAmount =
        invoice
            .getExTaxTotal()
            .multiply(invoiceTerm.getPercentage())
            .divide(new BigDecimal("100"))
            .multiply(invoiceTerm.getAmountRemaining())
            .divide(invoiceTerm.getAmount(), currencyScale, RoundingMode.HALF_UP);

    for (InterestRateHistoryLine interestRateHistoryLine : periodHistoryLinesFiltered) {
      BigDecimal interestRate =
          interestRateHistoryLine.getInterestRate().divide(new BigDecimal("100"));
      LocalDate fromDate = interestRateHistoryLine.getFromDate();
      if (fromDate.isBefore(startInterestDate)) {
        fromDate = startInterestDate;
      }
      LocalDate endDate = interestRateHistoryLine.getEndDate();
      long daysBetween = LocalDateHelper.daysBetween(fromDate, endDate, false);

      interestAmount =
          interestAmount.add(
              latePaymentAmount.multiply(interestRate).multiply(BigDecimal.valueOf(daysBetween)));
    }

    LocalDate currentPeriodFromDate;

    // if there is a history, use last period ennDate + 1
    if (!periodHistoryLinesFiltered.isEmpty()) {
      LocalDate lastPeriodEndDate =
          Collections.max(
                  periodHistoryLinesFiltered,
                  Comparator.comparing(InterestRateHistoryLine::getEndDate))
              .getEndDate();
      currentPeriodFromDate = lastPeriodEndDate.plusDays(1);
    } else {
      // if no history, use the dueDate
      currentPeriodFromDate = startInterestDate;
    }

    // add current periodRate
    BigDecimal interestRate = paymentMode.getInterestRate().divide(new BigDecimal("100"));

    long lastPeriod =
        LocalDateHelper.daysBetween(
            currentPeriodFromDate,
            appAccountService.getTodayDate(
                Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null)),
            false);
    interestAmount =
        interestAmount
            .add(latePaymentAmount.multiply(interestRate).multiply(BigDecimal.valueOf(lastPeriod)))
            .divide(new BigDecimal("365"), currencyScale, RoundingMode.HALF_UP);

    return interestAmount;
  }

  protected List<InterestRateHistoryLine> getPeriodHistoryLine(
      PaymentMode paymentMode, LocalDate dueDate) {
    return paymentMode.getInterestRateHistoryLineList().stream()
        .filter(
            interestRateHistoryLine ->
                LocalDateHelper.isBetween(
                        interestRateHistoryLine.getFromDate(),
                        interestRateHistoryLine.getEndDate(),
                        dueDate)
                    || interestRateHistoryLine.getFromDate().isAfter(dueDate))
        .sorted((line1, line2) -> line1.getFromDate().compareTo(line2.getEndDate()))
        .collect(Collectors.toList());
  }
}
