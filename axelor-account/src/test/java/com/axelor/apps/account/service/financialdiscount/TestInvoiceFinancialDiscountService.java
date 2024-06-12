package com.axelor.apps.account.service.financialdiscount;

import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.FinancialDiscountRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.module.AccountTest;
import com.axelor.apps.account.service.invoice.InvoiceFinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.inject.Beans;
import com.axelor.meta.loader.LoaderHelper;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletScopes;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestInvoiceFinancialDiscountService extends AccountTest {

  protected final InvoiceRepository invoiceRepository;
  protected final FinancialDiscountRepository financialDiscountRepository;
  protected final PartnerRepository partnerRepository;
  protected final InvoiceFinancialDiscountService invoiceFinancialDiscountService;
  protected final InvoiceTermService invoiceTermService;
  protected static final LoaderHelper loaderHelper = Beans.get(LoaderHelper.class);
  protected final Integer currencyScale = 2;

  @Inject
  public TestInvoiceFinancialDiscountService(
      InvoiceRepository invoiceRepository,
      FinancialDiscountRepository financialDiscountRepository) {
    this.invoiceRepository = invoiceRepository;
    this.financialDiscountRepository = financialDiscountRepository;

    RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());
    try (RequestScoper.CloseableScope ignored = scope.open()) {
      this.invoiceFinancialDiscountService = Beans.get(InvoiceFinancialDiscountService.class);
      this.partnerRepository = Beans.get(PartnerRepository.class);
      this.invoiceTermService = Beans.get(InvoiceTermService.class);
    }
  }

  @BeforeAll
  static void setUp() {
    loaderHelper.importCsv("data/base-config-input.xml");
    loaderHelper.importCsv("data/account-config-input.xml");
    // loaderHelper.importCsv("data/account-invoice-input.xml");
    // loaderHelper.importCsv("data/account-invoice-line-input.xml");
  }

  protected Invoice prepareInvoice(Long importId) throws AxelorException {
    Invoice invoice =
        Beans.get(InvoiceRepository.class)
            .all()
            .filter("self.importId = :importId")
            .bind("importId", importId)
            .fetchOne();
    invoice.setStatusSelect(InvoiceRepository.STATUS_DRAFT);
    invoice.setInvoiceTermList(new ArrayList<>());
    invoice = invoiceTermService.computeInvoiceTerms(invoice);
    return invoice;
  }

  @Test
  void testSetFinancialDiscountInformation() throws AxelorException {
    Invoice invoiceMonoInvoiceTerm = prepareInvoice(1L);
    Invoice invoiceMultiInvoiceTerm = prepareInvoice(2L);

    testSetFinancialDiscountInformationsOnInvoice(invoiceMonoInvoiceTerm);
    testSetFinancialDiscountInformationsOnInvoice(invoiceMultiInvoiceTerm);
  }

  protected void testSetFinancialDiscountInformationsOnInvoice(Invoice invoice) {
    verifyFinancialDiscountValuesOnInvoice(invoice, BigDecimal.ZERO, null);
    verifyFinancialDiscountValuesOnInvoice(invoice, BigDecimal.valueOf(0.02), 1L);
    verifyFinancialDiscountValuesOnInvoice(invoice, BigDecimal.valueOf(0.03), 2L);
    verifyFinancialDiscountValuesOnInvoice(invoice, BigDecimal.valueOf(0.02), 100L);
    verifyFinancialDiscountValuesOnInvoice(invoice, BigDecimal.valueOf(0.03), 101L);
    verifyFinancialDiscountValuesOnInvoice(invoice, BigDecimal.ZERO, null);
  }

  @Test
  void testComputeFinancialDiscount3PercentWT() {
    //    Invoice invoice = this.computeInvoiceData(2L);
    //
    //    invoiceFinancialDiscountService.setFinancialDiscountInformations(invoice);
  }

  @Test
  void testComputeFinancialDiscount2PercentATI() {
    //    Invoice invoice = this.computeInvoiceData(100L);
    //
    //    invoiceFinancialDiscountService.setFinancialDiscountInformations(invoice);
  }

  protected Invoice computeFinancialDiscount(Invoice invoice, Long financialDiscountId) {
    FinancialDiscount financialDiscount = null;
    if (financialDiscountId != null) {
      financialDiscount =
          financialDiscountRepository
              .all()
              .filter("self.importId = :importId")
              .bind("importId", financialDiscountId)
              .fetchOne();
    }
    invoice.setFinancialDiscount(financialDiscount);
    invoiceFinancialDiscountService.setFinancialDiscountInformations(invoice);
    return invoice;
  }

  protected void verifyFinancialDiscountValuesOnInvoice(
      Invoice invoice, BigDecimal rate, Long financialDiscountId) {
    invoice = computeFinancialDiscount(invoice, financialDiscountId);

    Assertions.assertEquals(
        invoice.getFinancialDiscountRate().intValue(),
        rate.multiply(new BigDecimal(100)).intValue());
    Assertions.assertEquals(
        invoice.getFinancialDiscountTotalAmount().intValue(),
        invoice.getExTaxTotal().multiply(rate).intValue());

    if (rate.signum() == 0) {
      Assertions.assertEquals(
          invoice.getRemainingAmountAfterFinDiscount().intValue(), BigDecimal.ZERO.intValue());
      Assertions.assertNull(invoice.getLegalNotice());
    } else {
      Assertions.assertEquals(
          invoice.getRemainingAmountAfterFinDiscount().intValue(),
          invoice.getExTaxTotal().multiply(BigDecimal.ONE.subtract(rate)).intValue());
      Assertions.assertNotNull(invoice.getLegalNotice());
    }
  }
}
