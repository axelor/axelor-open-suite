package com.axelor.apps.supplychain.service.invoice.sublines;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.sublines.InvoiceSubLineServiceImpl;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.studio.app.service.AppService;
import com.google.inject.Inject;

public class InvoiceSubLineSupplychainService extends InvoiceSubLineServiceImpl {

  protected AppService appService;
  protected AppPurchaseService appPurchaseService;

  @Inject
  public InvoiceSubLineSupplychainService(
      InvoiceLineRepository invoiceLineRepository,
      InvoiceRepository invoiceRepository,
      TaxService taxService,
      AppBaseService appBaseService,
      InvoiceLineService invoiceLineService,
      AppAccountService appAccountService,
      InvoiceService invoiceService,
      AppService appService,
      AppPurchaseService appPurchaseService) {
    super(
        invoiceLineRepository,
        invoiceRepository,
        taxService,
        appBaseService,
        invoiceLineService,
        appAccountService,
        invoiceService);
    this.appService = appService;
    this.appPurchaseService = appPurchaseService;
  }

  @Override
  public String getProductDomain(Invoice invoice, boolean isFilterOnSupplier) {
    String domain = super.getProductDomain(invoice, isFilterOnSupplier);
    if (appService.isApp("purchase")) {
      int operationTypeSelect = invoice.getOperationTypeSelect();
      if (appPurchaseService.getAppPurchase().getManageSupplierCatalog()
          && operationTypeSelect < 3
          && isFilterOnSupplier) {
        Long partnerId = invoice.getPartner().getId();
        domain =
            "self.isModel = false and self.id IN (SELECT product.id FROM SupplierCatalog WHERE supplierPartner.id = "
                + partnerId
                + " AND self.purchasable = true";
      }
    }
    return domain;
  }
}
