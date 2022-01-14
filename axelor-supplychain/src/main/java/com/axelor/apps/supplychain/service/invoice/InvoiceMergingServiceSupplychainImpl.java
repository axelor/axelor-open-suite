package com.axelor.apps.supplychain.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.InvoiceMergingServiceImpl;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.util.List;
import java.util.StringJoiner;

public class InvoiceMergingServiceSupplychainImpl extends InvoiceMergingServiceImpl {

  protected static class CommonFieldsSupplychainImpl extends CommonFieldsImpl {
    private SaleOrder commonSaleOrder;

    public SaleOrder getCommonSaleOrder() {
      return commonSaleOrder;
    }

    public void setCommonSaleOrder(SaleOrder commonSaleOrder) {
      this.commonSaleOrder = commonSaleOrder;
    }
  }

  protected static class ChecksSupplychainImpl extends ChecksImpl {
    private boolean saleOrderIsNull;

    public boolean isSaleOrderIsNull() {
      return saleOrderIsNull;
    }

    public void setSaleOrderIsNull(boolean saleOrderIsNull) {
      this.saleOrderIsNull = saleOrderIsNull;
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
      getCommonFields(result).setCommonSaleOrder(invoice.getSaleOrder());
      if (getCommonFields(result).getCommonSaleOrder() == null) {
        getChecks(result).setSaleOrderIsNull(true);
      }
    } else {
      if (getCommonFields(result).getCommonSaleOrder() != null
          && !getCommonFields(result).getCommonSaleOrder().equals(invoice.getSaleOrder())) {
        getCommonFields(result).setCommonSaleOrder(null);
      }
      if (invoice.getSaleOrder() != null) {
        getChecks(result).setSaleOrderIsNull(true);
      }
    }
  }

  @Override
  protected void checkErrors(StringJoiner fieldErrors, InvoiceMergingResult result)
      throws AxelorException {
    super.checkErrors(fieldErrors, result);
    if (getCommonFields(result).getCommonSaleOrder() == null
        && !getChecks(result).isSaleOrderIsNull()) {
      fieldErrors.add(
          I18n.get(
              com.axelor.apps.account.exception.IExceptionMessage.INVOICE_MERGE_ERROR_SALEORDER));
    }
  }

  @Override
  protected Invoice generateMergedInvoice(
      List<Invoice> invoicesToMerge, InvoiceMergingResult result) throws AxelorException {
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
}
