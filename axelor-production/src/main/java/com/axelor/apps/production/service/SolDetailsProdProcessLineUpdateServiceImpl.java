package com.axelor.apps.production.service;

import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SolDetailsProdProcessLineUpdateServiceImpl
    implements SolDetailsProdProcessLineUpdateService {

  @Override
  public boolean isSolDetailsUpdated(
      SaleOrderLine saleOrderLine, List<SaleOrderLineDetails> saleOrderLineDetailsList) {
    if (CollectionUtils.isEmpty(saleOrderLineDetailsList)) {
      return true;
    }
    for (SaleOrderLineDetails saleOrderLineDetails : saleOrderLineDetailsList) {
      if (!isSolDetailsSyncWithProdProcessLine(saleOrderLineDetails)) {
        return false;
      }
    }
    return true;
  }

  protected boolean isSolDetailsSyncWithProdProcessLine(SaleOrderLineDetails saleOrderLineDetails) {
    ProdProcessLine prodProcessLine = saleOrderLineDetails.getProdProcessLine();
    if (prodProcessLine == null) {
      return true;
    }

    return prodProcessLine
                .getMinCapacityPerCycle()
                .compareTo(saleOrderLineDetails.getMinCapacityPerCycle())
            == 0
        && prodProcessLine
                .getMaxCapacityPerCycle()
                .compareTo(saleOrderLineDetails.getMaxCapacityPerCycle())
            == 0
        && prodProcessLine.getDurationPerCycle().equals(saleOrderLineDetails.getDurationPerCycle())
        && prodProcessLine.getSetupDuration().equals(saleOrderLineDetails.getSetupDuration())
        && prodProcessLine.getStartingDuration().equals(saleOrderLineDetails.getStartingDuration())
        && prodProcessLine.getEndingDuration().equals(saleOrderLineDetails.getEndingDuration())
        && prodProcessLine.getCostTypeSelect().equals(saleOrderLineDetails.getCostTypeSelect())
        && prodProcessLine.getCostAmount().compareTo(saleOrderLineDetails.getCostAmount()) == 0
        && prodProcessLine.getHrCostTypeSelect().equals(saleOrderLineDetails.getHrCostTypeSelect())
        && prodProcessLine.getHrCostAmount().compareTo(saleOrderLineDetails.getHrCostAmount()) == 0;
  }
}
