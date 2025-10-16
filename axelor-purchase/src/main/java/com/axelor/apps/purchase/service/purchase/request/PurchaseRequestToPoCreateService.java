package com.axelor.apps.purchase.service.purchase.request;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseRequest;
import java.util.List;

public interface PurchaseRequestToPoCreateService {

  PurchaseRequestToPoGenerationResult createFromRequests(
      List<PurchaseRequest> purchaseRequests, Boolean groupBySupplier, Boolean groupByProduct)
      throws AxelorException;

  PurchaseOrder createFromRequest(PurchaseRequest purchaseRequest) throws AxelorException;
}
