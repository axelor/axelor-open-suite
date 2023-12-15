/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.businessproject.service.ProjectStockMoveInvoiceServiceImpl;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderMergingService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderMergingServiceSupplyChain;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychain;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
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
      AppSupplychainService appSupplychainService,
      AppStockService appStockService,
      SaleOrderMergingServiceSupplyChain saleOrderMergingServiceSupplyChain,
      PurchaseOrderMergingService purchaseOrderMergingService,
      BudgetInvoiceService budgetInvoiceService,
      AppBudgetService appBudgetService,
      InvoiceToolBudgetService invoiceToolBudgetService,
      BudgetInvoiceLineService budgetInvoiceLineService,
      BudgetToolsService budgetToolsService) {
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
        appSupplychainService,
        appStockService,
        saleOrderMergingServiceSupplyChain,
        purchaseOrderMergingService);
    this.budgetInvoiceService = budgetInvoiceService;
    this.appBudgetService = appBudgetService;
    this.invoiceToolBudgetService = invoiceToolBudgetService;
    this.budgetInvoiceLineService = budgetInvoiceLineService;
    this.budgetToolsService = budgetToolsService;
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
    if (appBudgetService.isApp("budget") && invoiceLine != null) {
      if (invoiceLine.getPurchaseOrderLine() != null) {
        PurchaseOrderLine purchaseOrderLine = invoiceLine.getPurchaseOrderLine();
        invoiceLine.setBudget(purchaseOrderLine.getBudget());
        invoiceToolBudgetService.copyBudgetDistributionList(
            purchaseOrderLine.getBudgetDistributionList(),
            invoiceLine,
            invoiceLine
                .getCompanyExTaxTotal()
                .divide(
                    purchaseOrderLine.getCompanyExTaxTotal(),
                    AppBaseService.COMPUTATION_SCALING,
                    RoundingMode.HALF_UP));
      } else if (invoiceLine.getSaleOrderLine() != null) {
        SaleOrderLine saleOrderLine = invoiceLine.getSaleOrderLine();
        invoiceLine.setBudget(saleOrderLine.getBudget());
        invoiceToolBudgetService.copyBudgetDistributionList(
            saleOrderLine.getBudgetDistributionList(),
            invoiceLine,
            invoiceLine
                .getCompanyExTaxTotal()
                .divide(
                    saleOrderLine.getCompanyExTaxTotal(),
                    AppBaseService.COMPUTATION_SCALING,
                    RoundingMode.HALF_UP));
      }

      invoiceLine.setBudgetRemainingAmountToAllocate(
          budgetToolsService.getBudgetRemainingAmountToAllocate(
              invoiceLine.getBudgetDistributionList(), invoiceLine.getCompanyExTaxTotal()));
    }
    return invoiceLine;
  }
}
