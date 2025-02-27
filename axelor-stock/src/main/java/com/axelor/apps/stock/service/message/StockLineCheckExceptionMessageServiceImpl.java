package com.axelor.apps.stock.service.message;

import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.i18n.I18n;

public class StockLineCheckExceptionMessageServiceImpl
    implements StockLineCheckExceptionMessageService {
  @Override
  public String getCheckMinMessageExceptionMessage() {
    return I18n.get(StockExceptionMessage.LOCATION_LINE_1);
  }

  @Override
  public String getCheckMinMessageExceptionMessage2() {
    return I18n.get(StockExceptionMessage.LOCATION_LINE_2);
  }
}
