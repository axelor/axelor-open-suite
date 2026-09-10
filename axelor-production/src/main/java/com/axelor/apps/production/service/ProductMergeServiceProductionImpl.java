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
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.CostSheetLine;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.purchase.db.repo.SupplierCatalogRepository;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.apps.supplychain.db.repo.ProductMergeLogRepository;
import com.axelor.apps.supplychain.service.ProductMergeServiceImpl;
import com.axelor.apps.supplychain.service.ProductMergeStockService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import jakarta.inject.Inject;
import java.util.Set;

public class ProductMergeServiceProductionImpl extends ProductMergeServiceImpl {

  protected final AppProductionService appProductionService;

  @Inject
  public ProductMergeServiceProductionImpl(
      AppSupplychainService appSupplychainService,
      ProductRepository productRepository,
      MrpRepository mrpRepository,
      MetaFieldRepository metaFieldRepository,
      MetaJsonFieldRepository metaJsonFieldRepository,
      DMSFileRepository dmsFileRepository,
      SupplierCatalogRepository supplierCatalogRepository,
      ProductMergeStockService productMergeStockService,
      ProductMergeLogRepository productMergeLogRepository,
      AppProductionService appProductionService) {
    super(
        appSupplychainService,
        productRepository,
        mrpRepository,
        metaFieldRepository,
        metaJsonFieldRepository,
        dmsFileRepository,
        supplierCatalogRepository,
        productMergeStockService,
        productMergeLogRepository);
    this.appProductionService = appProductionService;
  }

  /**
   * A cost sheet line belongs to a cost sheet computed at a given date: it keeps the product it was
   * computed for, as the other historical values do.
   */
  @Override
  protected Set<String> getExcludedModels() {
    Set<String> excludedModels = super.getExcludedModels();
    if (!appProductionService.isApp("production")) {
      return excludedModels;
    }

    excludedModels.add(CostSheetLine.class.getName());
    return excludedModels;
  }
}
