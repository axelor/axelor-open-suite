package com.axelor.apps.purchase.service.split;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public interface PurchaseOrderSplitService {

  PurchaseOrder separateInNewQuotation(
      PurchaseOrder purchaseOrder, ArrayList<LinkedHashMap<String, Object>> purchaseOrderLines)
      throws AxelorException;
}
