package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockLocation;

public interface ManufOrderStockMoveStockLocationService {

  /**
   * Given a manuf order, its company determine the in default stock location and return it. First
   * search in prodprocess, then in company stock configuration.
   *
   * @param manufOrder a manufacturing order.
   * @param company a company with stock config.
   * @return the found stock location, which can be null.
   * @throws AxelorException if the stock config is missing for the company.
   */
  StockLocation getDefaultInStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException;

  /**
   * Given a manuf order, its company determine the out default stock location and return it. First
   * search in prodprocess, then in company stock configuration.
   *
   * @param manufOrder a manufacturing order.
   * @param company a company with stock config.
   * @return the found stock location, which can be null.
   * @throws AxelorException if the stock config is missing for the company.
   */
  StockLocation getDefaultOutStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException;

  StockLocation getFromStockLocationForConsumedStockMove(ManufOrder manufOrder, Company company)
      throws AxelorException;

  StockLocation getVirtualStockLocationForConsumedStockMove(ManufOrder manufOrder, Company company)
      throws AxelorException;

  StockLocation _getVirtualProductionStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException;

  StockLocation _getVirtualOutsourcingStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException;

  StockLocation getProducedProductStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException;

  StockLocation getResidualProductStockLocation(ManufOrder manufOrder) throws AxelorException;

  StockLocation getVirtualStockLocationForProducedStockMove(ManufOrder manufOrder, Company company)
      throws AxelorException;
}
