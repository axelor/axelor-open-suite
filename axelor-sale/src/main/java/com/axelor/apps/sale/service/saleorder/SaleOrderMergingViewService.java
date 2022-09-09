package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingService.SaleOrderMergingResult;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import java.util.List;

public interface SaleOrderMergingViewService {

  /**
   * Method that build a ActionViewBuilder for confirm view in the sale order merge process.
   *
   * @param SaleOrderMergingResult
   * @return ActionViewBuilder
   */
  ActionViewBuilder buildConfirmView(
      SaleOrderMergingResult result, String lineToMerge, List<SaleOrder> saleOrdersToMerge);
}
