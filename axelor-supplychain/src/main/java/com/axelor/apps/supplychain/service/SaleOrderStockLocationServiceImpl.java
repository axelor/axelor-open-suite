package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.StockLocation;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderStockLocationServiceImpl implements SaleOrderStockLocationService {

  protected SaleOrderSupplychainService saleOrderSupplychainService;

  @Inject
  public SaleOrderStockLocationServiceImpl(
      SaleOrderSupplychainService saleOrderSupplychainService) {
    this.saleOrderSupplychainService = saleOrderSupplychainService;
  }

  @Override
  public Map<String, Object> getStockLocation(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    StockLocation shippingDefaultStockLocation =
        Optional.ofNullable(saleOrder.getTradingName())
            .map(TradingName::getShippingDefaultStockLocation)
            .orElse(null);
    if (shippingDefaultStockLocation != null && saleOrder.getStockLocation() != null) {
      return saleOrderMap;
    }
    Partner clientPartner = saleOrder.getClientPartner();
    Company company = saleOrder.getCompany();
    saleOrder.setStockLocation(
        saleOrderSupplychainService.getStockLocation(clientPartner, company));
    saleOrderMap.put("stockLocation", saleOrder.getStockLocation());
    return saleOrderMap;
  }

  @Override
  public Map<String, Object> getToStockLocation(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    saleOrder.setToStockLocation(
        saleOrderSupplychainService.getToStockLocation(
            saleOrder.getClientPartner(), saleOrder.getCompany()));
    saleOrderMap.put("toStockLocation", saleOrder.getToStockLocation());
    return saleOrderMap;
  }
}
