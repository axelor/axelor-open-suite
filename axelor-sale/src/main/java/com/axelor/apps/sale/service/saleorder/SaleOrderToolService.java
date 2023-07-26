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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderToolService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject protected CurrencyService currencyService;

  /**
   * Calculer le montant HT d'une ligne de devis.
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
        "Computation of W.T. amount with a quantity of {} for {} : {}",
        new Object[] {quantity, price, amount});

    return amount;
  }

  public BigDecimal getAccountingExTaxTotal(BigDecimal exTaxTotal, SaleOrder saleOrder)
      throws AxelorException {

    return currencyService
        .getAmountCurrencyConvertedAtDate(
            saleOrder.getCurrency(),
            saleOrder.getClientPartner().getCurrency(),
            exTaxTotal,
            saleOrder.getCreationDate())
        .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
  }
}
