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
package com.axelor.apps.account.service.invoice.generator.tax;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.module.AccountTest;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletScopes;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestTaxInvoiceLine extends AccountTest {

  protected TaxInvoiceLine taxInvoiceLine;

  @BeforeEach
  void setUp() {
    RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());
    try (RequestScoper.CloseableScope ignored = scope.open()) {
      taxInvoiceLine = new TaxInvoiceLine(new Invoice(), List.of());
    }
  }

  @Test
  void testNonDeductibleTaxOnlyAdjustsTaxSharingSameInvoiceLine() {
    InvoiceLine deductibleTenLine = createInvoiceLine("100");
    InvoiceLine deductibleTwentyLine = createInvoiceLine("100");
    InvoiceLineTax deductibleTenTax = createInvoiceLineTax("100", "10", false);
    InvoiceLineTax deductibleTwentyTax = createInvoiceLineTax("100", "20", false);
    InvoiceLineTax nonDeductibleTwentyTax = createInvoiceLineTax("100", "20", true);
    Map<InvoiceLineTax, Set<InvoiceLine>> invoiceLineSetByInvoiceLineTax =
        Map.of(
            deductibleTenTax,
            Set.of(deductibleTenLine),
            deductibleTwentyTax,
            Set.of(deductibleTwentyLine),
            nonDeductibleTwentyTax,
            Set.of(deductibleTwentyLine));

    assertTaxAmountEquals(
        "10",
        deductibleTenTax,
        taxInvoiceLine.getAdjustedTaxValue(
            deductibleTenTax,
            taxInvoiceLine.getTaxRate(deductibleTenTax),
            List.of(nonDeductibleTwentyTax),
            invoiceLineSetByInvoiceLineTax));
    assertTaxAmountEquals(
        "16",
        deductibleTwentyTax,
        taxInvoiceLine.getAdjustedTaxValue(
            deductibleTwentyTax,
            taxInvoiceLine.getTaxRate(deductibleTwentyTax),
            List.of(nonDeductibleTwentyTax),
            invoiceLineSetByInvoiceLineTax));
    assertTaxAmountEquals(
        "4",
        nonDeductibleTwentyTax,
        taxInvoiceLine.getAdjustedNonDeductibleTaxValue(
            nonDeductibleTwentyTax,
            taxInvoiceLine.getTaxRate(nonDeductibleTwentyTax),
            List.of(deductibleTenTax, deductibleTwentyTax),
            invoiceLineSetByInvoiceLineTax));
  }

  @Test
  void testNonDeductibleTaxPreservesPartialOverlapVentilation() {
    InvoiceLine sharedLine = createInvoiceLine("1000");
    InvoiceLine deductibleOnlyLine = createInvoiceLine("200");
    InvoiceLineTax deductibleTwentyTax = createInvoiceLineTax("1200", "20", false);
    InvoiceLineTax nonDeductibleTwentyTax = createInvoiceLineTax("1000", "20", true);
    Map<InvoiceLineTax, Set<InvoiceLine>> invoiceLineSetByInvoiceLineTax =
        Map.of(
            deductibleTwentyTax,
            Set.of(sharedLine, deductibleOnlyLine),
            nonDeductibleTwentyTax,
            Set.of(sharedLine));

    assertTaxAmountEquals(
        "200",
        deductibleTwentyTax,
        taxInvoiceLine.getAdjustedTaxValue(
            deductibleTwentyTax,
            taxInvoiceLine.getTaxRate(deductibleTwentyTax),
            List.of(nonDeductibleTwentyTax),
            invoiceLineSetByInvoiceLineTax));
    assertTaxAmountEquals(
        "40",
        nonDeductibleTwentyTax,
        taxInvoiceLine.getAdjustedNonDeductibleTaxValue(
            nonDeductibleTwentyTax,
            taxInvoiceLine.getTaxRate(nonDeductibleTwentyTax),
            List.of(deductibleTwentyTax),
            invoiceLineSetByInvoiceLineTax));
  }

  @Test
  void testNonDeductibleTaxWeightsDifferentDeductibleTaxesBySharedBase() {
    InvoiceLine deductibleTwentyLine = createInvoiceLine("100");
    InvoiceLine deductibleTenLine = createInvoiceLine("100");
    InvoiceLineTax deductibleTwentyTax = createInvoiceLineTax("100", "20", false);
    InvoiceLineTax deductibleTenTax = createInvoiceLineTax("100", "10", false);
    InvoiceLineTax nonDeductibleTwentyTax = createInvoiceLineTax("200", "20", true);
    Map<InvoiceLineTax, Set<InvoiceLine>> invoiceLineSetByInvoiceLineTax =
        Map.of(
            deductibleTwentyTax,
            Set.of(deductibleTwentyLine),
            deductibleTenTax,
            Set.of(deductibleTenLine),
            nonDeductibleTwentyTax,
            Set.of(deductibleTwentyLine, deductibleTenLine));

    assertTaxAmountEquals(
        "6",
        nonDeductibleTwentyTax,
        taxInvoiceLine.getAdjustedNonDeductibleTaxValue(
            nonDeductibleTwentyTax,
            taxInvoiceLine.getTaxRate(nonDeductibleTwentyTax),
            List.of(deductibleTwentyTax, deductibleTenTax),
            invoiceLineSetByInvoiceLineTax));
  }

  @Test
  void testMultipleNonDeductibleTaxesRemainBalancedOnSameInvoiceLine() {
    InvoiceLine sharedLine = createInvoiceLine("100");
    InvoiceLineTax deductibleTwentyTax = createInvoiceLineTax("100", "20", false);
    InvoiceLineTax nonDeductibleTenTax = createInvoiceLineTax("100", "10", true);
    InvoiceLineTax nonDeductibleTwentyTax = createInvoiceLineTax("100", "20", true);
    Map<InvoiceLineTax, Set<InvoiceLine>> invoiceLineSetByInvoiceLineTax =
        Map.of(
            deductibleTwentyTax,
            Set.of(sharedLine),
            nonDeductibleTenTax,
            Set.of(sharedLine),
            nonDeductibleTwentyTax,
            Set.of(sharedLine));

    assertTaxAmountEquals(
        "14",
        deductibleTwentyTax,
        taxInvoiceLine.getAdjustedTaxValue(
            deductibleTwentyTax,
            taxInvoiceLine.getTaxRate(deductibleTwentyTax),
            List.of(nonDeductibleTenTax, nonDeductibleTwentyTax),
            invoiceLineSetByInvoiceLineTax));
    assertTaxAmountEquals(
        "2",
        nonDeductibleTenTax,
        taxInvoiceLine.getAdjustedNonDeductibleTaxValue(
            nonDeductibleTenTax,
            taxInvoiceLine.getTaxRate(nonDeductibleTenTax),
            List.of(deductibleTwentyTax),
            invoiceLineSetByInvoiceLineTax));
    assertTaxAmountEquals(
        "4",
        nonDeductibleTwentyTax,
        taxInvoiceLine.getAdjustedNonDeductibleTaxValue(
            nonDeductibleTwentyTax,
            taxInvoiceLine.getTaxRate(nonDeductibleTwentyTax),
            List.of(deductibleTwentyTax),
            invoiceLineSetByInvoiceLineTax));
  }

  protected InvoiceLine createInvoiceLine(String exTaxTotal) {
    InvoiceLine invoiceLine = new InvoiceLine();
    invoiceLine.setExTaxTotal(new BigDecimal(exTaxTotal));
    return invoiceLine;
  }

  protected InvoiceLineTax createInvoiceLineTax(
      String exTaxBase, String taxRate, boolean isNonDeductibleTax) {
    Tax tax = new Tax();
    tax.setIsNonDeductibleTax(isNonDeductibleTax);
    TaxLine taxLine = new TaxLine();
    taxLine.setTax(tax);
    taxLine.setValue(new BigDecimal(taxRate));
    InvoiceLineTax invoiceLineTax = new InvoiceLineTax();
    invoiceLineTax.setTaxLine(taxLine);
    invoiceLineTax.setExTaxBase(new BigDecimal(exTaxBase));
    return invoiceLineTax;
  }

  protected void assertTaxAmountEquals(
      String expectedTaxAmount, InvoiceLineTax invoiceLineTax, BigDecimal adjustedTaxRate) {
    assertEquals(
        new BigDecimal(expectedTaxAmount).setScale(2, RoundingMode.HALF_UP),
        invoiceLineTax.getExTaxBase().multiply(adjustedTaxRate).setScale(2, RoundingMode.HALF_UP));
  }
}
