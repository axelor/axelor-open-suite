package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.apps.purchase.db.PurchaseOrder;
import java.util.List;

public interface CallTenderPurchaseOrderService {
  List<PurchaseOrder> generatePurchaseOrders(
      CallTender callTender, List<CallTenderOffer> selectedCallTenderOfferList)
      throws AxelorException;
}
