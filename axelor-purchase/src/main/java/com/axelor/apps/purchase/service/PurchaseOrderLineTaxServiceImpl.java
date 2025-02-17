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
package com.axelor.apps.purchase.service;

import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.OrderLineTaxService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.PurchaseOrderLineTax;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderLineTaxServiceImpl implements PurchaseOrderLineTaxService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected OrderLineTaxService orderLineTaxService;
  protected TaxService taxService;
  protected AppBaseService appBaseService;
  protected PurchaseOrderLineTaxComputeService purchaseOrderLineTaxComputeService;

  @Inject
  public PurchaseOrderLineTaxServiceImpl(
      OrderLineTaxService orderLineTaxService,
      TaxService taxService,
      AppBaseService appBaseService,
      PurchaseOrderLineTaxComputeService purchaseOrderLineTaxComputeService) {
    this.orderLineTaxService = orderLineTaxService;
    this.taxService = taxService;
    this.appBaseService = appBaseService;
    this.purchaseOrderLineTaxComputeService = purchaseOrderLineTaxComputeService;
  }

  /**
   * Créer les lignes de TVA de la commande. La création des lignes de TVA se basent sur les lignes
   * de commande.
   *
   * @param purchaseOrder La commande.
   * @param purchaseOrderLineList Les lignes de commandes.
   * @return La liste des lignes de TVA de la commande.
   * @throws AxelorException
   */
  @Override
  public List<PurchaseOrderLineTax> createsPurchaseOrderLineTax(
      PurchaseOrder purchaseOrder, List<PurchaseOrderLine> purchaseOrderLineList)
      throws AxelorException {

    List<PurchaseOrderLineTax> purchaseOrderLineTaxList = new ArrayList<>();
    List<PurchaseOrderLineTax> currentPurchaseOrderLineTaxList = new ArrayList<>();
    currentPurchaseOrderLineTaxList.addAll(purchaseOrder.getPurchaseOrderLineTaxList());
    purchaseOrder.clearPurchaseOrderLineTaxList();

    Map<TaxLine, PurchaseOrderLineTax> map = new HashMap<>();
    Set<String> specificNotes = new HashSet<>();
    boolean customerSpecificNote = orderLineTaxService.isCustomerSpecificNote(purchaseOrder);

    if (CollectionUtils.isNotEmpty(purchaseOrderLineList)) {
      LOG.debug("Creation of tax lines for purchase order lines.");
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
        getOrCreateLines(
            purchaseOrder, purchaseOrderLine, map, customerSpecificNote, specificNotes);
      }
    }

    purchaseOrderLineTaxComputeService.computeAndAddTaxToList(
        map,
        purchaseOrderLineTaxList,
        purchaseOrder.getCurrency(),
        currentPurchaseOrderLineTaxList);
    orderLineTaxService.setSpecificNotes(
        customerSpecificNote,
        purchaseOrder,
        specificNotes,
        purchaseOrder.getSupplierPartner().getSpecificTaxNote());

    return purchaseOrderLineTaxList;
  }

  protected void getOrCreateLines(
      PurchaseOrder purchaseOrder,
      PurchaseOrderLine purchaseOrderLine,
      Map<TaxLine, PurchaseOrderLineTax> map,
      boolean customerSpecificNote,
      Set<String> specificNotes)
      throws AxelorException {
    Set<TaxLine> taxLineSet = purchaseOrderLine.getTaxLineSet();
    if (CollectionUtils.isNotEmpty(taxLineSet)) {
      for (TaxLine taxLine : taxLineSet) {
        getOrCreateLine(purchaseOrder, purchaseOrderLine, taxLine, map, false);
      }
    }
    TaxEquiv taxEquiv = purchaseOrderLine.getTaxEquiv();
    // Reverse charged process
    Set<TaxLine> taxLineRCSet =
        (taxEquiv != null
                && taxEquiv.getReverseCharge()
                && ObjectUtils.notEmpty(taxEquiv.getReverseChargeTaxSet()))
            ? taxService.getTaxLineSet(
                taxEquiv.getReverseChargeTaxSet(),
                appBaseService.getTodayDate(
                    Optional.ofNullable(purchaseOrderLine.getPurchaseOrder())
                        .map(PurchaseOrder::getCompany)
                        .orElse(null)))
            : null;
    if (CollectionUtils.isNotEmpty(taxLineRCSet)) {
      for (TaxLine taxLineRC : taxLineRCSet) {
        getOrCreateLine(purchaseOrder, purchaseOrderLine, taxLineRC, map, true);
      }
    }

    orderLineTaxService.addTaxEquivSpecificNote(
        purchaseOrderLine, customerSpecificNote, specificNotes);
  }

  protected void getOrCreateLine(
      PurchaseOrder purchaseOrder,
      PurchaseOrderLine purchaseOrderLine,
      TaxLine taxLine,
      Map<TaxLine, PurchaseOrderLineTax> map,
      boolean reverseCharged) {
    if (taxLine != null) {
      LOG.debug("VAT {}", taxLine);
      if (map.containsKey(taxLine)) {
        PurchaseOrderLineTax purchaseOrderLineVat = map.get(taxLine);
        purchaseOrderLineVat.setReverseCharged(reverseCharged);
        purchaseOrderLineVat.setExTaxBase(
            purchaseOrderLineVat.getExTaxBase().add(purchaseOrderLine.getExTaxTotal()));

      } else {
        PurchaseOrderLineTax purchaseOrderLineTax =
            createPurchaseOrderLineTax(purchaseOrder, purchaseOrderLine, taxLine, reverseCharged);
        map.put(taxLine, purchaseOrderLineTax);
      }
    }
  }

  protected PurchaseOrderLineTax createPurchaseOrderLineTax(
      PurchaseOrder purchaseOrder,
      PurchaseOrderLine purchaseOrderLine,
      TaxLine taxLine,
      boolean reverseCharged) {
    PurchaseOrderLineTax purchaseOrderLineTax = new PurchaseOrderLineTax();
    purchaseOrderLineTax.setPurchaseOrder(purchaseOrder);
    purchaseOrderLineTax.setReverseCharged(reverseCharged);
    purchaseOrderLineTax.setExTaxBase(purchaseOrderLine.getExTaxTotal());
    purchaseOrderLineTax.setTaxLine(taxLine);
    purchaseOrderLineTax.setTaxType(
        Optional.ofNullable(taxLine.getTax()).map(Tax::getTaxType).orElse(null));
    return purchaseOrderLineTax;
  }

  @Override
  public List<PurchaseOrderLineTax> getUpdatedPurchaseOrderLineTax(PurchaseOrder purchaseOrder) {
    List<PurchaseOrderLineTax> purchaseOrderLineTaxList = new ArrayList<>();

    if (ObjectUtils.isEmpty(purchaseOrder.getPurchaseOrderLineTaxList())) {
      return purchaseOrderLineTaxList;
    }

    purchaseOrderLineTaxList.addAll(
        purchaseOrder.getPurchaseOrderLineTaxList().stream()
            .filter(
                purchaseOrderLineTax ->
                    orderLineTaxService.isManageByAmount(purchaseOrderLineTax)
                        && purchaseOrderLineTax
                                .getTaxTotal()
                                .compareTo(purchaseOrderLineTax.getPercentageTaxTotal())
                            != 0)
            .collect(Collectors.toList()));
    return purchaseOrderLineTaxList;
  }
}
