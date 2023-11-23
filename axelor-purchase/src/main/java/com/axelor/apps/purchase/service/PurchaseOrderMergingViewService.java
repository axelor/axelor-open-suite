package com.axelor.apps.purchase.service;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.service.PurchaseOrderMergingService.PurchaseOrderMergingResult;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import java.util.List;

public interface PurchaseOrderMergingViewService {

  /**
   * Method that build a ActionViewBuilder for confirm view in the purchase order merge process.
   *
   * @param PurchaseOrderMergingResult
   * @return ActionViewBuilder
   */
  ActionViewBuilder buildConfirmView(
      PurchaseOrderMergingResult result, List<PurchaseOrder> purchaseOrdersToMerge);
}
