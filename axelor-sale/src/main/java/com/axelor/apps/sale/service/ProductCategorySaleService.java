/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service;

import com.axelor.apps.base.db.ProductCategory;
import com.axelor.exception.AxelorException;

public interface ProductCategorySaleService {

  /**
   * Set discountsNeedReview boolean in sale order lines that are affected by the discount change
   * made on product category.
   *
   * @param productCategory a product category having its maxDiscount being changed.
   */
  void updateSaleOrderLines(ProductCategory productCategory) throws AxelorException;
}
