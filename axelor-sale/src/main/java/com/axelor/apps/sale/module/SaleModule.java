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
package com.axelor.apps.sale.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.base.db.repo.PartnerAddressRepository;
import com.axelor.apps.base.service.PartnerServiceImpl;
import com.axelor.apps.base.service.ProductCategoryServiceImpl;
import com.axelor.apps.crm.db.repo.OpportunityManagementRepository;
import com.axelor.apps.crm.service.OpportunityServiceImpl;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.AdvancePaymentRepository;
import com.axelor.apps.sale.db.repo.AdvancePaymentSaleRepository;
import com.axelor.apps.sale.db.repo.ConfiguratorCreatorRepository;
import com.axelor.apps.sale.db.repo.ConfiguratorCreatorSaleRepository;
import com.axelor.apps.sale.db.repo.OpportunitySaleRepository;
import com.axelor.apps.sale.db.repo.SaleBatchRepository;
import com.axelor.apps.sale.db.repo.SaleBatchSaleRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderManagementRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.AddressServiceSaleImpl;
import com.axelor.apps.sale.service.AdvancePaymentService;
import com.axelor.apps.sale.service.AdvancePaymentServiceImpl;
import com.axelor.apps.sale.service.PackLineService;
import com.axelor.apps.sale.service.PackLineServiceImpl;
import com.axelor.apps.sale.service.PartnerSaleService;
import com.axelor.apps.sale.service.PartnerSaleServiceImpl;
import com.axelor.apps.sale.service.ProductCategorySaleService;
import com.axelor.apps.sale.service.ProductCategoryServiceSaleImpl;
import com.axelor.apps.sale.service.SaleOrderDomainService;
import com.axelor.apps.sale.service.SaleOrderDomainServiceImpl;
import com.axelor.apps.sale.service.SaleOrderLineSaleRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.app.AppSaleServiceImpl;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.config.SaleConfigServiceImpl;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorImportService;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorImportServiceImpl;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorService;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorServiceImpl;
import com.axelor.apps.sale.service.configurator.ConfiguratorFormulaService;
import com.axelor.apps.sale.service.configurator.ConfiguratorFormulaServiceImpl;
import com.axelor.apps.sale.service.configurator.ConfiguratorMetaJsonFieldService;
import com.axelor.apps.sale.service.configurator.ConfiguratorMetaJsonFieldServiceImpl;
import com.axelor.apps.sale.service.configurator.ConfiguratorService;
import com.axelor.apps.sale.service.configurator.ConfiguratorServiceImpl;
import com.axelor.apps.sale.service.saleorder.OpportunitySaleOrderService;
import com.axelor.apps.sale.service.saleorder.OpportunitySaleOrderServiceImpl;
import com.axelor.apps.sale.service.saleorder.OpportunityServiceSaleImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineCalculationComboService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineCalculationComboServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineTaxService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineTaxServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineTreeComputationService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineTreeComputationServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineTreeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineTreeServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingViewService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingViewServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderOnLineChangeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderOnLineChangeServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.sale.service.saleorder.SaleOrderServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderVersionService;
import com.axelor.apps.sale.service.saleorder.SaleOrderVersionServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowService;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowServiceImpl;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderPrintService;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderPrintServiceImpl;

public class SaleModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(AddressServiceSaleImpl.class);
    bind(PartnerServiceImpl.class).to(PartnerSaleServiceImpl.class);
    bind(SaleOrderService.class).to(SaleOrderServiceImpl.class);
    bind(SaleOrderLineService.class).to(SaleOrderLineServiceImpl.class);
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
    bind(SaleOrderLineTaxService.class).to(SaleOrderLineTaxServiceImpl.class);
  }
}
