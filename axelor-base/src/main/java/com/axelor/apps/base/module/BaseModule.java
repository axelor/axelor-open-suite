/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.module;

import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.repo.PartnerAddressRepository;
import com.axelor.events.StartupEvent;
import javax.enterprise.event.Observes;
import javax.interceptor.Interceptor;

public class BaseModule {
  private BaseModule() {}

  public static final int PRIORITY = Interceptor.Priority.APPLICATION + 500;

  void onStartup(@Observes StartupEvent event) {
    PartnerAddressRepository.modelPartnerFieldMap.put(PartnerAddress.class.getName(), "_parent");
  }
}
