/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderToolService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private CurrencyService currencyService;

  /**
   * Calculer le montant HT d'une ligne de commande.
   *
   * @param quantity Quantité.
   * @param price Le prix.
   * @return Le montant HT de la ligne.
   */
  public BigDecimal computeAmount(BigDecimal quantity, BigDecimal price) {

    BigDecimal amount =
        quantity
            .multiply(price)
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);

    LOG.debug(
        "Calcul du montant HT avec une quantité de {} pour {} : {}",
        new Object[] {quantity, price, amount});

    return amount;
  }

  public BigDecimal getAccountingExTaxTotal(BigDecimal exTaxTotal, PurchaseOrder purchaseOrder)
      throws AxelorException {

    return currencyService
        .getAmountCurrencyConvertedAtDate(
            purchaseOrder.getCurrency(),
            purchaseOrder.getSupplierPartner().getCurrency(),
            exTaxTotal,
            purchaseOrder.getOrderDate())
        .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
  }
}
