package com.axelor.apps.gst.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceLineServiceImpl;
import com.axelor.apps.base.db.State;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class InvoiceLineGstServiceImpl extends InvoiceLineServiceImpl
    implements InvoiceLineGstService {

  @Inject
  public InvoiceLineGstServiceImpl(
      CurrencyService currencyService,
      PriceListService priceListService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AccountManagementAccountService accountManagementAccountService,
      ProductCompanyService productCompanyService) {
    super(
        currencyService,
        priceListService,
        appAccountService,
        analyticMoveLineService,
        accountManagementAccountService,
        productCompanyService);
  }

  @Override
  public Boolean checkIsStateDiff(Invoice invoice) {
    Boolean isStateDiff = null;

    State companyState = null;
    State invoiceAddress = null;
    if (invoice != null
        && invoice.getCompany() != null
        && invoice.getCompany().getAddress() != null) {
      companyState = invoice.getCompany().getAddress().getState();
    }

    if (invoice != null
        && invoice.getAddress() != null
        && invoice.getAddress().getState() != null) {
      invoiceAddress = invoice.getAddress().getState();
    }

    if (companyState != null && invoiceAddress != null) {
      if (companyState.equals(invoiceAddress)) isStateDiff = true;
      else if (!companyState.equals(invoiceAddress)) isStateDiff = false;
    }

    return isStateDiff;
  }

  @Override
  public InvoiceLine calculateInvoiceLine(InvoiceLine invoiceline, Boolean isStateDiff) {

    BigDecimal amount = BigDecimal.ZERO;
    BigDecimal igst = BigDecimal.ZERO;
    BigDecimal sgst = BigDecimal.ZERO;
    BigDecimal cgst = BigDecimal.ZERO;
    BigDecimal gross = BigDecimal.ZERO;

    amount = invoiceline.getPrice().multiply(invoiceline.getQty());

    invoiceline.setExTaxTotal(amount.setScale(2, RoundingMode.HALF_UP));

    BigDecimal multiply = amount.multiply(invoiceline.getGstRate()).divide(new BigDecimal(100));

    if (isStateDiff != null) {
      if (!isStateDiff) {
        invoiceline.setSgst(BigDecimal.ZERO);
        invoiceline.setCgst(BigDecimal.ZERO);
        igst = multiply;
        invoiceline.setIgst(igst.setScale(2, RoundingMode.HALF_UP));
      } else {
        invoiceline.setIgst(BigDecimal.ZERO);

        sgst = multiply.divide(new BigDecimal(2));
        invoiceline.setSgst(sgst.setScale(2, RoundingMode.HALF_UP));

        cgst = sgst;
        invoiceline.setCgst(cgst.setScale(2, RoundingMode.HALF_UP));
      }
    } else {
      invoiceline.setSgst(BigDecimal.ZERO);
      invoiceline.setCgst(BigDecimal.ZERO);
      invoiceline.setIgst(BigDecimal.ZERO);
    }
    gross = amount.add(igst).add(sgst);
    invoiceline.setInTaxTotal(gross);
    return invoiceline;
  }
}
