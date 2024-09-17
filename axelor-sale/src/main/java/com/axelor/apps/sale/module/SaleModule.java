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
package com.axelor.apps.sale.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.base.db.repo.PartnerAddressRepository;
import com.axelor.apps.base.service.PartnerServiceImpl;
import com.axelor.apps.base.service.ProductCategoryServiceImpl;
import com.axelor.apps.base.service.pricing.PricingGenericServiceImpl;
import com.axelor.apps.base.service.pricing.PricingGroupServiceImpl;
import com.axelor.apps.base.service.pricing.PricingMetaServiceImpl;
import com.axelor.apps.base.service.pricing.PricingObserverImpl;
import com.axelor.apps.crm.db.repo.OpportunityManagementRepository;
import com.axelor.apps.crm.service.OpportunityServiceImpl;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.AdvancePaymentRepository;
import com.axelor.apps.sale.db.repo.AdvancePaymentSaleRepository;
import com.axelor.apps.sale.db.repo.CartLineManagementRepository;
import com.axelor.apps.sale.db.repo.CartLineRepository;
import com.axelor.apps.sale.db.repo.ConfiguratorCreatorRepository;
import com.axelor.apps.sale.db.repo.ConfiguratorCreatorSaleRepository;
import com.axelor.apps.sale.db.repo.OpportunitySaleRepository;
import com.axelor.apps.sale.db.repo.SaleBatchRepository;
import com.axelor.apps.sale.db.repo.SaleBatchSaleRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineSaleRepository;
import com.axelor.apps.sale.db.repo.SaleOrderManagementRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.AddressServiceSaleImpl;
import com.axelor.apps.sale.service.AdvancePaymentService;
import com.axelor.apps.sale.service.AdvancePaymentServiceImpl;
import com.axelor.apps.sale.service.PackLineService;
import com.axelor.apps.sale.service.PackLineServiceImpl;
import com.axelor.apps.sale.service.PartnerSaleService;
import com.axelor.apps.sale.service.PartnerSaleServiceImpl;
import com.axelor.apps.sale.service.PricingGroupSaleServiceImpl;
import com.axelor.apps.sale.service.ProductCategorySaleService;
import com.axelor.apps.sale.service.ProductCategoryServiceSaleImpl;
import com.axelor.apps.sale.service.ProductRestService;
import com.axelor.apps.sale.service.ProductRestServiceImpl;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.app.AppSaleServiceImpl;
import com.axelor.apps.sale.service.cart.CartCreateService;
import com.axelor.apps.sale.service.cart.CartCreateServiceImpl;
import com.axelor.apps.sale.service.cart.CartInitValueService;
import com.axelor.apps.sale.service.cart.CartInitValueServiceImpl;
import com.axelor.apps.sale.service.cart.CartProductService;
import com.axelor.apps.sale.service.cart.CartProductServiceImpl;
import com.axelor.apps.sale.service.cart.CartResetService;
import com.axelor.apps.sale.service.cart.CartResetServiceImpl;
import com.axelor.apps.sale.service.cart.CartRetrievalService;
import com.axelor.apps.sale.service.cart.CartRetrievalServiceImpl;
import com.axelor.apps.sale.service.cart.CartSaleOrderGeneratorService;
import com.axelor.apps.sale.service.cart.CartSaleOrderGeneratorServiceImpl;
import com.axelor.apps.sale.service.cartline.CartLineCreateService;
import com.axelor.apps.sale.service.cartline.CartLineCreateServiceImpl;
import com.axelor.apps.sale.service.cartline.CartLinePriceService;
import com.axelor.apps.sale.service.cartline.CartLinePriceServiceImpl;
import com.axelor.apps.sale.service.cartline.CartLineProductService;
import com.axelor.apps.sale.service.cartline.CartLineProductServiceImpl;
import com.axelor.apps.sale.service.cartline.CartLineRetrievalService;
import com.axelor.apps.sale.service.cartline.CartLineRetrievalServiceImpl;
import com.axelor.apps.sale.service.cartline.CartLineUpdateService;
import com.axelor.apps.sale.service.cartline.CartLineUpdateServiceImpl;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.config.SaleConfigServiceImpl;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorImportService;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorImportServiceImpl;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorService;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorServiceImpl;
import com.axelor.apps.sale.service.configurator.ConfiguratorFormulaService;
import com.axelor.apps.sale.service.configurator.ConfiguratorFormulaServiceImpl;
import com.axelor.apps.sale.service.configurator.ConfiguratorInitService;
import com.axelor.apps.sale.service.configurator.ConfiguratorInitServiceImpl;
import com.axelor.apps.sale.service.configurator.ConfiguratorMetaJsonFieldService;
import com.axelor.apps.sale.service.configurator.ConfiguratorMetaJsonFieldServiceImpl;
import com.axelor.apps.sale.service.configurator.ConfiguratorService;
import com.axelor.apps.sale.service.configurator.ConfiguratorServiceImpl;
import com.axelor.apps.sale.service.observer.ProductPopulateSaleObserver;
import com.axelor.apps.sale.service.observer.SaleOrderLineFireService;
import com.axelor.apps.sale.service.observer.SaleOrderLineFireServiceImpl;
import com.axelor.apps.sale.service.observer.SaleOrderLineObserver;
import com.axelor.apps.sale.service.observer.SaleOrderObserver;
import com.axelor.apps.sale.service.saleorder.SaleOrderBankDetailsService;
import com.axelor.apps.sale.service.saleorder.SaleOrderBankDetailsServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderCheckService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCheckServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderDomainService;
import com.axelor.apps.sale.service.saleorder.SaleOrderDomainServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderGeneratorService;
import com.axelor.apps.sale.service.saleorder.SaleOrderGeneratorServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderInitValueService;
import com.axelor.apps.sale.service.saleorder.SaleOrderInitValueServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingViewService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingViewServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderOnChangeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderOnChangeServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderOnLineChangeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderOnLineChangeServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.sale.service.saleorder.SaleOrderServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderUserService;
import com.axelor.apps.sale.service.saleorder.SaleOrderUserServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderVersionService;
import com.axelor.apps.sale.service.saleorder.SaleOrderVersionServiceImpl;
import com.axelor.apps.sale.service.saleorder.opportunity.OpportunitySaleOrderService;
import com.axelor.apps.sale.service.saleorder.opportunity.OpportunitySaleOrderServiceImpl;
import com.axelor.apps.sale.service.saleorder.opportunity.OpportunityServiceSaleImpl;
import com.axelor.apps.sale.service.saleorder.pricing.PricingObserverSaleImpl;
import com.axelor.apps.sale.service.saleorder.pricing.SaleOrderLinePricingService;
import com.axelor.apps.sale.service.saleorder.pricing.SaleOrderLinePricingServiceImpl;
import com.axelor.apps.sale.service.saleorder.pricing.SalePricingGenericServiceImpl;
import com.axelor.apps.sale.service.saleorder.pricing.SalePricingMetaServiceImpl;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderPrintService;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderPrintServiceImpl;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderProductPrintingService;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderProductPrintingServiceImpl;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderConfirmService;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderConfirmServiceImpl;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderFinalizeService;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderFinalizeServiceImpl;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderWorkflowService;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderWorkflowServiceImpl;
import com.axelor.apps.sale.service.saleorder.views.SaleOrderAttrsService;
import com.axelor.apps.sale.service.saleorder.views.SaleOrderAttrsServiceImpl;
import com.axelor.apps.sale.service.saleorder.views.SaleOrderDummyService;
import com.axelor.apps.sale.service.saleorder.views.SaleOrderDummyServiceImpl;
import com.axelor.apps.sale.service.saleorder.views.SaleOrderGroupService;
import com.axelor.apps.sale.service.saleorder.views.SaleOrderGroupServiceImpl;
import com.axelor.apps.sale.service.saleorder.views.SaleOrderViewService;
import com.axelor.apps.sale.service.saleorder.views.SaleOrderViewServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCalculationComboService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCalculationComboServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCheckService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCheckServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCreateService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCreateServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCreateTaxLineService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCreateTaxLineServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDomainService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDomainServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDummyService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDummyServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineFiscalPositionService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineFiscalPositionServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineGeneratorService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineGeneratorServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineInitValueService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineInitValueServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineMultipleQtyService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineMultipleQtyServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineOnChangeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineOnChangeServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePackService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePackServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePriceService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePriceServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineTaxService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineTaxServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineViewService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineViewServiceImpl;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineComplementaryProductService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineComplementaryProductServiceImpl;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineProductService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineProductServiceImpl;
import com.axelor.apps.sale.service.saleorderline.saleorderlinetree.SaleOrderLineTreeComputationService;
import com.axelor.apps.sale.service.saleorderline.saleorderlinetree.SaleOrderLineTreeComputationServiceImpl;
import com.axelor.apps.sale.service.saleorderline.saleorderlinetree.SaleOrderLineTreeService;
import com.axelor.apps.sale.service.saleorderline.saleorderlinetree.SaleOrderLineTreeServiceImpl;

