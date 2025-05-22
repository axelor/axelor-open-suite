package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class SaleOrderCheckBlockingSupplychainServiceImpl
    implements SaleOrderCheckBlockingSupplychainService {

  protected final SaleOrderBlockingSupplychainService saleOrderBlockingSupplychainService;
  protected final AppSupplychainService appSupplychainService;

  @Inject
  public SaleOrderCheckBlockingSupplychainServiceImpl(
      SaleOrderBlockingSupplychainService saleOrderBlockingSupplychainService,
      AppSupplychainService appSupplychainService) {
    this.saleOrderBlockingSupplychainService = saleOrderBlockingSupplychainService;
    this.appSupplychainService = appSupplychainService;
  }

  @Override
  public List<String> checkBlocking(SaleOrder saleOrder) {
    List<String> alertList = new ArrayList<>();
    if (saleOrderBlockingSupplychainService.hasOnGoingBlocking(saleOrder)
        && appSupplychainService.getAppSupplychain().getCustomerStockMoveGenerationAuto()) {
      alertList.add(I18n.get(SupplychainExceptionMessage.SALE_ORDER_LINES_CANNOT_DELIVER));
    }
    return alertList;
  }
}
