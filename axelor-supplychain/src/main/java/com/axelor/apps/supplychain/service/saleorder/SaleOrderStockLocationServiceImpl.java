package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.PartnerStockSettingsService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderStockLocationServiceImpl implements SaleOrderStockLocationService {

  protected SaleOrderSupplychainService saleOrderSupplychainService;
  protected PartnerStockSettingsService partnerStockSettingsService;
  protected StockConfigService stockConfigService;

  @Inject
  public SaleOrderStockLocationServiceImpl(
      SaleOrderSupplychainService saleOrderSupplychainService,
      PartnerStockSettingsService partnerStockSettingsService,
      StockConfigService stockConfigService) {
    this.saleOrderSupplychainService = saleOrderSupplychainService;
    this.partnerStockSettingsService = partnerStockSettingsService;
    this.stockConfigService = stockConfigService;
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
    saleOrder.setStockLocation(getStockLocation(clientPartner, company));
    saleOrderMap.put("stockLocation", saleOrder.getStockLocation());
    return saleOrderMap;
  }

  @Override
  public Map<String, Object> getToStockLocation(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    saleOrder.setToStockLocation(
        getToStockLocation(saleOrder.getClientPartner(), saleOrder.getCompany()));
    saleOrderMap.put("toStockLocation", saleOrder.getToStockLocation());
    return saleOrderMap;
  }

  @Override
  public StockLocation getStockLocation(Partner clientPartner, Company company)
      throws AxelorException {
    if (company == null) {
      return null;
    }
    StockLocation stockLocation =
        partnerStockSettingsService.getDefaultStockLocation(
            clientPartner, company, StockLocation::getUsableOnSaleOrder);
    if (stockLocation == null) {
      StockConfig stockConfig = stockConfigService.getStockConfig(company);
      stockLocation = stockConfigService.getPickupDefaultStockLocation(stockConfig);
    }
    return stockLocation;
  }

  @Override
  public StockLocation getToStockLocation(Partner clientPartner, Company company)
      throws AxelorException {
    if (company == null) {
      return null;
    }
    StockLocation toStockLocation =
        partnerStockSettingsService.getDefaultExternalStockLocation(
            clientPartner, company, StockLocation::getUsableOnSaleOrder);
    if (toStockLocation == null) {
      StockConfig stockConfig = stockConfigService.getStockConfig(company);
      toStockLocation = stockConfigService.getCustomerVirtualStockLocation(stockConfig);
    }
    return toStockLocation;
  }
}
