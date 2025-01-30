package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.supplychain.service.TrackingNumberCompanySupplychainServiceImpl;
import com.google.inject.Inject;
import java.util.Optional;

public class TrackingNumberCompanyProductionServiceImpl
    extends TrackingNumberCompanySupplychainServiceImpl {

  @Inject
  public TrackingNumberCompanyProductionServiceImpl(
      StockMoveLineRepository stockMoveLineRepository) {
    super(stockMoveLineRepository);
  }

  @Override
  protected Optional<Company> getDefaultCompany(TrackingNumber trackingNumber)
      throws AxelorException {

    if (trackingNumber.getOriginMoveTypeSelect()
        == TrackingNumberRepository.ORIGIN_MOVE_TYPE_MANUFACTURING) {
      return Optional.ofNullable(trackingNumber.getOriginStockMoveLine())
          .map(this::getStockMoveLine)
          .map(StockMoveLine::getStockMove)
          .map(StockMove::getCompany)
          .or(
              () ->
                  Optional.ofNullable(trackingNumber.getOriginManufOrder())
                      .map(ManufOrder::getCompany)
                      .or(
                          () ->
                              Optional.ofNullable(trackingNumber.getOriginStockMoveLine())
                                  .map(StockMoveLine::getStockMove)
                                  .map(StockMove::getCompany)));
    }
    return super.getDefaultCompany(trackingNumber);
  }
}
