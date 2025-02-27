package com.axelor.apps.production.service.message;

import com.axelor.apps.base.contextholder.AxelorContextHolder;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.message.StockLineCheckExceptionMessageServiceImpl;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;

public class StockLineCheckExceptionMessageServiceProductionImpl
    extends StockLineCheckExceptionMessageServiceImpl {

  @Override
  public String getCheckMinMessageExceptionMessage() {
    Context currentContext = AxelorContextHolder.getContext();
    if (currentContext.getContextClass().isAssignableFrom(ManufOrder.class)) {
      return I18n.get(ProductionExceptionMessage.LOCATION_LINE_PRODUCTION_1);
    }
    return I18n.get(StockExceptionMessage.LOCATION_LINE_1);
  }

  @Override
  public String getCheckMinMessageExceptionMessage2() {
    Context currentContext = AxelorContextHolder.getContext();
    if (currentContext.getContextClass().isAssignableFrom(ManufOrder.class)) {
      return I18n.get(ProductionExceptionMessage.LOCATION_LINE_PRODUCTION_2);
    }
    return I18n.get(StockExceptionMessage.LOCATION_LINE_2);
  }
}
