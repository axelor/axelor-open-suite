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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleServiceImpl;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import java.math.BigDecimal;

public class CurrencyScaleServicePurchaseImpl extends CurrencyScaleServiceImpl
    implements CurrencyScaleServicePurchase {

  @Override
  public BigDecimal getScaledValue(PurchaseOrder purchaseOrder, BigDecimal amount) {
    return this.getScaledValue(amount, this.getScale(purchaseOrder.getCurrency()));
  }

  @Override
  public BigDecimal getCompanyScaledValue(PurchaseOrder purchaseOrder, BigDecimal amount) {
    return this.getScaledValue(amount, this.getCompanyScale(purchaseOrder.getCompany()));
  }

  @Override
  public BigDecimal getScaledValue(PurchaseOrderLine purchaseOrderLine, BigDecimal amount) {
    return purchaseOrderLine.getPurchaseOrder() != null
        ? this.getScaledValue(purchaseOrderLine.getPurchaseOrder(), amount)
        : this.getScaledValue(amount);
  }

  @Override
  public BigDecimal getCompanyScaledValue(PurchaseOrderLine purchaseOrderLine, BigDecimal amount) {
    return purchaseOrderLine.getPurchaseOrder() != null
        ? this.getCompanyScaledValue(purchaseOrderLine.getPurchaseOrder(), amount)
        : this.getScaledValue(amount);
  }

  @Override
  public int getScale(PurchaseOrder purchaseOrder) {
    return this.getScale(purchaseOrder.getCurrency());
  }

  @Override
  public int getCompanyScale(PurchaseOrder purchaseOrder) {
    return this.getCompanyScale(purchaseOrder.getCompany());
  }

  @Override
  public int getScale(PurchaseOrderLine purchaseOrderLine) {
    return purchaseOrderLine.getPurchaseOrder() != null
        ? this.getScale(purchaseOrderLine.getPurchaseOrder())
        : this.getScale();
  }

  @Override
  public int getCompanyScale(PurchaseOrderLine purchaseOrderLine) {
    return purchaseOrderLine.getPurchaseOrder() != null
        ? this.getCompanyScale(purchaseOrderLine.getPurchaseOrder())
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
