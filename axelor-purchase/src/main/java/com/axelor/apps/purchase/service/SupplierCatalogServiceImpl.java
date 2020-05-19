/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.purchase.db.repo.SupplierCatalogRepository;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupplierCatalogServiceImpl implements SupplierCatalogService {

  @Inject protected AppBaseService appBaseService;

  @Inject protected AppPurchaseService appPurchaseService;

  @Inject protected CurrencyService currencyService;

  @Override
  public Map<String, Object> updateInfoFromCatalog(
      Product product, BigDecimal qty, Partner partner, Currency currency, LocalDate date)
      throws AxelorException {

    Map<String, Object> info = null;
    List<SupplierCatalog> supplierCatalogList = null;

    if (appPurchaseService.getAppPurchase().getManageSupplierCatalog()) {
      supplierCatalogList = product.getSupplierCatalogList();
    }
    if (supplierCatalogList != null && !supplierCatalogList.isEmpty()) {
      SupplierCatalog supplierCatalog =
          Beans.get(SupplierCatalogRepository.class)
              .all()
              .filter(
                  "self.product = ?1 AND self.minQty <= ?2 AND self.supplierPartner = ?3 ORDER BY self.minQty DESC",
                  product,
                  qty,
                  partner)
              .fetchOne();

      if (supplierCatalog != null) {
        info = new HashMap<>();
        info.put(
            "price",
            currencyService
                .getAmountCurrencyConvertedAtDate(
                    supplierCatalog.getSupplierPartner().getCurrency(),
                    currency,
                    supplierCatalog.getPrice(),
                    date)
                .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP));
        info.put("productName", supplierCatalog.getProductSupplierName());
        info.put("productCode", supplierCatalog.getProductSupplierCode());
      } else if (this.getSupplierCatalog(product, partner) != null) {
        info = new HashMap<>();
        info.put(
            "price",
            currencyService
                .getAmountCurrencyConvertedAtDate(
                    product.getPurchaseCurrency(), currency, product.getPurchasePrice(), date)
                .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP));
        info.put("productName", null);
        info.put("productCode", null);
      }
    }

    return info;
  }

  @Override
  public SupplierCatalog getSupplierCatalog(Product product, Partner supplierPartner) {

    if (appPurchaseService.getAppPurchase().getManageSupplierCatalog()
        && product != null
        && product.getSupplierCatalogList() != null) {
      SupplierCatalog resSupplierCatalog = null;

      for (SupplierCatalog supplierCatalog : product.getSupplierCatalogList()) {
        if (supplierCatalog.getSupplierPartner().equals(supplierPartner)) {
          resSupplierCatalog =
              (resSupplierCatalog == null
                      || resSupplierCatalog.getMinQty().compareTo(supplierCatalog.getMinQty()) > 0)
                  ? supplierCatalog
                  : resSupplierCatalog;
        }
      }

      return resSupplierCatalog;
    }
    return null;
  }
}
