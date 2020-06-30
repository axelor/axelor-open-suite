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
