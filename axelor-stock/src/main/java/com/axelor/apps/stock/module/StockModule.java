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
package com.axelor.apps.stock.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.base.db.repo.PartnerAddressRepository;
import com.axelor.apps.base.db.repo.ProductBaseRepository;
import com.axelor.apps.base.db.repo.ProductCompanyBaseRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.InventoryLineManagementRepository;
import com.axelor.apps.stock.db.repo.InventoryLineRepository;
import com.axelor.apps.stock.db.repo.InventoryManagementRepository;
import com.axelor.apps.stock.db.repo.InventoryRepository;
import com.axelor.apps.stock.db.repo.LogisticalFormRepository;
import com.axelor.apps.stock.db.repo.LogisticalFormStockRepository;
import com.axelor.apps.stock.db.repo.MassStockMoveManagementRepository;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.db.repo.PickedProductRepository;
import com.axelor.apps.stock.db.repo.ProductCompanyStockRepository;
import com.axelor.apps.stock.db.repo.ProductStockRepository;
import com.axelor.apps.stock.db.repo.StockCorrectionRepository;
import com.axelor.apps.stock.db.repo.StockCorrectionStockRepository;
import com.axelor.apps.stock.db.repo.StockHistoryLineManagementRepository;
import com.axelor.apps.stock.db.repo.StockHistoryLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineStockRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockLocationStockRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineStockRepository;
import com.axelor.apps.stock.db.repo.StockMoveManagementRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.db.repo.StoredProductRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberManagementRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.stock.db.repo.massstockmove.PickedProductManagementRepository;
import com.axelor.apps.stock.db.repo.massstockmove.StoredProductManagementRepository;
import com.axelor.apps.stock.rest.StockProductRestService;
import com.axelor.apps.stock.rest.StockProductRestServiceImpl;
import com.axelor.apps.stock.service.AddressServiceStockImpl;
import com.axelor.apps.stock.service.InventoryLineService;
import com.axelor.apps.stock.service.InventoryLineServiceImpl;
import com.axelor.apps.stock.service.InventoryProductService;
import com.axelor.apps.stock.service.InventoryProductServiceImpl;
import com.axelor.apps.stock.service.InventoryStockLocationUpdateService;
import com.axelor.apps.stock.service.InventoryStockLocationUpdateServiceImpl;
import com.axelor.apps.stock.service.InventoryUpdateService;
import com.axelor.apps.stock.service.InventoryUpdateServiceImpl;
import com.axelor.apps.stock.service.LogisticalFormLineService;
import com.axelor.apps.stock.service.LogisticalFormLineServiceImpl;
import com.axelor.apps.stock.service.LogisticalFormSequenceService;
import com.axelor.apps.stock.service.LogisticalFormSequenceServiceImpl;
import com.axelor.apps.stock.service.LogisticalFormService;
import com.axelor.apps.stock.service.LogisticalFormServiceImpl;
import com.axelor.apps.stock.service.PartnerProductQualityRatingService;
import com.axelor.apps.stock.service.PartnerProductQualityRatingServiceImpl;
import com.axelor.apps.stock.service.PartnerStockSettingsService;
import com.axelor.apps.stock.service.PartnerStockSettingsServiceImpl;
import com.axelor.apps.stock.service.StockCorrectionService;
import com.axelor.apps.stock.service.StockCorrectionServiceImpl;
import com.axelor.apps.stock.service.StockHistoryService;
import com.axelor.apps.stock.service.StockHistoryServiceImpl;
import com.axelor.apps.stock.service.StockLocationAttrsService;
import com.axelor.apps.stock.service.StockLocationAttrsServiceImpl;
import com.axelor.apps.stock.service.StockLocationDomainService;
import com.axelor.apps.stock.service.StockLocationDomainServiceImpl;
import com.axelor.apps.stock.service.StockLocationLineFetchService;
import com.axelor.apps.stock.service.StockLocationLineFetchServiceImpl;
import com.axelor.apps.stock.service.StockLocationLineHistoryService;
import com.axelor.apps.stock.service.StockLocationLineHistoryServiceImpl;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.StockLocationLineServiceImpl;
import com.axelor.apps.stock.service.StockLocationPrintService;
import com.axelor.apps.stock.service.StockLocationPrintServiceImpl;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.stock.service.StockLocationServiceImpl;
import com.axelor.apps.stock.service.StockMoveCheckWapService;
import com.axelor.apps.stock.service.StockMoveCheckWapServiceImpl;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveLineServiceImpl;
import com.axelor.apps.stock.service.StockMoveMergingService;
import com.axelor.apps.stock.service.StockMoveMergingServiceImpl;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.StockMoveServiceImpl;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.apps.stock.service.StockMoveToolServiceImpl;
import com.axelor.apps.stock.service.StockMoveUpdateService;
import com.axelor.apps.stock.service.StockMoveUpdateServiceImpl;
import com.axelor.apps.stock.service.StockRulesService;
import com.axelor.apps.stock.service.StockRulesServiceImpl;
import com.axelor.apps.stock.service.TrackingNumberCompanyService;
import com.axelor.apps.stock.service.TrackingNumberCompanyServiceImpl;
import com.axelor.apps.stock.service.TrackingNumberConfigurationProfileService;
import com.axelor.apps.stock.service.TrackingNumberConfigurationProfileServiceImpl;
import com.axelor.apps.stock.service.TrackingNumberConfigurationService;
import com.axelor.apps.stock.service.TrackingNumberConfigurationServiceImpl;
import com.axelor.apps.stock.service.TrackingNumberCreateService;
import com.axelor.apps.stock.service.TrackingNumberCreateServiceImpl;
import com.axelor.apps.stock.service.TrackingNumberService;
import com.axelor.apps.stock.service.TrackingNumberServiceImpl;
import com.axelor.apps.stock.service.WeightedAveragePriceService;
import com.axelor.apps.stock.service.WeightedAveragePriceServiceImpl;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.stock.service.app.AppStockServiceImpl;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductAttrsService;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductAttrsServiceImpl;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductCancelService;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductCancelServiceImpl;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductQuantityService;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductQuantityServiceImpl;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductRealizeService;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductRealizeServiceImpl;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductServiceFactory;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductServiceFactoryImpl;
import com.axelor.apps.stock.service.massstockmove.MassStockMoveNeedService;
import com.axelor.apps.stock.service.massstockmove.MassStockMoveNeedServiceImpl;
import com.axelor.apps.stock.service.massstockmove.MassStockMoveNeedToPickedProductService;
import com.axelor.apps.stock.service.massstockmove.MassStockMoveNeedToPickedProductServiceImpl;
import com.axelor.apps.stock.service.massstockmove.MassStockMoveRecordService;
import com.axelor.apps.stock.service.massstockmove.MassStockMoveRecordServiceImpl;
import com.axelor.apps.stock.service.massstockmove.PickedProductAttrsService;
import com.axelor.apps.stock.service.massstockmove.PickedProductAttrsServiceImpl;
import com.axelor.apps.stock.service.massstockmove.PickedProductService;
import com.axelor.apps.stock.service.massstockmove.PickedProductServiceImpl;
import com.axelor.apps.stock.service.massstockmove.StoredProductAttrsService;
import com.axelor.apps.stock.service.massstockmove.StoredProductAttrsServiceImpl;
import com.axelor.apps.stock.service.massstockmove.StoredProductService;
import com.axelor.apps.stock.service.massstockmove.StoredProductServiceImpl;
import com.axelor.apps.stock.service.observer.ProductPopulateStockObserver;
import com.axelor.apps.stock.service.stockmove.print.ConformityCertificatePrintService;
import com.axelor.apps.stock.service.stockmove.print.ConformityCertificatePrintServiceImpl;
import com.axelor.apps.stock.service.stockmove.print.PickingStockMovePrintService;
import com.axelor.apps.stock.service.stockmove.print.PickingStockMovePrintServiceimpl;
import com.axelor.apps.stock.utils.StockLocationUtilsService;
import com.axelor.apps.stock.utils.StockLocationUtilsServiceImpl;

