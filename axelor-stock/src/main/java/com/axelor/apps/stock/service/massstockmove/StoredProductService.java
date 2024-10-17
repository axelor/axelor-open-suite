package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.StoredProduct;

public interface StoredProductService {

  public StoredProduct copy(StoredProduct src, StoredProduct dest);
}
