package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingServiceImpl;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.tool.MapTools;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import java.util.List;

public class SaleOrderMergingServiceSupplyChainImpl extends SaleOrderMergingServiceImpl {

  protected static class CommonFieldsSupplyChainImpl extends CommonFieldsImpl {
    private StockLocation commonStockLocation = null;

    public StockLocation getCommonStockLocation() {
      return commonStockLocation;
    }

    public void setCommonStockLocation(StockLocation commonStockLocation) {
      this.commonStockLocation = commonStockLocation;
    }
  }

  protected static class ChecksSupplyChainImpl extends ChecksImpl {
    private boolean existStockLocationDiff = false;

    public boolean isExistStockLocationDiff() {
      return existStockLocationDiff;
    }

    public void setExistStockLocationDiff(boolean existStockLocationDiff) {
      this.existStockLocationDiff = existStockLocationDiff;
    }
  }

  protected static class SaleOrderMergingResultSupplyChainImpl extends SaleOrderMergingResultImpl {
    private final CommonFieldsSupplyChainImpl commonFields;
    private final ChecksSupplyChainImpl checks;

    public SaleOrderMergingResultSupplyChainImpl() {
      super();
      this.commonFields = new CommonFieldsSupplyChainImpl();
      this.checks = new ChecksSupplyChainImpl();
    }
  }

  protected AppSaleService appSaleService;

  @Inject
  public SaleOrderMergingServiceSupplyChainImpl(
      SaleOrderCreateService saleOrdreCreateService, AppSaleService appSaleService) {
    super(saleOrdreCreateService);
    this.appSaleService = appSaleService;
  }

  @Override
  public SaleOrderMergingResultSupplyChainImpl create() {
    return new SaleOrderMergingResultSupplyChainImpl();
  }

  @Override
  public CommonFieldsSupplyChainImpl getCommonFields(SaleOrderMergingResult result) {
    return ((SaleOrderMergingResultSupplyChainImpl) result).commonFields;
  }

  @Override
  public ChecksSupplyChainImpl getChecks(SaleOrderMergingResult result) {
    return ((SaleOrderMergingResultSupplyChainImpl) result).checks;
  }

  @Override
  protected boolean isConfirmationNeeded(SaleOrderMergingResult result) {
    if (!appSaleService.isApp("supplychain")) {
      return super.isConfirmationNeeded(result);
    }
    return super.isConfirmationNeeded(result) || getChecks(result).existStockLocationDiff;
  }

  @Override
  protected void fillCommonFields(SaleOrder firstSaleOrder, SaleOrderMergingResult result) {
    super.fillCommonFields(firstSaleOrder, result);
    if (appSaleService.isApp("supplychain")) {
      getCommonFields(result).setCommonStockLocation(firstSaleOrder.getStockLocation());
    }
  }

  @Override
  protected void updateDiffsCommonFields(SaleOrder saleOrder, SaleOrderMergingResult result) {
    super.updateDiffsCommonFields(saleOrder, result);
    if (appSaleService.isApp("supplychain")) {
      CommonFieldsSupplyChainImpl commonFields = getCommonFields(result);
      ChecksSupplyChainImpl checks = getChecks(result);
      if ((commonFields.getCommonStockLocation() == null ^ saleOrder.getStockLocation() == null)
          || (commonFields.getCommonStockLocation() != saleOrder.getStockLocation()
              && !commonFields.getCommonStockLocation().equals(saleOrder.getStockLocation()))) {
        commonFields.setCommonStockLocation(null);
        checks.setExistStockLocationDiff(true);
      }
    }
  }

  @Override
  protected SaleOrder mergeSaleOrders(
      List<SaleOrder> saleOrdersToMerge, SaleOrderMergingResult result) throws AxelorException {
    if (!appSaleService.isApp("supplychain")) {
      return super.mergeSaleOrders(saleOrdersToMerge, result);
    }
    return Beans.get(SaleOrderCreateServiceSupplychainImpl.class)
        .mergeSaleOrders(
            saleOrdersToMerge,
            getCommonFields(result).getCommonCurrency(),
            getCommonFields(result).getCommonClientPartner(),
            getCommonFields(result).getCommonCompany(),
            getCommonFields(result).getCommonStockLocation(),
            getCommonFields(result).getCommonContactPartner(),
            getCommonFields(result).getCommonPriceList(),
            getCommonFields(result).getCommonTeam(),
            getCommonFields(result).getCommonTaxNumber(),
            getCommonFields(result).getCommonFiscalPosition());
  }

  @Override
  protected void updateResultWithContext(SaleOrderMergingResult result, Context context) {
    super.updateResultWithContext(result, context);
    if (appSaleService.isApp("supplychain")) {
      if (context.get("stockLocation") != null) {
        getCommonFields(result)
            .setCommonStockLocation(
                MapTools.findObject(StockLocation.class, context.get("stockLocation")));
      }
    }
  }
}
