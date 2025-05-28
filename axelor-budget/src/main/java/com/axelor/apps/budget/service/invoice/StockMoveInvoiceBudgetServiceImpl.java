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
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetAmountToolService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.businessproject.service.ProjectStockMoveInvoiceServiceImpl;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.PurchaseOrderMergingSupplychainService;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychain;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderInvoiceService;
import com.axelor.apps.supplychain.service.saleorder.merge.SaleOrderMergingServiceSupplyChain;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

public class StockMoveInvoiceBudgetServiceImpl extends ProjectStockMoveInvoiceServiceImpl {

  protected BudgetInvoiceService budgetInvoiceService;
  protected AppBudgetService appBudgetService;
  protected InvoiceToolBudgetService invoiceToolBudgetService;
  protected BudgetInvoiceLineService budgetInvoiceLineService;
  protected BudgetToolsService budgetToolsService;
  protected BudgetAmountToolService budgetAmountToolService;

  @Inject
  public StockMoveInvoiceBudgetServiceImpl(
      SaleOrderInvoiceService saleOrderInvoiceService,
      PurchaseOrderInvoiceService purchaseOrderInvoiceService,
      StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain,
      InvoiceRepository invoiceRepository,
      SaleOrderRepository saleOrderRepo,
      PurchaseOrderRepository purchaseOrderRepo,
      StockMoveLineRepository stockMoveLineRepository,
      InvoiceLineRepository invoiceLineRepository,
      SupplyChainConfigService supplyChainConfigService,
      AppBaseService appBaseService,
      AppStockService appStockService,
      SaleOrderMergingServiceSupplyChain saleOrderMergingServiceSupplyChain,
      PurchaseOrderMergingSupplychainService purchaseOrderMergingSupplychainService,
      UnitConversionService unitConversionService,
      BudgetInvoiceService budgetInvoiceService,
      AppBudgetService appBudgetService,
      InvoiceToolBudgetService invoiceToolBudgetService,
      BudgetInvoiceLineService budgetInvoiceLineService,
      BudgetToolsService budgetToolsService,
      BudgetAmountToolService budgetAmountToolService) {
    super(
        saleOrderInvoiceService,
        purchaseOrderInvoiceService,
        stockMoveLineServiceSupplychain,
        invoiceRepository,
        saleOrderRepo,
        purchaseOrderRepo,
        stockMoveLineRepository,
        invoiceLineRepository,
        supplyChainConfigService,
        appBaseService,
        appStockService,
        saleOrderMergingServiceSupplyChain,
        purchaseOrderMergingSupplychainService,
        unitConversionService);
    this.budgetInvoiceService = budgetInvoiceService;
    this.appBudgetService = appBudgetService;
    this.invoiceToolBudgetService = invoiceToolBudgetService;
    this.budgetInvoiceLineService = budgetInvoiceLineService;
    this.budgetToolsService = budgetToolsService;
    this.budgetAmountToolService = budgetAmountToolService;
  }

  @Override
  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice,
      StockMove stockMove,
      List<StockMoveLine> stockMoveLineList,
      Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException {
    List<InvoiceLine> invoiceLineList =
        super.createInvoiceLines(invoice, stockMove, stockMoveLineList, qtyToInvoiceMap);

    if (appBudgetService.isApp("budget")) {
      budgetInvoiceService.setComputedBudgetLinesAmount(invoiceLineList);
    }

    return invoiceLineList;
  }

  @Override
  public InvoiceLine createInvoiceLine(Invoice invoice, StockMoveLine stockMoveLine, BigDecimal qty)
      throws AxelorException {
    InvoiceLine invoiceLine = super.createInvoiceLine(invoice, stockMoveLine, qty);
    BigDecimal invoiceLineAmount = budgetAmountToolService.getBudgetMaxAmount(invoiceLine);
    if (appBudgetService.isApp("budget")
        && invoiceLine != null
        && invoiceLineAmount.signum() != 0) {
      if (invoiceLine.getPurchaseOrderLine() != null) {
        PurchaseOrderLine purchaseOrderLine = invoiceLine.getPurchaseOrderLine();
        invoiceLine.setBudget(purchaseOrderLine.getBudget());

        BigDecimal orderAmount = budgetAmountToolService.getBudgetMaxAmount(purchaseOrderLine);
        if (orderAmount.signum() != 0) {
          invoiceToolBudgetService.copyBudgetDistributionList(
              purchaseOrderLine.getBudgetDistributionList(),
              invoiceLine,
              invoiceLineAmount.divide(
                  orderAmount, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP));
        }
      } else if (invoiceLine.getSaleOrderLine() != null) {
        SaleOrderLine saleOrderLine = invoiceLine.getSaleOrderLine();
        invoiceLine.setBudget(saleOrderLine.getBudget());

        BigDecimal orderAmount = budgetAmountToolService.getBudgetMaxAmount(saleOrderLine);
        if (orderAmount.signum() != 0) {
          invoiceToolBudgetService.copyBudgetDistributionList(
              saleOrderLine.getBudgetDistributionList(),
              invoiceLine,
              invoiceLineAmount.divide(
                  orderAmount, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP));
        }
      }

      invoiceLine.setBudgetRemainingAmountToAllocate(
          budgetToolsService.getBudgetRemainingAmountToAllocate(
              invoiceLine.getBudgetDistributionList(), invoiceLineAmount));
    }
    return invoiceLine;
  }
}
