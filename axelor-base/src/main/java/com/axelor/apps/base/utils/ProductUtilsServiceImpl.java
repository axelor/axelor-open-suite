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
package com.axelor.apps.base.utils;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.studio.db.repo.AppBaseRepository;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

public class ProductUtilsServiceImpl implements ProductUtilsService {

  protected MetaFiles metaFiles;
  protected AppBaseService appBaseService;
  protected SequenceService sequenceService;

  @Inject
  public ProductUtilsServiceImpl(
      MetaFiles metaFiles, AppBaseService appBaseService, SequenceService sequenceService) {
    this.metaFiles = metaFiles;
    this.appBaseService = appBaseService;
    this.sequenceService = sequenceService;
  }

  @Override
  public String getSequence(Product product) throws AxelorException {
    String seq = null;
    if (appBaseService
        .getAppBase()
        .getProductSequenceTypeSelect()
        .equals(AppBaseRepository.SEQUENCE_PER_PRODUCT_CATEGORY)) {
      ProductCategory productCategory = product.getProductCategory();
      if (productCategory.getSequence() != null) {
        seq =
            sequenceService.getSequenceNumber(
                productCategory.getSequence(), Product.class, "code", product);
      }
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.CATEGORY_NO_SEQUENCE));
      }
    } else if (appBaseService
        .getAppBase()
        .getProductSequenceTypeSelect()
        .equals(AppBaseRepository.SEQUENCE_PER_PRODUCT)) {
      Sequence productSequence = appBaseService.getAppBase().getProductSequence();
      if (productSequence != null) {
        seq = sequenceService.getSequenceNumber(productSequence, Product.class, "code", product);
      }
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.APP_BASE_NO_SEQUENCE));
      }
    }

    if (seq == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.PRODUCT_NO_SEQUENCE));
    }

    return seq;
  }

  @Override
  public void copyProduct(Product product, Product copy) {
    copy.setBarCode(null);
    try {
      if (product.getPicture() != null) {
        File file = MetaFiles.getPath(product.getPicture()).toFile();
        copy.setPicture(metaFiles.upload(file));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    copy.setStartDate(null);
    copy.setEndDate(null);
    copy.setCostPrice(BigDecimal.ZERO);
    copy.setPurchasePrice(BigDecimal.ZERO);
    copy.setProductCompanyList(null);
    copy.setLastPurchaseDate(null);
    copy.setCode(null);
  }
}
