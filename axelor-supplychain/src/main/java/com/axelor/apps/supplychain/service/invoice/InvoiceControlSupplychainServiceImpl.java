package com.axelor.apps.supplychain.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceControlServiceImpl;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.Objects;

public class InvoiceControlSupplychainServiceImpl extends InvoiceControlServiceImpl
    implements InvoiceControlSupplychainService {

  @Inject
  public InvoiceControlSupplychainServiceImpl(InvoiceRepository invoiceRepository) {
    super(invoiceRepository);
  }

  @Override
  public void checkOrders(Invoice invoice) throws AxelorException {
    if (this.checkSaleOrder(invoice)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.INVOICE_SALE_ORDER_INVOICED));
    }

    if (this.checkSaleOrderLines(invoice)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.INVOICE_SALE_ORDER_LINE_INVOICED));
    }

    if (this.checkPurchaseOrder(invoice)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.INVOICE_PURCHASE_ORDER_INVOICED));
    }

    if (this.checkPurchaseOrderLines(invoice)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.INVOICE_PURCHASE_ORDER_LINE_INVOICED));
    }
  }

  protected boolean checkSaleOrder(Invoice invoice) {
    return invoice.getSaleOrder() != null && this.isSaleOrderFullyInvoiced(invoice.getSaleOrder());
  }

  protected boolean checkSaleOrderLines(Invoice invoice) {
    return invoice.getInvoiceLineList().stream()
        .map(InvoiceLine::getSaleOrderLine)
        .filter(Objects::nonNull)
        .map(SaleOrderLine::getSaleOrder)
        .anyMatch(this::isSaleOrderFullyInvoiced);
  }

  protected boolean isSaleOrderFullyInvoiced(SaleOrder saleOrder) {
    return saleOrder.getAmountInvoiced().compareTo(saleOrder.getExTaxTotal()) >= 0;
  }

  protected boolean checkPurchaseOrder(Invoice invoice) {
    return invoice.getPurchaseOrder() != null
        && this.isPurchaseOrderFullyInvoiced(invoice.getPurchaseOrder());
  }

  protected boolean checkPurchaseOrderLines(Invoice invoice) {
    return invoice.getInvoiceLineList().stream()
        .map(InvoiceLine::getPurchaseOrderLine)
        .filter(Objects::nonNull)
        .map(PurchaseOrderLine::getPurchaseOrder)
        .anyMatch(this::isPurchaseOrderFullyInvoiced);
  }

  protected boolean isPurchaseOrderFullyInvoiced(PurchaseOrder purchaseOrder) {
    return purchaseOrder.getAmountInvoiced().compareTo(purchaseOrder.getExTaxTotal()) >= 0;
  }
}
