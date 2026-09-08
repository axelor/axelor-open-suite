/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductCompanyRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.ProductVariantConfigRepository;
import com.axelor.apps.base.db.repo.ProductVariantRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductVariantService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.service.ProductServicePurchaseImpl;
import jakarta.inject.Inject;
import java.math.BigDecimal;

public class ProductSupplychainServiceImpl extends ProductServicePurchaseImpl {

  @Inject
  public ProductSupplychainServiceImpl(
      ProductVariantService productVariantService,
      ProductVariantRepository productVariantRepo,
      SequenceService sequenceService,
      AppBaseService appBaseService,
      ProductRepository productRepo,
      ProductCompanyService productCompanyService,
      ProductCompanyRepository productCompanyRepository,
      ProductVariantConfigRepository productVariantConfigRepository) {
    super(
        productVariantService,
        productVariantRepo,
        sequenceService,
        appBaseService,
        productRepo,
        productCompanyService,
        productCompanyRepository,
        productVariantConfigRepository);
  }

  @Override
  public void copyProduct(Product product, Product copy) {
    super.copyProduct(product, copy);
    copy.setStockRotationCategory(null);
    copy.setAutoAssignStockRotationCategory(Boolean.TRUE);
    copy.setRevaluationRate(BigDecimal.ZERO);
  }
}
