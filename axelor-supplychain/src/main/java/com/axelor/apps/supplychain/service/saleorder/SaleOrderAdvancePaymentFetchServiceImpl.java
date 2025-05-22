package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;
import java.util.List;

public class SaleOrderAdvancePaymentFetchServiceImpl
    implements SaleOrderAdvancePaymentFetchService {

  protected final InvoiceRepository invoiceRepository;

  @Inject
  public SaleOrderAdvancePaymentFetchServiceImpl(InvoiceRepository invoiceRepository) {
    this.invoiceRepository = invoiceRepository;
  }

  @Override
  public List<Invoice> getAdvancePayments(SaleOrder saleOrder) {
    return invoiceRepository
        .all()
        .filter(
            "self.saleOrder.id = :saleOrderId AND self.operationSubTypeSelect = :operationSubTypeSelect AND self.operationTypeSelect = :operationTypeSelect")
        .bind("saleOrderId", saleOrder.getId())
        .bind("operationSubTypeSelect", InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE)
        .bind("operationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)
        .fetch();
  }
}
