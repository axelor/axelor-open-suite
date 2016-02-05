/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.InventoryManagementRepository;
import com.axelor.apps.stock.db.repo.InventoryRepository;
import com.axelor.apps.stock.db.repo.StockMoveManagementRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.AddressServiceStockImpl;
import com.axelor.apps.stock.service.LocationService;
import com.axelor.apps.stock.service.LocationServiceImpl;
import com.axelor.apps.stock.service.MinStockRulesService;
import com.axelor.apps.stock.service.MinStockRulesServiceImpl;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.StockMoveServiceImpl;


public class StockModule extends AxelorModule {

    @Override
    protected void configure() {
        bind(MinStockRulesService.class).to(MinStockRulesServiceImpl.class);
        bind(AddressServiceStockImpl.class);
        bind(InventoryRepository.class).to(InventoryManagementRepository.class);
        bind(StockMoveRepository.class).to(StockMoveManagementRepository.class);
        bind(StockMoveService.class).to(StockMoveServiceImpl.class);
        bind(LocationService.class).to(LocationServiceImpl.class);
        IPartner.modelPartnerFieldMap.put(StockMove.class.getName(), "partner");
    }
}