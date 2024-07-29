package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.StockLocation;
import java.util.Map;

public interface SaleOrderStockLocationService {
  Map<String, Object> getStockLocation(SaleOrder saleOrder) throws AxelorException;

  Map<String, Object> getToStockLocation(SaleOrder saleOrder) throws AxelorException;

  StockLocation getStockLocation(Partner clientPartner, Company company) throws AxelorException;

  StockLocation getToStockLocation(Partner clientPartner, Company company) throws AxelorException;
}
