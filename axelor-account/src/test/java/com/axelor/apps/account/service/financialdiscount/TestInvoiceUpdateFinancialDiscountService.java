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
package com.axelor.apps.account.service.financialdiscount;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.repo.FinancialDiscountRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentConditionRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.meta.loader.LoaderHelper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestInvoiceUpdateFinancialDiscountService extends TestInvoiceFinancialDiscountService {

  private Map<String, BigDecimal> amountByField;

  @Inject
  public TestInvoiceUpdateFinancialDiscountService(
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
  void testUpdateFinancialDiscountMonoInvoiceTermWithoutDiscount_FinancialDiscountAmount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.ZERO, null, 1L);
    thenVerifyFinancialDiscountAmount();
  }

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithoutDiscount_RemainingAmountAfterFinDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.ZERO, null, 1L);
    thenVerifyAmountWithoutRate(
        "remainingAmountAfterFinDiscount", "Remaining amount after fin discount");
  }

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithoutDiscount_AmountRemainingAfterFinDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.ZERO, null, 1L);
    thenVerifyAmountWithoutRate(
        "amountRemainingAfterFinDiscount", "Amount remaining after fin discount");
  }

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithoutDiscount_ApplyFinancialDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.ZERO, null, 1L);
    thenVerifyApplyFinancialDiscountWithoutRate();
  }

  // TEST : MONO INVOICE TERM WITH 2% WT FINANCIAL DISCOUNT

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithDiscount_2WT_FinancialDiscountAmount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.02), 1L, 1L);
    thenVerifyFinancialDiscountAmount();
  }

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithDiscount_2WT_RemainingAmountAfterFinDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.02), 1L, 1L);
    thenVerifyAmountWithRate(
        "remainingAmountAfterFinDiscount", "Remaining amount after fin discount");
  }

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithDiscount_2WT_AmountRemainingAfterFinDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.02), 1L, 1L);
    thenVerifyAmountWithRate(
        "amountRemainingAfterFinDiscount", "Amount remaining after fin discount");
  }

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithDiscount_2WT_ApplyFinancialDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.02), 1L, 1L);
    thenVerifyApplyFinancialDiscountWithRate();
  }

  // TEST : MONO INVOICE TERM WITH 3% WT FINANCIAL DISCOUNT

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithDiscount_3WT_FinancialDiscountAmount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.03), 2L, 1L);
    thenVerifyFinancialDiscountAmount();
  }

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithDiscount_3WT_RemainingAmountAfterFinDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.03), 2L, 1L);
    thenVerifyAmountWithRate(
        "remainingAmountAfterFinDiscount", "Remaining amount after fin discount");
  }

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithDiscount_3WT_AmountRemainingAfterFinDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.03), 2L, 1L);
    thenVerifyAmountWithRate(
        "amountRemainingAfterFinDiscount", "Amount remaining after fin discount");
  }

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithDiscount_3WT_ApplyFinancialDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.03), 2L, 1L);
    thenVerifyApplyFinancialDiscountWithRate();
  }

  // TEST : MONO INVOICE TERM WITH 2% ATI FINANCIAL DISCOUNT

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithDiscount_2ATI_FinancialDiscountAmount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.02), 100L, 1L);
    thenVerifyFinancialDiscountAmount();
  }

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithDiscount_2ATI_RemainingAmountAfterFinDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.02), 100L, 1L);
    thenVerifyAmountWithRate(
        "remainingAmountAfterFinDiscount", "Remaining amount after fin discount");
  }

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithDiscount_2ATI_AmountRemainingAfterFinDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.02), 100L, 1L);
    thenVerifyAmountWithRate(
        "amountRemainingAfterFinDiscount", "Amount remaining after fin discount");
  }

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithDiscount_2ATI_ApplyFinancialDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.02), 100L, 1L);
    thenVerifyApplyFinancialDiscountWithRate();
  }

  // TEST : MONO INVOICE TERM WITH 3% ATI FINANCIAL DISCOUNT

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithDiscount_3ATI_FinancialDiscountAmount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.03), 101L, 1L);
    thenVerifyFinancialDiscountAmount();
  }

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithDiscount_3ATI_RemainingAmountAfterFinDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.03), 101L, 1L);
    thenVerifyAmountWithRate(
        "remainingAmountAfterFinDiscount", "Remaining amount after fin discount");
  }

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithDiscount_3ATI_AmountRemainingAfterFinDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.03), 101L, 1L);
    thenVerifyAmountWithRate(
        "amountRemainingAfterFinDiscount", "Amount remaining after fin discount");
  }

  @Test
  void testUpdateFinancialDiscountMonoInvoiceTermWithDiscount_3ATI_ApplyFinancialDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.03), 101L, 1L);
    thenVerifyApplyFinancialDiscountWithRate();
  }

  // TEST : MULTI INVOICE TERM WITHOUT FINANCIAL DISCOUNT

  @Test
  void testUpdateFinancialDiscountMultiInvoiceTermWithoutDiscount_FinancialDiscountAmount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.ZERO, null, 1000L);
    thenVerifyFinancialDiscountAmount();
  }

  @Test
  void testUpdateFinancialDiscountMultiInvoiceTermWithoutDiscount_RemainingAmountAfterFinDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.ZERO, null, 1000L);
    thenVerifyAmountWithoutRate(
        "remainingAmountAfterFinDiscount", "Remaining amount after fin discount");
  }

  @Test
  void testUpdateFinancialDiscountMultiInvoiceTermWithoutDiscount_AmountRemainingAfterFinDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.ZERO, null, 1000L);
    thenVerifyAmountWithoutRate(
        "amountRemainingAfterFinDiscount", "Amount remaining after fin discount");
  }

  @Test
  void testUpdateFinancialDiscountMultiInvoiceTermWithoutDiscount_ApplyFinancialDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.ZERO, null, 1000L);
    thenVerifyApplyFinancialDiscountWithoutRate();
  }

  // TEST : MULTI INVOICE TERM WITH 2% WT FINANCIAL DISCOUNT

  @Test
  void testUpdateFinancialDiscountMultiInvoiceTermWithDiscount_2WT_FinancialDiscountAmount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.02), 1L, 1000L);
    thenVerifyFinancialDiscountAmount();
  }

  @Test
  void testUpdateFinancialDiscountMultiInvoiceTermWithDiscount_2WT_RemainingAmountAfterFinDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.02), 1L, 1000L);
    thenVerifyAmountWithRate(
        "remainingAmountAfterFinDiscount", "Remaining amount after fin discount");
  }

  @Test
  void testUpdateFinancialDiscountMultiInvoiceTermWithDiscount_2WT_AmountRemainingAfterFinDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.02), 1L, 1000L);
    thenVerifyAmountWithRate(
        "amountRemainingAfterFinDiscount", "Amount remaining after fin discount");
  }

  @Test
  void testUpdateFinancialDiscountMultiInvoiceTermWithDiscount_2WT_ApplyFinancialDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.02), 1L, 1000L);
    thenVerifyApplyFinancialDiscountWithRate();
  }

  // TEST : MULTI INVOICE TERM WITH 3% WT FINANCIAL DISCOUNT

  @Test
  void testUpdateFinancialDiscountMultiInvoiceTermWithDiscount_3WT_FinancialDiscountAmount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.03), 2L, 1000L);
    thenVerifyFinancialDiscountAmount();
  }

  @Test
  void testUpdateFinancialDiscountMultiInvoiceTermWithDiscount_3WT_RemainingAmountAfterFinDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.03), 2L, 1000L);
    thenVerifyAmountWithRate(
        "remainingAmountAfterFinDiscount", "Remaining amount after fin discount");
  }

  @Test
  void testUpdateFinancialDiscountMultiInvoiceTermWithDiscount_3WT_AmountRemainingAfterFinDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.03), 2L, 1000L);
    thenVerifyAmountWithRate(
        "amountRemainingAfterFinDiscount", "Amount remaining after fin discount");
  }

  @Test
  void testUpdateFinancialDiscountMultiInvoiceTermWithDiscount_3WT_ApplyFinancialDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.03), 2L, 1000L);
    thenVerifyApplyFinancialDiscountWithRate();
  }

  // TEST : MULTI INVOICE TERM WITH 2% ATI FINANCIAL DISCOUNT

  @Test
  void testUpdateFinancialDiscountMultiInvoiceTermWithDiscount_2ATI_FinancialDiscountAmount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.02), 100L, 1000L);
    thenVerifyFinancialDiscountAmount();
  }

  @Test
  void
      testUpdateFinancialDiscountMultiInvoiceTermWithDiscount_2ATI_RemainingAmountAfterFinDiscount()
          throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.02), 100L, 1000L);
    thenVerifyAmountWithRate(
        "remainingAmountAfterFinDiscount", "Remaining amount after fin discount");
  }

  @Test
  void
      testUpdateFinancialDiscountMultiInvoiceTermWithDiscount_2ATI_AmountRemainingAfterFinDiscount()
          throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.02), 100L, 1000L);
    thenVerifyAmountWithRate(
        "amountRemainingAfterFinDiscount", "Amount remaining after fin discount");
  }

  @Test
  void testUpdateFinancialDiscountMultiInvoiceTermWithDiscount_2ATI_ApplyFinancialDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.02), 100L, 1000L);
    thenVerifyApplyFinancialDiscountWithRate();
  }

  // TEST : MULTI INVOICE TERM WITH 3% ATI FINANCIAL DISCOUNT

  @Test
  void testUpdateFinancialDiscountMultiInvoiceTermWithDiscount_3ATI_FinancialDiscountAmount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.03), 101L, 1000L);
    thenVerifyFinancialDiscountAmount();
  }

  @Test
  void
      testUpdateFinancialDiscountMultiInvoiceTermWithDiscount_3ATI_RemainingAmountAfterFinDiscount()
          throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.03), 101L, 1000L);
    thenVerifyAmountWithRate(
        "remainingAmountAfterFinDiscount", "Remaining amount after fin discount");
  }

  @Test
  void
      testUpdateFinancialDiscountMultiInvoiceTermWithDiscount_3ATI_AmountRemainingAfterFinDiscount()
          throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.03), 101L, 1000L);
    thenVerifyAmountWithRate(
        "amountRemainingAfterFinDiscount", "Amount remaining after fin discount");
  }

  @Test
  void testUpdateFinancialDiscountMultiInvoiceTermWithDiscount_3ATI_ApplyFinancialDiscount()
      throws AxelorException {
    givenUpdateFinancialDiscount(BigDecimal.valueOf(0.03), 101L, 1000L);
    thenVerifyApplyFinancialDiscountWithRate();
  }

  // PREPARE DATA FOR TEST

  protected void givenUpdateFinancialDiscount(
      BigDecimal rate, Long financialDiscountImportId, Long paymentConditionId)
      throws AxelorException {
    givenComputeFinancialDiscount(rate, financialDiscountImportId, paymentConditionId);
    List<InvoiceTerm> invoiceTermList =
        invoiceFinancialDiscountService.updateFinancialDiscount(invoice);
    givenAmountsByField(invoiceTermList);
  }

  protected void givenAmountsByField(List<InvoiceTerm> invoiceTermList) {
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

    this.amountByField = amountByField;
  }

  // DEFINE ALL TESTS THAT WILL BE USED

  private void thenVerifyFinancialDiscountAmount() {
    Assertions.assertEquals(
        amountByField.get("financialDiscountAmount").intValue(),
        invoice.getExTaxTotal().multiply(rate).intValue(),
        "Financial discount amount");
  }

  private void thenVerifyAmountWithoutRate(String key, String message) {
    Assertions.assertEquals(0, amountByField.get(key).intValue(), message);
  }

  private void thenVerifyAmountWithRate(String key, String message) {
    Assertions.assertEquals(
        invoice.getExTaxTotal().multiply(BigDecimal.ONE.subtract(rate)).intValue(),
        amountByField.get(key).intValue(),
        message);
  }

  private void thenVerifyApplyFinancialDiscountWithoutRate() {
    Assertions.assertTrue(
        amountByField.get("applyFinancialDiscount").signum() == 0, "Apply Financial discount");
  }

  private void thenVerifyApplyFinancialDiscountWithRate() {
    Assertions.assertTrue(
        amountByField.get("applyFinancialDiscount").signum() > 0, "Apply Financial discount");
  }
}
