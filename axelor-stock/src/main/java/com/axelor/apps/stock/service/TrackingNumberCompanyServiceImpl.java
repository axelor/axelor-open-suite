package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.Optional;

public class TrackingNumberCompanyServiceImpl implements TrackingNumberCompanyService {

  protected final StockMoveLineRepository stockMoveLineRepository;

  @Inject
  public TrackingNumberCompanyServiceImpl(StockMoveLineRepository stockMoveLineRepository) {
    this.stockMoveLineRepository = stockMoveLineRepository;
  }

  @Override
  public Optional<Company> getCompany(TrackingNumber trackingNumber) throws AxelorException {

    Objects.requireNonNull(trackingNumber);

    switch (trackingNumber.getOriginMoveTypeSelect()) {
      case TrackingNumberRepository.ORIGIN_MOVE_TYPE_MANUAL:
        return Optional.ofNullable(trackingNumber.getOriginStockMoveLine())
            .map(this::getStockMoveLine)
            .map(StockMoveLine::getStockMove)
            .map(StockMove::getCompany)
            .or(
                () ->
                    Optional.ofNullable(trackingNumber.getCreatedBy()).map(User::getActiveCompany));
      case TrackingNumberRepository.ORIGIN_MOVE_TYPE_INVENTORY:
        return Optional.ofNullable(trackingNumber.getOriginStockMoveLine())
            .map(this::getStockMoveLine)
            .map(StockMoveLine::getStockMove)
            .map(StockMove::getCompany)
            .or(
                () ->
                    Optional.ofNullable(trackingNumber.getOriginInventoryLine())
                        .map(InventoryLine::getInventory)
                        .map(Inventory::getCompany));
      default:
        return getDefaultCompany(trackingNumber);
    }
  }

  protected Optional<Company> getDefaultCompany(TrackingNumber trackingNumber)
      throws AxelorException {
    return Optional.empty();
  }

  protected StockMoveLine getStockMoveLine(StockMoveLine stockMoveLine) {
    if (stockMoveLine.getId() != null) {
      return stockMoveLineRepository.find(stockMoveLine.getId());
    }
    return stockMoveLine;
  }
}
