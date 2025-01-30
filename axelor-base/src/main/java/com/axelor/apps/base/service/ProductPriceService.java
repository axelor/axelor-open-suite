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
package com.axelor.apps.base.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public interface ProductPriceService {
  BigDecimal getSaleUnitPrice(
      Company company,
      Product product,
      boolean inAti,
      Partner partner,
      Currency currency,
      boolean conmpanyInAti)
      throws AxelorException;

  BigDecimal getSaleUnitPrice(Company company, Product product, boolean inAti, Partner partner)
      throws AxelorException;

  BigDecimal getSaleUnitPrice(
      Company company,
      Product product,
      Set<TaxLine> taxLineSet,
      boolean resultInAti,
      LocalDate localDate,
      Currency toCurrency)
      throws AxelorException;

  BigDecimal getPurchaseUnitPrice(
      Company company,
      Product product,
      Set<TaxLine> taxLineSet,
      boolean resultInAti,
      LocalDate localDate,
      Currency toCurrency)
      throws AxelorException;

  BigDecimal getConvertedPrice(
      Company company,
      Product product,
      Set<TaxLine> taxLineSet,
      boolean resultInAti,
      LocalDate localDate,
      BigDecimal price,
      Currency fromCurrency,
      Currency toCurrency)
      throws AxelorException;
}
