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
package com.axelor.apps.supplychain.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.db.repo.AnalyticMoveLineMngtRepository;
import com.axelor.apps.account.db.repo.InvoiceManagementRepository;
import com.axelor.apps.account.service.AccountCustomerServiceImpl;
import com.axelor.apps.account.service.AccountingCutOffServiceImpl;
import com.axelor.apps.account.service.AccountingSituationInitServiceImpl;
import com.axelor.apps.account.service.AccountingSituationServiceImpl;
import com.axelor.apps.account.service.BudgetService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineServiceImpl;
import com.axelor.apps.account.service.batch.BatchAccountingCutOff;
import com.axelor.apps.account.service.fixedasset.FixedAssetGenerationServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceLineServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceMergingServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceServiceImpl;
import com.axelor.apps.account.service.invoice.workflow.cancel.WorkflowCancelServiceImpl;
import com.axelor.apps.account.service.invoice.workflow.ventilate.WorkflowVentilationServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolServiceImpl;
import com.axelor.apps.purchase.db.repo.PurchaseOrderManagementRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderWorkflowServiceImpl;
import com.axelor.apps.purchase.service.PurchaseRequestServiceImpl;
import com.axelor.apps.sale.db.repo.AdvancePaymentSaleRepository;
import com.axelor.apps.sale.db.repo.SaleOrderManagementRepository;
import com.axelor.apps.sale.service.AdvancePaymentServiceImpl;
import com.axelor.apps.sale.service.PartnerSaleServiceImpl;
import com.axelor.apps.sale.service.SaleOrderLineSaleRepository;
import com.axelor.apps.sale.service.saleorder.OpportunitySaleOrderServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingViewServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowServiceImpl;
import com.axelor.apps.stock.db.repo.StockMoveLineStockRepository;
import com.axelor.apps.stock.db.repo.StockMoveManagementRepository;
import com.axelor.apps.stock.rest.StockProductRestServiceImpl;
import com.axelor.apps.stock.service.LogisticalFormServiceImpl;
import com.axelor.apps.stock.service.ProductVariantServiceStockImpl;
import com.axelor.apps.stock.service.StockCorrectionServiceImpl;
import com.axelor.apps.stock.service.StockHistoryServiceImpl;
import com.axelor.apps.stock.service.StockLocationLineServiceImpl;
import com.axelor.apps.stock.service.StockLocationServiceImpl;
import com.axelor.apps.stock.service.StockMoveLineServiceImpl;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.StockMoveServiceImpl;
import com.axelor.apps.stock.service.StockRulesService;
import com.axelor.apps.stock.service.StockRulesServiceImpl;
import com.axelor.apps.supplychain.db.repo.AdvancePaymentSupplychainRepository;
import com.axelor.apps.supplychain.db.repo.AnalyticMoveLineSupplychainRepository;
import com.axelor.apps.supplychain.db.repo.InvoiceSupplychainRepository;
import com.axelor.apps.supplychain.db.repo.MrpForecastManagementRepository;
import com.axelor.apps.supplychain.db.repo.MrpForecastRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineManagementRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineRepository;
import com.axelor.apps.supplychain.db.repo.MrpManagementRepository;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.apps.supplychain.db.repo.PurchaseOrderSupplychainRepository;
import com.axelor.apps.supplychain.db.repo.SaleOrderLineSupplychainRepository;
import com.axelor.apps.supplychain.db.repo.SaleOrderSupplychainRepository;
import com.axelor.apps.supplychain.db.repo.StockMoveLineSupplychainRepository;
import com.axelor.apps.supplychain.db.repo.StockMoveSupplychainRepository;
import com.axelor.apps.supplychain.db.repo.SupplychainBatchRepository;
import com.axelor.apps.supplychain.db.repo.SupplychainBatchSupplychainRepository;
import com.axelor.apps.supplychain.rest.StockProductRestServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.AccountCustomerServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.AccountingCutOffSupplyChainService;
import com.axelor.apps.supplychain.service.AccountingCutOffSupplyChainServiceImpl;
import com.axelor.apps.supplychain.service.AccountingSituationInitSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainService;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.AdvancePaymentServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.AnalyticMoveLineSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.BudgetSupplychainService;
import com.axelor.apps.supplychain.service.CommonInvoiceService;
import com.axelor.apps.supplychain.service.CommonInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.IntercoService;
import com.axelor.apps.supplychain.service.IntercoServiceImpl;
import com.axelor.apps.supplychain.service.InvoiceLineSupplychainService;
import com.axelor.apps.supplychain.service.InvoicePaymentToolServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.LogisticalFormSupplychainService;
import com.axelor.apps.supplychain.service.LogisticalFormSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.MrpFilterSaleOrderLineService;
import com.axelor.apps.supplychain.service.MrpFilterSaleOrderLineServiceImpl;
import com.axelor.apps.supplychain.service.MrpForecastService;
import com.axelor.apps.supplychain.service.MrpForecastServiceImpl;
import com.axelor.apps.supplychain.service.MrpLineService;
import com.axelor.apps.supplychain.service.MrpLineServiceImpl;
import com.axelor.apps.supplychain.service.MrpLineTypeService;
import com.axelor.apps.supplychain.service.MrpLineTypeServiceImpl;
import com.axelor.apps.supplychain.service.MrpSaleOrderCheckLateSaleService;
import com.axelor.apps.supplychain.service.MrpSaleOrderCheckLateSaleServiceImpl;
import com.axelor.apps.supplychain.service.MrpService;
import com.axelor.apps.supplychain.service.MrpServiceImpl;
import com.axelor.apps.supplychain.service.OpportunitySaleOrderSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.PartnerSupplychainLinkService;
import com.axelor.apps.supplychain.service.PartnerSupplychainLinkServiceImpl;
import com.axelor.apps.supplychain.service.PartnerSupplychainService;
import com.axelor.apps.supplychain.service.PartnerSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.ProductStockLocationService;
import com.axelor.apps.supplychain.service.ProductStockLocationServiceImpl;
import com.axelor.apps.supplychain.service.ProductVariantServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.ProjectedStockService;
import com.axelor.apps.supplychain.service.ProjectedStockServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplyChain;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderStockService;
import com.axelor.apps.supplychain.service.PurchaseOrderStockServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderSupplychainService;
import com.axelor.apps.supplychain.service.PurchaseOrderWorkflowServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PurchaseRequestServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.ReservedQtyService;
import com.axelor.apps.supplychain.service.ReservedQtyServiceImpl;
import com.axelor.apps.supplychain.service.SaleInvoicingStateService;
import com.axelor.apps.supplychain.service.SaleInvoicingStateServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderCheckAnalyticService;
import com.axelor.apps.supplychain.service.SaleOrderCheckAnalyticServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderComputeServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.SaleOrderCreateServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChain;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.SaleOrderMergingServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.SaleOrderMergingViewServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseService;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderReservedQtyService;
import com.axelor.apps.supplychain.service.SaleOrderReservedQtyServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.SaleOrderStockService;
import com.axelor.apps.supplychain.service.SaleOrderStockServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderSupplychainService;
import com.axelor.apps.supplychain.service.SaleOrderWorkflowServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockCorrectionServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockHistoryServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.StockLocationLineReservationService;
import com.axelor.apps.supplychain.service.StockLocationLineReservationServiceImpl;
import com.axelor.apps.supplychain.service.StockLocationLineServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockLocationServiceSupplychain;
import com.axelor.apps.supplychain.service.StockLocationServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockMoveInvoiceService;
import com.axelor.apps.supplychain.service.StockMoveInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychain;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockMoveMultiInvoiceService;
import com.axelor.apps.supplychain.service.StockMoveMultiInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.StockMoveReservedQtyService;
import com.axelor.apps.supplychain.service.StockMoveReservedQtyServiceImpl;
import com.axelor.apps.supplychain.service.StockMoveServiceSupplychain;
import com.axelor.apps.supplychain.service.StockMoveServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockRulesServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.SupplychainSaleConfigService;
import com.axelor.apps.supplychain.service.SupplychainSaleConfigServiceImpl;
import com.axelor.apps.supplychain.service.TimetableService;
import com.axelor.apps.supplychain.service.TimetableServiceImpl;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.app.AppSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.batch.BatchAccountingCutOffSupplyChain;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigServiceImpl;
import com.axelor.apps.supplychain.service.declarationofexchanges.DeclarationOfExchangesService;
import com.axelor.apps.supplychain.service.declarationofexchanges.DeclarationOfExchangesServiceImpl;
import com.axelor.apps.supplychain.service.fixedasset.FixedAssetServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.invoice.InvoiceLineAnalyticSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.invoice.InvoiceMergingServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychain;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.invoice.SubscriptionInvoiceService;
import com.axelor.apps.supplychain.service.invoice.SubscriptionInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineOrderService;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineOrderServiceImpl;
import com.axelor.apps.supplychain.service.workflow.WorkflowCancelServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.workflow.WorkflowVentilationServiceSupplychainImpl;

