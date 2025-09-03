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
package com.axelor.apps.supplychain.service.analytic;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class AnalyticToolSupplychainServiceImpl implements AnalyticToolSupplychainService {

  protected AnalyticLineModelService analyticLineModelService;

  @Inject
  public AnalyticToolSupplychainServiceImpl(AnalyticLineModelService analyticLineModelService) {
    this.analyticLineModelService = analyticLineModelService;
  }

  @Override
  public void checkSaleOrderLinesAnalyticDistribution(SaleOrder saleOrder) throws AxelorException {
    if (!analyticLineModelService.analyticDistributionTemplateRequired(
        false, saleOrder.getCompany())) {
      return;
    }

    List<String> productList =
        saleOrder.getSaleOrderLineList().stream()
            .filter(
                saleOrderLine ->
                    saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_NORMAL
                        && saleOrderLine.getAnalyticDistributionTemplate() == null)
            .map(SaleOrderLine::getProductName)
            .collect(Collectors.toList());

    if (!productList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(SupplychainExceptionMessage.SALE_ORDER_ANALYTIC_DISTRIBUTION_ERROR),
          productList);
    }
  }

  @Override
  public void checkPurchaseOrderLinesAnalyticDistribution(PurchaseOrder purchaseOrder)
      throws AxelorException {
    if (!analyticLineModelService.analyticDistributionTemplateRequired(
        true, purchaseOrder.getCompany())) {
      return;
    }

    List<String> productList =
        purchaseOrder.getPurchaseOrderLineList().stream()
            .filter(
                purchaseOrderLine ->
                    purchaseOrderLine.getAnalyticDistributionTemplate() == null
                        && !purchaseOrderLine.getIsTitleLine())
            .map(PurchaseOrderLine::getProductName)
            .collect(Collectors.toList());

    if (!productList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(SupplychainExceptionMessage.PURCHASE_ORDER_ANALYTIC_DISTRIBUTION_ERROR),
          productList);
    }
  }
}
