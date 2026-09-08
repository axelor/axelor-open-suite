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
package com.axelor.apps.sale.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.meta.loader.ModuleManager;
import jakarta.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class ProductSaleDomainServiceImpl implements ProductSaleDomainService {

  protected AppBaseService appBaseService;
  protected AppSaleService appSaleService;

  @Inject
  public ProductSaleDomainServiceImpl(
      AppBaseService appBaseService, AppSaleService appSaleService) {
    this.appBaseService = appBaseService;
    this.appSaleService = appSaleService;
  }

  @Override
  public String computeProductDomain(Company company, TradingName tradingName) {
    String domain =
        "self.isModel = false"
            + " and (self.startDate IS null or self.startDate <= :__date__)"
            + " and (self.endDate IS null or self.endDate > :__date__)"
            + " and self.dtype = 'Product'";

    if (appBaseService.getAppBase().getCompanySpecificProductFieldsSet() != null
        && appBaseService.getAppBase().getCompanySpecificProductFieldsSet().stream()
            .anyMatch(it -> "sellable".equals(it.getName()))
        && company != null) {
      domain +=
          " and (SELECT sellable "
              + "FROM ProductCompany productCompany "
              + "WHERE productCompany.product.id = self.id "
              + "AND productCompany.company.id = "
              + company.getId()
              + ") IS TRUE ";
    } else {
      domain += " and self.sellable = true ";
    }

    if (appSaleService.getAppSale().getEnableSalesProductByTradName()
        && tradingName != null
        && company != null
        && !CollectionUtils.isEmpty(company.getTradingNameList())) {
      domain +=
          " AND (" + tradingName.getId() + " IN (SELECT tn.id FROM self.tradingNameSellerSet tn))";
    }

    // The standard way to do this would be to override the method in HR module.
    // But here, we have to do this because overriding a sale service in hr module will prevent the
    // override in supplychain module.
    if (ModuleManager.isInstalled("axelor-human-resource")) {
      domain += " AND (self.expense = false OR self.expense IS NULL)";
    }

    return domain;
  }
}
