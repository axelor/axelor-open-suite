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
package com.axelor.apps.sale.service.saleorderline.product;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.CustomerCatalog;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineCustomerCatalogService {

  /**
   * Fill the customer catalog of the sale order line from its product and the customer of the sale
   * order. The current customer catalog is kept if it still matches the product and the customer,
   * otherwise it is replaced, or emptied if the product is not in the customer catalog.
   *
   * @param saleOrderLine
   * @param saleOrder
   * @return the map of the updated fields
   */
  Map<String, Object> computeCustomerCatalog(SaleOrderLine saleOrderLine, SaleOrder saleOrder);

  /**
   * Find the customer catalog of the given customer for the given product.
   *
   * @param product
   * @param customerPartner
   * @return the customer catalog, or null if the customer catalog management is disabled or if no
   *     customer catalog is found
   */
  CustomerCatalog getCustomerCatalog(Product product, Partner customerPartner);

  /**
   * Find the customer catalog of the given customer for the given customer reference, ignoring
   * case.
   *
   * @param productCustomerCode
   * @param customerPartner
   * @return the customer catalog, or null if the customer catalog management is disabled or if no
   *     customer catalog is found
   */
  CustomerCatalog getCustomerCatalog(String productCustomerCode, Partner customerPartner);

  /**
   * Recompute the customer catalog of all the lines of the sale order, including sub lines, for
   * instance after a change of customer.
   *
   * @param saleOrder
   */
  void updateSaleOrderLinesCustomerCatalog(SaleOrder saleOrder);
}
