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
package com.axelor.apps.supplychain.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.db.repo.AnalyticMoveLineMngtRepository;
import com.axelor.apps.account.db.repo.InvoiceManagementRepository;
import com.axelor.apps.account.service.AccountCustomerServiceImpl;
import com.axelor.apps.account.service.AccountingCutOffServiceImpl;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationGroupServiceImpl;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationInitServiceImpl;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationServiceImpl;
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
import com.axelor.apps.base.service.PartnerLinkServiceImpl;
import com.axelor.apps.base.service.wizard.BaseConvertLeadWizardService;
import com.axelor.apps.purchase.db.repo.PurchaseOrderManagementRepository;
import com.axelor.apps.purchase.service.PurchaseOrderCreateServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderLineServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderLineTaxComputeServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderMergingServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderMergingViewServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderWorkflowServiceImpl;
import com.axelor.apps.purchase.service.PurchaseRequestServiceImpl;
import com.axelor.apps.sale.db.repo.AdvancePaymentSaleRepository;
import com.axelor.apps.sale.db.repo.CartLineManagementRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineSaleRepository;
import com.axelor.apps.sale.db.repo.SaleOrderManagementRepository;
import com.axelor.apps.sale.service.AdvancePaymentServiceImpl;
import com.axelor.apps.sale.service.PartnerSaleServiceImpl;
import com.axelor.apps.sale.service.batch.SaleBatchService;
import com.axelor.apps.sale.service.cart.CartResetServiceImpl;
import com.axelor.apps.sale.service.cart.CartSaleOrderGeneratorServiceImpl;
import com.axelor.apps.sale.service.cartline.CartLineProductServiceImpl;
import com.axelor.apps.sale.service.configurator.ConfiguratorCheckServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderCheckServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderInitValueServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderVersionServiceImpl;
import com.axelor.apps.sale.service.saleorder.merge.SaleOrderMergingServiceImpl;
import com.axelor.apps.sale.service.saleorder.merge.SaleOrderMergingViewServiceImpl;
import com.axelor.apps.sale.service.saleorder.onchange.SaleOrderOnChangeServiceImpl;
import com.axelor.apps.sale.service.saleorder.onchange.SaleOrderOnLineChangeServiceImpl;
import com.axelor.apps.sale.service.saleorder.opportunity.OpportunitySaleOrderServiceImpl;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderProductPrintingService;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderProductPrintingServiceImpl;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderFinalizeServiceImpl;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderWorkflowServiceImpl;
import com.axelor.apps.sale.service.saleorder.views.SaleOrderViewServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCheckServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineOnChangeServiceImpl;
import com.axelor.apps.sale.service.saleorderline.creation.SaleOrderLineCreateServiceImpl;
import com.axelor.apps.sale.service.saleorderline.creation.SaleOrderLineInitValueServiceImpl;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineProductServiceImpl;
import com.axelor.apps.sale.service.saleorderline.view.SaleOrderLineDummyServiceImpl;
import com.axelor.apps.sale.service.saleorderline.view.SaleOrderLineViewServiceImpl;
import com.axelor.apps.stock.db.repo.StockMoveLineStockRepository;
import com.axelor.apps.stock.db.repo.StockMoveManagementRepository;
import com.axelor.apps.stock.rest.StockProductRestServiceImpl;
import com.axelor.apps.stock.service.LogisticalFormServiceImpl;
import com.axelor.apps.stock.service.StockCorrectionServiceImpl;
import com.axelor.apps.stock.service.StockHistoryServiceImpl;
import com.axelor.apps.stock.service.StockLocationLineFetchServiceImpl;
import com.axelor.apps.stock.service.StockLocationLineServiceImpl;
import com.axelor.apps.stock.service.StockMoveLineServiceImpl;
import com.axelor.apps.stock.service.StockMoveMergingServiceImpl;
import com.axelor.apps.stock.service.StockMoveServiceImpl;
import com.axelor.apps.stock.service.StockRulesService;
import com.axelor.apps.stock.service.StockRulesServiceImpl;
import com.axelor.apps.stock.service.TrackingNumberCompanyServiceImpl;
import com.axelor.apps.stock.utils.StockLocationUtilsServiceImpl;
import com.axelor.apps.supplychain.db.repo.AdvancePaymentSupplychainRepository;
import com.axelor.apps.supplychain.db.repo.AnalyticMoveLineSupplychainRepository;
import com.axelor.apps.supplychain.db.repo.CartLineSupplychainRepository;
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
import com.axelor.apps.supplychain.service.AccountingSituationGroupSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.AccountingSituationInitSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainService;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.AdvancePaymentServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.AnalyticLineModelServiceImpl;
import com.axelor.apps.supplychain.service.AnalyticMoveLineSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.CommonInvoiceService;
import com.axelor.apps.supplychain.service.CommonInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.ConfiguratorCheckServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.FreightCarrierModeService;
import com.axelor.apps.supplychain.service.FreightCarrierModeServiceImpl;
import com.axelor.apps.supplychain.service.IntercoService;
import com.axelor.apps.supplychain.service.IntercoServiceImpl;
import com.axelor.apps.supplychain.service.InvoiceLineSupplierCatalogService;
import com.axelor.apps.supplychain.service.InvoiceLineSupplierCatalogServiceImpl;
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
import com.axelor.apps.supplychain.service.MrpProposalService;
import com.axelor.apps.supplychain.service.MrpProposalServiceImpl;
import com.axelor.apps.supplychain.service.MrpSaleOrderCheckLateSaleService;
import com.axelor.apps.supplychain.service.MrpSaleOrderCheckLateSaleServiceImpl;
import com.axelor.apps.supplychain.service.MrpService;
import com.axelor.apps.supplychain.service.MrpServiceImpl;
import com.axelor.apps.supplychain.service.PartnerLinkSupplychainService;
import com.axelor.apps.supplychain.service.PartnerLinkSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.PartnerSupplychainService;
import com.axelor.apps.supplychain.service.PartnerSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.ProductStockLocationService;
import com.axelor.apps.supplychain.service.ProductStockLocationServiceImpl;
import com.axelor.apps.supplychain.service.ProjectedStockService;
import com.axelor.apps.supplychain.service.ProjectedStockServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderCreateServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderCreateSupplychainService;
import com.axelor.apps.supplychain.service.PurchaseOrderFromSaleOrderLinesService;
import com.axelor.apps.supplychain.service.PurchaseOrderFromSaleOrderLinesServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplyChain;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderLineTaxComputeSupplychainServiceImp;
import com.axelor.apps.supplychain.service.PurchaseOrderMergingServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderMergingSupplychainService;
import com.axelor.apps.supplychain.service.PurchaseOrderMergingViewServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderShipmentService;
import com.axelor.apps.supplychain.service.PurchaseOrderShipmentServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderStockService;
import com.axelor.apps.supplychain.service.PurchaseOrderStockServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderSupplychainService;
import com.axelor.apps.supplychain.service.PurchaseOrderWorkflowServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PurchaseRequestServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.ReservedQtyService;
import com.axelor.apps.supplychain.service.ReservedQtyServiceImpl;
import com.axelor.apps.supplychain.service.SaleInvoicingStateService;
import com.axelor.apps.supplychain.service.SaleInvoicingStateServiceImpl;
import com.axelor.apps.supplychain.service.ShippingService;
import com.axelor.apps.supplychain.service.ShippingServiceImpl;
import com.axelor.apps.supplychain.service.StockCorrectionServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockHistoryServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.StockLocationLineFetchServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockLocationLineReservationService;
import com.axelor.apps.supplychain.service.StockLocationLineReservationServiceImpl;
import com.axelor.apps.supplychain.service.StockLocationLineServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockMoveInvoiceService;
import com.axelor.apps.supplychain.service.StockMoveInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychain;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockMoveMergingServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockMoveMultiInvoiceService;
import com.axelor.apps.supplychain.service.StockMoveMultiInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.StockMoveReservedQtyService;
import com.axelor.apps.supplychain.service.StockMoveReservedQtyServiceImpl;
import com.axelor.apps.supplychain.service.StockMoveServiceSupplychain;
import com.axelor.apps.supplychain.service.StockMoveServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockRulesSupplychainService;
import com.axelor.apps.supplychain.service.StockRulesSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.SupplyChainConvertLeadWizardServiceImpl;
import com.axelor.apps.supplychain.service.SupplychainSaleConfigService;
import com.axelor.apps.supplychain.service.SupplychainSaleConfigServiceImpl;
import com.axelor.apps.supplychain.service.TimetableService;
import com.axelor.apps.supplychain.service.TimetableServiceImpl;
import com.axelor.apps.supplychain.service.TrackingNumberCompanySupplychainServiceImpl;
import com.axelor.apps.supplychain.service.TrackingNumberSupplychainService;
import com.axelor.apps.supplychain.service.TrackingNumberSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.analytic.AnalyticAttrsSupplychainService;
import com.axelor.apps.supplychain.service.analytic.AnalyticAttrsSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.analytic.AnalyticToolSupplychainService;
import com.axelor.apps.supplychain.service.analytic.AnalyticToolSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.app.AppSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.batch.BatchAccountingCutOffSupplyChain;
import com.axelor.apps.supplychain.service.batch.SaleBatchSupplyChainService;
import com.axelor.apps.supplychain.service.cart.CartResetSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.cart.CartSaleOrderGeneratorSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.cart.CartStockLocationService;
import com.axelor.apps.supplychain.service.cart.CartStockLocationServiceImpl;
import com.axelor.apps.supplychain.service.cartline.CartLineAvailabilityService;
import com.axelor.apps.supplychain.service.cartline.CartLineAvailabilityServiceImpl;
import com.axelor.apps.supplychain.service.cartline.CartLineProductSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigServiceImpl;
import com.axelor.apps.supplychain.service.declarationofexchanges.DeclarationOfExchangesService;
import com.axelor.apps.supplychain.service.declarationofexchanges.DeclarationOfExchangesServiceImpl;
import com.axelor.apps.supplychain.service.fixedasset.FixedAssetServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.invoice.AdvancePaymentRefundService;
import com.axelor.apps.supplychain.service.invoice.AdvancePaymentRefundServiceImpl;
import com.axelor.apps.supplychain.service.invoice.InvoiceLineAnalyticSupplychainService;
import com.axelor.apps.supplychain.service.invoice.InvoiceLineAnalyticSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.invoice.InvoiceMergingServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychain;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.invoice.InvoiceTaxService;
import com.axelor.apps.supplychain.service.invoice.InvoiceTaxServiceImpl;
import com.axelor.apps.supplychain.service.invoice.SubscriptionInvoiceService;
import com.axelor.apps.supplychain.service.invoice.SubscriptionInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineOrderService;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineOrderServiceImpl;
import com.axelor.apps.supplychain.service.observer.SaleOrderLineSupplychainObserver;
import com.axelor.apps.supplychain.service.observer.SaleOrderSupplychainObserver;
import com.axelor.apps.supplychain.service.order.OrderInvoiceService;
import com.axelor.apps.supplychain.service.order.OrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderCheckSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderComputeServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderCreateServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderCreateSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderInitValueSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderIntercoService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderIntercoServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderInvoiceService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderPurchaseService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderPurchaseServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderReservedQtyService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderReservedQtyServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderShipmentService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderShipmentServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderStockLocationService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderStockLocationServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderStockService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderStockServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderTaxNumberService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderTaxNumberServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderVersionSupplyChainServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.merge.SaleOrderMergingServiceSupplyChain;
import com.axelor.apps.supplychain.service.saleorder.merge.SaleOrderMergingServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.saleorder.merge.SaleOrderMergingViewServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.saleorder.onchange.SaleOrderOnChangeSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.onchange.SaleOrderOnLineChangeSupplyChainServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.opportunity.OpportunitySaleOrderSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.status.SaleOrderConfirmSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.status.SaleOrderConfirmSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.status.SaleOrderFinalizeSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.status.SaleOrderWorkflowServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.saleorder.views.SaleOrderViewSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineAnalyticService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineAnalyticServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineCheckSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineCheckSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineComputeSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineCreateSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineDomainSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineDomainSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineDummySupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineInitValueSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineOnChangeSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineProductSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineProductSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineServiceSupplyChain;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineViewSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineViewSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.workflow.WorkflowCancelServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.workflow.WorkflowVentilationServiceSupplychainImpl;
import com.axelor.apps.supplychain.utils.StockLocationUtilsServiceSupplychain;
import com.axelor.apps.supplychain.utils.StockLocationUtilsServiceSupplychainImpl;

