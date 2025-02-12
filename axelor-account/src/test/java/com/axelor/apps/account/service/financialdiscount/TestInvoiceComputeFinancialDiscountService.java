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
package com.axelor.apps.account.service.financialdiscount;

import com.axelor.apps.account.db.repo.FinancialDiscountRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentConditionRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.inject.Beans;
import com.axelor.meta.loader.LoaderHelper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestInvoiceComputeFinancialDiscountService extends TestInvoiceFinancialDiscountService {

  @Inject
  public TestInvoiceComputeFinancialDiscountService(
      InvoiceRepository invoiceRepository,
      FinancialDiscountRepository financialDiscountRepository,
      PaymentConditionRepository paymentConditionRepository,
      CompanyRepository companyRepository) {
    super(
        invoiceRepository,
        financialDiscountRepository,
        paymentConditionRepository,
        companyRepository);
  }

  @BeforeAll
  static void setUp() {
    LoaderHelper loaderHelper = Beans.get(LoaderHelper.class);
    loaderHelper.importCsv("data/account-config-input.xml");
  }

  // TEST : MONO INVOICE TERM WITHOUT FINANCIAL DISCOUNT

  @Test
  void testComputeFinancialDiscountMonoInvoiceTermWithoutDiscount_FinancialDiscountRate()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.ZERO, null, 1L);
    thenVerifyFinancialDiscountRate();
  }

  @Test
  void testComputeFinancialDiscountMonoInvoiceTermWithoutDiscount_FinancialDiscountTotalAmount()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.ZERO, null, 1L);
    thenVerifyFinancialDiscountTotalAmount();
  }

  @Test
  void testComputeFinancialDiscountMonoInvoiceTermWithoutDiscount_RemainingAmountAfterFinDiscount()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.ZERO, null, 1L);
    thenVerifyRemainingAmountAfterFinDiscountWithoutRate();
  }

  @Test
  void testComputeFinancialDiscountMonoInvoiceTermWithoutDiscount_LegalNotice()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.ZERO, null, 1L);
    thenVerifyLegalNoticeWithoutRate();
  }

  // TEST : MONO INVOICE TERM WITH 2% WT FINANCIAL DISCOUNT

  @Test
  void testComputeFinancialDiscountMonoInvoiceTermWithDiscount_2WT_FinancialDiscountRate()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.02), 1L, 1L);
    thenVerifyFinancialDiscountRate();
  }

  @Test
  void testComputeFinancialDiscountMonoInvoiceTermWithDiscount_2WT_FinancialDiscountTotalAmount()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.02), 1L, 1L);
    thenVerifyFinancialDiscountTotalAmount();
  }

  @Test
  void testComputeFinancialDiscountMonoInvoiceTermWithDiscount_2WT_RemainingAmountAfterFinDiscount()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.02), 1L, 1L);
    thenVerifyRemainingAmountAfterFinDiscountWithRate();
  }

  @Test
  void testComputeFinancialDiscountMonoInvoiceTermWithDiscount_2WT_LegalNotice()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.02), 1L, 1L);
    thenVerifyLegalNoticeWithRate();
  }

  // TEST : MONO INVOICE TERM WITH 3% WT FINANCIAL DISCOUNT

  @Test
  void testComputeFinancialDiscountMonoInvoiceTermWithDiscount_3WT_FinancialDiscountRate()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.03), 2L, 1L);
    thenVerifyFinancialDiscountRate();
  }

  @Test
  void testComputeFinancialDiscountMonoInvoiceTermWithDiscount_3WT_FinancialDiscountTotalAmount()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.03), 2L, 1L);
    thenVerifyFinancialDiscountTotalAmount();
  }

  @Test
  void testComputeFinancialDiscountMonoInvoiceTermWithDiscount_3WT_RemainingAmountAfterFinDiscount()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.03), 2L, 1L);
    thenVerifyRemainingAmountAfterFinDiscountWithRate();
  }

  @Test
  void testComputeFinancialDiscountMonoInvoiceTermWithDiscount_3WT_LegalNotice()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.03), 2L, 1L);
    thenVerifyLegalNoticeWithRate();
  }

  // TEST : MONO INVOICE TERM WITH 2% ATI FINANCIAL DISCOUNT

  @Test
  void testComputeFinancialDiscountMonoInvoiceTermWithDiscount_2ATI_FinancialDiscountRate()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.02), 100L, 1L);
    thenVerifyFinancialDiscountRate();
  }

  @Test
  void testComputeFinancialDiscountMonoInvoiceTermWithDiscount_2ATI_FinancialDiscountTotalAmount()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.02), 100L, 1L);
    thenVerifyFinancialDiscountTotalAmount();
  }

  @Test
  void
      testComputeFinancialDiscountMonoInvoiceTermWithDiscount_2ATI_RemainingAmountAfterFinDiscount()
          throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.02), 100L, 1L);
    thenVerifyRemainingAmountAfterFinDiscountWithRate();
  }

  @Test
  void testComputeFinancialDiscountMonoInvoiceTermWithDiscount_2ATI_LegalNotice()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.02), 100L, 1L);
    thenVerifyLegalNoticeWithRate();
  }

  // TEST : MONO INVOICE TERM WITH 3% ATI FINANCIAL DISCOUNT

  @Test
  void testComputeFinancialDiscountMonoInvoiceTermWithDiscount_3ATI_FinancialDiscountRate()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.03), 101L, 1L);
    thenVerifyFinancialDiscountRate();
  }

  @Test
  void testComputeFinancialDiscountMonoInvoiceTermWithDiscount_3ATI_FinancialDiscountTotalAmount()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.03), 101L, 1L);
    thenVerifyFinancialDiscountTotalAmount();
  }

  @Test
  void
      testComputeFinancialDiscountMonoInvoiceTermWithDiscount_3ATI_RemainingAmountAfterFinDiscount()
          throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.03), 101L, 1L);
    thenVerifyRemainingAmountAfterFinDiscountWithRate();
  }

  @Test
  void testComputeFinancialDiscountMonoInvoiceTermWithDiscount_3ATI_LegalNotice()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.03), 101L, 1L);
    thenVerifyLegalNoticeWithRate();
  }

  // TEST : MULTI INVOICE TERM WITHOUT FINANCIAL DISCOUNT

  @Test
  void testComputeFinancialDiscountMultiInvoiceTermWithoutDiscount_FinancialDiscountRate()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.ZERO, null, 1000L);
    thenVerifyFinancialDiscountRate();
  }

  @Test
  void testComputeFinancialDiscountMultiInvoiceTermWithoutDiscount_FinancialDiscountTotalAmount()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.ZERO, null, 1000L);
    thenVerifyFinancialDiscountTotalAmount();
  }

  @Test
  void testComputeFinancialDiscountMultiInvoiceTermWithoutDiscount_RemainingAmountAfterFinDiscount()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.ZERO, null, 1000L);
    thenVerifyRemainingAmountAfterFinDiscountWithoutRate();
  }

  @Test
  void testComputeFinancialDiscountMultiInvoiceTermWithoutDiscount_LegalNotice()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.ZERO, null, 1000L);
    thenVerifyLegalNoticeWithoutRate();
  }

  // TEST : MULTI INVOICE TERM WITH 2% WT FINANCIAL DISCOUNT

  @Test
  void testComputeFinancialDiscountMultiInvoiceTermWithDiscount_2WT_FinancialDiscountRate()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.02), 1L, 1000L);
    thenVerifyFinancialDiscountRate();
  }

  @Test
  void testComputeFinancialDiscountMultiInvoiceTermWithDiscount_2WT_FinancialDiscountTotalAmount()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.02), 1L, 1000L);
    thenVerifyFinancialDiscountTotalAmount();
  }

  @Test
  void
      testComputeFinancialDiscountMultiInvoiceTermWithDiscount_2WT_RemainingAmountAfterFinDiscount()
          throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.02), 1L, 1000L);
    thenVerifyRemainingAmountAfterFinDiscountWithRate();
  }

  @Test
  void testComputeFinancialDiscountMultiInvoiceTermWithDiscount_2WT_LegalNotice()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.02), 1L, 1000L);
    thenVerifyLegalNoticeWithRate();
  }

  // TEST : MULTI INVOICE TERM WITH 3% WT FINANCIAL DISCOUNT

  @Test
  void testComputeFinancialDiscountMultiInvoiceTermWithDiscount_3WT_FinancialDiscountRate()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.03), 2L, 1000L);
    thenVerifyFinancialDiscountRate();
  }

  @Test
  void testComputeFinancialDiscountMultiInvoiceTermWithDiscount_3WT_FinancialDiscountTotalAmount()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.03), 2L, 1000L);
    thenVerifyFinancialDiscountTotalAmount();
  }

  @Test
  void
      testComputeFinancialDiscountMultiInvoiceTermWithDiscount_3WT_RemainingAmountAfterFinDiscount()
          throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.03), 2L, 1000L);
    thenVerifyRemainingAmountAfterFinDiscountWithRate();
  }

  @Test
  void testComputeFinancialDiscountMultiInvoiceTermWithDiscount_3WT_LegalNotice()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.03), 2L, 1000L);
    thenVerifyLegalNoticeWithRate();
  }

  // TEST : MULTI INVOICE TERM WITH 2% ATI FINANCIAL DISCOUNT

  @Test
  void testComputeFinancialDiscountMultiInvoiceTermWithDiscount_2ATI_FinancialDiscountRate()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.02), 100L, 1000L);
    thenVerifyFinancialDiscountRate();
  }

  @Test
  void testComputeFinancialDiscountMultiInvoiceTermWithDiscount_2ATI_FinancialDiscountTotalAmount()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.02), 100L, 1000L);
    thenVerifyFinancialDiscountTotalAmount();
  }

  @Test
  void
      testComputeFinancialDiscountMultiInvoiceTermWithDiscount_2ATI_RemainingAmountAfterFinDiscount()
          throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.02), 100L, 1000L);
    thenVerifyRemainingAmountAfterFinDiscountWithRate();
  }

  @Test
  void testComputeFinancialDiscountMultiInvoiceTermWithDiscount_2ATI_LegalNotice()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.02), 100L, 1000L);
    thenVerifyLegalNoticeWithRate();
  }

  // TEST : MULTI INVOICE TERM WITH 3% ATI FINANCIAL DISCOUNT

  @Test
  void testComputeFinancialDiscountMultiInvoiceTermWithDiscount_3ATI_FinancialDiscountRate()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.03), 101L, 1000L);
    thenVerifyFinancialDiscountRate();
  }

  @Test
  void testComputeFinancialDiscountMultiInvoiceTermWithDiscount_3ATI_FinancialDiscountTotalAmount()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.03), 101L, 1000L);
    thenVerifyFinancialDiscountTotalAmount();
  }

  @Test
  void
      testComputeFinancialDiscountMultiInvoiceTermWithDiscount_3ATI_RemainingAmountAfterFinDiscount()
          throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.03), 101L, 1000L);
    thenVerifyRemainingAmountAfterFinDiscountWithRate();
  }

  @Test
  void testComputeFinancialDiscountMultiInvoiceTermWithDiscount_3ATI_LegalNotice()
      throws AxelorException {
    givenComputeFinancialDiscount(BigDecimal.valueOf(0.03), 101L, 1000L);
    thenVerifyLegalNoticeWithRate();
  }

  // DEFINE ALL TESTS THAT WILL BE USED

  private void thenVerifyFinancialDiscountRate() {
    Assertions.assertEquals(
        rate.multiply(new BigDecimal(100)).intValue(),
        invoice.getFinancialDiscountRate().intValue(),
        "Financial discount rate");
  }

  private void thenVerifyFinancialDiscountTotalAmount() {
    Assertions.assertEquals(
        invoice.getExTaxTotal().multiply(rate).intValue(),
        invoice.getFinancialDiscountTotalAmount().intValue(),
        "Financial discount total amount");
  }

  private void thenVerifyRemainingAmountAfterFinDiscountWithoutRate() {
    Assertions.assertEquals(
        0,
        invoice.getRemainingAmountAfterFinDiscount().intValue(),
        "Remaining amount after fin discount");
  }

  private void thenVerifyRemainingAmountAfterFinDiscountWithRate() {
    Assertions.assertEquals(
        invoice.getExTaxTotal().multiply(BigDecimal.ONE.subtract(rate)).intValue(),
        invoice.getRemainingAmountAfterFinDiscount().intValue(),
        "Remaining amount after fin discount");
  }

  private void thenVerifyLegalNoticeWithoutRate() {
    Assertions.assertNull(invoice.getLegalNotice(), "Legal notice");
  }

  private void thenVerifyLegalNoticeWithRate() {
    Assertions.assertNotNull(invoice.getLegalNotice(), "Legal notice");
  }
}
