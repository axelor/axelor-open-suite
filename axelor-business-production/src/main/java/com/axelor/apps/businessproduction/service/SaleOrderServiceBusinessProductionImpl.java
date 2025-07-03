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
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.service.SaleOrderProductionSyncService;
import com.axelor.apps.production.service.SolBomCustomizationService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorderline.creation.SaleOrderLineCreateService;
import com.axelor.apps.sale.service.saleorderline.pack.SaleOrderLinePackService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineComplementaryProductService;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainService;
import com.axelor.apps.supplychain.service.PartnerLinkSupplychainService;
import com.axelor.apps.supplychain.service.SaleInvoicingStateService;
import com.axelor.apps.supplychain.service.TrackingNumberSupplychainService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderStockService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineAnalyticService;
import com.axelor.studio.db.AppProduction;
import com.axelor.studio.db.repo.AppProductionRepository;
import com.axelor.studio.db.repo.AppSaleRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderServiceBusinessProductionImpl extends SaleOrderServiceSupplychainImpl {

  protected final AppSaleService appSaleService;
  protected final AppProductionService appProductionService;
  protected final SaleOrderProductionSyncService saleOrderProductionSyncService;
  protected final SolDetailsBusinessProductionService solDetailsBusinessProductionService;
  protected final SolBomCustomizationService solBomCustomizationService;

  @Inject
  public SaleOrderServiceBusinessProductionImpl(
      AppBaseService appBaseService,
      SaleOrderLineRepository saleOrderLineRepo,
      SaleOrderRepository saleOrderRepo,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderMarginService saleOrderMarginService,
      SaleConfigService saleConfigService,
      SaleOrderLineCreateService saleOrderLineCreateService,
      SaleOrderLineComplementaryProductService saleOrderLineComplementaryProductService,
      SaleOrderLinePackService saleOrderLinePackService,
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      AppSupplychainService appSupplychainService,
      SaleOrderStockService saleOrderStockService,
      AccountingSituationSupplychainService accountingSituationSupplychainService,
      TrackingNumberSupplychainService trackingNumberSupplychainService,
      PartnerLinkSupplychainService partnerLinkSupplychainService,
      SaleInvoicingStateService saleInvoicingStateService,
      AppSaleService appSaleService,
      AppProductionService appProductionService,
      SaleOrderProductionSyncService saleOrderProductionSyncService,
      SolDetailsBusinessProductionService solDetailsBusinessProductionService,
      SolBomCustomizationService solBomCustomizationService,
      SaleOrderLineAnalyticService saleOrderLineAnalyticService) {
    super(
        appBaseService,
        saleOrderLineRepo,
        saleOrderRepo,
        saleOrderComputeService,
        saleOrderMarginService,
        saleConfigService,
        saleOrderLineCreateService,
        saleOrderLineComplementaryProductService,
        saleOrderLinePackService,
        saleOrderLineDiscountService,
        saleOrderLineComputeService,
        appSupplychainService,
        saleOrderStockService,
        accountingSituationSupplychainService,
        trackingNumberSupplychainService,
        partnerLinkSupplychainService,
        saleInvoicingStateService,
        saleOrderLineAnalyticService);
    this.appSaleService = appSaleService;
    this.appProductionService = appProductionService;
    this.saleOrderProductionSyncService = saleOrderProductionSyncService;
    this.solDetailsBusinessProductionService = solDetailsBusinessProductionService;
    this.solBomCustomizationService = solBomCustomizationService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validateChanges(SaleOrder saleOrder) throws AxelorException {
    AppProduction appProduction = appProductionService.getAppProduction();
    int updateProjectSolDetailsLineTypeSelect =
        appProduction.getUpdateProjectSolDetailsLineTypeSelect();
    if (updateProjectSolDetailsLineTypeSelect
            == AppProductionRepository.UPDATE_PROJECT_SOL_DETAILS_TYPE_SELECT_UPDATE
        && appSaleService.getAppSale().getListDisplayTypeSelect()
            == AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_MULTI) {
      solDetailsBusinessProductionService.deleteSolDetailsList(saleOrder);
      solBomCustomizationService.customSaleOrderLineList(saleOrder.getSaleOrderLineList());
      saleOrderProductionSyncService.syncSaleOrderLineList(
          saleOrder, saleOrder.getSaleOrderLineList());
      solDetailsBusinessProductionService.copySolDetailsList(saleOrder);
    }
    super.validateChanges(saleOrder);
  }
}