public class SupplychainModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(StockRulesService.class).to(StockRulesServiceImpl.class);
    bind(StockRulesSupplychainService.class).to(StockRulesSupplychainServiceImpl.class);
    bind(PurchaseOrderServiceImpl.class).to(PurchaseOrderServiceSupplychainImpl.class);
    bind(SaleOrderServiceImpl.class).to(SaleOrderServiceSupplychainImpl.class);
    bind(SaleOrderCreateServiceImpl.class).to(SaleOrderCreateServiceSupplychainImpl.class);
    bind(SaleOrderComputeServiceImpl.class).to(SaleOrderComputeServiceSupplychainImpl.class);
    bind(SaleOrderWorkflowServiceImpl.class).to(SaleOrderWorkflowServiceSupplychainImpl.class);
    bind(PurchaseOrderInvoiceService.class).to(PurchaseOrderInvoiceServiceImpl.class);
    bind(SaleOrderInvoiceService.class).to(SaleOrderInvoiceServiceImpl.class);
    bind(OrderInvoiceService.class).to(OrderInvoiceServiceImpl.class);
    bind(SaleOrderPurchaseService.class).to(SaleOrderPurchaseServiceImpl.class);
    bind(StockMoveInvoiceService.class).to(StockMoveInvoiceServiceImpl.class);
    bind(SaleOrderManagementRepository.class).to(SaleOrderSupplychainRepository.class);
    bind(StockMoveServiceImpl.class).to(StockMoveServiceSupplychainImpl.class);
    bind(SaleOrderLineCreateServiceImpl.class).to(SaleOrderLineCreateSupplychainServiceImpl.class);
    bind(AdvancePaymentSaleRepository.class).to(AdvancePaymentSupplychainRepository.class);
    bind(AdvancePaymentServiceImpl.class).to(AdvancePaymentServiceSupplychainImpl.class);
    bind(MrpService.class).to(MrpServiceImpl.class);
    bind(MrpLineService.class).to(MrpLineServiceImpl.class);
    bind(MrpFilterSaleOrderLineService.class).to(MrpFilterSaleOrderLineServiceImpl.class);
    bind(MrpLineTypeService.class).to(MrpLineTypeServiceImpl.class);
    bind(MrpSaleOrderCheckLateSaleService.class).to(MrpSaleOrderCheckLateSaleServiceImpl.class);
    bind(AnalyticMoveLineMngtRepository.class).to(AnalyticMoveLineSupplychainRepository.class);
    bind(StockMoveLineServiceImpl.class).to(StockMoveLineServiceSupplychainImpl.class);
    bind(InvoiceLineServiceImpl.class).to(InvoiceLineSupplychainService.class);
    bind(SaleOrderStockService.class).to(SaleOrderStockServiceImpl.class);
    bind(PurchaseOrderManagementRepository.class).to(PurchaseOrderSupplychainRepository.class);
    bind(AppSupplychainService.class).to(AppSupplychainServiceImpl.class);
    bind(SupplychainSaleConfigService.class).to(SupplychainSaleConfigServiceImpl.class);
    bind(AccountCustomerServiceImpl.class).to(AccountCustomerServiceSupplyChainImpl.class);
    bind(AccountingSituationServiceImpl.class).to(AccountingSituationSupplychainServiceImpl.class);
    bind(AccountingSituationSupplychainService.class)
        .to(AccountingSituationSupplychainServiceImpl.class);
    bind(AccountingSituationGroupServiceImpl.class)
        .to(AccountingSituationGroupSupplychainServiceImpl.class);
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
    bind(StockLocationUtilsServiceImpl.class).to(StockLocationUtilsServiceSupplychainImpl.class);
    bind(StockLocationUtilsServiceSupplychain.class)
        .to(StockLocationUtilsServiceSupplychainImpl.class);
    bind(ReservedQtyService.class).to(ReservedQtyServiceImpl.class);
    bind(PurchaseOrderLineServiceImpl.class).to(PurchaseOrderLineServiceSupplyChainImpl.class);
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
    bind(PurchaseOrderCreateServiceImpl.class).to(PurchaseOrderCreateServiceSupplychainImpl.class);
    bind(PurchaseOrderCreateSupplychainService.class)
        .to(PurchaseOrderCreateServiceSupplychainImpl.class);
    bind(SaleOrderSupplychainService.class).to(SaleOrderServiceSupplychainImpl.class);
    bind(StockMoveServiceSupplychain.class).to(StockMoveServiceSupplychainImpl.class);
    bind(InvoiceManagementRepository.class).to(InvoiceSupplychainRepository.class);
    bind(StockMoveReservedQtyService.class).to(StockMoveReservedQtyServiceImpl.class);
    bind(PurchaseOrderLineServiceSupplyChain.class)
        .to(PurchaseOrderLineServiceSupplyChainImpl.class);
    bind(PurchaseOrderWorkflowServiceImpl.class)
        .to(PurchaseOrderWorkflowServiceSupplychainImpl.class);
    bind(InvoiceServiceSupplychain.class).to(InvoiceServiceSupplychainImpl.class);
    bind(InvoiceManagementRepository.class).to(InvoiceSupplychainRepository.class);
    bind(OpportunitySaleOrderServiceImpl.class)
        .to(OpportunitySaleOrderSupplychainServiceImpl.class);
    bind(PartnerSaleServiceImpl.class).to(PartnerSupplychainServiceImpl.class);
    bind(PartnerSupplychainService.class).to(PartnerSupplychainServiceImpl.class);
    bind(StockHistoryServiceImpl.class).to(StockHistoryServiceSupplyChainImpl.class);
    bind(AccountingSituationInitServiceImpl.class)
        .to(AccountingSituationInitSupplychainServiceImpl.class);
    bind(SaleOrderMergingViewServiceImpl.class)
        .to(SaleOrderMergingViewServiceSupplyChainImpl.class);
    bind(SaleOrderMergingServiceImpl.class).to(SaleOrderMergingServiceSupplyChainImpl.class);
    bind(InvoiceMergingServiceImpl.class).to(InvoiceMergingServiceSupplychainImpl.class);
    bind(CommonInvoiceService.class).to(CommonInvoiceServiceImpl.class);
    bind(MrpForecastService.class).to(MrpForecastServiceImpl.class);
    bind(BatchAccountingCutOff.class).to(BatchAccountingCutOffSupplyChain.class);
    bind(StockProductRestServiceImpl.class).to(StockProductRestServiceSupplychainImpl.class);
    bind(AnalyticMoveLineServiceImpl.class).to(AnalyticMoveLineSupplychainServiceImpl.class);
    bind(InvoiceLineAnalyticServiceImpl.class).to(InvoiceLineAnalyticSupplychainServiceImpl.class);
    bind(InvoiceLineAnalyticSupplychainService.class)
        .to(InvoiceLineAnalyticSupplychainServiceImpl.class);
    bind(InvoiceLineOrderService.class).to(InvoiceLineOrderServiceImpl.class);
    bind(MrpProposalService.class).to(MrpProposalServiceImpl.class);
    bind(SaleInvoicingStateService.class).to(SaleInvoicingStateServiceImpl.class);
    bind(BaseConvertLeadWizardService.class).to(SupplyChainConvertLeadWizardServiceImpl.class);
    bind(PurchaseOrderFromSaleOrderLinesService.class)
        .to(PurchaseOrderFromSaleOrderLinesServiceImpl.class);
    bind(SaleOrderVersionServiceImpl.class).to(SaleOrderVersionSupplyChainServiceImpl.class);
    bind(AnalyticLineModelService.class).to(AnalyticLineModelServiceImpl.class);
    bind(AnalyticAttrsSupplychainService.class).to(AnalyticAttrsSupplychainServiceImpl.class);
    bind(PartnerLinkSupplychainService.class).to(PartnerLinkSupplychainServiceImpl.class);
    bind(PartnerLinkServiceImpl.class).to(PartnerLinkSupplychainServiceImpl.class);
    bind(SaleOrderOnLineChangeServiceImpl.class)
        .to(SaleOrderOnLineChangeSupplyChainServiceImpl.class);
    bind(PurchaseOrderMergingServiceImpl.class)
        .to(PurchaseOrderMergingServiceSupplyChainImpl.class);
    bind(PurchaseOrderMergingSupplychainService.class)
        .to(PurchaseOrderMergingServiceSupplyChainImpl.class);
    bind(PurchaseOrderMergingViewServiceImpl.class)
        .to(PurchaseOrderMergingViewServiceSupplyChainImpl.class);
    bind(AnalyticToolSupplychainService.class).to(AnalyticToolSupplychainServiceImpl.class);
    bind(SaleOrderShipmentService.class).to(SaleOrderShipmentServiceImpl.class);
    bind(StockMoveMergingServiceImpl.class).to(StockMoveMergingServiceSupplychainImpl.class);
    bind(SaleOrderMergingServiceSupplyChain.class).to(SaleOrderMergingServiceSupplyChainImpl.class);
    bind(SaleOrderCreateSupplychainService.class).to(SaleOrderCreateServiceSupplychainImpl.class);
    bind(AdvancePaymentRefundService.class).to(AdvancePaymentRefundServiceImpl.class);
    bind(TrackingNumberSupplychainService.class).to(TrackingNumberSupplychainServiceImpl.class);
    bind(SaleOrderLineComputeServiceImpl.class)
        .to(SaleOrderLineComputeSupplychainServiceImpl.class);
    bind(SaleOrderLineProductServiceImpl.class)
        .to(SaleOrderLineProductSupplychainServiceImpl.class);
    bind(SaleOrderLineProductSupplychainService.class)
        .to(SaleOrderLineProductSupplychainServiceImpl.class);
    bind(SaleOrderLineOnChangeServiceImpl.class)
        .to(SaleOrderLineOnChangeSupplychainServiceImpl.class);
    bind(SaleOrderInitValueServiceImpl.class).to(SaleOrderInitValueSupplychainServiceImpl.class);
    bind(SaleOrderViewServiceImpl.class).to(SaleOrderViewSupplychainServiceImpl.class);
    bind(SaleOrderOnChangeServiceImpl.class).to(SaleOrderOnChangeSupplychainServiceImpl.class);
    bind(SaleOrderIntercoService.class).to(SaleOrderIntercoServiceImpl.class);
    bind(SaleOrderProductPrintingService.class).to(SaleOrderProductPrintingServiceImpl.class);
    bind(SaleOrderStockLocationService.class).to(SaleOrderStockLocationServiceImpl.class);
    bind(StockLocationLineFetchServiceImpl.class)
        .to(StockLocationLineFetchServiceSupplychainImpl.class);
    bind(SaleOrderFinalizeServiceImpl.class).to(SaleOrderFinalizeSupplychainServiceImpl.class);
    bind(SaleOrderLineViewSupplychainService.class)
        .to(SaleOrderLineViewSupplychainServiceImpl.class);
    bind(SaleOrderLineViewServiceImpl.class).to(SaleOrderLineViewSupplychainServiceImpl.class);
    bind(SaleOrderLineDomainSupplychainService.class)
        .to(SaleOrderLineDomainSupplychainServiceImpl.class);
    bind(SaleOrderTaxNumberService.class).to(SaleOrderTaxNumberServiceImpl.class);
    bind(SaleOrderCheckServiceImpl.class).to(SaleOrderCheckSupplychainServiceImpl.class);
    bind(SaleOrderLineSupplychainObserver.class);
    bind(SaleOrderLineDummyServiceImpl.class).to(SaleOrderLineDummySupplychainServiceImpl.class);
    bind(SaleOrderLineInitValueServiceImpl.class)
        .to(SaleOrderLineInitValueSupplychainServiceImpl.class);
    bind(SaleOrderLineAnalyticService.class).to(SaleOrderLineAnalyticServiceImpl.class);
    bind(SaleOrderSupplychainObserver.class);
    bind(SaleOrderConfirmSupplychainService.class).to(SaleOrderConfirmSupplychainServiceImpl.class);
    bind(SaleOrderLineCheckSupplychainService.class)
        .to(SaleOrderLineCheckSupplychainServiceImpl.class);
    bind(SaleOrderLineCheckServiceImpl.class).to(SaleOrderLineCheckSupplychainServiceImpl.class);
    bind(CartStockLocationService.class).to(CartStockLocationServiceImpl.class);
    bind(SaleBatchService.class).to(SaleBatchSupplyChainService.class);
    bind(CartLineManagementRepository.class).to(CartLineSupplychainRepository.class);
    bind(CartLineAvailabilityService.class).to(CartLineAvailabilityServiceImpl.class);
    bind(CartSaleOrderGeneratorServiceImpl.class)
        .to(CartSaleOrderGeneratorSupplychainServiceImpl.class);
    bind(CartLineProductServiceImpl.class).to(CartLineProductSupplychainServiceImpl.class);
    bind(CartResetServiceImpl.class).to(CartResetSupplychainServiceImpl.class);
    bind(InvoiceTaxService.class).to(InvoiceTaxServiceImpl.class);
    bind(InvoiceLineSupplierCatalogService.class).to(InvoiceLineSupplierCatalogServiceImpl.class);
    bind(ConfiguratorCheckServiceImpl.class).to(ConfiguratorCheckServiceSupplychainImpl.class);
    bind(TrackingNumberCompanyServiceImpl.class)
        .to(TrackingNumberCompanySupplychainServiceImpl.class);
    bind(PurchaseOrderLineTaxComputeServiceImpl.class)
        .to(PurchaseOrderLineTaxComputeSupplychainServiceImp.class);
    bind(PurchaseOrderShipmentService.class).to(PurchaseOrderShipmentServiceImpl.class);
    bind(ShippingService.class).to(ShippingServiceImpl.class);
    bind(FreightCarrierModeService.class).to(FreightCarrierModeServiceImpl.class);
  }
}
