/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.tool.service.TranslationService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import javax.persistence.PersistenceException;
import javax.validation.ValidationException;

public class ProductBaseRepository extends ProductRepository {

  @Inject private MetaFiles metaFiles;

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
      throw new PersistenceException(e);
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
    if (product.getBarCode() == null
        && appBaseService.getAppBase().getActivateBarCodeGeneration()) {
      try {
        boolean addPadding = false;
        InputStream inStream;
        if (!appBaseService.getAppBase().getEditProductBarcodeType()) {
          inStream =
              barcodeGeneratorService.createBarCode(
                  product.getSerialNumber(),
                  appBaseService.getAppBase().getBarcodeTypeConfig(),
                  addPadding);
        } else {
          inStream =
              barcodeGeneratorService.createBarCode(
                  product.getSerialNumber(), product.getBarcodeTypeConfig(), addPadding);
        }
        if (inStream != null) {
          MetaFile barcodeFile =
              metaFiles.upload(inStream, String.format("ProductBarCode%d.png", product.getId()));
          product.setBarCode(barcodeFile);
        }
      } catch (IOException e) {
        e.printStackTrace();
      } catch (AxelorException e) {
        TraceBackService.traceExceptionFromSaveMethod(e);
        throw new ValidationException(e);
      }
    }
    return super.save(product);
  }

  @Override
  public Product copy(Product product, boolean deep) {
    Product copy = super.copy(product, deep);
    Beans.get(ProductService.class).copyProduct(product, copy);

    try {
      if (appBaseService.getAppBase().getGenerateProductSequence()) {
        copy.setCode(Beans.get(ProductService.class).getSequence(product));
      }
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e);
    }
    return copy;
  }
}
