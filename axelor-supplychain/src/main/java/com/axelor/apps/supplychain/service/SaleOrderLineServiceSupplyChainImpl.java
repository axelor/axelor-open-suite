/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.AppAccountRepository;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineServiceImpl;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SaleOrderLineServiceSupplyChainImpl extends SaleOrderLineServiceImpl
    implements SaleOrderLineServiceSupplyChain {

  @Inject protected AppAccountService appAccountService;

  @Inject protected AnalyticMoveLineService analyticMoveLineService;

  @Override
  public void computeProductInformation(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    super.computeProductInformation(saleOrderLine, saleOrder);
    saleOrderLine.setSaleSupplySelect(saleOrderLine.getProduct().getSaleSupplySelect());
  }

  public SaleOrderLine computeAnalyticDistribution(SaleOrderLine saleOrderLine)
      throws AxelorException {

    if (appAccountService.getAppAccount().getAnalyticDistributionTypeSelect()
        == AppAccountRepository.DISTRIBUTION_TYPE_FREE) {
      return saleOrderLine;
    }

    SaleOrder saleOrder = saleOrderLine.getSaleOrder();
    List<AnalyticMoveLine> analyticMoveLineList = saleOrderLine.getAnalyticMoveLineList();
    if ((analyticMoveLineList == null || analyticMoveLineList.isEmpty())) {
      analyticMoveLineList =
          analyticMoveLineService.generateLines(
              saleOrder.getClientPartner(),
              saleOrderLine.getProduct(),
              saleOrder.getCompany(),
              saleOrderLine.getExTaxTotal());
      saleOrderLine.setAnalyticMoveLineList(analyticMoveLineList);
    }
    if (analyticMoveLineList != null) {
      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        this.updateAnalyticMoveLine(analyticMoveLine, saleOrderLine);
      }
    }
    return saleOrderLine;
  }

  public void updateAnalyticMoveLine(
      AnalyticMoveLine analyticMoveLine, SaleOrderLine saleOrderLine) {

    analyticMoveLine.setSaleOrderLine(saleOrderLine);
    analyticMoveLine.setAmount(analyticMoveLineService.computeAmount(analyticMoveLine));
    analyticMoveLine.setDate(appAccountService.getTodayDate());
    analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_FORECAST_ORDER);
  }

  public SaleOrderLine createAnalyticDistributionWithTemplate(SaleOrderLine saleOrderLine)
      throws AxelorException {
    List<AnalyticMoveLine> analyticMoveLineList = null;
    analyticMoveLineList =
        analyticMoveLineService.generateLinesWithTemplate(
            saleOrderLine.getAnalyticDistributionTemplate(), saleOrderLine.getExTaxTotal());
    if (analyticMoveLineList != null) {
      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        analyticMoveLine.setSaleOrderLine(saleOrderLine);
      }
    }
    saleOrderLine.setAnalyticMoveLineList(analyticMoveLineList);
    return saleOrderLine;
  }

  @Override
  public BigDecimal getAvailableStock(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    StockLocationLine stockLocationLine =
        Beans.get(StockLocationLineService.class)
            .getStockLocationLine(saleOrder.getStockLocation(), saleOrderLine.getProduct());

    if (stockLocationLine == null) {
      return BigDecimal.ZERO;
    }
    return stockLocationLine.getCurrentQty().subtract(stockLocationLine.getReservedQty());
  }

  @Transactional
  public void changeReservedQty(SaleOrderLine saleOrderLine, BigDecimal reservedQty) {
    saleOrderLine.setReservedQty(reservedQty);
    Beans.get(SaleOrderLineRepository.class).save(saleOrderLine);
  }

  @Override
  public BigDecimal computeUndeliveredQty(SaleOrderLine saleOrderLine) {
    Preconditions.checkNotNull(saleOrderLine);
    SaleOrder saleOrder = saleOrderLine.getSaleOrder();
    Preconditions.checkNotNull(saleOrder);
    Product product = saleOrderLine.getProduct();
    BigDecimal deliveredQty =
        product != null
            ? saleOrder
                .getSaleOrderLineList()
                .stream()
                .filter(line -> product.equals(line.getProduct()))
                .reduce(
                    BigDecimal.ZERO,
                    (qty, line) -> qty.add(line.getDeliveredQty()),
                    BigDecimal::add)
            : BigDecimal.ZERO;

    return saleOrderLine.getQty().subtract(deliveredQty);
  }

  @Override
  public List<Long> getSupplierPartnerList(SaleOrderLine saleOrderLine) {
    Product product = saleOrderLine.getProduct();
    if (product == null || product.getSupplierCatalogList() == null) {
      return new ArrayList<>();
    }
    return product
        .getSupplierCatalogList()
        .stream()
        .map(SupplierCatalog::getSupplierPartner)
        .filter(Objects::nonNull)
        .map(Partner::getId)
        .collect(Collectors.toList());
  }

  @Override
  public void updateDeliveryStates(List<SaleOrderLine> saleOrderLineList) {
    if (ObjectUtils.isEmpty(saleOrderLineList)) {
      return;
    }

    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      updateDeliveryState(saleOrderLine);
    }
  }

  @Override
  public void updateDeliveryState(SaleOrderLine saleOrderLine) {
    if (saleOrderLine.getDeliveredQty().signum() == 0) {
      saleOrderLine.setDeliveryState(SaleOrderLineRepository.DELIVERY_STATE_NOT_DELIVERED);
    } else if (saleOrderLine.getDeliveredQty().compareTo(saleOrderLine.getQty()) < 0) {
      saleOrderLine.setDeliveryState(SaleOrderLineRepository.DELIVERY_STATE_PARTIALLY_DELIVERED);
    } else {
      saleOrderLine.setDeliveryState(SaleOrderLineRepository.DELIVERY_STATE_DELIVERED);
    }
  }

  @Override
  public SaleOrderLine createPackLine(PackLine packLine, SaleOrder saleOrder)
      throws AxelorException {
    SaleOrderLine subLine = super.createPackLine(packLine, saleOrder);
    subLine.setSaleSupplySelect(subLine.getProduct().getSaleSupplySelect());
    return subLine;
  }
}
