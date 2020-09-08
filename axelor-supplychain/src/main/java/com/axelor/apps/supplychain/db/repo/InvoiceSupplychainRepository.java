package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceManagementRepository;

public class InvoiceSupplychainRepository extends InvoiceManagementRepository {
  @Override
  public Invoice copy(Invoice entity, boolean deep) {
    Invoice copy = super.copy(entity, deep);

    copy.setSaleOrder(null);
    copy.setPurchaseOrder(null);
    copy.setStockMoveSet(null);

    for (InvoiceLine line : copy.getInvoiceLineList()) {
      line.setSaleOrderLine(null);
      line.setPurchaseOrderLine(null);
      line.setStockMoveLine(null);
      line.setOutgoingStockMove(null);
      line.setIncomingStockMove(null);
    }

    return copy;
  }
}
