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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ProductCategory;
import java.math.BigDecimal;
import java.util.List;
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

  /**
   * Find child of given category, and recursively children of found children.
   *
   * @param productCategory a product category
   * @return all children of the category
   */
  List<ProductCategory> fetchChildrenCategoryList(ProductCategory productCategory)
      throws AxelorException;

  /**
   * Find parent of given category, and recursively parents of found parents.
   *
   * @param productCategory a product category
   * @return all parents of the category
   */
  List<ProductCategory> fetchParentCategoryList(ProductCategory productCategory)
      throws AxelorException;

  /**
   * Get the growth coefficient of product category. If the coeff is equal to the default value (1),
   * the method will get growth coeff of parentProductCategory.
   *
   * @param productCategory
   * @return growth coefficient
   */
  BigDecimal getGrowthCoeff(ProductCategory productCategory);
}
