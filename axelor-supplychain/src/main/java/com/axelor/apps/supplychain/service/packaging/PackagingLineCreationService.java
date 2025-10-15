package com.axelor.apps.supplychain.service.packaging;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.PackagingLine;
import java.math.BigDecimal;
import java.util.List;

public interface PackagingLineCreationService {

  void addPackagingLines(Packaging packaging, List<StockMoveLine> stockMoveLineList)
      throws AxelorException;

  String getStockMoveLineDomain(LogisticalForm logisticalForm) throws AxelorException;

  PackagingLine createPackagingLine(
      Packaging packaging, StockMoveLine stockMoveLine, BigDecimal quantity) throws AxelorException;

  void updateQuantity(PackagingLine packagingLine, BigDecimal quantity) throws AxelorException;

  void deletePackagingLine(PackagingLine packagingLine) throws AxelorException;

  LogisticalForm getParentLogisticalForm(PackagingLine packagingLine);
}
