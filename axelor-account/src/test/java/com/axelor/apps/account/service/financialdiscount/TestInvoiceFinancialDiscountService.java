package com.axelor.apps.account.service.financialdiscount;

import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.repo.FinancialDiscountRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentConditionRepository;
import com.axelor.apps.account.module.AccountTest;
import com.axelor.apps.account.service.invoice.InvoiceFinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.meta.loader.LoaderHelper;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletScopes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestInvoiceFinancialDiscountService extends AccountTest {

  protected final InvoiceRepository invoiceRepository;
  protected final FinancialDiscountRepository financialDiscountRepository;
  protected final PartnerRepository partnerRepository;
  protected final PaymentConditionRepository paymentConditionRepository;
  protected final CompanyRepository companyRepository;
  protected final InvoiceFinancialDiscountService invoiceFinancialDiscountService;
  protected final InvoiceTermService invoiceTermService;
  protected static final LoaderHelper loaderHelper = Beans.get(LoaderHelper.class);

  @Inject
  public TestInvoiceFinancialDiscountService(
      InvoiceRepository invoiceRepository,
      FinancialDiscountRepository financialDiscountRepository,
      PaymentConditionRepository paymentConditionRepository,
      CompanyRepository companyRepository) {
    this.invoiceRepository = invoiceRepository;
    this.financialDiscountRepository = financialDiscountRepository;
    this.paymentConditionRepository = paymentConditionRepository;
    this.companyRepository = companyRepository;

    RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());
    try (RequestScoper.CloseableScope ignored = scope.open()) {
      this.invoiceFinancialDiscountService = Beans.get(InvoiceFinancialDiscountService.class);
      this.partnerRepository = Beans.get(PartnerRepository.class);
      this.invoiceTermService = Beans.get(InvoiceTermService.class);
    }
  }

  @BeforeAll
  static void setUp() {
    loaderHelper.importCsv("data/account-config-input.xml");
  }

  protected Invoice prepareInvoice(Long paymentConditionId) throws AxelorException {
    Invoice invoice = new Invoice();
    invoice.setInvoiceDate(LocalDate.of(2024, 1, 1));
    invoice.setDueDate(LocalDate.of(2024, 1, 15));
    invoice.setExTaxTotal(new BigDecimal(100));
    invoice.setInTaxTotal(new BigDecimal(100));
    invoice.setAmountRemaining(new BigDecimal(100));
    invoice.setOperationTypeSelect(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE);
    invoice.setOperationSubTypeSelect(InvoiceRepository.OPERATION_SUB_TYPE_DEFAULT);
    invoice.setPaymentCondition(
        paymentConditionRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", paymentConditionId)
            .fetchOne());
    /*invoice.setCompany(companyRepository.all()
    .filter("self.importId = :importId")
    .bind("importId", 1L)
    .fetchOne());*/
    invoice.setStatusSelect(InvoiceRepository.STATUS_DRAFT);
    invoice.setInvoiceTermList(new ArrayList<>());
    invoice = invoiceTermService.computeInvoiceTerms(invoice);
    return invoice;
  }

  @Test
  void testSetFinancialDiscountInformation() throws AxelorException {
    Invoice invoiceMonoInvoiceTerm = prepareInvoice(1L);
    Invoice invoiceMultiInvoiceTerm = prepareInvoice(1000L);

    testSetFinancialDiscountInformationOnInvoice(invoiceMonoInvoiceTerm);
    testSetFinancialDiscountInformationOnInvoice(invoiceMultiInvoiceTerm);
  }

  protected void testSetFinancialDiscountInformationOnInvoice(Invoice invoice) {
    verifyFinancialDiscountValuesOnInvoice(invoice, BigDecimal.ZERO, null);

    // Verify values for WT financial discounts
    verifyFinancialDiscountValuesOnInvoice(invoice, BigDecimal.valueOf(0.02), 1L);
    verifyFinancialDiscountValuesOnInvoice(invoice, BigDecimal.valueOf(0.03), 2L);

    // Verify values for ATI financial discounts
    verifyFinancialDiscountValuesOnInvoice(invoice, BigDecimal.valueOf(0.02), 100L);
    verifyFinancialDiscountValuesOnInvoice(invoice, BigDecimal.valueOf(0.03), 101L);

    // Verify values without financial discounts
    verifyFinancialDiscountValuesOnInvoice(invoice, BigDecimal.ZERO, null);
  }

  protected void verifyFinancialDiscountValuesOnInvoice(
      Invoice invoice, BigDecimal rate, Long financialDiscountId) {
    invoice = computeFinancialDiscount(invoice, financialDiscountId);

    Assertions.assertEquals(
        rate.multiply(new BigDecimal(100)).intValue(),
        invoice.getFinancialDiscountRate().intValue(),
        "Financial discount rate");
    Assertions.assertEquals(
        invoice.getExTaxTotal().multiply(rate).intValue(),
        invoice.getFinancialDiscountTotalAmount().intValue(),
        "Financial discount total amount");

    if (rate.signum() == 0) {
      Assertions.assertEquals(
          0,
          invoice.getRemainingAmountAfterFinDiscount().intValue(),
          "Remaining amount after fin discount");
      Assertions.assertNull(invoice.getLegalNotice(), "Legal notice");
    } else {
      Assertions.assertEquals(
          invoice.getExTaxTotal().multiply(BigDecimal.ONE.subtract(rate)).intValue(),
          invoice.getRemainingAmountAfterFinDiscount().intValue(),
          "Remaining amount after fin discount");
      Assertions.assertNotNull(invoice.getLegalNotice(), "Legal notice");
    }
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

  @Test
  void testUpdateFinancialDiscount() throws AxelorException {
    Invoice invoiceMonoInvoiceTerm = prepareInvoice(1L);
    Invoice invoiceMultiInvoiceTerm = prepareInvoice(1000L);

    testUpdateFinancialDiscountOnInvoiceTermList(invoiceMonoInvoiceTerm);
    testUpdateFinancialDiscountOnInvoiceTermList(invoiceMultiInvoiceTerm);
  }

  protected void testUpdateFinancialDiscountOnInvoiceTermList(Invoice invoice) {
    verifyFinancialDiscountValuesOnInvoiceTerm(invoice, BigDecimal.ZERO, null);

    // Verify values for WT financial discounts
    verifyFinancialDiscountValuesOnInvoiceTerm(invoice, BigDecimal.valueOf(0.02), 1L);
    verifyFinancialDiscountValuesOnInvoiceTerm(invoice, BigDecimal.valueOf(0.03), 2L);

    // Verify values for ATI financial discounts
    verifyFinancialDiscountValuesOnInvoiceTerm(invoice, BigDecimal.valueOf(0.02), 100L);
    verifyFinancialDiscountValuesOnInvoiceTerm(invoice, BigDecimal.valueOf(0.03), 101L);

    // Verify values without financial discounts
    verifyFinancialDiscountValuesOnInvoiceTerm(invoice, BigDecimal.ZERO, null);
  }

  protected void verifyFinancialDiscountValuesOnInvoiceTerm(
      Invoice invoice, BigDecimal rate, Long financialDiscountId) {
    invoice = computeFinancialDiscount(invoice, financialDiscountId);

    Map<String, BigDecimal> amountByField = updateFinancialDiscount(invoice, financialDiscountId);

    Assertions.assertEquals(
        amountByField.get("financialDiscountAmount").intValue(),
        invoice.getExTaxTotal().multiply(rate).intValue(),
        "Financial discount amount");

    if (rate.signum() == 0) {
      Assertions.assertEquals(
          0,
          amountByField.get("remainingAmountAfterFinDiscount").intValue(),
          "Remaining amount after fin discount");
      Assertions.assertEquals(
          0,
          amountByField.get("amountRemainingAfterFinDiscount").intValue(),
          "Amount remaining after fin discount");
      Assertions.assertTrue(
          amountByField.get("applyFinancialDiscount").signum() == 0, "Apply Financial discount");
    } else {
      Assertions.assertEquals(
          invoice.getExTaxTotal().multiply(BigDecimal.ONE.subtract(rate)).intValue(),
          amountByField.get("remainingAmountAfterFinDiscount").intValue(),
          "Remaining amount after fin discount");
      Assertions.assertEquals(
          invoice.getExTaxTotal().multiply(BigDecimal.ONE.subtract(rate)).intValue(),
          amountByField.get("amountRemainingAfterFinDiscount").intValue(),
          "Amount remaining after fin discount");
      Assertions.assertTrue(
          amountByField.get("applyFinancialDiscount").signum() > 0, "Apply Financial discount");
    }
  }

  protected Map<String, BigDecimal> updateFinancialDiscount(
      Invoice invoice, Long financialDiscountId) {
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
    List<InvoiceTerm> invoiceTermList =
        invoiceFinancialDiscountService.updateFinancialDiscount(invoice);

    Map<String, BigDecimal> amountByField = new HashMap<>();
    amountByField.put("applyFinancialDiscount", BigDecimal.ONE);
    amountByField.put("financialDiscountAmount", BigDecimal.ZERO);
    amountByField.put("remainingAmountAfterFinDiscount", BigDecimal.ZERO);
    amountByField.put("amountRemainingAfterFinDiscount", BigDecimal.ZERO);
    if (!ObjectUtils.isEmpty(invoiceTermList)) {
      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        if (!invoiceTerm.getApplyFinancialDiscount()) {
          amountByField.replace("applyFinancialDiscount", BigDecimal.ZERO);
        }
        amountByField.replace(
            "financialDiscountAmount",
            amountByField
                .get("financialDiscountAmount")
                .add(invoiceTerm.getFinancialDiscountAmount()));
        amountByField.replace(
            "remainingAmountAfterFinDiscount",
            amountByField
                .get("remainingAmountAfterFinDiscount")
                .add(invoiceTerm.getRemainingAmountAfterFinDiscount()));
        amountByField.replace(
            "amountRemainingAfterFinDiscount",
            amountByField
                .get("amountRemainingAfterFinDiscount")
                .add(invoiceTerm.getAmountRemainingAfterFinDiscount()));
      }
    }

    return amountByField;
  }
}
