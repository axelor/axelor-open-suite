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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.ProductVariantAttr;
import com.axelor.apps.base.db.ProductVariantValue;
import java.math.BigDecimal;

public interface ProductVariantService {

  ProductVariant createProductVariant(
      ProductVariantAttr productVariantAttr1,
      ProductVariantAttr productVariantAttr2,
      ProductVariantAttr productVariantAttr3,
      ProductVariantAttr productVariantAttr4,
      ProductVariantAttr productVariantAttr5,
      ProductVariantValue productVariantValue1,
      ProductVariantValue productVariantValue2,
      ProductVariantValue productVariantValue3,
      ProductVariantValue productVariantValue4,
      ProductVariantValue productVariantValue5,
      boolean usedForStock);

  ProductVariantValue createProductVariantValue(
      ProductVariantAttr productVariantAttr, String code, String name, BigDecimal priceExtra);

  ProductVariantAttr createProductVariantAttr(String name);

  boolean equalsName(ProductVariant productVariant1, ProductVariant productVariant2);

  boolean equals(ProductVariant productVariant1, ProductVariant productVariant2);

  ProductVariant copyProductVariant(ProductVariant productVariant, boolean usedForStock);

  ProductVariant getStockProductVariant(ProductVariant productVariant);

  Product getProductVariant(Product parentProduct, Product productModel);

  Product copyAdditionalFields(Product product, Product productModel);
}
