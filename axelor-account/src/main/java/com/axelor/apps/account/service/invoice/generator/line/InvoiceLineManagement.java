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
package com.axelor.apps.account.service.invoice.generator.line;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class InvoiceLineManagement {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public abstract List<?> creates() throws AxelorException;

  /**
   * Compute the quantity per the unit price
   *
   * @param quantity
   * @param price The unit price.
   * @return The Excluded tax total amount.
   */
  public static BigDecimal computeAmount(BigDecimal quantity, BigDecimal price) {

    BigDecimal amount =
        quantity
            .multiply(price)
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);

    LOG.debug(
        "Computation of W.T. amount with quantity of {} for {} : {}",
        new Object[] {quantity, price, amount});

    return amount;
  }

  /**
   * Compute the quantity per the unit price
   *
   * @param quantity
   * @param price The unit price.
   * @param scale Scale to apply on the result
   * @return The Excluded tax total amount.
   */
  public static BigDecimal computeAmount(BigDecimal quantity, BigDecimal price, int scale) {

    BigDecimal amount = quantity.multiply(price).setScale(scale, RoundingMode.HALF_UP);

    LOG.debug(
        "Computation of W.T. amount with quantity of {} for {} : {}",
        new Object[] {quantity, price, amount});

    return amount;
  }
}
