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
package com.axelor.apps.supplierportal.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.supplierportal.db.ProductSupplier;
import com.axelor.apps.supplierportal.db.repo.ProductSupplierRepository;
import com.axelor.apps.supplierportal.exceptions.SupplierPortalExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class ProductSupplierServiceImpl implements ProductSupplierService {

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public Product addOnCatalog(ProductSupplier productSupplier) throws AxelorException {
    ProductRepository productRepo = Beans.get(ProductRepository.class);
    if (productSupplier.getProductCode() == null) {
      throw new AxelorException(
          productSupplier,
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(SupplierPortalExceptionMessage.PRODUCT_SUPPLIER_NO_CODE));
    }
    if (productSupplier.getProductName() == null) {
      throw new AxelorException(
          productSupplier,
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(SupplierPortalExceptionMessage.PRODUCT_SUPPLIER_NO_NAME),
          productSupplier.getProductCode());
    }
    if (productRepo.findByCode(productSupplier.getProductCode()) != null) {
      throw new AxelorException(
          productSupplier,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SupplierPortalExceptionMessage.PRODUCT_SUPPLIER_SAME_CODE));
    }
    Product productCreated = createProductFromProductSupplier(productSupplier);
    productSupplier.setProductCreated(productCreated);
    Beans.get(ProductSupplierRepository.class).save(productSupplier);
    return productCreated;
  }

  protected Product createProductFromProductSupplier(ProductSupplier productSupplier) {
    Product product =
        new Product(
            productSupplier.getProductCode(),
            productSupplier.getProductCode(),
            productSupplier.getDescription(),
            null,
            productSupplier.getImgProduct(),
            null,
            null,
            productSupplier.getPurchaseUnit(),
            null,
            ProductRepository.PRODUCT_TYPE_STORABLE,
            null,
            productSupplier.getPurchaseCurrency(),
            productSupplier.getPurchaseCurrency(),
            null,
            null);
    product.setPurchasePrice(productSupplier.getPurchasePrice());
    return product;
  }
}
