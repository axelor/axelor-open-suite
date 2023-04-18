/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.PurchaseOrderLineTax;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderLineTaxService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected CurrencyService currencyService;

  @Inject
  public PurchaseOrderLineTaxService(CurrencyService currencyService) {
    this.currencyService = currencyService;
  }

  /**
   * Créer les lignes de TVA de la commande. La création des lignes de TVA se basent sur les lignes
   * de commande.
   *
   * @param purchaseOrder La commande.
   * @param purchaseOrderLineList Les lignes de commandes.
   * @return La liste des lignes de TVA de la commande.
   */
  public List<PurchaseOrderLineTax> createsPurchaseOrderLineTax(
      PurchaseOrder purchaseOrder, List<PurchaseOrderLine> purchaseOrderLineList) {

    List<PurchaseOrderLineTax> purchaseOrderLineTaxList = new ArrayList<PurchaseOrderLineTax>();
    Map<TaxLine, PurchaseOrderLineTax> map = new HashMap<TaxLine, PurchaseOrderLineTax>();
    Set<String> specificNotes = new HashSet<String>();

    boolean customerSpecificNote = false;
    FiscalPosition fiscalPosition = purchaseOrder.getFiscalPosition();
    if (fiscalPosition != null) {
      customerSpecificNote = fiscalPosition.getCustomerSpecificNote();
    }

    createTaxLines(purchaseOrder, purchaseOrderLineList, map, specificNotes, customerSpecificNote);
    computeLinesTax(purchaseOrder, purchaseOrderLineTaxList, map);
    setSpecificNotes(purchaseOrder, specificNotes, customerSpecificNote);

    return purchaseOrderLineTaxList;
  }

  protected void setSpecificNotes(
      PurchaseOrder purchaseOrder, Set<String> specificNotes, boolean customerSpecificNote) {
    if (!customerSpecificNote) {
      purchaseOrder.setSpecificNotes(Joiner.on('\n').join(specificNotes));
    } else {
      purchaseOrder.setSpecificNotes(purchaseOrder.getSupplierPartner().getSpecificTaxNote());
    }
  }

  protected void computeLinesTax(
      PurchaseOrder purchaseOrder,
      List<PurchaseOrderLineTax> purchaseOrderLineTaxList,
      Map<TaxLine, PurchaseOrderLineTax> map) {
    for (PurchaseOrderLineTax purchaseOrderLineTax : map.values()) {
      // Dans la devise de la commande
      BigDecimal exTaxBase =
          (purchaseOrderLineTax.getReverseCharged())
              ? purchaseOrderLineTax.getExTaxBase().negate()
              : purchaseOrderLineTax.getExTaxBase();
      BigDecimal taxTotal = BigDecimal.ZERO;
      TaxLine taxLine = purchaseOrderLineTax.getTaxLine();
      int scale = currencyService.computeScaleForView(purchaseOrder.getCurrency());

      if (taxLine != null) {
        taxTotal =
            exTaxBase.multiply(
                taxLine.getValue().divide(new BigDecimal(100), scale, RoundingMode.HALF_UP));
      }

      purchaseOrderLineTax.setTaxTotal(taxTotal.setScale(scale, RoundingMode.HALF_UP));
      purchaseOrderLineTax.setInTaxTotal(
          purchaseOrderLineTax.getExTaxBase().add(taxTotal).setScale(scale, RoundingMode.HALF_UP));
      purchaseOrderLineTaxList.add(purchaseOrderLineTax);

      LOG.debug(
          "Tax line : Tax total => {}, Total W.T. => {}",
          new Object[] {purchaseOrderLineTax.getTaxTotal(), purchaseOrderLineTax.getInTaxTotal()});
    }
  }

  protected void createTaxLines(
      PurchaseOrder purchaseOrder,
      List<PurchaseOrderLine> purchaseOrderLineList,
      Map<TaxLine, PurchaseOrderLineTax> map,
      Set<String> specificNotes,
      boolean customerSpecificNote) {
    if (purchaseOrderLineList != null && !purchaseOrderLineList.isEmpty()) {
      LOG.debug("Creation of tax lines for purchase order lines.");
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
        TaxLine taxLine = purchaseOrderLine.getTaxLine();
        TaxEquiv taxEquiv = purchaseOrderLine.getTaxEquiv();
        TaxLine taxLineRC =
            (taxEquiv != null
                    && taxEquiv.getReverseCharge()
                    && taxEquiv.getReverseChargeTax() != null)
                ? taxEquiv.getReverseChargeTax().getActiveTaxLine()
                : null;

        createLineTax(purchaseOrder, map, purchaseOrderLine, taxLine);
        createLineTaxRC(purchaseOrder, map, purchaseOrderLine, taxEquiv, taxLineRC);
        setSpecificNotes(specificNotes, customerSpecificNote, taxEquiv);
      }
    }
  }

  protected void setSpecificNotes(
      Set<String> specificNotes, boolean customerSpecificNote, TaxEquiv taxEquiv) {
    if (!customerSpecificNote) {
      if (taxEquiv != null && taxEquiv.getSpecificNote() != null) {
        specificNotes.add(taxEquiv.getSpecificNote());
      }
    }
  }

  protected void createLineTaxRC(
      PurchaseOrder purchaseOrder,
      Map<TaxLine, PurchaseOrderLineTax> map,
      PurchaseOrderLine purchaseOrderLine,
      TaxEquiv taxEquiv,
      TaxLine taxLineRC) {
    if (taxLineRC != null) {
      LOG.debug("VAT {}", taxLineRC);
      if (map.containsKey(taxLineRC)) {
        setPurchaseOrderLineTax(
            map, taxEquiv.getReverseChargeTax().getActiveTaxLine(), purchaseOrderLine, true);
      } else {
        map.put(
            taxLineRC, getPurchaseOrderLineTax(purchaseOrder, purchaseOrderLine, true, taxLineRC));
      }
    }
  }

  protected void createLineTax(
      PurchaseOrder purchaseOrder,
      Map<TaxLine, PurchaseOrderLineTax> map,
      PurchaseOrderLine purchaseOrderLine,
      TaxLine taxLine) {
    if (taxLine != null) {
      LOG.debug("VAT {}", taxLine);
      if (map.containsKey(taxLine)) {
        setPurchaseOrderLineTax(map, taxLine, purchaseOrderLine, false);
      } else {
        map.put(taxLine, getPurchaseOrderLineTax(purchaseOrder, purchaseOrderLine, false, taxLine));
      }
    }
  }

  protected void setPurchaseOrderLineTax(
      Map<TaxLine, PurchaseOrderLineTax> map,
      TaxLine taxLine,
      PurchaseOrderLine purchaseOrderLine,
      boolean reversedCharged) {
    PurchaseOrderLineTax purchaseOrderLineVat = map.get(taxLine);
    purchaseOrderLineVat.setExTaxBase(
        purchaseOrderLineVat.getExTaxBase().add(purchaseOrderLine.getExTaxTotal()));
    purchaseOrderLineVat.setReverseCharged(reversedCharged);
  }

  protected PurchaseOrderLineTax getPurchaseOrderLineTax(
      PurchaseOrder purchaseOrder,
      PurchaseOrderLine purchaseOrderLine,
      boolean reversedCharged,
      TaxLine taxLine) {
    PurchaseOrderLineTax purchaseOrderLineTax = new PurchaseOrderLineTax();
    purchaseOrderLineTax.setPurchaseOrder(purchaseOrder);
    purchaseOrderLineTax.setExTaxBase(purchaseOrderLine.getExTaxTotal());
    purchaseOrderLineTax.setReverseCharged(reversedCharged);
    purchaseOrderLineTax.setTaxLine(taxLine);
    return purchaseOrderLineTax;
  }
}
