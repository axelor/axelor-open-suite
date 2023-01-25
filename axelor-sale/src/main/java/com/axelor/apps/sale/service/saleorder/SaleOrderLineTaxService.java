/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderLineTax;
import com.google.common.base.Joiner;
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

public class SaleOrderLineTaxService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Créer les lignes de TVA du devis. La création des lignes de TVA se basent sur les lignes de
   * devis ainsi que les sous-lignes de devis de celles-ci. Si une ligne de devis comporte des
   * sous-lignes de devis, alors on se base uniquement sur celles-ci.
   *
   * @param saleOrder Le devis de vente.
   * @param saleOrderLineList Les lignes du devis de vente.
   * @return La liste des lignes de taxe du devis de vente.
   */
  public List<SaleOrderLineTax> createsSaleOrderLineTax(
      SaleOrder saleOrder, List<SaleOrderLine> saleOrderLineList) {

    List<SaleOrderLineTax> saleOrderLineTaxList = new ArrayList<SaleOrderLineTax>();
    Map<TaxLine, SaleOrderLineTax> map = new HashMap<TaxLine, SaleOrderLineTax>();
    Set<String> specificNotes = new HashSet<String>();

    boolean customerSpecificNote = false;
    FiscalPosition fiscalPosition = saleOrder.getFiscalPosition();
    if (fiscalPosition != null) {
      customerSpecificNote = fiscalPosition.getCustomerSpecificNote();
    }

    createVatLines(saleOrder, saleOrderLineList, map, specificNotes, customerSpecificNote);
    computeTaxes(saleOrderLineTaxList, map);
    setSpecificNotes(saleOrder, specificNotes, customerSpecificNote);

    return saleOrderLineTaxList;
  }

  protected void setSpecificNotes(
      SaleOrder saleOrder, Set<String> specificNotes, boolean customerSpecificNote) {
    if (!customerSpecificNote) {
      saleOrder.setSpecificNotes(Joiner.on('\n').join(specificNotes));
    } else {
      saleOrder.setSpecificNotes(saleOrder.getClientPartner().getSpecificTaxNote());
    }
  }

  protected void computeTaxes(
      List<SaleOrderLineTax> saleOrderLineTaxList, Map<TaxLine, SaleOrderLineTax> map) {
    for (SaleOrderLineTax saleOrderLineTax : map.values()) {

      // Dans la devise de la facture
      BigDecimal exTaxBase = saleOrderLineTax.getExTaxBase();
      BigDecimal taxTotal = BigDecimal.ZERO;
      if (saleOrderLineTax.getTaxLine() != null) {
        BigDecimal taxValue =
            saleOrderLineTax
                .getTaxLine()
                .getValue()
                .divide(new BigDecimal(100), RoundingMode.HALF_UP);
        taxTotal = exTaxBase.multiply(taxValue);
        saleOrderLineTax.setTaxTotal(taxTotal);
      }
      saleOrderLineTax.setInTaxTotal(exTaxBase.add(taxTotal));
      saleOrderLineTaxList.add(saleOrderLineTax);

      LOG.debug(
          "VAT line : VAT total => {}, W.T. total => {}",
          new Object[] {saleOrderLineTax.getTaxTotal(), saleOrderLineTax.getInTaxTotal()});
    }
  }

  protected void createVatLines(
      SaleOrder saleOrder,
      List<SaleOrderLine> saleOrderLineList,
      Map<TaxLine, SaleOrderLineTax> map,
      Set<String> specificNotes,
      boolean customerSpecificNote) {
    if (saleOrderLineList == null && saleOrderLineList.isEmpty()) {
      return;
    }
    LOG.debug("Creation of VAT lines for sale order lines.");
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      TaxLine taxLine = saleOrderLine.getTaxLine();
      setDefaultValues(saleOrder, map, saleOrderLine, taxLine);
      addCustomerSpecificNotes(specificNotes, customerSpecificNote, saleOrderLine);
    }
  }

  protected void addCustomerSpecificNotes(
      Set<String> specificNotes, boolean customerSpecificNote, SaleOrderLine saleOrderLine) {
    if (customerSpecificNote) {
      return;
    }
    TaxEquiv taxEquiv = saleOrderLine.getTaxEquiv();
    if (taxEquiv != null && taxEquiv.getSpecificNote() != null) {
      specificNotes.add(taxEquiv.getSpecificNote());
    }
  }

  protected void setDefaultValues(
      SaleOrder saleOrder,
      Map<TaxLine, SaleOrderLineTax> map,
      SaleOrderLine saleOrderLine,
      TaxLine taxLine) {
    if (taxLine == null) {
      return;
    }
    LOG.debug("Tax {}", taxLine);
    if (map.containsKey(taxLine)) {
      SaleOrderLineTax saleOrderLineTax = map.get(taxLine);
      saleOrderLineTax.setExTaxBase(
          saleOrderLineTax.getExTaxBase().add(saleOrderLine.getExTaxTotal()));
    } else {
      SaleOrderLineTax saleOrderLineTax = new SaleOrderLineTax();
      saleOrderLineTax.setSaleOrder(saleOrder);
      saleOrderLineTax.setExTaxBase(saleOrderLine.getExTaxTotal());
      saleOrderLineTax.setTaxLine(taxLine);
      map.put(taxLine, saleOrderLineTax);
    }
  }
}
