package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.TimesheetLine;
import java.math.BigDecimal;
import java.util.List;

public interface TimesheetInvoiceService {

  List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<TimesheetLine> timesheetLineList, int priority) throws AxelorException;

  List<InvoiceLine> createInvoiceLine(
      Invoice invoice,
      Product product,
      Employee employee,
      String date,
      BigDecimal hoursDuration,
      int priority,
      PriceList priceList)
      throws AxelorException;
}
