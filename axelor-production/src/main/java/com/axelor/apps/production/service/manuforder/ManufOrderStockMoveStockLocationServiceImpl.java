package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.config.StockConfigProductionService;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class ManufOrderStockMoveStockLocationServiceImpl
    implements ManufOrderStockMoveStockLocationService {

  protected final StockConfigProductionService stockConfigProductionService;
  protected static final int STOCK_LOCATION_IN = 1;
  protected static final int STOCK_LOCATION_OUT = 2;

  @Inject
  public ManufOrderStockMoveStockLocationServiceImpl(
      StockConfigProductionService stockConfigProductionService) {
    this.stockConfigProductionService = stockConfigProductionService;
  }

  @Override
  public StockLocation getDefaultInStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException {

    if (manufOrder.getOutsourcing()) {
      return _getDefaultInOutsourcingStockLocation(manufOrder, company);
    } else {
      return _getDefaultInStockLocation(manufOrder, company);
    }
  }

  protected StockLocation _getDefaultInStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException {
    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);
    StockLocation stockLocation =
        getDefaultStockLocation(manufOrder.getProdProcess(), STOCK_LOCATION_IN);
    if (stockLocation == null) {
      return stockConfigProductionService.getComponentDefaultStockLocation(
          manufOrder.getWorkshopStockLocation(), stockConfig);
    }
    return stockLocation;
  }

  /**
   * Given a prodprocess and whether we want to create a in or out stock move, determine the stock
   * location and return it.
   *
   * @param prodProcess a production process.
   * @param inOrOut can be {@link ManufOrderStockMoveStockLocationServiceImpl#STOCK_LOCATION_IN} or
   *     {@link ManufOrderStockMoveStockLocationServiceImpl#STOCK_LOCATION_OUT}.
   * @return the found stock location, or null if the prod process is null.
   */
  protected StockLocation getDefaultStockLocation(ProdProcess prodProcess, int inOrOut) {
    if (inOrOut != STOCK_LOCATION_IN && inOrOut != STOCK_LOCATION_OUT) {
      throw new IllegalArgumentException(
          I18n.get(ProductionExceptionMessage.IN_OR_OUT_INVALID_ARG));
    }
    if (prodProcess == null) {
      return null;
    }
    if (inOrOut == STOCK_LOCATION_IN) {
      return prodProcess.getStockLocation();
    } else {
      return prodProcess.getProducedProductStockLocation();
    }
  }

  protected StockLocation _getDefaultInOutsourcingStockLocation(
      ManufOrder manufOrder, Company company) throws AxelorException {
    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);
    // Because it will be send to outsource.
    return stockConfigProductionService.getPickupDefaultStockLocation(stockConfig);
  }

  @Override
  public StockLocation getDefaultOutStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException {
    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);
    StockLocation stockLocation =
        getDefaultStockLocation(manufOrder.getProdProcess(), STOCK_LOCATION_OUT);
    if (stockLocation == null) {
      return stockConfigProductionService.getFinishedProductsDefaultStockLocation(
          manufOrder.getWorkshopStockLocation(), stockConfig);
    }
    return stockLocation;
  }

  @Override
  public StockLocation _getVirtualProductionStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException {
    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);

    return stockConfigProductionService.getProductionVirtualStockLocation(stockConfig, false);
  }

  @Override
  public StockLocation _getVirtualOutsourcingStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException {
    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);

    return stockConfigProductionService.getVirtualOutsourcingStockLocation(stockConfig);
  }

  @Override
  public StockLocation getFromStockLocationForConsumedStockMove(
      ManufOrder manufOrder, Company company) throws AxelorException {
    StockLocation fromStockLocation = getDefaultInStockLocation(manufOrder, company);

    if (fromStockLocation == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(
              ProductionExceptionMessage.MANUF_ORDER_STOCK_MOVE_MISSING_SOURCE_STOCK_LOCATION));
    }
    return fromStockLocation;
  }

  @Override
  public StockLocation getVirtualStockLocationForConsumedStockMove(
      ManufOrder manufOrder, Company company) throws AxelorException {

    if (manufOrder.getOutsourcing()) {
      return _getVirtualOutsourcingStockLocation(manufOrder, company);
    } else {
      return _getVirtualProductionStockLocation(manufOrder, company);
    }
  }

  public StockLocation getProducedProductStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException {

    if (manufOrder.getOutsourcing()) {
      return _getReceiptOutsourcingStockLocation(manufOrder, company);
    } else {
      return _getProducedProductStockLocation(manufOrder, company);
    }
  }

  @Override
  public StockLocation getResidualProductStockLocation(ManufOrder manufOrder)
      throws AxelorException {
    Company company = manufOrder.getCompany();
    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);

    StockLocation residualProductStockLocation =
        manufOrder.getProdProcess().getResidualProductsDefaultStockLocation();
    if (residualProductStockLocation == null) {
      residualProductStockLocation =
          stockConfigProductionService.getResidualProductsDefaultStockLocation(stockConfig);
    }
    return residualProductStockLocation;
  }

  protected StockLocation _getReceiptOutsourcingStockLocation(
      ManufOrder manufOrder, Company company) throws AxelorException {
    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);
    return stockConfigProductionService.getReceiptDefaultStockLocation(stockConfig);
  }

  protected StockLocation _getProducedProductStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException {
    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);

    StockLocation producedProductStockLocation =
        manufOrder.getProdProcess().getProducedProductStockLocation();
    if (producedProductStockLocation == null) {
      producedProductStockLocation =
          stockConfigProductionService.getFinishedProductsDefaultStockLocation(
              manufOrder.getWorkshopStockLocation(), stockConfig);
    }
    return producedProductStockLocation;
  }

  @Override
  public StockLocation getVirtualStockLocationForProducedStockMove(
      ManufOrder manufOrder, Company company) throws AxelorException {

    if (manufOrder.getOutsourcing()) {
      return _getVirtualOutsourcingStockLocation(manufOrder, company);
    } else {
      return _getVirtualProductionStockLocation(manufOrder, company);
    }
  }
}
