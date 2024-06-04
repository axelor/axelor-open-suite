package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Objects;

public class TrackingNumberSupplychainServiceImpl implements TrackingNumberSupplychainService {

  protected final TrackingNumberRepository trackingNumberRepository;

  @Inject
  public TrackingNumberSupplychainServiceImpl(TrackingNumberRepository trackingNumberRepository) {
    this.trackingNumberRepository = trackingNumberRepository;
  }

  @Override
  public void freeOriginSaleOrderLine(SaleOrderLine saleOrderLine) {
    trackingNumberRepository
        .all()
        .filter("self.originSaleOrderLine = :saleOrderLine")
        .bind("saleOrderLine", saleOrderLine)
        .fetch()
        .forEach(this::freeOriginSaleOrderLine);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void freeOriginSaleOrderLine(TrackingNumber trackingNumber) {
    Objects.requireNonNull(trackingNumber);

    trackingNumber.setOriginSaleOrderLine(null);
  }
}
