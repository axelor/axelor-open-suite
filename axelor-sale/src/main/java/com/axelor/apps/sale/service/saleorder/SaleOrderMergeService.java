package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.service.saleorder.model.SaleOrderMergeObject;
import com.axelor.rpc.Context;
import java.util.Map;

public interface SaleOrderMergeService {

  /**
   * Method that check if the context contains value that need to be put in map.
   *
   * @param context
   * @param map
   */
  void computeMapWithContext(Context context, Map<String, SaleOrderMergeObject> map);
}
