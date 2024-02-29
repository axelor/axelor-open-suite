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
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import java.math.BigDecimal;

public interface CurrencyScaleServicePurchase {

  BigDecimal getScaledValue(PurchaseOrder purchaseOrder, BigDecimal amount);

  BigDecimal getCompanyScaledValue(PurchaseOrder purchaseOrder, BigDecimal amount);

  BigDecimal getScaledValue(PurchaseOrderLine purchaseOrderLine, BigDecimal amount);

  BigDecimal getCompanyScaledValue(PurchaseOrderLine purchaseOrderLine, BigDecimal amount);

  int getScale(PurchaseOrder purchaseOrder);

  int getCompanyScale(PurchaseOrder purchaseOrder);

  int getScale(PurchaseOrderLine purchaseOrderLine);

  int getCompanyScale(PurchaseOrderLine purchaseOrderLine);

  int getScale(Currency currency);

  int getCompanyScale(Company company);
}
