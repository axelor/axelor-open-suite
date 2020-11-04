package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.service.SaleOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.SaleOrderStockService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderServiceProjectImpl extends SaleOrderServiceSupplychainImpl {

  private AnalyticMoveLineRepository analyticMoveLineRepository;

  @Inject
  public SaleOrderServiceProjectImpl(
      AppSupplychainService appSupplychainService,
      SaleOrderStockService saleOrderStockService,
      SaleOrderRepository saleOrderRepository,
      AnalyticMoveLineRepository analyticMoveLineRepository) {
    super(appSupplychainService, saleOrderStockService, saleOrderRepository);
    this.analyticMoveLineRepository = analyticMoveLineRepository;
  }

  @Transactional(rollbackOn = Exception.class)
  public SaleOrder updateLines(SaleOrder saleOrder) {
    for (SaleOrderLine orderLine : saleOrder.getSaleOrderLineList()) {
      orderLine.setProject(saleOrder.getProject());
      for (AnalyticMoveLine analyticMoveLine : orderLine.getAnalyticMoveLineList()) {
        analyticMoveLine.setProject(saleOrder.getProject());
        analyticMoveLineRepository.save(analyticMoveLine);
      }
    }
    return saleOrder;
  }
}
