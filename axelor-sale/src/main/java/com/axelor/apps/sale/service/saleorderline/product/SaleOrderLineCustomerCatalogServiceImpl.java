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
package com.axelor.apps.sale.service.saleorderline.product;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.CustomerCatalog;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.CustomerCatalogRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.common.StringUtils;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineCustomerCatalogServiceImpl
    implements SaleOrderLineCustomerCatalogService {

  protected AppSaleService appSaleService;
  protected CustomerCatalogRepository customerCatalogRepository;

  @Inject
  public SaleOrderLineCustomerCatalogServiceImpl(
      AppSaleService appSaleService, CustomerCatalogRepository customerCatalogRepository) {
    this.appSaleService = appSaleService;
    this.customerCatalogRepository = customerCatalogRepository;
  }

  @Override
  public Map<String, Object> computeCustomerCatalog(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Product product = saleOrderLine.getProduct();
    Partner customerPartner = saleOrder != null ? saleOrder.getClientPartner() : null;
    CustomerCatalog customerCatalog = saleOrderLine.getCustomerCatalog();

    if (!isMatching(customerCatalog, product, customerPartner)) {
      customerCatalog = getCustomerCatalog(product, customerPartner);
    }
    saleOrderLine.setCustomerCatalog(customerCatalog);

    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.put("customerCatalog", customerCatalog);
    saleOrderLineMap.put("customerProductCode", saleOrderLine.getCustomerProductCode());
    return saleOrderLineMap;
  }

  @Override
  public CustomerCatalog getCustomerCatalog(Product product, Partner customerPartner) {
    if (!appSaleService.getAppSale().getEnableCustomerCatalogMgt()
        || product == null
        || customerPartner == null) {
      return null;
    }
    return customerCatalogRepository
        .all()
        .filter("self.product = :product AND self.customerPartner = :customerPartner")
        .bind("product", product)
        .bind("customerPartner", customerPartner)
        .order("id")
        .fetchOne();
  }

  @Override
  public CustomerCatalog getCustomerCatalog(String productCustomerCode, Partner customerPartner) {
    if (!appSaleService.getAppSale().getEnableCustomerCatalogMgt()
        || StringUtils.isBlank(productCustomerCode)
        || customerPartner == null) {
      return null;
    }
    return customerCatalogRepository
        .all()
        .filter(
            "LOWER(self.productCustomerCode) = LOWER(:productCustomerCode) AND self.customerPartner = :customerPartner")
        .bind("productCustomerCode", productCustomerCode.trim())
        .bind("customerPartner", customerPartner)
        .order("id")
        .fetchOne();
  }

  @Override
  public void updateSaleOrderLinesCustomerCatalog(SaleOrder saleOrder) {
    updateCustomerCatalog(saleOrder.getSaleOrderLineList(), saleOrder);
  }

  protected void updateCustomerCatalog(List<SaleOrderLine> saleOrderLineList, SaleOrder saleOrder) {
    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return;
    }
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      computeCustomerCatalog(saleOrderLine, saleOrder);
      updateCustomerCatalog(saleOrderLine.getSubSaleOrderLineList(), saleOrder);
    }
  }

  protected boolean isMatching(
      CustomerCatalog customerCatalog, Product product, Partner customerPartner) {
    return appSaleService.getAppSale().getEnableCustomerCatalogMgt()
        && customerCatalog != null
        && product != null
        && customerPartner != null
        && Objects.equals(customerCatalog.getProduct(), product)
        && Objects.equals(customerCatalog.getCustomerPartner(), customerPartner);
  }
}
