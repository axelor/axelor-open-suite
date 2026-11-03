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
package com.axelor.apps.account.service.moveline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.util.TaxConfiguration;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TestMoveLineCreateServiceNonDeductibleTax {

  protected MoveLineCreateServiceImpl moveLineCreateService =
      new MoveLineCreateServiceImpl(
          null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
          null, null, null, null, null);

  @Test
  void testSplitIsProportionalToDeductibleTax() {
    TaxLine vat20 = createTaxLine(1L, "20", false);
    InvoiceLine line1 = createInvoiceLine("1000");
    InvoiceLine line2 = createInvoiceLine("500");
    Map<TaxConfiguration, Set<InvoiceLine>> map = new HashMap<>();
    map.put(new TaxConfiguration(vat20, null, 1), Set.of(line1, line2));

    assertSplitEquals(List.of("200.00", "100.00"), "300.00", List.of(line1, line2), map);
  }

  @Test
  void testSplitUsesTheDeductibleTaxRateOfEachLine() {
    TaxLine vat20 = createTaxLine(1L, "20", false);
    TaxLine vat10 = createTaxLine(2L, "10", false);
    InvoiceLine line1 = createInvoiceLine("1000");
    InvoiceLine line2 = createInvoiceLine("1000");
    Map<TaxConfiguration, Set<InvoiceLine>> map = new HashMap<>();
    map.put(new TaxConfiguration(vat20, null, 1), Set.of(line1));
    map.put(new TaxConfiguration(vat10, null, 1), Set.of(line2));
    map.put(new TaxConfiguration(createTaxLine(3L, "100", true), null, 1), Set.of(line1, line2));

    assertSplitEquals(List.of("200.00", "100.00"), "300.00", List.of(line1, line2), map);
  }

  @Test
  void testRoundingGapIsOnTheLastLine() {
    TaxLine vat20 = createTaxLine(1L, "20", false);
    InvoiceLine line1 = createInvoiceLine("100");
    InvoiceLine line2 = createInvoiceLine("100");
    InvoiceLine line3 = createInvoiceLine("100");
    Map<TaxConfiguration, Set<InvoiceLine>> map = new HashMap<>();
    map.put(new TaxConfiguration(vat20, null, 1), Set.of(line1, line2, line3));

    assertSplitEquals(
        List.of("20.00", "20.00", "20.01"), "60.01", List.of(line1, line2, line3), map);
  }

  protected void assertSplitEquals(
      List<String> expectedList,
      String amount,
      List<InvoiceLine> invoiceLineList,
      Map<TaxConfiguration, Set<InvoiceLine>> invoiceLineSetByTaxConfiguration) {
    List<BigDecimal> weightList =
        moveLineCreateService.getNonDeductibleTaxWeightList(
            invoiceLineList, invoiceLineSetByTaxConfiguration);
    List<BigDecimal> amountList =
        moveLineCreateService.splitNonDeductibleTaxAmount(new BigDecimal(amount), weightList, 2);

    assertEquals(expectedList.size(), amountList.size());
    for (int i = 0; i < expectedList.size(); i++) {
      assertEquals(0, new BigDecimal(expectedList.get(i)).compareTo(amountList.get(i)));
    }
  }

  protected InvoiceLine createInvoiceLine(String exTaxTotal) {
    InvoiceLine invoiceLine = new InvoiceLine();
    invoiceLine.setExTaxTotal(new BigDecimal(exTaxTotal));
    invoiceLine.setCompanyExTaxTotal(new BigDecimal(exTaxTotal));
    return invoiceLine;
  }

  protected TaxLine createTaxLine(Long id, String value, boolean isNonDeductibleTax) {
    Tax tax = new Tax();
    tax.setIsNonDeductibleTax(isNonDeductibleTax);
    TaxLine taxLine = new TaxLine();
    taxLine.setId(id);
    taxLine.setTax(tax);
    taxLine.setValue(new BigDecimal(value));
    return taxLine;
  }
}
