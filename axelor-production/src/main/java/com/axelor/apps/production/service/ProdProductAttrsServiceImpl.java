package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.service.config.StockConfigProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderOutsourceService;
import com.google.inject.Inject;
import java.util.Objects;

public class ProdProductAttrsServiceImpl implements ProdProductAttrsService {

  protected final StockConfigProductionService stockConfigProductionService;
  protected final ManufOrderOutsourceService manufOrderOutsourceService;

  @Inject
  public ProdProductAttrsServiceImpl(
      StockConfigProductionService stockConfigProductionService,
      ManufOrderOutsourceService manufOrderOutsourceService) {
    this.stockConfigProductionService = stockConfigProductionService;
    this.manufOrderOutsourceService = manufOrderOutsourceService;
  }

  @Override
  public String getTrackingNumberDomain(ManufOrder manufOrder, ProdProduct prodProduct)
      throws AxelorException {

    Objects.requireNonNull(prodProduct);

    if (prodProduct.getProduct() == null || manufOrder == null) {
      return "self.id IN (0)";
    }

    var stockConfig = stockConfigProductionService.getStockConfig(manufOrder.getCompany());
    var productionStockLocation =
        stockConfigProductionService.getProductionVirtualStockLocation(
            stockConfig, manufOrderOutsourceService.isOutsource(manufOrder));

    String domain =
        "self.product.id = %d AND"
            + " (self IN (SELECT stockLocationLine.trackingNumber FROM StockLocationLine stockLocationLine WHERE stockLocationLine.detailsStockLocation = %d AND stockLocationLine.currentQty > 0))";
    return String.format(domain, prodProduct.getProduct().getId(), productionStockLocation.getId());
  }
}
