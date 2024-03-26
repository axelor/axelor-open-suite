package com.axelor.apps.account.service.financialdiscount;

import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.FinancialDiscountRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.TaxLineRepository;
import com.axelor.apps.account.service.invoice.InvoiceFinancialDiscountService;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.UnitRepository;
import com.axelor.inject.Beans;
import com.axelor.meta.loader.LoaderHelper;
import com.axelor.utils.junit.BaseTest;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletScopes;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestInvoiceFinancialDiscountComputation extends BaseTest {

  protected final LoaderHelper loaderHelper;
  protected final InvoiceRepository invoiceRepository;
  protected final FinancialDiscountRepository financialDiscountRepository;
  protected final PartnerRepository partnerRepository;
  protected final CompanyRepository companyRepository = null;
  protected final AddressRepository addressRepository = null;
  protected final ProductRepository productRepository = null;
  protected final TaxLineRepository taxLineRepository = null;
  protected final UnitRepository unitRepository = null;
  protected final InvoiceFinancialDiscountService invoiceFinancialDiscountService;

  @Inject
  public TestInvoiceFinancialDiscountComputation(
      LoaderHelper loaderHelper,
      InvoiceRepository invoiceRepository,
      FinancialDiscountRepository financialDiscountRepository) {
    this.loaderHelper = loaderHelper;
    this.invoiceRepository = invoiceRepository;
    this.financialDiscountRepository = financialDiscountRepository;
    RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());
    try (RequestScoper.CloseableScope ignored = scope.open()) {
      this.invoiceFinancialDiscountService = Beans.get(InvoiceFinancialDiscountService.class);
      this.partnerRepository = Beans.get(PartnerRepository.class);
    }
  }

  @BeforeEach
  void setUp() {
    loaderHelper.importCsv("data/base-config-input.xml");
    loaderHelper.importCsv("data/account-config-input.xml");
    loaderHelper.importCsv("data/account-invoice-input.xml");
    loaderHelper.importCsv("data/account-invoice-line-input.xml");
  }

  @Test
  void testComputeFinancialDiscount2PercentWT() {
    // Invoice invoice = this.computeInvoiceData(1L);

    // invoiceFinancialDiscountService.setFinancialDiscountInformations(invoice);

    // this.checkResult(invoice);
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

  @Test
  void testComputeFinancialDiscount3PercentATI() {
    //    Invoice invoice = this.computeInvoiceData(101L);
    //
    //    invoiceFinancialDiscountService.setFinancialDiscountInformations(invoice);
  }

  protected Invoice computeInvoiceData(Long financialDiscountId) {
    FinancialDiscount financialDiscount =
        financialDiscountRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", financialDiscountId)
            .fetchOne();
    Invoice invoice =
        invoiceRepository.all().filter("self.importId = :importId").bind("importId", 1L).fetchOne();

    invoice.setFinancialDiscount(financialDiscount);
    return invoice;
  }

  protected void checkResult(Invoice invoice) {
    Assertions.assertEquals(BigDecimal.ZERO, invoice.getFinancialDiscountTotalAmount());
    Assertions.assertEquals(BigDecimal.ZERO, invoice.getRemainingAmountAfterFinDiscount());

    //        getFinancialDiscountDeadlineDate();
    //        setRemainingAmountAfterFinDiscount();
    //        setFinancialDiscountTotalAmount();
    //        setFinancialDiscountRate();
    //        setLegalNotice();
  }
}
