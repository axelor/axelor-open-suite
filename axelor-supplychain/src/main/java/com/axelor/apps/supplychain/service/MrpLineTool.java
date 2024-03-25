package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.MrpLineOrigin;
import com.axelor.db.JPA;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class MrpLineTool {

  private MrpLineTool() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Return a saleOrderLine that's in the origins of a MrpLine. The origins are generally a list of
   * one element only, if this behavior changes think about changing the logic in this method
   *
   * @param mrpLine
   * @return
   */
  public static Optional<SaleOrderLine> getOriginSaleOrderLineInMrpLineOrigin(MrpLine mrpLine) {
    if (mrpLine == null) {
      return Optional.empty();
    }
    List<MrpLineOrigin> mrpLineOrigins = mrpLine.getMrpLineOriginList();

    if (mrpLineOrigins == null || CollectionUtils.isEmpty(mrpLineOrigins)) {
      return Optional.empty();
    }
    return mrpLineOrigins.stream()
        .filter(mlo -> SaleOrderLine.class.getName().equals(mlo.getRelatedToSelect()))
        .map(MrpLineOrigin::getRelatedToSelectId)
        .map(solId -> JPA.find(SaleOrderLine.class, solId))
        .findAny();
  }
}
