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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.tax.OrderLineTaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderLineTax;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderLineTaxServiceImpl implements SaleOrderLineTaxService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected OrderLineTaxService orderLineTaxService;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public SaleOrderLineTaxServiceImpl(
      OrderLineTaxService orderLineTaxService, CurrencyScaleService currencyScaleService) {
    this.orderLineTaxService = orderLineTaxService;
    this.currencyScaleService = currencyScaleService;
  }

  /**
   * Créer les lignes de TVA du devis. La création des lignes de TVA se basent sur les lignes de
   * devis ainsi que les sous-lignes de devis de celles-ci. Si une ligne de devis comporte des
   * sous-lignes de devis, alors on se base uniquement sur celles-ci.
   *
   * @param saleOrder Le devis de vente.
   * @param saleOrderLineList Les lignes du devis de vente.
   * @return La liste des lignes de taxe du devis de vente.
   */
  @Override
  public List<SaleOrderLineTax> createsSaleOrderLineTax(
      SaleOrder saleOrder, List<SaleOrderLine> saleOrderLineList) {

    List<SaleOrderLineTax> saleOrderLineTaxList = new ArrayList<>();
    Map<TaxLine, SaleOrderLineTax> map = new HashMap<>();
    Set<String> specificNotes = new HashSet<>();
    boolean customerSpecificNote = orderLineTaxService.isCustomerSpecificNote(saleOrder);

    if (CollectionUtils.isNotEmpty(saleOrderLineList)) {
      LOG.debug("Creation of VAT lines for sale order lines.");
      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        getOrCreateLines(saleOrder, saleOrderLine, map, customerSpecificNote, specificNotes);
      }
    }

    computeAndAddTaxToList(map, saleOrderLineTaxList, saleOrder.getCurrency());
    orderLineTaxService.setSpecificNotes(
        customerSpecificNote,
        saleOrder,
        specificNotes,
        saleOrder.getClientPartner().getSpecificTaxNote());

    return saleOrderLineTaxList;
  }

  protected void getOrCreateLines(
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      Map<TaxLine, SaleOrderLineTax> map,
      boolean customerSpecificNote,
      Set<String> specificNotes) {
    TaxLine taxLine = saleOrderLine.getTaxLine();
    getOrCreateLine(saleOrder, saleOrderLine, map, taxLine);
    orderLineTaxService.addTaxEquivSpecificNote(saleOrderLine, customerSpecificNote, specificNotes);
  }

  protected void getOrCreateLine(
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      Map<TaxLine, SaleOrderLineTax> map,
      TaxLine taxLine) {
    if (taxLine != null) {
      LOG.debug("Tax {}", taxLine);
      if (map.containsKey(taxLine)) {
        SaleOrderLineTax saleOrderLineTax = map.get(taxLine);
        saleOrderLineTax.setExTaxBase(
            currencyScaleService.getScaledValue(
                saleOrder, saleOrderLineTax.getExTaxBase().add(saleOrderLine.getExTaxTotal())));
      } else {
        SaleOrderLineTax saleOrderLineTax =
            createSaleOrderLineTax(saleOrder, saleOrderLine, taxLine);
        map.put(taxLine, saleOrderLineTax);
      }
    }
  }

  protected SaleOrderLineTax createSaleOrderLineTax(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, TaxLine taxLine) {
    SaleOrderLineTax saleOrderLineTax = new SaleOrderLineTax();
    saleOrderLineTax.setSaleOrder(saleOrder);
    saleOrderLineTax.setExTaxBase(saleOrderLine.getExTaxTotal());
    saleOrderLineTax.setTaxLine(taxLine);
    return saleOrderLineTax;
  }

  protected void computeAndAddTaxToList(
      Map<TaxLine, SaleOrderLineTax> map,
      List<SaleOrderLineTax> saleOrderLineTaxList,
      Currency currency) {
    for (SaleOrderLineTax saleOrderLineTax : map.values()) {
      // Dans la devise de la facture
      orderLineTaxService.computeTax(saleOrderLineTax, currency);
      saleOrderLineTaxList.add(saleOrderLineTax);
      LOG.debug(
          "VAT line : VAT total => {}, W.T. total => {}",
          saleOrderLineTax.getTaxTotal(),
          saleOrderLineTax.getInTaxTotal());
    }
  }
}
