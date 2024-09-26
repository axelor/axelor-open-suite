package com.axelor.apps.account.service.invoice;

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
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppAccount;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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
                        && !invoiceTerm.getIsPaid())
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

    if (paymentMode == null || paymentMode.getInterestRate() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.LATE_PAYMENT_INTEREST_NO_PAYMENT_MODE_RATE));
    }
    BigDecimal interestRate = paymentMode.getInterestRate().divide(new BigDecimal("100"));

    int currencyScale = currencyScaleService.getCurrencyScale(invoiceTerm.getCurrency());

    return invoiceTerm
        .getAmountRemaining()
        .multiply(interestRate)
        .multiply(new BigDecimal(String.valueOf(numberOfDaySinceDueDate(invoiceTerm.getDueDate()))))
        .divide(new BigDecimal("365"), currencyScale, RoundingMode.HALF_UP);
  }

  protected long numberOfDaySinceDueDate(LocalDate dueDate) {
    return ChronoUnit.DAYS.between(dueDate, appAccountService.getTodayDate(null));
  }
}
