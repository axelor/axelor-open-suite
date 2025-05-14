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
import com.axelor.apps.purchase.db.PurchaseOrderLineTax;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PurchaseOrderLineTaxComputeService {

  void computeAndAddTaxToList(
      Map<TaxLine, PurchaseOrderLineTax> map,
      List<PurchaseOrderLineTax> purchaseOrderLineTaxList,
      Currency currency,
      List<PurchaseOrderLineTax> currentPurchaseOrderLineTaxList);

  void computePurchaseOrderLineTax(
      PurchaseOrderLineTax purchaseOrderLineTax,
      Currency currency,
      BigDecimal taxTotal,
      List<PurchaseOrderLineTax> currentPurchaseOrderLineTaxList,
      List<PurchaseOrderLineTax> purchaseOrderLineTaxList);

  BigDecimal computeTaxLineTaxTotal(TaxLine taxLine, PurchaseOrderLineTax purchaseOrderLineTax);
}
