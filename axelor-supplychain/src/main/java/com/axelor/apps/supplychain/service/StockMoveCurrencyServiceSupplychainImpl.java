/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.service.StockMoveCurrencyServiceImpl;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;

public class StockMoveCurrencyServiceSupplychainImpl extends StockMoveCurrencyServiceImpl {

  @Override
  public Currency getCurrency(StockMove stockMove) {
    if (isMultiCurrency(stockMove)) {
      return super.getCurrency(stockMove);
    }
    if (CollectionUtils.isNotEmpty(stockMove.getSaleOrderSet())) {
      Currency currency =
          stockMove.getSaleOrderSet().stream()
              .map(SaleOrder::getCurrency)
              .filter(Objects::nonNull)
              .findFirst()
              .orElse(null);
      if (currency != null) {
        return currency;
      }
    }
    if (CollectionUtils.isNotEmpty(stockMove.getPurchaseOrderSet())) {
      Currency currency =
          stockMove.getPurchaseOrderSet().stream()
              .map(PurchaseOrder::getCurrency)
              .filter(Objects::nonNull)
              .findFirst()
              .orElse(null);
      if (currency != null) {
        return currency;
      }
    }
    return super.getCurrency(stockMove);
  }

  @Override
  public boolean isMultiCurrency(StockMove stockMove) {
    Set<Currency> currencySet =
        Stream.concat(
                stockMove.getSaleOrderSet().stream().map(SaleOrder::getCurrency),
                stockMove.getPurchaseOrderSet().stream().map(PurchaseOrder::getCurrency))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    if (CollectionUtils.isEmpty(currencySet)) {
      return super.isMultiCurrency(stockMove);
    }
    return currencySet.size() > 1;
  }
}
