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

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrderLineTax;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderLineTaxComputeServiceImpl implements PurchaseOrderLineTaxComputeService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected CurrencyScaleService currencyScaleService;

  @Inject
  public PurchaseOrderLineTaxComputeServiceImpl(CurrencyScaleService currencyScaleService) {
    this.currencyScaleService = currencyScaleService;
  }

  public void computeAndAddTaxToList(
      Map<TaxLine, PurchaseOrderLineTax> map,
      List<PurchaseOrderLineTax> purchaseOrderLineTaxList,
      Currency currency,
      List<PurchaseOrderLineTax> currentPurchaseOrderLineTaxList) {
    for (PurchaseOrderLineTax purchaseOrderLineTax : map.values()) {
      computeAndAddPurchaseOrderLineTax(
          purchaseOrderLineTax,
          purchaseOrderLineTaxList,
          currency,
          currentPurchaseOrderLineTaxList);
    }
  }

  protected void computeAndAddPurchaseOrderLineTax(
      PurchaseOrderLineTax purchaseOrderLineTax,
      List<PurchaseOrderLineTax> purchaseOrderLineTaxList,
      Currency currency,
      List<PurchaseOrderLineTax> currentPurchaseOrderLineTaxList) {
    TaxLine taxLine = purchaseOrderLineTax.getTaxLine();
    BigDecimal taxTotal = this.computeTaxLineTaxTotal(taxLine, purchaseOrderLineTax);

    this.computePurchaseOrderLineTax(
        purchaseOrderLineTax,
        currency,
        taxTotal,
        currentPurchaseOrderLineTaxList,
        purchaseOrderLineTaxList);
  }

  public void computePurchaseOrderLineTax(
      PurchaseOrderLineTax purchaseOrderLineTax,
      Currency currency,
      BigDecimal taxTotal,
      List<PurchaseOrderLineTax> currentPurchaseOrderLineTaxList,
      List<PurchaseOrderLineTax> purchaseOrderLineTaxList) {
    int currencyScale = currencyScaleService.getCurrencyScale(currency);

    purchaseOrderLineTax.setTaxTotal(currencyScaleService.getScaledValue(taxTotal, currencyScale));
    purchaseOrderLineTax.setInTaxTotal(
        currencyScaleService.getScaledValue(
            purchaseOrderLineTax.getExTaxBase().add(taxTotal), currencyScale));
    purchaseOrderLineTax.setPercentageTaxTotal(purchaseOrderLineTax.getTaxTotal());

    PurchaseOrderLineTax oldPurchaseOrderLineTax =
        getExistingPurchaseOrderLineTax(purchaseOrderLineTax, currentPurchaseOrderLineTaxList);
    if (oldPurchaseOrderLineTax == null) {
      purchaseOrderLineTaxList.add(purchaseOrderLineTax);

      LOG.debug(
          "Tax line : Tax total => {}, Total W.T. => {}",
          purchaseOrderLineTax.getTaxTotal(),
          purchaseOrderLineTax.getInTaxTotal());
    } else {
      purchaseOrderLineTaxList.add(oldPurchaseOrderLineTax);
    }
  }

  protected PurchaseOrderLineTax getExistingPurchaseOrderLineTax(
      PurchaseOrderLineTax purchaseOrderLineTax,
      List<PurchaseOrderLineTax> purchaseOrderLineTaxList) {
    if (ObjectUtils.isEmpty(purchaseOrderLineTaxList) || purchaseOrderLineTax == null) {
      return null;
    }

    for (PurchaseOrderLineTax purchaseOrderLineTaxItem : purchaseOrderLineTaxList) {
      if (Objects.equals(purchaseOrderLineTaxItem.getTaxLine(), purchaseOrderLineTax.getTaxLine())
          && purchaseOrderLineTaxItem
                  .getPercentageTaxTotal()
                  .compareTo(purchaseOrderLineTax.getTaxTotal())
              == 0
          && purchaseOrderLineTaxItem.getExTaxBase().compareTo(purchaseOrderLineTax.getExTaxBase())
              == 0) {
        return purchaseOrderLineTaxItem;
      }
    }

    return null;
  }

  public BigDecimal computeTaxLineTaxTotal(
      TaxLine taxLine, PurchaseOrderLineTax purchaseOrderLineTax) {
    BigDecimal taxTotal = BigDecimal.ZERO;

    // Dans la devise de la commande
    BigDecimal exTaxBase =
        purchaseOrderLineTax.getReverseCharged()
            ? purchaseOrderLineTax.getExTaxBase().negate()
            : purchaseOrderLineTax.getExTaxBase();

    if (taxLine != null) {
      taxTotal =
          exTaxBase.multiply(
              taxLine
                  .getValue()
                  .divide(
                      new BigDecimal(100),
                      AppBaseService.COMPUTATION_SCALING,
                      RoundingMode.HALF_UP));
    }

    return taxTotal;
  }
}
