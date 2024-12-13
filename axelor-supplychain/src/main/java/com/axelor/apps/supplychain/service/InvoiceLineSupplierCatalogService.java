package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Map;

public interface InvoiceLineSupplierCatalogService {

  void setSupplierCatalogInfo(
      Invoice invoice, InvoiceLine invoiceLine, Map<String, Object> productInformation)
      throws AxelorException;

  void checkMinQty(
      Invoice invoice, InvoiceLine invoiceLine, ActionRequest request, ActionResponse response)
      throws AxelorException;

  Map<String, Object> updateInfoFromCatalog(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException;

  SupplierCatalog getSupplierCatalog(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException;
}
