/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.saleorderline;

import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.utils.MapTools;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.view.SaleOrderLineViewServiceImpl;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.service.saleorderline.view.SaleOrderLineViewSupplychainService;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderLineViewServiceSupplychainImpl extends SaleOrderLineViewServiceImpl
    implements SaleOrderLineViewServiceSupplychain {

  protected AnalyticAttrsService analyticAttrsService;
  protected SaleOrderLineViewSupplychainService saleOrderLineViewSupplychainService;

  @Inject
  public SaleOrderLineViewServiceSupplychainImpl(
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      ProductMultipleQtyService productMultipleQtyService,
      AnalyticAttrsService analyticAttrsService,
      SaleOrderLineViewSupplychainService saleOrderLineViewSupplychainService) {
    super(appBaseService, appSaleService, productMultipleQtyService);
    this.analyticAttrsService = analyticAttrsService;
    this.saleOrderLineViewSupplychainService = saleOrderLineViewSupplychainService;
  }

  @Override
  public Map<String, Map<String, Object>> getProductOnChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs =
        super.getProductOnChangeAttrs(saleOrderLine, saleOrder);
    MapTools.addMap(attrs, hideDeliveryPanel(saleOrderLine));
    analyticAttrsService.addAnalyticAxisAttrs(saleOrder.getCompany(), null, attrs);
    MapTools.addMap(
        attrs,
        saleOrderLineViewSupplychainService.setAnalyticDistributionPanelHidden(
            saleOrder, saleOrderLine));
    return attrs;
  }

  public Map<String, Map<String, Object>> hideDeliveryPanel(SaleOrderLine saleOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    String productTypeSelect =
        Optional.ofNullable(saleOrderLine.getProduct())
            .map(Product::getProductTypeSelect)
            .orElse("");
    boolean hidePanels = false;
    if (productTypeSelect.equals(ProductRepository.PRODUCT_TYPE_STORABLE)) {
      hidePanels =
          Optional.ofNullable(AuthUtils.getUser().getActiveCompany())
              .map(Company::getSupplyChainConfig)
              .map(SupplyChainConfig::getHasOutSmForStorableProduct)
              .orElse(false);
    }
    if (productTypeSelect.equals(ProductRepository.PRODUCT_TYPE_SERVICE)) {
      hidePanels =
          Optional.ofNullable(AuthUtils.getUser().getActiveCompany())
              .map(Company::getSupplyChainConfig)
              .map(SupplyChainConfig::getHasOutSmForNonStorableProduct)
              .orElse(false);
    }
    attrs.put("deliveryPanel", Map.of(HIDDEN_ATTR, !hidePanels));
    return attrs;
  }
}
