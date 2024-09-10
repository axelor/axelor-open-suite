package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorder.SaleOrderCheckSaveServiceImpl;
import com.axelor.i18n.I18n;

public class SaleOrderProductionCheckSaveServiceImpl extends SaleOrderCheckSaveServiceImpl {

  @Override
  public void checkSaleOrderLineSubList(SaleOrder saleOrder) throws AxelorException {
    super.checkSaleOrderLineSubList(saleOrder);

    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      if ((!saleOrderLine.getIsToProduce() && !saleOrderLine.getSubSaleOrderLineList().isEmpty())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(ProductionExceptionMessage.SUB_SALE_ORDER_LINE_CAN_NOT_BE_CREATED));
      }
      if (!saleOrderLine.getSubSaleOrderLineList().isEmpty()) {
        checkSubList(saleOrderLine);
      }
    }
  }

  protected void checkSubList(SaleOrderLine saleOrderLine) throws AxelorException {
    for (SaleOrderLine subSol : saleOrderLine.getSubSaleOrderLineList()) {
      if ((!subSol.getIsToProduce() && !subSol.getSubSaleOrderLineList().isEmpty())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(ProductionExceptionMessage.SUB_SALE_ORDER_LINE_CAN_NOT_BE_CREATED));
      }
      if (!subSol.getSubSaleOrderLineList().isEmpty()) {
        checkSubList(subSol);
      }
    }
  }
}
