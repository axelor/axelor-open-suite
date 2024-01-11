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

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface SupplierCatalogService {

  public Map<String, Object> updateInfoFromCatalog(
      Product product,
      BigDecimal qty,
      Partner partner,
      Currency currency,
      LocalDate date,
      Company company)
      throws AxelorException;

  public SupplierCatalog getSupplierCatalog(
      Product product, Partner supplierPartner, Company company) throws AxelorException;

  Map<String, String> getProductSupplierInfos(Partner partner, Company company, Product product)
      throws AxelorException;

  BigDecimal getQty(Product product, Partner supplierPartner, Company company)
      throws AxelorException;

  /**
   * A function used to get the unit price of a purchase order line or invoice line either in ati or
   * wt
   *
   * @param product
   * @param supplierPartner
   * @param company
   * @param currency
   * @param localDate
   * @param taxLine
   * @param resultInAti
   * @return
   * @throws AxelorException
   */
  BigDecimal getUnitPrice(
      Product product,
      Partner supplierPartner,
      Company company,
      Currency currency,
      LocalDate localDate,
      TaxLine taxLine,
      boolean resultInAti)
      throws AxelorException;

  BigDecimal getMinQty(Product product, Partner supplierPartner, Company company)
      throws AxelorException;

  void checkMinQty(
      Product product,
      Partner supplierPartner,
      Company company,
      BigDecimal qty,
      ActionRequest request,
      ActionResponse response)
      throws AxelorException;

  public BigDecimal getPurchasePrice(SupplierCatalog supplierCatalog, Company company)
      throws AxelorException;
}
