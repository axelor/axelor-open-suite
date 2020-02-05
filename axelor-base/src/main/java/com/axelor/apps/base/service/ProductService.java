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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.ProductVariantConfig;
import com.axelor.apps.base.db.ProductVariantValue;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public interface ProductService {

  @Transactional
  public void updateProductPrice(Product product);

  public String getSequence() throws AxelorException;

  public void updateSalePrice(Product product);

  @Transactional
  public void generateProductVariants(Product productModel);

  public Product createProduct(Product productModel, ProductVariant productVariant, int seq);

  /**
   * @param productVariant
   * @param applicationPriceSelect - 1 : Sale price - 2 : Cost price
   * @return
   */
  public BigDecimal getProductExtraPrice(ProductVariant productVariant, int applicationPriceSelect);

  public ProductVariant createProductVariant(
      ProductVariantConfig productVariantConfig,
      ProductVariantValue productVariantValue1,
      ProductVariantValue productVariantValue2,
      ProductVariantValue productVariantValue3,
      ProductVariantValue productVariantValue4);

  public void copyProduct(Product product, Product copy);
}
