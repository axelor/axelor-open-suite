package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.rest.dto.SaleOrderLinePostRequest;
import java.util.List;

public interface SaleOrderRestService {

  SaleOrder fetchAndAddSaleOrderLines(
      List<SaleOrderLinePostRequest> saleOrderLinePostRequests, SaleOrder saleOrder)
      throws AxelorException;
}
