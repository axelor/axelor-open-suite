/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.compute.BudgetLineComputeService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.common.ObjectUtils;
import com.axelor.meta.CallMethod;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class BudgetInvoiceServiceImpl implements BudgetInvoiceService {

  protected InvoiceRepository invoiceRepo;
  protected AppBaseService appBaseService;
  protected BudgetRepository budgetRepository;
  protected BudgetInvoiceLineService budgetInvoiceLineService;
  protected BudgetDistributionService budgetDistributionService;

  protected BudgetService budgetService;
  protected AppBudgetService appBudgetService;
  protected BudgetToolsService budgetToolsService;
  protected BudgetLineComputeService budgetLineComputeService;

  @Inject
  public BudgetInvoiceServiceImpl(
      InvoiceRepository invoiceRepo,
      AppBaseService appBaseService,
      BudgetRepository budgetRepository,
      BudgetInvoiceLineService budgetInvoiceLineService,
      BudgetDistributionService budgetDistributionService,
      BudgetService budgetService,
      AppBudgetService appBudgetService,
      BudgetToolsService budgetToolsService,
      BudgetLineComputeService budgetLineComputeService) {
    this.invoiceRepo = invoiceRepo;
    this.appBaseService = appBaseService;
    this.budgetRepository = budgetRepository;
    this.budgetInvoiceLineService = budgetInvoiceLineService;
    this.budgetDistributionService = budgetDistributionService;
    this.budgetService = budgetService;
    this.appBudgetService = appBudgetService;
    this.budgetToolsService = budgetToolsService;
    this.budgetLineComputeService = budgetLineComputeService;
  }

  @Override
  @Transactional
  public String computeBudgetDistribution(Invoice invoice) throws AxelorException {
    List<String> alertMessageTokenList = new ArrayList<>();
    if (!CollectionUtils.isEmpty(invoice.getInvoiceLineList())) {
      for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
        String alertMessage =
            budgetInvoiceLineService.computeBudgetDistribution(invoice, invoiceLine);
        if (Strings.isNullOrEmpty(alertMessage)) {
          invoice.setBudgetDistributionGenerated(true);
        } else {
          alertMessageTokenList.add(alertMessage);
        }
      }
      invoiceRepo.save(invoice);
    }
    return String.join(", ", alertMessageTokenList);
  }

  @Override
  @CallMethod
  public String getBudgetExceedAlert(Invoice invoice) {
    String budgetExceedAlert = "";

    List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();
    LocalDate date =
        invoice.getInvoiceDate() != null
            ? invoice.getInvoiceDate()
            : appBaseService.getTodayDate(invoice.getCompany());

    if (appBudgetService.getAppBudget() != null
        && appBudgetService.getAppBudget().getCheckAvailableBudget()
        && invoice.getId() != null
        && CollectionUtils.isNotEmpty(invoiceLineList)
        && date != null) {

      Map<Budget, BigDecimal> amountPerBudgetMap = new HashMap<>();

      for (InvoiceLine invoiceLine : invoiceLineList) {
        if (appBudgetService.getAppBudget().getManageMultiBudget()
            && CollectionUtils.isNotEmpty(invoiceLine.getBudgetDistributionList())) {

          for (BudgetDistribution budgetDistribution : invoiceLine.getBudgetDistributionList()) {
            Budget budget = budgetDistribution.getBudget();

            budgetToolsService.fillAmountPerBudgetMap(
                budget, budgetDistribution.getAmount(), amountPerBudgetMap);
          }
        } else {
          Budget budget = invoiceLine.getBudget();
          budgetToolsService.fillAmountPerBudgetMap(
              budget, invoiceLine.getCompanyExTaxTotal(), amountPerBudgetMap);
        }
      }

      for (Map.Entry<Budget, BigDecimal> budgetEntry : amountPerBudgetMap.entrySet()) {
        budgetExceedAlert +=
            budgetDistributionService.getBudgetExceedAlert(
                budgetEntry.getKey(), budgetEntry.getValue(), date);
      }
    }
    return budgetExceedAlert;
  }

  @Override
  public boolean isBudgetInLines(Invoice invoice) {
    if (!CollectionUtils.isEmpty(invoice.getInvoiceLineList())) {
      for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
        if (invoiceLine.getBudget() != null
            || !CollectionUtils.isEmpty(invoiceLine.getBudgetDistributionList())) {
          return true;
        }
      }
    }
    return false;
  }

  protected LocalDate computeImputationDate(Invoice invoice, InvoiceLine invoiceLine) {
    LocalDate imputationDate =
        invoice.getInvoiceDate() != null
            ? invoice.getInvoiceDate()
            : appBaseService.getTodayDate(invoice.getCompany());
    PurchaseOrder purchaseOrder =
        Optional.of(invoiceLine)
            .map(InvoiceLine::getInvoice)
            .map(Invoice::getPurchaseOrder)
            .orElse(
                Optional.of(invoiceLine)
                    .map(InvoiceLine::getPurchaseOrderLine)
                    .map(PurchaseOrderLine::getPurchaseOrder)
                    .orElse(null));
    SaleOrder saleOrder =
        Optional.of(invoiceLine)
            .map(InvoiceLine::getInvoice)
            .map(Invoice::getSaleOrder)
            .orElse(
                Optional.of(invoiceLine)
                    .map(InvoiceLine::getSaleOrderLine)
                    .map(SaleOrderLine::getSaleOrder)
                    .orElse(null));

    if (purchaseOrder != null && purchaseOrder.getOrderDate() != null) {
      imputationDate = purchaseOrder.getOrderDate();
    } else if (saleOrder != null) {
      imputationDate =
          saleOrder.getOrderDate() != null ? saleOrder.getOrderDate() : saleOrder.getCreationDate();
    }

    return imputationDate;
  }

  @Override
  public void generateBudgetDistribution(Invoice invoice) {
    if (invoice.getInvoiceLineList() != null) {
      for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
        if (invoiceLine.getBudget() != null
            && (ObjectUtils.isEmpty(invoiceLine.getBudgetDistributionList()))) {
          BudgetDistribution budgetDistribution = new BudgetDistribution();
          budgetDistribution.setBudget(invoiceLine.getBudget());
          budgetDistribution.setAmount(invoiceLine.getCompanyExTaxTotal());
          budgetDistributionService.linkBudgetDistributionWithParent(
              budgetDistribution, invoiceLine);
        }
        invoiceLine.setBudgetRemainingAmountToAllocate(BigDecimal.ZERO);
      }
    }
  }

  @Override
  public void setComputedBudgetLinesAmount(List<InvoiceLine> invoiceLineList) {
    invoiceLineList.forEach(invoiceLine -> computeBudgetLineAmount(invoiceLineList, invoiceLine));
  }

  protected void computeBudgetLineAmount(
      List<InvoiceLine> invoiceLineList, InvoiceLine invoiceLine) {

    Product product = invoiceLine.getProduct();

    if (invoiceLine != null && !CollectionUtils.isEmpty(invoiceLine.getBudgetDistributionList())) {
      invoiceLine
          .getBudgetDistributionList()
          .forEach(
              budgetDistribution ->
                  budgetDistribution.setAmount(
                      divideBudgetDistributionAmount(
                          budgetDistribution, product, invoiceLineList)));
    }
  }

  protected BigDecimal divideBudgetDistributionAmount(
      BudgetDistribution budgetDistribution, Product product, List<InvoiceLine> invoiceLineList) {
    return budgetDistribution
        .getAmount()
        .divide(
            new BigDecimal(
                countInvoiceLineWithSameProductAndBudget(
                    product, invoiceLineList, budgetDistribution.getBudget())),
            RoundingMode.HALF_UP);
  }

  protected long countInvoiceLineWithSameProductAndBudget(
      Product product, List<InvoiceLine> invoiceLineList, Budget budget) {
    return invoiceLineList.stream()
        .filter(
            invoiceLine ->
                product.equals(invoiceLine.getProduct()) && useSameBudget(budget, invoiceLine))
        .count();
  }

  protected boolean useSameBudget(Budget budget, InvoiceLine invoiceLine) {
    List<BudgetDistribution> budgetDistributionList = invoiceLine.getBudgetDistributionList();
    if (budgetDistributionList == null) {
      return false;
    }
    return budgetDistributionList.stream()
        .anyMatch(budgetDistribution -> budget.equals(budgetDistribution.getBudget()));
  }

  @Override
  public void autoComputeBudgetDistribution(Invoice invoice) throws AxelorException {
    LocalDate date =
        invoice.getInvoiceDate() != null
            ? invoice.getInvoiceDate()
            : appBaseService.getTodayDate(invoice.getCompany());
    if (!budgetToolsService.canAutoComputeBudgetDistribution(
            invoice.getCompany(), invoice.getInvoiceLineList())
        || date == null) {
      return;
    }

    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      budgetDistributionService.autoComputeBudgetDistribution(
          invoiceLine.getAnalyticMoveLineList(),
          invoiceLine.getAccount(),
          invoice.getCompany(),
          date,
          invoiceLine.getCompanyExTaxTotal(),
          invoiceLine);
      invoiceLine.setBudgetRemainingAmountToAllocate(
          budgetToolsService.getBudgetRemainingAmountToAllocate(
              invoiceLine.getBudgetDistributionList(), invoiceLine.getCompanyExTaxTotal()));
    }
  }
}
