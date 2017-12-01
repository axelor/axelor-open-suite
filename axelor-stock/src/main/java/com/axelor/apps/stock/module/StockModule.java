/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.stock.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.base.db.IPartner;
import com.axelor.apps.base.db.repo.ProductBaseRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.InventoryManagementRepository;
import com.axelor.apps.stock.db.repo.InventoryRepository;
import com.axelor.apps.stock.db.repo.LocationRepository;
import com.axelor.apps.stock.db.repo.LocationStockRepository;
import com.axelor.apps.stock.db.repo.LogisticalFormStockRepository;
import com.axelor.apps.stock.db.repo.LogisticalFormRepository;
import com.axelor.apps.stock.db.repo.ProductStockRepository;
import com.axelor.apps.stock.db.repo.StockMoveManagementRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.AddressServiceStockImpl;
import com.axelor.apps.stock.service.LocationLineService;
import com.axelor.apps.stock.service.LocationLineServiceImpl;
import com.axelor.apps.stock.service.LocationService;
import com.axelor.apps.stock.service.LocationServiceImpl;
import com.axelor.apps.stock.service.LogisticalFormLineService;
import com.axelor.apps.stock.service.LogisticalFormLineServiceImpl;
import com.axelor.apps.stock.service.LogisticalFormService;
import com.axelor.apps.stock.service.LogisticalFormServiceImpl;
import com.axelor.apps.stock.service.PartnerProductQualityRatingService;
import com.axelor.apps.stock.service.PartnerProductQualityRatingServiceImpl;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveLineServiceImpl;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.StockMoveServiceImpl;
import com.axelor.apps.stock.service.StockRulesService;
import com.axelor.apps.stock.service.StockRulesServiceImpl;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.stock.service.app.AppStockServiceImpl;


public class StockModule extends AxelorModule {

    @Override
    protected void configure() {
        bind(StockRulesService.class).to(StockRulesServiceImpl.class);
        bind(AddressServiceStockImpl.class);
        bind(InventoryRepository.class).to(InventoryManagementRepository.class);
        bind(StockMoveRepository.class).to(StockMoveManagementRepository.class);
        bind(LocationLineService.class).to(LocationLineServiceImpl.class);
		bind(StockMoveLineService.class).to(StockMoveLineServiceImpl.class);
        bind(StockMoveService.class).to(StockMoveServiceImpl.class);
        bind(LocationService.class).to(LocationServiceImpl.class);
        bind(ProductBaseRepository.class).to(ProductStockRepository.class);
        bind(PartnerProductQualityRatingService.class).to(PartnerProductQualityRatingServiceImpl.class);
        bind(LogisticalFormService.class).to(LogisticalFormServiceImpl.class);
        bind(LogisticalFormLineService.class).to(LogisticalFormLineServiceImpl.class);
		bind(LogisticalFormRepository.class).to(LogisticalFormStockRepository.class);
        bind(LocationRepository.class).to(LocationStockRepository.class);
		bind(AppStockService.class).to(AppStockServiceImpl.class);
        IPartner.modelPartnerFieldMap.put(StockMove.class.getName(), "partner");
    }
}
