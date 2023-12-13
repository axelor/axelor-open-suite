package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.report.engine.ReportSettings;
import java.time.LocalDateTime;

public interface StockLocationPrintService {

  ReportSettings print(
      Integer printType,
      String exportType,
      LocalDateTime financialDataDateTime,
      Boolean withoutDetailsByStockLocation,
      Long... stockLocationIds)
      throws AxelorException;
}