public class SupplychainModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(StockRulesService.class).to(StockRulesServiceImpl.class);
    bind(StockRulesServiceImpl.class).to(StockRulesServiceSupplychainImpl.class);
    bind(StockMoveService.class).to(StockMoveServiceImpl.class);
    bind(PurchaseOrderServiceImpl.class).to(PurchaseOrderServiceSupplychainImpl.class);
    bind(SaleOrderServiceImpl.class).to(SaleOrderServiceSupplychainImpl.class);
    bind(SaleOrderCreateServiceImpl.class).to(SaleOrderCreateServiceSupplychainImpl.class);
    bind(SaleOrderComputeServiceImpl.class).to(SaleOrderComputeServiceSupplychainImpl.class);
    bind(SaleOrderWorkflowServiceImpl.class).to(SaleOrderWorkflowServiceSupplychainImpl.class);
    bind(PurchaseOrderInvoiceService.class).to(PurchaseOrderInvoiceServiceImpl.class);
    bind(SaleOrderInvoiceService.class).to(SaleOrderInvoiceServiceImpl.class);
    bind(SaleOrderPurchaseService.class).to(SaleOrderPurchaseServiceImpl.class);
    bind(StockMoveInvoiceService.class).to(StockMoveInvoiceServiceImpl.class);
    bind(SaleOrderManagementRepository.class).to(SaleOrderSupplychainRepository.class);
    bind(StockMoveServiceImpl.class).to(StockMoveServiceSupplychainImpl.class);
    bind(SaleOrderLineServiceImpl.class).to(SaleOrderLineServiceSupplyChainImpl.class);
    bind(AdvancePaymentSaleRepository.class).to(AdvancePaymentSupplychainRepository.class);
    bind(AdvancePaymentServiceImpl.class).to(AdvancePaymentServiceSupplychainImpl.class);
    bind(MrpService.class).to(MrpServiceImpl.class);
    bind(MrpLineService.class).to(MrpLineServiceImpl.class);
    bind(MrpFilterSaleOrderLineService.class).to(MrpFilterSaleOrderLineServiceImpl.class);
    bind(MrpLineTypeService.class).to(MrpLineTypeServiceImpl.class);
    bind(MrpSaleOrderCheckLateSaleService.class).to(MrpSaleOrderCheckLateSaleServiceImpl.class);
    bind(AnalyticMoveLineMngtRepository.class).to(AnalyticMoveLineSupplychainRepository.class);
    bind(StockMoveLineServiceImpl.class).to(StockMoveLineServiceSupplychainImpl.class);
    bind(BudgetService.class).to(BudgetSupplychainService.class);
    bind(InvoiceLineServiceImpl.class).to(InvoiceLineSupplychainService.class);
    bind(SaleOrderStockService.class).to(SaleOrderStockServiceImpl.class);
    bind(PurchaseOrderManagementRepository.class).to(PurchaseOrderSupplychainRepository.class);
    bind(AppSupplychainService.class).to(AppSupplychainServiceImpl.class);
    bind(SupplychainSaleConfigService.class).to(SupplychainSaleConfigServiceImpl.class);
    bind(AccountCustomerServiceImpl.class).to(AccountCustomerServiceSupplyChainImpl.class);
    bind(AccountingSituationServiceImpl.class).to(AccountingSituationSupplychainServiceImpl.class);
    bind(AccountingSituationSupplychainService.class)
        .to(AccountingSituationSupplychainServiceImpl.class);
    bind(StockLocationLineServiceImpl.class).to(StockLocationLineServiceSupplychainImpl.class);
    bind(InvoiceServiceImpl.class).to(InvoiceServiceSupplychainImpl.class);
    bind(InvoicePaymentToolServiceImpl.class).to(InvoicePaymentToolServiceSupplychainImpl.class);
    bind(WorkflowVentilationServiceImpl.class).to(WorkflowVentilationServiceSupplychainImpl.class);
    bind(WorkflowCancelServiceImpl.class).to(WorkflowCancelServiceSupplychainImpl.class);
    bind(IntercoService.class).to(IntercoServiceImpl.class);
    bind(LogisticalFormServiceImpl.class).to(LogisticalFormSupplychainServiceImpl.class);
    bind(LogisticalFormSupplychainService.class).to(LogisticalFormSupplychainServiceImpl.class);
    bind(StockLocationLineServiceImpl.class).to(StockLocationLineServiceSupplychainImpl.class);
    bind(SaleOrderLineServiceSupplyChain.class).to(SaleOrderLineServiceSupplyChainImpl.class);
    bind(SupplyChainConfigService.class).to(SupplyChainConfigServiceImpl.class);
    bind(SupplychainBatchRepository.class).to(SupplychainBatchSupplychainRepository.class);
    bind(SubscriptionInvoiceService.class).to(SubscriptionInvoiceServiceImpl.class);
    bind(TimetableService.class).to(TimetableServiceImpl.class);
    bind(StockMoveLineServiceSupplychain.class).to(StockMoveLineServiceSupplychainImpl.class);
    bind(StockLocationServiceImpl.class).to(StockLocationServiceSupplychainImpl.class);
    bind(StockLocationServiceSupplychain.class).to(StockLocationServiceSupplychainImpl.class);
    bind(ReservedQtyService.class).to(ReservedQtyServiceImpl.class);
    bind(PurchaseOrderLineServiceImpl.class).to(PurchaseOrderLineServiceSupplychainImpl.class);
    bind(PurchaseOrderStockService.class).to(PurchaseOrderStockServiceImpl.class);
    bind(AccountingCutOffServiceImpl.class).to(AccountingCutOffSupplyChainServiceImpl.class);
    bind(AccountingCutOffSupplyChainService.class).to(AccountingCutOffSupplyChainServiceImpl.class);
    bind(DeclarationOfExchangesService.class).to(DeclarationOfExchangesServiceImpl.class);
    bind(StockMoveManagementRepository.class).to(StockMoveSupplychainRepository.class);
    bind(StockCorrectionServiceImpl.class).to(StockCorrectionServiceSupplychainImpl.class);
    bind(StockMoveMultiInvoiceService.class).to(StockMoveMultiInvoiceServiceImpl.class);
    bind(MrpRepository.class).to(MrpManagementRepository.class);
    bind(MrpLineRepository.class).to(MrpLineManagementRepository.class);
    bind(SaleOrderReservedQtyService.class).to(SaleOrderReservedQtyServiceImpl.class);
    bind(StockLocationLineReservationService.class)
        .to(StockLocationLineReservationServiceImpl.class);
    bind(PurchaseRequestServiceImpl.class).to(PurchaseRequestServiceSupplychainImpl.class);
    bind(ProductStockLocationService.class).to(ProductStockLocationServiceImpl.class);
    bind(ProjectedStockService.class).to(ProjectedStockServiceImpl.class);
    bind(SaleOrderLineSaleRepository.class).to(SaleOrderLineSupplychainRepository.class);
    bind(FixedAssetGenerationServiceImpl.class).to(FixedAssetServiceSupplyChainImpl.class);
    bind(StockMoveLineStockRepository.class).to(StockMoveLineSupplychainRepository.class);
    bind(MrpForecastRepository.class).to(MrpForecastManagementRepository.class);
    bind(PurchaseOrderSupplychainService.class).to(PurchaseOrderServiceSupplychainImpl.class);
    bind(SaleOrderSupplychainService.class).to(SaleOrderServiceSupplychainImpl.class);
    bind(StockMoveServiceSupplychain.class).to(StockMoveServiceSupplychainImpl.class);
    bind(PartnerSupplychainLinkService.class).to(PartnerSupplychainLinkServiceImpl.class);
    bind(InvoiceManagementRepository.class).to(InvoiceSupplychainRepository.class);
    bind(StockMoveReservedQtyService.class).to(StockMoveReservedQtyServiceImpl.class);
    bind(PurchaseOrderLineServiceSupplyChain.class)
        .to(PurchaseOrderLineServiceSupplychainImpl.class);
    bind(PurchaseOrderWorkflowServiceImpl.class)
        .to(PurchaseOrderWorkflowServiceSupplychainImpl.class);
    bind(InvoiceServiceSupplychain.class).to(InvoiceServiceSupplychainImpl.class);
    bind(InvoiceManagementRepository.class).to(InvoiceSupplychainRepository.class);
    bind(OpportunitySaleOrderServiceImpl.class)
        .to(OpportunitySaleOrderSupplychainServiceImpl.class);
    bind(PartnerSaleServiceImpl.class).to(PartnerSupplychainServiceImpl.class);
    bind(PartnerSupplychainService.class).to(PartnerSupplychainServiceImpl.class);
    bind(ProductVariantServiceStockImpl.class).to(ProductVariantServiceSupplyChainImpl.class);
    bind(StockHistoryServiceImpl.class).to(StockHistoryServiceSupplyChainImpl.class);
    bind(AccountingSituationInitServiceImpl.class)
        .to(AccountingSituationInitSupplychainServiceImpl.class);
    bind(SaleOrderMergingViewServiceImpl.class)
        .to(SaleOrderMergingViewServiceSupplyChainImpl.class);
    bind(SaleOrderMergingServiceImpl.class).to(SaleOrderMergingServiceSupplyChainImpl.class);
    bind(InvoiceMergingServiceImpl.class).to(InvoiceMergingServiceSupplychainImpl.class);
    bind(CommonInvoiceService.class).to(CommonInvoiceServiceImpl.class);
    bind(MrpForecastService.class).to(MrpForecastServiceImpl.class);
    bind(SaleOrderCheckAnalyticService.class).to(SaleOrderCheckAnalyticServiceImpl.class);
    bind(BatchAccountingCutOff.class).to(BatchAccountingCutOffSupplyChain.class);
    bind(StockProductRestServiceImpl.class).to(StockProductRestServiceSupplychainImpl.class);
    bind(AnalyticMoveLineServiceImpl.class).to(AnalyticMoveLineSupplychainServiceImpl.class);
    bind(InvoiceLineAnalyticServiceImpl.class).to(InvoiceLineAnalyticSupplychainServiceImpl.class);
    bind(InvoiceLineOrderService.class).to(InvoiceLineOrderServiceImpl.class);
    bind(SaleInvoicingStateService.class).to(SaleInvoicingStateServiceImpl.class);
  }
}
