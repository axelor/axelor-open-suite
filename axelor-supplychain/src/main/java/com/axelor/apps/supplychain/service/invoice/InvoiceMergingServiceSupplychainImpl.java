package com.axelor.apps.supplychain.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceMergingServiceImpl;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.util.List;
import java.util.StringJoiner;

public class InvoiceMergingServiceSupplychainImpl extends InvoiceMergingServiceImpl {

  protected static class CommonFieldsSupplychainImpl extends CommonFieldsImpl {
    private SaleOrder commonSaleOrder;
    private PurchaseOrder commonPurchaseOrder;

    public SaleOrder getCommonSaleOrder() {
      return commonSaleOrder;
    }

    public void setCommonSaleOrder(SaleOrder commonSaleOrder) {
      this.commonSaleOrder = commonSaleOrder;
    }

    public PurchaseOrder getCommonPurchaseOrder() {
      return commonPurchaseOrder;
    }

    public void setCommonPurchaseOrder(PurchaseOrder commonPurchaseOrder) {
      this.commonPurchaseOrder = commonPurchaseOrder;
    }
  }

  protected static class ChecksSupplychainImpl extends ChecksImpl {
    private boolean saleOrderIsNull;
    private boolean purchaseOrderIsNull;

    public boolean isSaleOrderIsNull() {
      return saleOrderIsNull;
    }

    public void setSaleOrderIsNull(boolean saleOrderIsNull) {
      this.saleOrderIsNull = saleOrderIsNull;
    }

    public boolean isPurchaseOrderIsNull() {
      return purchaseOrderIsNull;
    }

    public void setPurchaseOrderIsNull(boolean purchaseOrderIsNull) {
      this.purchaseOrderIsNull = purchaseOrderIsNull;
    }
  }

  protected static class InvoiceMergingResultSupplychainImpl extends InvoiceMergingResultImpl {
    private final CommonFieldsSupplychainImpl commonFields;
    private final ChecksSupplychainImpl checks;

    public InvoiceMergingResultSupplychainImpl() {
      super();
      this.commonFields = new CommonFieldsSupplychainImpl();
      this.checks = new ChecksSupplychainImpl();
    }
  }

  @Override
  public InvoiceMergingResultSupplychainImpl create() {
    return new InvoiceMergingResultSupplychainImpl();
  }

  @Override
  public CommonFieldsSupplychainImpl getCommonFields(InvoiceMergingResult result) {
    return ((InvoiceMergingResultSupplychainImpl) result).commonFields;
  }

  @Override
  public ChecksSupplychainImpl getChecks(InvoiceMergingResult result) {
    return ((InvoiceMergingResultSupplychainImpl) result).checks;
  }

  @Override
  protected void fillCommonFields(Invoice invoice, InvoiceMergingResult result, int invoiceCount) {
    super.fillCommonFields(invoice, result, invoiceCount);
    if (invoiceCount == 1) {
      if (result.getInvoiceType().equals(InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)) {
        getCommonFields(result).setCommonSaleOrder(invoice.getSaleOrder());
        if (getCommonFields(result).getCommonSaleOrder() == null) {
          getChecks(result).setSaleOrderIsNull(true);
        }
      }
      if (result.getInvoiceType().equals(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)) {
        getCommonFields(result).setCommonPurchaseOrder(invoice.getPurchaseOrder());
        if (getCommonFields(result).getCommonPurchaseOrder() == null) {
          getChecks(result).setPurchaseOrderIsNull(true);
        }
      }
    } else {
      if (result.getInvoiceType().equals(InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)) {
        if (getCommonFields(result).getCommonSaleOrder() != null
            && !getCommonFields(result).getCommonSaleOrder().equals(invoice.getSaleOrder())) {
          getCommonFields(result).setCommonSaleOrder(null);
        }
        if (invoice.getSaleOrder() != null) {
          getChecks(result).setSaleOrderIsNull(true);
        }
      }
      if (result.getInvoiceType().equals(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)) {
        if (getCommonFields(result).getCommonPurchaseOrder() != null
            && !getCommonFields(result)
                .getCommonPurchaseOrder()
                .equals(invoice.getPurchaseOrder())) {
          getCommonFields(result).setCommonPurchaseOrder(null);
        }
        if (invoice.getPurchaseOrder() != null) {
          getChecks(result).setPurchaseOrderIsNull(true);
        }
      }
    }
  }

  @Override
  protected void checkErrors(StringJoiner fieldErrors, InvoiceMergingResult result)
      throws AxelorException {
    super.checkErrors(fieldErrors, result);
    if (result.getInvoiceType().equals(InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)) {
      if (getCommonFields(result).getCommonSaleOrder() == null
          && !getChecks(result).isSaleOrderIsNull()) {
        fieldErrors.add(
            I18n.get(
                com.axelor.apps.account.exception.IExceptionMessage.INVOICE_MERGE_ERROR_SALEORDER));
      }
    }
    if (result.getInvoiceType().equals(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)) {
      if (getCommonFields(result).getCommonPurchaseOrder() == null
          && !getChecks(result).isPurchaseOrderIsNull()) {
        fieldErrors.add(
            I18n.get(
                com.axelor.apps.account.exception.IExceptionMessage
                    .INVOICE_MERGE_ERROR_PURCHASEORDER));
      }
    }
  }

  @Override
  protected Invoice generateMergedInvoice(
      List<Invoice> invoicesToMerge, InvoiceMergingResult result) throws AxelorException {
    if (result.getInvoiceType().equals(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)) {
      return Beans.get(PurchaseOrderInvoiceService.class)
          .mergeInvoice(
              invoicesToMerge,
              getCommonFields(result).getCommonCompany(),
              getCommonFields(result).getCommonCurrency(),
              getCommonFields(result).getCommonPartner(),
              getCommonFields(result).getCommonContactPartner(),
              getCommonFields(result).getCommonPriceList(),
              getCommonFields(result).getCommonPaymentMode(),
              getCommonFields(result).getCommonPaymentCondition(),
              getCommonFields(result).getCommonSupplierInvoiceNb(),
              getCommonFields(result).getCommonOriginDate(),
              getCommonFields(result).getCommonPurchaseOrder());
    }
    if (result.getInvoiceType().equals(InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)) {
      return Beans.get(SaleOrderInvoiceService.class)
          .mergeInvoice(
              invoicesToMerge,
              getCommonFields(result).getCommonCompany(),
              getCommonFields(result).getCommonCurrency(),
              getCommonFields(result).getCommonPartner(),
              getCommonFields(result).getCommonContactPartner(),
              getCommonFields(result).getCommonPriceList(),
              getCommonFields(result).getCommonPaymentMode(),
              getCommonFields(result).getCommonPaymentCondition(),
              getCommonFields(result).getCommonSaleOrder());
    }
    return null;
  }
}
