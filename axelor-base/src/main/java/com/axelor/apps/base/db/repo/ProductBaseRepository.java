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
package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.BarcodeTypeConfig;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.service.TranslationService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class ProductBaseRepository extends ProductRepository {

  @Inject protected AppBaseService appBaseService;

  @Inject protected TranslationService translationService;

  protected static final String FULL_NAME_FORMAT = "[%s] %s";

  @Inject protected BarcodeGeneratorService barcodeGeneratorService;

  @Override
  public Product save(Product product) {
    try {
      if (appBaseService.getAppBase().getGenerateProductSequence()
          && Strings.isNullOrEmpty(product.getCode())) {
        product.setCode(Beans.get(ProductService.class).getSequence(product));
      }
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }

    product.setFullName(String.format(FULL_NAME_FORMAT, product.getCode(), product.getName()));

    if (product.getId() != null) {
      Product oldProduct = Beans.get(ProductRepository.class).find(product.getId());
      translationService.updateFormatedValueTranslations(
          oldProduct.getFullName(), FULL_NAME_FORMAT, product.getCode(), product.getName());
    } else {
      translationService.createFormatedValueTranslations(
          FULL_NAME_FORMAT, product.getCode(), product.getName());
    }

    product = super.save(product);

    // Barcode generation
    if (product.getBarCode() == null
        && appBaseService.getAppBase().getActivateBarCodeGeneration()) {
      boolean addPadding = false;
      BarcodeTypeConfig barcodeTypeConfig = product.getBarcodeTypeConfig();
      if (!appBaseService.getAppBase().getEditProductBarcodeType()) {
        barcodeTypeConfig = appBaseService.getAppBase().getBarcodeTypeConfig();
      }
      MetaFile barcodeFile =
          barcodeGeneratorService.createBarCode(
              product.getId(),
              "ProductBarCode%d.png",
              product.getSerialNumber(),
              barcodeTypeConfig,
              addPadding);
      if (barcodeFile != null) {
        product.setBarCode(barcodeFile);
      }
    }
    return super.save(product);
  }

  @Override
  public Product copy(Product product, boolean deep) {
    Product copy = super.copy(product, deep);
    Beans.get(ProductService.class).copyProduct(product, copy);
    return copy;
  }
}
