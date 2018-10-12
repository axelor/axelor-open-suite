package com.axelor.apps.sale.service.saleorder.print;

import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface SaleOrderPrintService {

  /**
   * Print a list of sale orders in the same output.
   *
   * @param ids ids of the sale order.
   * @return the link to the generated file.
   * @throws IOException
   */
  String printSaleOrders(List<Long> ids) throws IOException;

  ReportSettings prepareReportSettings(SaleOrder saleOrder, boolean proforma, String format)
      throws AxelorException;

  File print(SaleOrder saleOrder, boolean proforma, String format) throws AxelorException;

  String printSaleOrder(SaleOrder saleOrder, boolean proforma, String format)
      throws AxelorException, IOException;
}
