/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.repo.PartnerAddressRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.service.AddressServiceStockImpl;
import com.axelor.events.StartupEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.interceptor.Interceptor;

@ApplicationScoped
public class StockModule {
  private StockModule() {}

  public static final int PRIORITY = Interceptor.Priority.APPLICATION + 1000;

  void onStartup(@Observes StartupEvent event) {
    AddressServiceStockImpl.init();
    PartnerAddressRepository.modelPartnerFieldMap.put(StockMove.class.getName(), "partner");
  }
}
