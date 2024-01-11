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
package com.axelor.apps.purchase.service;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.PurchaseOrderLineTax;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
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

  @Inject private PurchaseOrderToolService purchaseOrderToolService;

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

        if (taxLine != null) {
          LOG.debug("VAT {}", taxLine);

          if (map.containsKey(taxLine)) {

            PurchaseOrderLineTax purchaseOrderLineVat = map.get(taxLine);

            purchaseOrderLineVat.setExTaxBase(
                purchaseOrderLineVat.getExTaxBase().add(purchaseOrderLine.getExTaxTotal()));

            purchaseOrderLineVat.setReverseCharged(false);

          } else {

            PurchaseOrderLineTax purchaseOrderLineTax = new PurchaseOrderLineTax();
            purchaseOrderLineTax.setPurchaseOrder(purchaseOrder);

            purchaseOrderLineTax.setExTaxBase(purchaseOrderLine.getExTaxTotal());

            purchaseOrderLineTax.setReverseCharged(false);

            purchaseOrderLineTax.setTaxLine(taxLine);
            map.put(taxLine, purchaseOrderLineTax);
          }
        }

        if (taxLineRC != null) {
          LOG.debug("VAT {}", taxLineRC);

          if (map.containsKey(taxLineRC)) {

            PurchaseOrderLineTax purchaseOrderLineRC =
                map.get(taxEquiv.getReverseChargeTax().getActiveTaxLine());

            purchaseOrderLineRC.setExTaxBase(
                purchaseOrderLineRC.getExTaxBase().add(purchaseOrderLine.getExTaxTotal()));

            purchaseOrderLineRC.setReverseCharged(true);

          } else {

            PurchaseOrderLineTax purchaseOrderLineTaxRC = new PurchaseOrderLineTax();
            purchaseOrderLineTaxRC.setPurchaseOrder(purchaseOrder);

            purchaseOrderLineTaxRC.setExTaxBase(purchaseOrderLine.getExTaxTotal());

            purchaseOrderLineTaxRC.setReverseCharged(true);

            purchaseOrderLineTaxRC.setTaxLine(taxLineRC);
            map.put(taxLineRC, purchaseOrderLineTaxRC);
          }
        }
        if (!customerSpecificNote) {
          if (taxEquiv != null && taxEquiv.getSpecificNote() != null) {
            specificNotes.add(taxEquiv.getSpecificNote());
          }
        }
      }
    }

    for (PurchaseOrderLineTax purchaseOrderLineTax : map.values()) {

      // Dans la devise de la commande
      BigDecimal exTaxBase =
          (purchaseOrderLineTax.getReverseCharged())
              ? purchaseOrderLineTax.getExTaxBase().negate()
              : purchaseOrderLineTax.getExTaxBase();
      BigDecimal taxTotal = BigDecimal.ZERO;
      if (purchaseOrderLineTax.getTaxLine() != null)
        taxTotal =
            purchaseOrderToolService.computeAmount(
                exTaxBase,
                purchaseOrderLineTax.getTaxLine().getValue().divide(new BigDecimal(100)));
      purchaseOrderLineTax.setTaxTotal(taxTotal);
      purchaseOrderLineTax.setInTaxTotal(purchaseOrderLineTax.getExTaxBase().add(taxTotal));

      purchaseOrderLineTaxList.add(purchaseOrderLineTax);

      LOG.debug(
          "Tax line : Tax total => {}, Total W.T. => {}",
          new Object[] {purchaseOrderLineTax.getTaxTotal(), purchaseOrderLineTax.getInTaxTotal()});
    }

    if (!customerSpecificNote) {
      purchaseOrder.setSpecificNotes(Joiner.on('\n').join(specificNotes));
    } else {
      purchaseOrder.setSpecificNotes(purchaseOrder.getSupplierPartner().getSpecificTaxNote());
    }

    return purchaseOrderLineTaxList;
  }
}
