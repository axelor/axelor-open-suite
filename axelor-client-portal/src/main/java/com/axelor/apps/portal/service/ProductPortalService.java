/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;

public interface ProductPortalService {

  BigDecimal getAvailableQty(Product product, Company company, StockLocation stockLocation)
      throws AxelorException;

  BigDecimal getUnitPrice(Product product, Currency targetCurrency, Company company, Boolean isAti)
      throws AxelorException;

  BigDecimal getUnitPriceDiscounted(
      Product product, Currency targetCurrency, Company company, Boolean isAti)
      throws AxelorException;

  String getDiscountStr(Product product, Currency currency, Company company) throws AxelorException;
}
