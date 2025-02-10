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
package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.invoice.InvoiceToolBudgetService;
import com.axelor.apps.budget.service.saleorderline.SaleOrderLineBudgetService;
import com.axelor.apps.businessproject.service.SaleOrderInvoiceProjectServiceImpl;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderDeliveryAddressService;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderWorkflowService;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.supplychain.service.CommonInvoiceService;
import com.axelor.apps.supplychain.service.SaleInvoicingStateService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.invoice.InvoiceTaxService;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineOrderService;
import com.axelor.apps.supplychain.service.order.OrderInvoiceService;
import com.axelor.common.StringUtils;
import com.axelor.meta.CallMethod;
import com.axelor.studio.db.AppBudget;
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

public class SaleOrderBudgetServiceImpl extends SaleOrderInvoiceProjectServiceImpl
    implements SaleOrderBudgetService {

  protected AppBudgetService appBudgetService;
  protected BudgetDistributionService budgetDistributionService;
  protected SaleOrderLineBudgetService saleOrderLineBudgetService;
  protected BudgetService budgetService;
  protected BudgetToolsService budgetToolsService;
  protected InvoiceToolBudgetService invoiceToolBudgetService;

  @Inject
  public SaleOrderBudgetServiceImpl(
      AppBaseService appBaseService,
      AppStockService appStockService,
      AppSupplychainService appSupplychainService,
      SaleOrderRepository saleOrderRepo,
      InvoiceRepository invoiceRepo,
      InvoiceServiceSupplychainImpl invoiceService,
      StockMoveRepository stockMoveRepository,
      SaleOrderWorkflowService saleOrderWorkflowService,
      InvoiceTermService invoiceTermService,
      CommonInvoiceService commonInvoiceService,
      InvoiceLineOrderService invoiceLineOrderService,
      SaleInvoicingStateService saleInvoicingStateService,
      CurrencyScaleService currencyScaleService,
      OrderInvoiceService orderInvoiceService,
      InvoiceTaxService invoiceTaxService,
      SaleOrderDeliveryAddressService saleOrderDeliveryAddressService,
      AppBusinessProjectService appBusinessProjectService,
      AppBudgetService appBudgetService,
      BudgetDistributionService budgetDistributionService,
      SaleOrderLineBudgetService saleOrderLineBudgetService,
      BudgetService budgetService,
      BudgetToolsService budgetToolsService,
      InvoiceToolBudgetService invoiceToolBudgetService) {
    super(
        appBaseService,
        appStockService,
        appSupplychainService,
        saleOrderRepo,
        invoiceRepo,
        invoiceService,
        stockMoveRepository,
        saleOrderWorkflowService,
        invoiceTermService,
        commonInvoiceService,
        invoiceLineOrderService,
        saleInvoicingStateService,
        currencyScaleService,
        orderInvoiceService,
        invoiceTaxService,
        saleOrderDeliveryAddressService,
        appBusinessProjectService);
    this.appBudgetService = appBudgetService;
    this.budgetDistributionService = budgetDistributionService;
    this.saleOrderLineBudgetService = saleOrderLineBudgetService;
    this.budgetService = budgetService;
    this.budgetToolsService = budgetToolsService;
    this.invoiceToolBudgetService = invoiceToolBudgetService;
  }

  @Override
  public void generateBudgetDistribution(SaleOrder saleOrder) {
    if (Optional.of(appBudgetService.getAppBudget())
        .map(AppBudget::getManageMultiBudget)
        .orElse(true)) {
      return;
    }

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
        }
        budgetDistribution.setAmount(saleOrderLine.getCompanyExTaxTotal());
        budgetDistributionService.linkBudgetDistributionWithParent(
            budgetDistribution, saleOrderLine);
      }
      saleOrderLine.setBudgetRemainingAmountToAllocate(BigDecimal.ZERO);
    }
  }

  @Override
  @Transactional
  public String computeBudgetDistribution(SaleOrder saleOrder) throws AxelorException {
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
        String productCode =
            Optional.of(soLine)
                .map(SaleOrderLine::getProduct)
                .map(Product::getCode)
                .orElse(soLine.getProductName());
        if (StringUtils.notEmpty(productCode)) {
          budgetService.validateBudgetDistributionAmounts(
              soLine.getBudgetDistributionList(), soLine.getCompanyExTaxTotal(), productCode);
        }
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
    if (!appBudgetService.isApp("budget")) {
      return invoiceLines;
    }

    for (InvoiceLine invoiceLine : invoiceLines) {
      if (saleOrderLine != null && saleOrderLine.getQty().signum() > 0) {
        invoiceLine.setBudget(saleOrderLine.getBudget());
        invoiceLine.setBudgetRemainingAmountToAllocate(
            saleOrderLine.getBudgetRemainingAmountToAllocate());
        invoiceToolBudgetService.copyBudgetDistributionList(
            saleOrderLine.getBudgetDistributionList(),
            invoiceLine,
            qtyToInvoice.divide(saleOrderLine.getQty(), RoundingMode.HALF_UP));
        invoiceLine.setBudgetRemainingAmountToAllocate(
            budgetToolsService.getBudgetRemainingAmountToAllocate(
                invoiceLine.getBudgetDistributionList(), invoiceLine.getCompanyExTaxTotal()));
      }
    }
    return invoiceLines;
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
                    LocalDate computeDate =
                        saleOrder.getOrderDate() != null
                            ? saleOrder.getOrderDate()
                            : saleOrder.getCreationDate();
                    budgetDistribution.setImputationDate(computeDate);
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
      LocalDate date =
          saleOrder.getOrderDate() != null ? saleOrder.getOrderDate() : saleOrder.getCreationDate();
      for (SaleOrderLine saleOrderLine : saleOrderLineList) {

        if (appBudgetService.getAppBudget().getManageMultiBudget()
            && CollectionUtils.isNotEmpty(saleOrderLine.getBudgetDistributionList())) {

          for (BudgetDistribution budgetDistribution : saleOrderLine.getBudgetDistributionList()) {
            Budget budget = budgetDistribution.getBudget();

            budgetToolsService.fillAmountPerBudgetMap(
                budget, budgetDistribution.getAmount(), amountPerBudgetMap);
          }
        } else {
          Budget budget = saleOrderLine.getBudget();
          if (budget != null) {
            budgetToolsService.fillAmountPerBudgetMap(
                budget, saleOrderLine.getCompanyExTaxTotal(), amountPerBudgetMap);
          }
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
  public void autoComputeBudgetDistribution(SaleOrder saleOrder) throws AxelorException {
    LocalDate date =
        saleOrder.getOrderDate() != null ? saleOrder.getOrderDate() : saleOrder.getCreationDate();
    if (!budgetToolsService.canAutoComputeBudgetDistribution(
            saleOrder.getCompany(), saleOrder.getSaleOrderLineList())
        || date == null) {
      return;
    }
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      budgetDistributionService.autoComputeBudgetDistribution(
          saleOrderLine.getAnalyticMoveLineList(),
          saleOrderLine.getAccount(),
          saleOrder.getCompany(),
          date,
          saleOrderLine.getCompanyExTaxTotal(),
          saleOrderLine);
      saleOrderLine.setBudgetRemainingAmountToAllocate(
          budgetToolsService.getBudgetRemainingAmountToAllocate(
              saleOrderLine.getBudgetDistributionList(), saleOrderLine.getCompanyExTaxTotal()));
      saleOrderLineBudgetService.fillBudgetStrOnLine(saleOrderLine, true);
    }
  }
}
