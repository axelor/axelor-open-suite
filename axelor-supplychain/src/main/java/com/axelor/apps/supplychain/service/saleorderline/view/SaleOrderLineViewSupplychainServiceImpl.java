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
import static com.axelor.apps.sale.service.saleorderline.view.SaleOrderLineViewService.READONLY_ATTR;

import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.analytic.AnalyticAttrsSupplychainService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.studio.db.AppAccount;
import com.axelor.studio.db.AppSupplychain;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderLineViewSupplychainServiceImpl
    implements SaleOrderLineViewSupplychainService {

  AppSupplychainService appSupplychainService;
  AppAccountService appAccountService;
  AccountConfigRepository accountConfigRepository;
  AnalyticAttrsSupplychainService analyticAttrsSupplychainService;

  @Inject
  public SaleOrderLineViewSupplychainServiceImpl(
      AppSupplychainService appSupplychainService,
      AppAccountService appAccountService,
      AccountConfigRepository accountConfigRepository,
      AnalyticAttrsSupplychainService analyticAttrsSupplychainService) {
    this.appSupplychainService = appSupplychainService;
    this.appAccountService = appAccountService;
    this.accountConfigRepository = accountConfigRepository;
    this.analyticAttrsSupplychainService = analyticAttrsSupplychainService;
  }

  @Override
  public Map<String, Map<String, Object>> hideSupplychainPanels(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    int statusSelect = saleOrder.getStatusSelect();
    boolean hidePanels =
        statusSelect == SaleOrderRepository.STATUS_DRAFT_QUOTATION
            || statusSelect == SaleOrderRepository.STATUS_FINALIZED_QUOTATION;
    attrs.put("stockMoveLineOfSOPanel", Map.of(HIDDEN_ATTR, hidePanels));
    attrs.put("projectTaskListPanel", Map.of(HIDDEN_ATTR, hidePanels));
    attrs.put("invoicingFollowUpPanel", Map.of(HIDDEN_ATTR, hidePanels));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> hideDeliveredQty(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    int statusSelect = saleOrder.getStatusSelect();
    attrs.put(
        "deliveredQty",
        Map.of(
            HIDDEN_ATTR,
            statusSelect == SaleOrderRepository.STATUS_DRAFT_QUOTATION
                || statusSelect == SaleOrderRepository.STATUS_FINALIZED_QUOTATION));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> hideAllocatedQtyBtn(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    String productTypeSelect =
        Optional.ofNullable(saleOrderLine.getProduct())
            .map(Product::getProductTypeSelect)
            .orElse("");
    int statusSelect = saleOrder.getStatusSelect();
    attrs.put(
        "updateAllocatedQtyBtn",
        Map.of(
            HIDDEN_ATTR,
            saleOrderLine.getId() == null
                || statusSelect != SaleOrderRepository.STATUS_ORDER_CONFIRMED
                || productTypeSelect.equals(ProductRepository.PRODUCT_TYPE_SERVICE)));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> setAnalyticDistributionPanelHidden(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);
    analyticAttrsSupplychainService.addAnalyticDistributionPanelHiddenAttrs(
        analyticLineModel, attrs);
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> setReservedQtyReadonly(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    AppSupplychain appSupplychain = appSupplychainService.getAppSupplychain();
    if (!appSupplychainService.isApp("supplychain")
        || !appSupplychain.getManageStockReservation()) {
      return attrs;
    }

    attrs.put(
        "requestedReservedQty",
        Map.of(
            READONLY_ATTR,
            saleOrder.getStatusSelect() > SaleOrderRepository.STATUS_FINALIZED_QUOTATION));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> setDistributionLineReadonly(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    AppAccount accountApp = appAccountService.getAppAccount();
    int distributionTypeSelect =
        accountConfigRepository
            .findByCompany(saleOrder.getCompany())
            .getAnalyticDistributionTypeSelect();
    attrs.put(
        "analyticMoveLineList",
        Map.of(
            READONLY_ATTR,
            accountApp == null
                || distributionTypeSelect != AccountConfigRepository.DISTRIBUTION_TYPE_FREE));
    return attrs;
  }
}
