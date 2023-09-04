package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderVersionService;
import com.axelor.apps.sale.service.saleorder.SaleOrderVersionServiceImpl;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderVersionSupplyChainServiceImpl extends SaleOrderVersionServiceImpl
    implements SaleOrderVersionService {

  @Inject
  public SaleOrderVersionSupplyChainServiceImpl(
      SaleOrderRepository saleOrderRepository,
      SaleOrderLineRepository saleOrderLineRepository,
      AppBaseService appBaseService) {
    super(saleOrderRepository, saleOrderLineRepository, appBaseService);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void createNewVersion(SaleOrder saleOrder) {
    saleOrder.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_NOT_DELIVERED);
    saleOrder.setInvoicingState(SaleOrderRepository.INVOICING_STATE_NOT_INVOICED);
    super.createNewVersion(saleOrder);
  }
}
