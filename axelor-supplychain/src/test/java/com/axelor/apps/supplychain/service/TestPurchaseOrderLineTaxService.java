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
package com.axelor.apps.supplychain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.VatExemptionReason;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleServiceImpl;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.OrderLineTaxService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.PurchaseOrderLineTax;
import com.axelor.apps.purchase.service.PurchaseOrderLineTaxServiceImpl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestPurchaseOrderLineTaxService {

  protected OrderLineTaxService orderLineTaxService;
  protected AppBaseService appBaseService;
  protected PurchaseOrderLineTaxServiceImpl purchaseOrderLineTaxService;

  @BeforeEach
  void setUp() {
    orderLineTaxService = mock(OrderLineTaxService.class);
    appBaseService = mock(AppBaseService.class);
    purchaseOrderLineTaxService = createPurchaseOrderLineTaxService(mock(TaxService.class));
  }

  @Test
  void testNonDeductibleTaxOnlyAdjustsTaxSharingSamePurchaseOrderLine() throws AxelorException {
    TaxLine deductibleTenTax = createTaxLine("10", false);
    TaxLine deductibleTwentyTax = createTaxLine("20", false);
    TaxLine nonDeductibleTwentyTax = createTaxLine("20", true);
    PurchaseOrderLine deductibleTenLine = createPurchaseOrderLine("100", "110", deductibleTenTax);
    PurchaseOrderLine affectedLine =
        createPurchaseOrderLine("100", "140", deductibleTwentyTax, nonDeductibleTwentyTax);
    PurchaseOrder purchaseOrder = createPurchaseOrder(deductibleTenLine, affectedLine);

    List<PurchaseOrderLineTax> firstComputation = computeTaxes(purchaseOrder);

    assertTaxAmount(firstComputation, deductibleTenTax, "10");
    assertTaxAmount(firstComputation, deductibleTwentyTax, "16");
    assertTaxAmount(firstComputation, nonDeductibleTwentyTax, "4");
    assertAmount("30", sumTaxAmounts(firstComputation));
    assertAmount("110", deductibleTenLine.getInTaxTotal());
    assertAmount("140", affectedLine.getInTaxTotal());

    purchaseOrder.setPurchaseOrderLineTaxList(new ArrayList<>(firstComputation));
    List<PurchaseOrderLineTax> secondComputation = computeTaxes(purchaseOrder);

    assertEquals(3, secondComputation.size());
    assertTaxAmount(secondComputation, deductibleTenTax, "10");
    assertTaxAmount(secondComputation, deductibleTwentyTax, "16");
    assertTaxAmount(secondComputation, nonDeductibleTwentyTax, "4");
  }

  @Test
  void testNonDeductibleTaxPreservesPartialOverlapVentilation() throws AxelorException {
    TaxLine deductibleTwentyTax = createTaxLine("20", false);
    TaxLine nonDeductibleTwentyTax = createTaxLine("20", true);
    PurchaseOrder purchaseOrder =
        createPurchaseOrder(
            createPurchaseOrderLine("1000", "1400", deductibleTwentyTax, nonDeductibleTwentyTax),
            createPurchaseOrderLine("200", "240", deductibleTwentyTax));

    List<PurchaseOrderLineTax> taxList = computeTaxes(purchaseOrder);

    assertTaxAmount(taxList, deductibleTwentyTax, "200");
    assertTaxAmount(taxList, nonDeductibleTwentyTax, "40");
    assertAmount("240", sumTaxAmounts(taxList));
  }

  @Test
  void testNonDeductibleTaxWeightsDifferentDeductibleTaxesBySharedBase() throws AxelorException {
    TaxLine deductibleTwentyTax = createTaxLine("20", false);
    TaxLine deductibleTenTax = createTaxLine("10", false);
    TaxLine nonDeductibleTwentyTax = createTaxLine("20", true);
    PurchaseOrder purchaseOrder =
        createPurchaseOrder(
            createPurchaseOrderLine("100", "140", deductibleTwentyTax, nonDeductibleTwentyTax),
            createPurchaseOrderLine("100", "130", deductibleTenTax, nonDeductibleTwentyTax));

    List<PurchaseOrderLineTax> taxList = computeTaxes(purchaseOrder);

    assertTaxAmount(taxList, deductibleTwentyTax, "16");
    assertTaxAmount(taxList, deductibleTenTax, "8");
    assertTaxAmount(taxList, nonDeductibleTwentyTax, "6");
    assertAmount("30", sumTaxAmounts(taxList));
  }

  @Test
  void testMultipleNonDeductibleTaxesRemainBalancedOnSamePurchaseOrderLine()
      throws AxelorException {
    TaxLine deductibleTwentyTax = createTaxLine("20", false);
    TaxLine nonDeductibleTenTax = createTaxLine("10", true);
    TaxLine nonDeductibleTwentyTax = createTaxLine("20", true);
    PurchaseOrder purchaseOrder =
        createPurchaseOrder(
            createPurchaseOrderLine(
                "100", "150", deductibleTwentyTax, nonDeductibleTenTax, nonDeductibleTwentyTax));

    List<PurchaseOrderLineTax> taxList = computeTaxes(purchaseOrder);

    assertTaxAmount(taxList, deductibleTwentyTax, "14");
    assertTaxAmount(taxList, nonDeductibleTenTax, "2");
    assertTaxAmount(taxList, nonDeductibleTwentyTax, "4");
    assertAmount("20", sumTaxAmounts(taxList));
  }

  @Test
  void testRegularTaxesAndZeroBasesRemainUnchanged() throws AxelorException {
    TaxLine deductibleTenTax = createTaxLine("10", false);
    PurchaseOrder regularPurchaseOrder =
        createPurchaseOrder(createPurchaseOrderLine("100", "110", deductibleTenTax));

    assertTaxAmount(computeTaxes(regularPurchaseOrder), deductibleTenTax, "10");

    TaxLine deductibleTwentyTax = createTaxLine("20", false);
    TaxLine nonDeductibleTwentyTax = createTaxLine("20", true);
    PurchaseOrder zeroBasePurchaseOrder =
        createPurchaseOrder(
            createPurchaseOrderLine("0", "0", deductibleTwentyTax, nonDeductibleTwentyTax));

    List<PurchaseOrderLineTax> zeroBaseTaxList = computeTaxes(zeroBasePurchaseOrder);
    assertTaxAmount(zeroBaseTaxList, deductibleTwentyTax, "0");
    assertTaxAmount(zeroBaseTaxList, nonDeductibleTwentyTax, "0");
  }

  @Test
  void testNegativeBasePreservesTaxSignsAndBalance() throws AxelorException {
    TaxLine deductibleTwentyTax = createTaxLine("20", false);
    TaxLine nonDeductibleTwentyTax = createTaxLine("20", true);
    PurchaseOrder purchaseOrder =
        createPurchaseOrder(
            createPurchaseOrderLine("-100", "-140", deductibleTwentyTax, nonDeductibleTwentyTax));

    List<PurchaseOrderLineTax> taxList = computeTaxes(purchaseOrder);

    assertTaxAmount(taxList, deductibleTwentyTax, "-16");
    assertTaxAmount(taxList, nonDeductibleTwentyTax, "-4");
    assertAmount("-20", sumTaxAmounts(taxList));
  }

  @Test
  void testVatExemptionReasonsKeepTaxContributionsSeparated() throws AxelorException {
    TaxLine deductibleTenTax = createTaxLine("10", false);
    TaxLine nonDeductibleTwentyTax = createTaxLine("20", true);
    VatExemptionReason unaffectedReason = new VatExemptionReason();
    VatExemptionReason affectedReason = new VatExemptionReason();
    PurchaseOrderLine unaffectedLine = createPurchaseOrderLine("100", "110", deductibleTenTax);
    unaffectedLine.setVatExemptionReason(unaffectedReason);
    PurchaseOrderLine affectedLine =
        createPurchaseOrderLine("100", "130", deductibleTenTax, nonDeductibleTwentyTax);
    affectedLine.setVatExemptionReason(affectedReason);

    List<PurchaseOrderLineTax> taxList =
        computeTaxes(createPurchaseOrder(unaffectedLine, affectedLine));

    assertTaxAmount(taxList, deductibleTenTax, unaffectedReason, "10");
    assertTaxAmount(taxList, deductibleTenTax, affectedReason, "8");
    assertTaxAmount(taxList, nonDeductibleTwentyTax, affectedReason, "2");
  }

  @Test
  void testReverseChargeTaxKeepsItsSign() throws AxelorException {
    TaxLine deductibleTwentyTax = createTaxLine("20", false);
    TaxLine reverseChargeTwentyTax = createTaxLine("20", false);
    TaxEquiv taxEquiv = new TaxEquiv();
    taxEquiv.setReverseCharge(true);
    taxEquiv.addReverseChargeTaxSetItem(reverseChargeTwentyTax.getTax());
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine("100", "120", deductibleTwentyTax);
    purchaseOrderLine.setTaxEquiv(taxEquiv);
    purchaseOrderLineTaxService = createPurchaseOrderLineTaxService(new TaxService());

    List<PurchaseOrderLineTax> taxList = computeTaxes(createPurchaseOrder(purchaseOrderLine));

    assertTaxAmount(taxList, deductibleTwentyTax, "20");
    assertTaxAmount(taxList, reverseChargeTwentyTax, "-20");
    assertTrue(findTax(taxList, reverseChargeTwentyTax).getReverseCharged());
  }

  @Test
  void testExistingManagedByAmountTaxIsReused() throws AxelorException {
    TaxLine deductibleTenTax = createTaxLine("10", false);
    deductibleTenTax.getTax().setManageByAmount(true);
    PurchaseOrder purchaseOrder =
        createPurchaseOrder(createPurchaseOrderLine("100", "110", deductibleTenTax));
    PurchaseOrderLineTax managedTax = new PurchaseOrderLineTax();
    managedTax.setPurchaseOrder(purchaseOrder);
    managedTax.setTaxLine(deductibleTenTax);
    managedTax.setExTaxBase(new BigDecimal("100"));
    managedTax.setTaxTotal(new BigDecimal("9"));
    managedTax.setPercentageTaxTotal(new BigDecimal("10"));
    managedTax.setInTaxTotal(new BigDecimal("109"));
    purchaseOrder.addPurchaseOrderLineTaxListItem(managedTax);

    List<PurchaseOrderLineTax> taxList = computeTaxes(purchaseOrder);

    assertEquals(1, taxList.size());
    assertSame(managedTax, taxList.get(0));
    assertAmount("9", taxList.get(0).getTaxTotal());
    assertAmount("10", taxList.get(0).getPercentageTaxTotal());
  }

  protected PurchaseOrderLineTaxServiceImpl createPurchaseOrderLineTaxService(
      TaxService taxService) {
    return new PurchaseOrderLineTaxServiceImpl(
        orderLineTaxService,
        taxService,
        appBaseService,
        new PurchaseOrderLineTaxComputeSupplychainServiceImp(new CurrencyScaleServiceImpl()));
  }

  protected PurchaseOrder createPurchaseOrder(PurchaseOrderLine... purchaseOrderLines) {
    Currency currency = new Currency();
    currency.setNumberOfDecimals(2);
    PurchaseOrder purchaseOrder = new PurchaseOrder();
    purchaseOrder.setCurrency(currency);
    purchaseOrder.setPurchaseOrderLineTaxList(new ArrayList<>());
    for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLines) {
      purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLine);
    }
    return purchaseOrder;
  }

  protected PurchaseOrderLine createPurchaseOrderLine(
      String exTaxTotal, String inTaxTotal, TaxLine... taxLines) {
    PurchaseOrderLine purchaseOrderLine = new PurchaseOrderLine();
    purchaseOrderLine.setExTaxTotal(new BigDecimal(exTaxTotal));
    purchaseOrderLine.setInTaxTotal(new BigDecimal(inTaxTotal));
    purchaseOrderLine.setTaxLineSet(new HashSet<>(List.of(taxLines)));
    return purchaseOrderLine;
  }

  protected TaxLine createTaxLine(String value, boolean nonDeductible) {
    Tax tax = new Tax();
    tax.setIsNonDeductibleTax(nonDeductible);
    TaxLine taxLine = new TaxLine();
    taxLine.setTax(tax);
    taxLine.setValue(new BigDecimal(value));
    tax.setActiveTaxLine(taxLine);
    return taxLine;
  }

  protected List<PurchaseOrderLineTax> computeTaxes(PurchaseOrder purchaseOrder)
      throws AxelorException {
    return purchaseOrderLineTaxService.createsPurchaseOrderLineTax(
        purchaseOrder, purchaseOrder.getPurchaseOrderLineList());
  }

  protected PurchaseOrderLineTax findTax(List<PurchaseOrderLineTax> taxList, TaxLine taxLine) {
    return taxList.stream()
        .filter(purchaseOrderLineTax -> purchaseOrderLineTax.getTaxLine() == taxLine)
        .findFirst()
        .orElseThrow();
  }

  protected PurchaseOrderLineTax findTax(
      List<PurchaseOrderLineTax> taxList, TaxLine taxLine, VatExemptionReason vatExemptionReason) {
    return taxList.stream()
        .filter(purchaseOrderLineTax -> purchaseOrderLineTax.getTaxLine() == taxLine)
        .filter(
            purchaseOrderLineTax ->
                purchaseOrderLineTax.getVatExemptionReason() == vatExemptionReason)
        .findFirst()
        .orElseThrow();
  }

  protected void assertTaxAmount(
      List<PurchaseOrderLineTax> taxList, TaxLine taxLine, String expectedAmount) {
    assertAmount(expectedAmount, findTax(taxList, taxLine).getTaxTotal());
  }

  protected void assertTaxAmount(
      List<PurchaseOrderLineTax> taxList,
      TaxLine taxLine,
      VatExemptionReason vatExemptionReason,
      String expectedAmount) {
    assertAmount(expectedAmount, findTax(taxList, taxLine, vatExemptionReason).getTaxTotal());
  }

  protected BigDecimal sumTaxAmounts(List<PurchaseOrderLineTax> taxList) {
    return taxList.stream()
        .map(PurchaseOrderLineTax::getTaxTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  protected void assertAmount(String expectedAmount, BigDecimal actualAmount) {
    assertEquals(0, new BigDecimal(expectedAmount).compareTo(actualAmount));
  }
}
