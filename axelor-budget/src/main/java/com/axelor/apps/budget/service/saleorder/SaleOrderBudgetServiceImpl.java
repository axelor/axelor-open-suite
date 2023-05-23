package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.service.BudgetBudgetDistributionService;
import com.axelor.apps.budget.service.BudgetBudgetService;
import com.axelor.apps.businessproject.service.SaleOrderInvoiceProjectServiceImpl;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowService;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.service.CommonInvoiceService;
import com.axelor.apps.supplychain.service.SaleInvoicingStateService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineOrderService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderBudgetServiceImpl extends SaleOrderInvoiceProjectServiceImpl
    implements SaleOrderBudgetService {

  protected AppAccountService appAccountService;
  protected BudgetBudgetDistributionService budgetDistributionService;
  protected SaleOrderLineBudgetService saleOrderLineBudgetService;
  protected BudgetBudgetService budgetBudgetService;

  @Inject
  public SaleOrderBudgetServiceImpl(
      AppBaseService appBaseService,
      AppSupplychainService appSupplychainService,
      SaleOrderRepository saleOrderRepo,
      InvoiceRepository invoiceRepo,
      InvoiceServiceSupplychainImpl invoiceService,
      AppBusinessProjectService appBusinessProjectService,
      StockMoveRepository stockMoveRepository,
      SaleOrderLineService saleOrderLineService,
      SaleOrderWorkflowService saleOrderWorkflowService,
      InvoiceTermService invoiceTermService,
      CommonInvoiceService commonInvoiceService,
      InvoiceLineOrderService invoiceLineOrderService,
      SaleInvoicingStateService saleInvoicingStateService,
      AppAccountService appAccountService,
      BudgetBudgetDistributionService budgetDistributionService,
      SaleOrderLineBudgetService saleOrderLineBudgetService,
      BudgetBudgetService budgetBudgetService) {
    super(
        appBaseService,
        appSupplychainService,
        saleOrderRepo,
        invoiceRepo,
        invoiceService,
        saleOrderLineService,
        stockMoveRepository,
        invoiceTermService,
        saleOrderWorkflowService,
        commonInvoiceService,
        invoiceLineOrderService,
        saleInvoicingStateService,
        appBusinessProjectService);
    this.appAccountService = appAccountService;
    this.budgetDistributionService = budgetDistributionService;
    this.saleOrderLineBudgetService = saleOrderLineBudgetService;
    this.budgetBudgetService = budgetBudgetService;
  }

  @Override
  public void generateBudgetDistribution(SaleOrder saleOrder) {

    if (CollectionUtils.isNotEmpty(saleOrder.getSaleOrderLineList())) {
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {

        if (saleOrderLine.getBudget() != null) {
          BudgetDistribution budgetDistribution = null;
          if (CollectionUtils.isNotEmpty(saleOrderLine.getBudgetDistributionList())) {
            Optional<BudgetDistribution> optionalBudgetDistribution =
                saleOrderLine.getBudgetDistributionList().stream()
                    .filter(
                        it ->
                            it.getBudget() != null
                                && it.getBudget().equals(saleOrderLine.getBudget()))
                    .findFirst();
            budgetDistribution =
                optionalBudgetDistribution.isPresent() ? optionalBudgetDistribution.get() : null;
          }
          if (budgetDistribution == null) {
            budgetDistribution = new BudgetDistribution();
            budgetDistribution.setBudget(saleOrderLine.getBudget());
            saleOrderLine.addBudgetDistributionListItem(budgetDistribution);
          }
          budgetDistribution.setAmount(saleOrderLine.getCompanyExTaxTotal());
        }
      }
    }
  }

  @Override
  @Transactional
  public String computeBudgetDistribution(SaleOrder saleOrder) {
    List<String> alertMessageTokenList = new ArrayList<>();
    if (!CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
        String alertMessage = saleOrderLineBudgetService.computeBudgetDistribution(saleOrderLine);
        if (!Strings.isNullOrEmpty(alertMessage)) {
          alertMessageTokenList.add(alertMessage);
        }
      }
      saleOrderRepo.save(saleOrder);
    }
    return String.join(", ", alertMessageTokenList);
  }

  @Override
  public void validateSaleAmountWithBudgetDistribution(SaleOrder saleOrder) throws AxelorException {
    if (!CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
      for (SaleOrderLine soLine : saleOrder.getSaleOrderLineList()) {
        budgetBudgetService.validateBudgetDistributionAmounts(
            soLine.getBudgetDistributionList(),
            soLine.getCompanyExTaxTotal(),
            soLine.getProduct().getCode());
      }
    }
  }

  @Override
  public boolean isBudgetInLines(SaleOrder saleOrder) {
    if (!CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
        if (saleOrderLine.getBudget() != null
            || !CollectionUtils.isEmpty(saleOrderLine.getBudgetDistributionList())) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public List<InvoiceLine> createInvoiceLine(
      Invoice invoice, SaleOrderLine saleOrderLine, BigDecimal qtyToInvoice)
      throws AxelorException {
    List<InvoiceLine> invoiceLines = super.createInvoiceLine(invoice, saleOrderLine, qtyToInvoice);

    for (InvoiceLine invoiceLine : invoiceLines) {
      if (saleOrderLine != null) {
        invoiceLine.setBudget(saleOrderLine.getBudget());
        this.copyBudgetDistributionList(saleOrderLine.getBudgetDistributionList(), invoiceLine);
      }
    }
    return invoiceLines;
  }

  public void copyBudgetDistributionList(
      List<BudgetDistribution> originalBudgetDistributionList, InvoiceLine invoiceLine) {

    if (CollectionUtils.isEmpty(originalBudgetDistributionList)) {
      return;
    }

    for (BudgetDistribution budgetDistributionIt : originalBudgetDistributionList) {
      BudgetDistribution budgetDistribution = new BudgetDistribution();
      budgetDistribution.setBudget(budgetDistributionIt.getBudget());
      budgetDistribution.setAmount(budgetDistributionIt.getAmount());
      budgetDistribution.setBudgetAmountAvailable(budgetDistributionIt.getBudgetAmountAvailable());
      invoiceLine.addBudgetDistributionListItem(budgetDistribution);
    }
  }

  @Override
  public void updateBudgetLinesFromSaleOrder(SaleOrder saleOrder) {

    if (CollectionUtils.isNotEmpty(saleOrder.getSaleOrderLineList())) {
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
        if (CollectionUtils.isNotEmpty(saleOrderLine.getBudgetDistributionList())) {
          saleOrderLine.getBudgetDistributionList().stream()
              .forEach(
                  budgetDistribution -> {
                    Budget budget = budgetDistribution.getBudget();
                    budgetBudgetService.updateLines(budget);
                    budgetBudgetService.computeTotalAmountCommitted(budget);
                    budgetBudgetService.computeTotalAmountPaid(budget);
                    budgetBudgetService.computeToBeCommittedAmount(budget);
                  });
        }
      }
    }
  }
}