public class StockModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(AddressServiceStockImpl.class);
    bind(StockRulesService.class).to(StockRulesServiceImpl.class);
    bind(InventoryRepository.class).to(InventoryManagementRepository.class);
    bind(StockMoveRepository.class).to(StockMoveManagementRepository.class);
    bind(StockLocationLineService.class).to(StockLocationLineServiceImpl.class);
    bind(StockMoveLineService.class).to(StockMoveLineServiceImpl.class);
    bind(StockMoveService.class).to(StockMoveServiceImpl.class);
    bind(StockMoveUpdateService.class).to(StockMoveUpdateServiceImpl.class);
    bind(StockLocationService.class).to(StockLocationServiceImpl.class);
    bind(ProductBaseRepository.class).to(ProductStockRepository.class);
    bind(PartnerProductQualityRatingService.class).to(PartnerProductQualityRatingServiceImpl.class);
    bind(LogisticalFormService.class).to(LogisticalFormServiceImpl.class);
    bind(LogisticalFormLineService.class).to(LogisticalFormLineServiceImpl.class);
    bind(LogisticalFormRepository.class).to(LogisticalFormStockRepository.class);
    bind(StockLocationRepository.class).to(StockLocationStockRepository.class);
    bind(PartnerStockSettingsService.class).to(PartnerStockSettingsServiceImpl.class);
    bind(AppStockService.class).to(AppStockServiceImpl.class);
    bind(StockMoveLineRepository.class).to(StockMoveLineStockRepository.class);
    PartnerAddressRepository.modelPartnerFieldMap.put(StockMove.class.getName(), "partner");
    bind(TrackingNumberRepository.class).to(TrackingNumberManagementRepository.class);
    bind(StockMoveToolService.class).to(StockMoveToolServiceImpl.class);
    bind(PickingStockMovePrintService.class).to(PickingStockMovePrintServiceimpl.class);
    bind(ConformityCertificatePrintService.class).to(ConformityCertificatePrintServiceImpl.class);
    bind(StockLocationLineRepository.class).to(StockLocationLineStockRepository.class);
    bind(StockCorrectionService.class).to(StockCorrectionServiceImpl.class);
    bind(WeightedAveragePriceService.class).to(WeightedAveragePriceServiceImpl.class);
    bind(StockHistoryService.class).to(StockHistoryServiceImpl.class);
    bind(StockCorrectionRepository.class).to(StockCorrectionStockRepository.class);
    bind(InventoryProductService.class).to(InventoryProductServiceImpl.class);
    bind(TrackingNumberConfigurationService.class).to(TrackingNumberConfigurationServiceImpl.class);
    bind(StockProductRestService.class).to(StockProductRestServiceImpl.class);
    bind(InventoryUpdateService.class).to(InventoryUpdateServiceImpl.class);
    bind(StockHistoryLineRepository.class).to(StockHistoryLineManagementRepository.class);
    bind(StockMoveCheckWapService.class).to(StockMoveCheckWapServiceImpl.class);
    bind(StockLocationLineHistoryService.class).to(StockLocationLineHistoryServiceImpl.class);
    bind(StockMoveMergingService.class).to(StockMoveMergingServiceImpl.class);
    bind(InventoryLineService.class).to(InventoryLineServiceImpl.class);
    bind(StockLocationPrintService.class).to(StockLocationPrintServiceImpl.class);
    bind(InventoryLineRepository.class).to(InventoryLineManagementRepository.class);
    bind(TrackingNumberService.class).to(TrackingNumberServiceImpl.class);
    bind(TrackingNumberConfigurationProfileService.class)
        .to(TrackingNumberConfigurationProfileServiceImpl.class);
    bind(StockLocationAttrsService.class).to(StockLocationAttrsServiceImpl.class);
    bind(MassStockMovableProductAttrsService.class)
        .to(MassStockMovableProductAttrsServiceImpl.class);
    bind(MassStockMoveRecordService.class).to(MassStockMoveRecordServiceImpl.class);
    bind(MassStockMovableProductRealizeService.class)
        .to(MassStockMovableProductRealizeServiceImpl.class);
    bind(MassStockMovableProductServiceFactory.class)
        .to(MassStockMovableProductServiceFactoryImpl.class);
    bind(StoredProductAttrsService.class).to(StoredProductAttrsServiceImpl.class);
    bind(PickedProductService.class).to(PickedProductServiceImpl.class);
    bind(MassStockMovableProductQuantityService.class)
        .to(MassStockMovableProductQuantityServiceImpl.class);
    bind(StoredProductRepository.class).to(StoredProductManagementRepository.class);
    bind(PickedProductRepository.class).to(PickedProductManagementRepository.class);
    bind(StockLocationUtilsService.class).to(StockLocationUtilsServiceImpl.class);
    bind(StockLocationLineFetchService.class).to(StockLocationLineFetchServiceImpl.class);
    bind(TrackingNumberCreateService.class).to(TrackingNumberCreateServiceImpl.class);
    bind(MassStockMovableProductCancelService.class)
        .to(MassStockMovableProductCancelServiceImpl.class);
    bind(MassStockMoveRepository.class).to(MassStockMoveManagementRepository.class);
    bind(ProductPopulateStockObserver.class);
    bind(PickedProductAttrsService.class).to(PickedProductAttrsServiceImpl.class);
    bind(StockLocationDomainService.class).to(StockLocationDomainServiceImpl.class);
    bind(MassStockMoveNeedService.class).to(MassStockMoveNeedServiceImpl.class);
    bind(MassStockMoveNeedToPickedProductService.class)
        .to(MassStockMoveNeedToPickedProductServiceImpl.class);
    bind(StoredProductService.class).to(StoredProductServiceImpl.class);
    bind(LogisticalFormSequenceService.class).to(LogisticalFormSequenceServiceImpl.class);
    bind(TrackingNumberCompanyService.class).to(TrackingNumberCompanyServiceImpl.class);
    bind(InventoryStockLocationUpdateService.class)
        .to(InventoryStockLocationUpdateServiceImpl.class);
    bind(ProductCompanyBaseRepository.class).to(ProductCompanyStockRepository.class);
  }
}
