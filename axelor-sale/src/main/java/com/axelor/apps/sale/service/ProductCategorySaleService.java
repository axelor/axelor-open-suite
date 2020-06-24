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
