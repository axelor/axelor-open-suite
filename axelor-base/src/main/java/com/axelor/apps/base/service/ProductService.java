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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.ProductVariantConfig;
import com.axelor.apps.base.db.ProductVariantValue;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public interface ProductService {

  @Transactional(rollbackOn = {Exception.class})
  public void updateProductPrice(Product product) throws AxelorException;

  public String getSequence(Product product) throws AxelorException;

  public void updateSalePrice(Product product, Company company) throws AxelorException;

  public boolean hasActivePriceList(Product product);

  @Transactional(rollbackOn = {Exception.class})
  public void generateProductVariants(Product productModel) throws AxelorException;

  public Product createProduct(Product productModel, ProductVariant productVariant, int seq)
      throws AxelorException;

  /**
   * @param productVariant
   * @param applicationPriceSelect - 1 : Sale price - 2 : Cost price - 3 : Purchase Price
   * @return
   */
  public BigDecimal getProductExtraPrice(ProductVariant productVariant, int applicationPriceSelect);

  public ProductVariant createProductVariant(
      ProductVariantConfig productVariantConfig,
      ProductVariantValue productVariantValue1,
      ProductVariantValue productVariantValue2,
      ProductVariantValue productVariantValue3,
      ProductVariantValue productVariantValue4,
      ProductVariantValue productVariantValue5);

  public void copyProduct(Product product, Product copy);
}
