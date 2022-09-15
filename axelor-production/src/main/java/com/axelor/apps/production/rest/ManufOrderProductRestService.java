package com.axelor.apps.production.rest;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.rest.dto.ManufOrderProductResponse;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface ManufOrderProductRestService {

  List<ManufOrderProductResponse> getConsumedProductList(ManufOrder manufOrder)
      throws AxelorException;

  List<ManufOrderProductResponse> getProducedProductList(ManufOrder manufOrder)
      throws AxelorException;
}
