/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
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
import com.axelor.meta.CallMethod;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderBudgetServiceImpl extends SaleOrderInvoiceProjectServiceImpl
    implements SaleOrderBudgetService {

  protected AppBudgetService appBudgetService;
  protected BudgetDistributionService budgetDistributionService;
  protected SaleOrderLineBudgetService saleOrderLineBudgetService;
  protected BudgetService budgetService;

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
      AppBudgetService appBudgetService,
      BudgetDistributionService budgetDistributionService,
      SaleOrderLineBudgetService saleOrderLineBudgetService,
      BudgetService budgetService) {
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
    this.appBudgetService = appBudgetService;
    this.budgetDistributionService = budgetDistributionService;
    this.saleOrderLineBudgetService = saleOrderLineBudgetService;
    this.budgetService = budgetService;
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
        String alertMessage =
            saleOrderLineBudgetService.computeBudgetDistribution(saleOrder, saleOrderLine);
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
        budgetService.validateBudgetDistributionAmounts(
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

    if (CollectionUtils.isNotEmpty(saleOrder.getSaleOrderLineList())
        && (saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_FINALIZED_QUOTATION
            || saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_ORDER_CONFIRMED
            || saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_ORDER_COMPLETED)) {
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
        if (CollectionUtils.isNotEmpty(saleOrderLine.getBudgetDistributionList())) {
          saleOrderLine.getBudgetDistributionList().stream()
              .forEach(
                  budgetDistribution -> {
                    Budget budget = budgetDistribution.getBudget();
                    budgetService.updateLines(budget);
                    budgetService.computeTotalAmountCommitted(budget);
                    budgetService.computeTotalAmountPaid(budget);
                    budgetService.computeToBeCommittedAmount(budget);
                  });
        }
      }
    }
  }

  @Override
  @CallMethod
  public String getBudgetExceedAlert(SaleOrder saleOrder) {
    String budgetExceedAlert = "";

    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();

    if (appBudgetService.getAppBudget() != null
        && appBudgetService.getAppBudget().getCheckAvailableBudget()
        && saleOrder.getId() != null
        && CollectionUtils.isNotEmpty(saleOrderLineList)) {

      Map<Budget, BigDecimal> amountPerBudgetMap = new HashMap<>();

      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        LocalDate date =
            saleOrderLine.getSaleOrder().getOrderDate() != null
                ? saleOrderLine.getSaleOrder().getOrderDate()
                : saleOrderLine.getSaleOrder().getCreationDate();
        if (appBudgetService.getAppBudget().getManageMultiBudget()
            && CollectionUtils.isNotEmpty(saleOrderLine.getBudgetDistributionList())) {

          for (BudgetDistribution budgetDistribution : saleOrderLine.getBudgetDistributionList()) {
            Budget budget = budgetDistribution.getBudget();

            if (!amountPerBudgetMap.containsKey(budget)) {
              amountPerBudgetMap.put(budget, budgetDistribution.getAmount());
            } else {
              BigDecimal oldAmount = amountPerBudgetMap.get(budget);

              amountPerBudgetMap.remove(budget);
              amountPerBudgetMap.put(budget, oldAmount.add(budgetDistribution.getAmount()));
            }
          }
          for (Map.Entry<Budget, BigDecimal> budgetEntry : amountPerBudgetMap.entrySet()) {
            budgetExceedAlert +=
                budgetDistributionService.getBudgetExceedAlert(
                    budgetEntry.getKey(), budgetEntry.getValue(), date);
          }
        } else {
          Budget budget = saleOrderLine.getBudget();
          if (budget != null) {
            if (!amountPerBudgetMap.containsKey(budget)) {
              amountPerBudgetMap.put(budget, saleOrderLine.getExTaxTotal());
            } else {
              BigDecimal oldAmount = amountPerBudgetMap.get(budget);
              amountPerBudgetMap.put(budget, oldAmount.add(saleOrderLine.getExTaxTotal()));
            }

            budgetExceedAlert +=
                budgetDistributionService.getBudgetExceedAlert(
                    budget, amountPerBudgetMap.get(budget), date);
          }
        }
      }
    }
    return budgetExceedAlert;
  }
}
