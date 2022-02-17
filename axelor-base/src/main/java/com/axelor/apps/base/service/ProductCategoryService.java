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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ProductCategory;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.util.Optional;

public interface ProductCategoryService {

  /**
   * Compute discount message from existing parent and child categories with a discount.
   *
   * @param productCategory a product category
   * @return a discount message describing discounts from other categories
   */
  String computeDiscountMessage(ProductCategory productCategory) throws AxelorException;

  /**
   * The maximum applicable discount for a product category is the max discount of the category or,
   * if this value is empty, the maximum applicable discount of its parent.
   *
   * @param productCategory
   * @return an optional with the maximum applicable discount if found.
   */
  Optional<BigDecimal> computeMaxDiscount(ProductCategory productCategory) throws AxelorException;
}
