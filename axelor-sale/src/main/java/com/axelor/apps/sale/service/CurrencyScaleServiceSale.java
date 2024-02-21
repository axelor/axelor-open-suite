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
package com.axelor.apps.sale.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;

public interface CurrencyScaleServiceSale {

  BigDecimal getScaledValue(SaleOrder saleOrder, BigDecimal amount);

  BigDecimal getCompanyScaledValue(SaleOrder saleOrder, BigDecimal amount);

  BigDecimal getScaledValue(SaleOrderLine saleOrderLine, BigDecimal amount);

  BigDecimal getCompanyScaledValue(SaleOrderLine saleOrderLine, BigDecimal amount);

  int getScale(SaleOrder saleOrder);

  int getCompanyScale(SaleOrder saleOrder);

  int getScale(SaleOrderLine saleOrderLine);

  int getCompanyScale(SaleOrderLine saleOrderLine);

  int getScale(Currency currency);

  int getCompanyScale(Company company);
}
