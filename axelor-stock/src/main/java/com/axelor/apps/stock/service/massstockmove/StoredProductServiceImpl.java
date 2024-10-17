package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.StoredProduct;

public class StoredProductServiceImpl implements StoredProductService {
  @Override
  public StoredProduct copy(StoredProduct src, StoredProduct dest) {
    dest.setToStockLocation(src.getToStockLocation());
    dest.setStoredProduct(src.getStoredProduct());
    dest.setStoredQty(src.getStoredQty());
    dest.setTrackingNumber(src.getTrackingNumber());
    dest.setUnit(src.getUnit());
    return dest;
  }
}
