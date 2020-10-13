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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.ProductVariantRepository;
import com.axelor.apps.base.service.ProductServiceImpl;
import com.axelor.apps.base.service.ProductVariantService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class ProductServicePurchaseImpl extends ProductServiceImpl {

  @Inject
  public ProductServicePurchaseImpl(
      ProductVariantService productVariantService,
      ProductVariantRepository productVariantRepo,
      SequenceService sequenceService,
      AppBaseService appBaseService,
      ProductRepository productRepo) {
    super(productVariantService, productVariantRepo, sequenceService, appBaseService, productRepo);
  }

  @Override
  public void copyProduct(Product product, Product copy) {
    super.copyProduct(product, copy);

    if (Beans.get(AppBaseService.class).isApp("purchase-request")) {
      copy.setSupplierCatalogList(null);
    }
  }
}
