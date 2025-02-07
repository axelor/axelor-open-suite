package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.stock.service.TrackingNumberCompanyServiceImpl;
import com.google.inject.Inject;
import java.util.Optional;

public class TrackingNumberCompanySupplychainServiceImpl extends TrackingNumberCompanyServiceImpl {

  @Inject
  public TrackingNumberCompanySupplychainServiceImpl(
      StockMoveLineRepository stockMoveLineRepository) {
    super(stockMoveLineRepository);
  }

  @Override
  protected Optional<Company> getDefaultCompany(TrackingNumber trackingNumber)
      throws AxelorException {

    switch (trackingNumber.getOriginMoveTypeSelect()) {
      case TrackingNumberRepository.ORIGIN_MOVE_TYPE_SALE:
        return Optional.ofNullable(trackingNumber.getOriginStockMoveLine())
            .map(this::getStockMoveLine)
            .map(StockMoveLine::getStockMove)
            .map(StockMove::getCompany)
            .or(
                () ->
                    Optional.ofNullable(trackingNumber.getOriginSaleOrderLine())
                        .map(SaleOrderLine::getSaleOrder)
                        .map(SaleOrder::getCompany));
      case TrackingNumberRepository.ORIGIN_MOVE_TYPE_PURCHASE:
        return Optional.ofNullable(trackingNumber.getOriginStockMoveLine())
            .map(this::getStockMoveLine)
            .map(StockMoveLine::getStockMove)
            .map(StockMove::getCompany)
            .or(
                () ->
                    Optional.ofNullable(trackingNumber.getOriginPurchaseOrderLine())
                        .map(PurchaseOrderLine::getPurchaseOrder)
                        .map(PurchaseOrder::getCompany));
      default:
        return super.getDefaultCompany(trackingNumber);
    }
  }
}
