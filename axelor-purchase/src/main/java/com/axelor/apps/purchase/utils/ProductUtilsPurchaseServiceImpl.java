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
package com.axelor.apps.purchase.utils;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.utils.ProductUtilsServiceImpl;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;

public class ProductUtilsPurchaseServiceImpl extends ProductUtilsServiceImpl {

  @Inject
  public ProductUtilsPurchaseServiceImpl(
      MetaFiles metaFiles, AppBaseService appBaseService, SequenceService sequenceService) {
    super(metaFiles, appBaseService, sequenceService);
  }

  @Override
  public void copyProduct(Product product, Product copy) {
    super.copyProduct(product, copy);

    if (appBaseService.isApp("purchase-request")) {
      copy.setSupplierCatalogList(null);
    }
  }
}
