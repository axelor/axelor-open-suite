package com.axelor.apps.production.rest;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.rest.dto.ConsumedProductResponse;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface ManufOrderProductRestService {

  List<ConsumedProductResponse> getConsumedProductList(ManufOrder manufOrder)
      throws AxelorException;
}
