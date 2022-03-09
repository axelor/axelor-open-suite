package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.exception.AxelorException;

public interface StockMoveInvoiceViewGeneratorService {

  String invoiceGridGenerator(Invoice invoice) throws AxelorException;

  String invoiceFilterGenerator(Invoice invoice) throws AxelorException;
}
