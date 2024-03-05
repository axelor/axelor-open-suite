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
import com.axelor.apps.base.service.CurrencyScaleServiceImpl;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;

public class CurrencyScaleServiceSaleImpl extends CurrencyScaleServiceImpl
    implements CurrencyScaleServiceSale {

  @Override
  public BigDecimal getScaledValue(SaleOrder saleOrder, BigDecimal amount) {
    return this.getScaledValue(amount, this.getScale(saleOrder.getCurrency()));
  }

  @Override
  public BigDecimal getCompanyScaledValue(SaleOrder saleOrder, BigDecimal amount) {
    return this.getScaledValue(amount, this.getCompanyScale(saleOrder.getCompany()));
  }

  @Override
  public BigDecimal getScaledValue(SaleOrderLine saleOrderLine, BigDecimal amount) {
    return saleOrderLine.getSaleOrder() != null
        ? this.getScaledValue(saleOrderLine.getSaleOrder(), amount)
        : this.getScaledValue(amount);
  }

  @Override
  public BigDecimal getCompanyScaledValue(SaleOrderLine saleOrderLine, BigDecimal amount) {
    return saleOrderLine.getSaleOrder() != null
        ? this.getCompanyScaledValue(saleOrderLine.getSaleOrder(), amount)
        : this.getScaledValue(amount);
  }

  @Override
  public int getScale(SaleOrder saleOrder) {
    return this.getScale(saleOrder.getCurrency());
  }

  @Override
  public int getCompanyScale(SaleOrder saleOrder) {
    return this.getCompanyScale(saleOrder.getCompany());
  }

  @Override
  public int getScale(SaleOrderLine saleOrderLine) {
    return saleOrderLine.getSaleOrder() != null
        ? this.getScale(saleOrderLine.getSaleOrder())
        : this.getScale();
  }

  @Override
  public int getCompanyScale(SaleOrderLine saleOrderLine) {
    return saleOrderLine.getSaleOrder() != null
        ? this.getCompanyScale(saleOrderLine.getSaleOrder())
        : this.getScale();
  }

  @Override
  public int getScale(Currency currency) {
    return this.getCurrencyScale(currency);
  }

  @Override
  public int getCompanyScale(Company company) {
    return this.getCompanyCurrencyScale(company);
  }

  protected int getCompanyCurrencyScale(Company company) {
    return company != null && company.getCurrency() != null
        ? this.getCurrencyScale(company.getCurrency())
        : this.getScale();
  }

  protected int getCurrencyScale(Currency currency) {
    return currency != null ? currency.getNumberOfDecimals() : this.getScale();
  }
}