public class SaleModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(AddressServiceSaleImpl.class);
    bind(PartnerServiceImpl.class).to(PartnerSaleServiceImpl.class);
    bind(SaleOrderService.class).to(SaleOrderServiceImpl.class);
    bind(SaleOrderLineCreateService.class).to(SaleOrderLineCreateServiceImpl.class);
    bind(SaleOrderRepository.class).to(SaleOrderManagementRepository.class);
    bind(SaleOrderWorkflowService.class).to(SaleOrderWorkflowServiceImpl.class);
    bind(SaleOrderMarginService.class).to(SaleOrderMarginServiceImpl.class);
    bind(SaleOrderCreateService.class).to(SaleOrderCreateServiceImpl.class);
    bind(SaleOrderComputeService.class).to(SaleOrderComputeServiceImpl.class);
    bind(OpportunityServiceImpl.class).to(OpportunityServiceSaleImpl.class);
    bind(OpportunitySaleOrderService.class).to(OpportunitySaleOrderServiceImpl.class);
    bind(AdvancePaymentService.class).to(AdvancePaymentServiceImpl.class);
    bind(AppSaleService.class).to(AppSaleServiceImpl.class);
    bind(SaleConfigService.class).to(SaleConfigServiceImpl.class);
    bind(SaleBatchRepository.class).to(SaleBatchSaleRepository.class);
    PartnerAddressRepository.modelPartnerFieldMap.put(SaleOrder.class.getName(), "clientPartner");
    bind(AdvancePaymentRepository.class).to(AdvancePaymentSaleRepository.class);
    bind(ConfiguratorCreatorService.class).to(ConfiguratorCreatorServiceImpl.class);
    bind(ConfiguratorService.class).to(ConfiguratorServiceImpl.class);
    bind(ConfiguratorInitService.class).to(ConfiguratorInitServiceImpl.class);
    bind(ConfiguratorFormulaService.class).to(ConfiguratorFormulaServiceImpl.class);
    bind(ConfiguratorCreatorImportService.class).to(ConfiguratorCreatorImportServiceImpl.class);
    bind(SaleOrderPrintService.class).to(SaleOrderPrintServiceImpl.class);
    bind(OpportunityManagementRepository.class).to(OpportunitySaleRepository.class);
    bind(PartnerSaleService.class).to(PartnerSaleServiceImpl.class);
    bind(PackLineService.class).to(PackLineServiceImpl.class);
    bind(ProductCategorySaleService.class).to(ProductCategoryServiceSaleImpl.class);
    bind(ProductCategoryServiceImpl.class).to(ProductCategoryServiceSaleImpl.class);
    bind(SaleOrderLineRepository.class).to(SaleOrderLineSaleRepository.class);
    bind(ConfiguratorCreatorRepository.class).to(ConfiguratorCreatorSaleRepository.class);
    bind(ConfiguratorMetaJsonFieldService.class).to(ConfiguratorMetaJsonFieldServiceImpl.class);
    bind(SaleOrderDomainService.class).to(SaleOrderDomainServiceImpl.class);
    bind(SaleOrderMergingViewService.class).to(SaleOrderMergingViewServiceImpl.class);
    bind(SaleOrderMergingService.class).to(SaleOrderMergingServiceImpl.class);
    bind(SaleOrderOnLineChangeService.class).to(SaleOrderOnLineChangeServiceImpl.class);
    bind(SaleOrderVersionService.class).to(SaleOrderVersionServiceImpl.class);
    bind(SaleOrderLineTreeService.class).to(SaleOrderLineTreeServiceImpl.class);
    bind(SaleOrderLineTreeComputationService.class)
        .to(SaleOrderLineTreeComputationServiceImpl.class);
    bind(SaleOrderLineCalculationComboService.class)
        .to(SaleOrderLineCalculationComboServiceImpl.class);
    bind(SaleOrderLineCreateTaxLineService.class).to(SaleOrderLineCreateTaxLineServiceImpl.class);
    bind(PricingObserverImpl.class).to(PricingObserverSaleImpl.class);
    bind(PricingGenericServiceImpl.class).to(SalePricingGenericServiceImpl.class);
    bind(PricingMetaServiceImpl.class).to(SalePricingMetaServiceImpl.class);
    bind(PricingGroupServiceImpl.class).to(PricingGroupSaleServiceImpl.class);
    bind(SaleOrderAttrsService.class).to(SaleOrderAttrsServiceImpl.class);
    bind(SaleOrderGroupService.class).to(SaleOrderGroupServiceImpl.class);
    bind(SaleOrderLineComputeService.class).to(SaleOrderLineComputeServiceImpl.class);
    bind(SaleOrderLineComplementaryProductService.class)
        .to(SaleOrderLineComplementaryProductServiceImpl.class);
    bind(SaleOrderLinePackService.class).to(SaleOrderLinePackServiceImpl.class);
    bind(SaleOrderLinePricingService.class).to(SaleOrderLinePricingServiceImpl.class);
    bind(SaleOrderLineProductService.class).to(SaleOrderLineProductServiceImpl.class);
    bind(SaleOrderLineComputeService.class).to(SaleOrderLineComputeServiceImpl.class);
    bind(SaleOrderLineTaxService.class).to(SaleOrderLineTaxServiceImpl.class);
    bind(SaleOrderLineDiscountService.class).to(SaleOrderLineDiscountServiceImpl.class);
    bind(SaleOrderLineDomainService.class).to(SaleOrderLineDomainServiceImpl.class);
    bind(SaleOrderLineFiscalPositionService.class).to(SaleOrderLineFiscalPositionServiceImpl.class);
    bind(SaleOrderLineMultipleQtyService.class).to(SaleOrderLineMultipleQtyServiceImpl.class);
    bind(SaleOrderLinePriceService.class).to(SaleOrderLinePriceServiceImpl.class);
    bind(SaleOrderLineOnChangeService.class).to(SaleOrderLineOnChangeServiceImpl.class);
    bind(SaleOrderInitValueService.class).to(SaleOrderInitValueServiceImpl.class);
    bind(SaleOrderViewService.class).to(SaleOrderViewServiceImpl.class);
    bind(SaleOrderDummyService.class).to(SaleOrderDummyServiceImpl.class);
    bind(SaleOrderOnChangeService.class).to(SaleOrderOnChangeServiceImpl.class);
    bind(SaleOrderUserService.class).to(SaleOrderUserServiceImpl.class);
    bind(SaleOrderProductPrintingService.class).to(SaleOrderProductPrintingServiceImpl.class);
    bind(SaleOrderCheckService.class).to(SaleOrderCheckServiceImpl.class);
    bind(SaleOrderFinalizeService.class).to(SaleOrderFinalizeServiceImpl.class);
    bind(SaleOrderLineViewService.class).to(SaleOrderLineViewServiceImpl.class);
    bind(SaleOrderLineDummyService.class).to(SaleOrderLineDummyServiceImpl.class);
    bind(SaleOrderBankDetailsService.class).to(SaleOrderBankDetailsServiceImpl.class);
    bind(SaleOrderLineFireService.class).to(SaleOrderLineFireServiceImpl.class);
    bind(SaleOrderLineObserver.class);
    bind(SaleOrderLineInitValueService.class).to(SaleOrderLineInitValueServiceImpl.class);
    bind(SaleOrderObserver.class);
    bind(SaleOrderConfirmService.class).to(SaleOrderConfirmServiceImpl.class);
    bind(SaleOrderLineCheckService.class).to(SaleOrderLineCheckServiceImpl.class);
    bind(SaleOrderLineGeneratorService.class).to(SaleOrderLineGeneratorServiceImpl.class);
    bind(SaleOrderGeneratorService.class).to(SaleOrderGeneratorServiceImpl.class);
    bind(CartInitValueService.class).to(CartInitValueServiceImpl.class);
    bind(CartCreateService.class).to(CartCreateServiceImpl.class);
    bind(CartRetrievalService.class).to(CartRetrievalServiceImpl.class);
    bind(CartResetService.class).to(CartResetServiceImpl.class);
    bind(CartProductService.class).to(CartProductServiceImpl.class);
    bind(CartLineRepository.class).to(CartLineManagementRepository.class);
    bind(CartLineCreateService.class).to(CartLineCreateServiceImpl.class);
    bind(CartLineRetrievalService.class).to(CartLineRetrievalServiceImpl.class);
    bind(CartLineUpdateService.class).to(CartLineUpdateServiceImpl.class);
    bind(CartLineProductService.class).to(CartLineProductServiceImpl.class);
    bind(CartLinePriceService.class).to(CartLinePriceServiceImpl.class);
    bind(ProductPopulateSaleObserver.class);
    bind(ProductRestService.class).to(ProductRestServiceImpl.class);
    bind(CartSaleOrderGeneratorService.class).to(CartSaleOrderGeneratorServiceImpl.class);
  }
}
