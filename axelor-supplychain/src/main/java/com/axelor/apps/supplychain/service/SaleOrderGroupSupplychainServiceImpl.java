package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.SaleOrderGroupServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineTreeService;
import com.axelor.apps.sale.service.saleorder.attributes.SaleOrderAttrsService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;

public class SaleOrderGroupSupplychainServiceImpl extends SaleOrderGroupServiceImpl {

  protected SaleOrderServiceSupplychainImpl saleOrderServiceSupplychain;
  protected AppSupplychainService appSupplychainService;

  @Inject
  public SaleOrderGroupSupplychainServiceImpl(
      SaleOrderAttrsService saleOrderAttrsService,
      SaleOrderRepository saleOrderRepository,
      SaleOrderLineTreeService saleOrderLineTreeService,
      SaleOrderServiceSupplychainImpl saleOrderServiceSupplychain,
      AppSupplychainService appSupplychainService) {
    super(saleOrderAttrsService, saleOrderRepository, saleOrderLineTreeService);
    this.saleOrderServiceSupplychain = saleOrderServiceSupplychain;
    this.appSupplychainService = appSupplychainService;
  }

  @Override
  public void onSave(SaleOrder saleOrder) throws AxelorException {
    if (appSupplychainService.isApp("supplychain")) {
      if (saleOrder.getOrderBeingEdited() && saleOrder.getId() != null) {
        SaleOrder savedSaleOrder = saleOrderRepository.find(saleOrder.getId());
        saleOrderServiceSupplychain.checkModifiedConfirmedOrder(savedSaleOrder, saleOrder);
      }
    }

    super.onSave(saleOrder);
  }
}
