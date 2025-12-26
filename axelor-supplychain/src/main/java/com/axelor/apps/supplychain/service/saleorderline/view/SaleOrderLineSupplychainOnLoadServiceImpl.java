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
package com.axelor.apps.supplychain.service.saleorderline.view;

import static com.axelor.apps.sale.service.saleorderline.view.SaleOrderLineViewService.HIDDEN_ATTR;

import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.utils.MapTools;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineViewServiceSupplychain;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderLineSupplychainOnLoadServiceImpl
    implements SaleOrderLineSupplychainOnLoadService {

  protected AnalyticAttrsService analyticAttrsService;
  protected SaleOrderLineViewSupplychainService saleOrderLineViewSupplychainService;
  protected SaleOrderLineViewServiceSupplychain saleOrderLineViewServiceSupplychain;

  @Inject
  public SaleOrderLineSupplychainOnLoadServiceImpl(
      AnalyticAttrsService analyticAttrsService,
      SaleOrderLineViewSupplychainService saleOrderLineViewSupplychainService,
      SaleOrderLineViewServiceSupplychain saleOrderLineViewServiceSupplychain) {
    this.analyticAttrsService = analyticAttrsService;
    this.saleOrderLineViewSupplychainService = saleOrderLineViewSupplychainService;
    this.saleOrderLineViewServiceSupplychain = saleOrderLineViewServiceSupplychain;
  }

  @Override
  public Map<String, Map<String, Object>> getSupplychainOnLoadAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    MapTools.addMap(attrs, saleOrderLineViewSupplychainService.hideSupplychainPanels(saleOrder));
    MapTools.addMap(attrs, saleOrderLineViewSupplychainService.hideDeliveredQty(saleOrder));
    MapTools.addMap(attrs, saleOrderLineViewServiceSupplychain.hideDeliveryPanel(saleOrderLine));
    MapTools.addMap(
        attrs, saleOrderLineViewSupplychainService.hideAllocatedQtyBtn(saleOrder, saleOrderLine));
    MapTools.addMap(attrs, hideReservedQty(saleOrder, saleOrderLine));
    analyticAttrsService.addAnalyticAxisAttrs(saleOrder.getCompany(), null, attrs);
    MapTools.addMap(
        attrs,
        saleOrderLineViewSupplychainService.setAnalyticDistributionPanelHidden(
            saleOrder, saleOrderLine));
    MapTools.addMap(attrs, saleOrderLineViewSupplychainService.setReservedQtyReadonly(saleOrder));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideReservedQty(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    String productTypeSelect =
        Optional.ofNullable(saleOrderLine.getProduct())
            .map(Product::getProductTypeSelect)
            .orElse("");
    int statusSelect = saleOrder.getStatusSelect();
    attrs.put(
        "reservedQty",
        Map.of(
            HIDDEN_ATTR,
            statusSelect != SaleOrderRepository.STATUS_ORDER_CONFIRMED
                || productTypeSelect.equals(ProductRepository.PRODUCT_TYPE_SERVICE)));
    return attrs;
  }
}
