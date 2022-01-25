package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.service.saleorder.model.SaleOrderMergeObject;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import java.util.List;
import java.util.Map;

public interface SaleOrderMergeViewService {

  /**
   * Method called in sale orger merge process that checks if one of the entry of contactPartner,
   * priceList or team is different. If one is different, that means that not every saleorder in the
   * process share the same value.
   *
   * @param map
   * @return true if there is a diff, else false.
   */
  boolean existDiffForConfirmView(Map<String, SaleOrderMergeObject> commonMap);

  /**
   * Method that build a ActionViewBuilder for confirm view in the sale order merge process.
   *
   * @param commonMap
   * @return ActionViewBuilder
   */
  ActionViewBuilder buildConfirmView(
      Map<String, SaleOrderMergeObject> commonMap, String lineToMerge, List<Long> saleOrderIdList);
}
