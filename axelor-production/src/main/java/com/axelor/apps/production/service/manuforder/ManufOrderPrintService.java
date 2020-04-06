package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.exception.AxelorException;
import java.io.IOException;
import java.util.List;

public interface ManufOrderPrintService {

  /**
   * Print a list of manuf orders
   *
   * @return ReportSettings
   * @throws IOException
   * @throws AxelorException
   */
  String printManufOrders(List<Long> ids) throws IOException;

  /**
   * Print a single manuf order
   *
   * @param manufOrder
   * @return a path to printed manuf order.
   * @throws AxelorException
   */
  String printManufOrder(ManufOrder manufOrder) throws AxelorException;

  /** Returns the filename of a printing with multiple manuf orders. */
  String getManufOrdersFilename();

  /** Returns the filename of a printing with one manuf order. */
  String getFileName(ManufOrder manufOrder);
}
