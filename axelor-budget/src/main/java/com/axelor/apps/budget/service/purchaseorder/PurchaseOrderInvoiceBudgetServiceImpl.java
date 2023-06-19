package com.axelor.apps.budget.service.purchaseorder;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.repo.BudgetDistributionRepository;
import com.axelor.apps.businessproject.service.PurchaseOrderInvoiceProjectServiceImpl;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.supplychain.db.repo.TimetableRepository;
import com.axelor.apps.supplychain.service.CommonInvoiceService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychain;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineOrderService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.List;

public class PurchaseOrderInvoiceBudgetServiceImpl extends PurchaseOrderInvoiceProjectServiceImpl {

  protected BudgetDistributionRepository budgetDistributionRepository;

  @Inject
  public PurchaseOrderInvoiceBudgetServiceImpl(
      InvoiceServiceSupplychain invoiceServiceSupplychain,
      InvoiceService invoiceService,
      InvoiceRepository invoiceRepo,
      TimetableRepository timetableRepo,
      AppSupplychainService appSupplychainService,
      AccountConfigService accountConfigService,
      CommonInvoiceService commonInvoiceService,
      AddressService addressService,
      InvoiceLineOrderService invoiceLineOrderService,
      PriceListService priceListService,
      PurchaseOrderLineService purchaseOrderLineService,
      AppBusinessProjectService appBusinessProjectService,
      ProductCompanyService productCompanyService,
      BudgetDistributionRepository budgetDistributionRepository) {
    super(
        invoiceServiceSupplychain,
        invoiceService,
        invoiceRepo,
        timetableRepo,
        appSupplychainService,
        accountConfigService,
        commonInvoiceService,
        addressService,
        invoiceLineOrderService,
        priceListService,
        purchaseOrderLineService,
        appBusinessProjectService,
        productCompanyService);
    this.budgetDistributionRepository = budgetDistributionRepository;
  }

  @Override
  public void processPurchaseOrderLine(
      Invoice invoice, List<InvoiceLine> invoiceLineList, PurchaseOrderLine purchaseOrderLine)
      throws AxelorException {
    super.processPurchaseOrderLine(invoice, invoiceLineList, purchaseOrderLine);

    invoiceLineList = copyBudgetDistribution(invoiceLineList, purchaseOrderLine);
  }

  protected List<InvoiceLine> copyBudgetDistribution(
      List<InvoiceLine> invoiceLineList, PurchaseOrderLine purchaseOrderLine) {
    if (!ObjectUtils.isEmpty(invoiceLineList)) {
      for (InvoiceLine invoiceLine : invoiceLineList) {
        invoiceLine.setBudget(purchaseOrderLine.getBudget());
        invoiceLine.setBudgetDistributionSumAmount(
            purchaseOrderLine.getBudgetDistributionSumAmount());
        if (!ObjectUtils.isEmpty(purchaseOrderLine.getBudgetDistributionList())) {
          for (BudgetDistribution budgetDistribution :
              purchaseOrderLine.getBudgetDistributionList()) {
            BudgetDistribution copyBudgetDistribution = new BudgetDistribution();
            copyBudgetDistribution.setBudget(budgetDistribution.getBudget());
            copyBudgetDistribution.setAmount(budgetDistribution.getAmount());
            copyBudgetDistribution.setBudgetAmountAvailable(
                budgetDistribution.getBudgetAmountAvailable());
            invoiceLine.addBudgetDistributionListItem(copyBudgetDistribution);
          }
        }
      }
    }
    return invoiceLineList;
  }
}
