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
package com.axelor.apps.sale.service;

import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.db.repo.StreetRepository;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.address.AddressAttrsService;
import com.axelor.apps.base.service.address.AddressServiceImpl;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.db.JPA;
import com.axelor.utils.helpers.address.AddressHelper;
import com.google.inject.Inject;

public class AddressServiceSaleImpl extends AddressServiceImpl {
  static {
    registerCheckUsedFunc(AddressServiceSaleImpl::checkAddressUsedSale);
  }

  @Inject
  public AddressServiceSaleImpl(
      AddressHelper ads,
      MapService mapService,
      CityRepository cityRepository,
      StreetRepository streetRepository,
      AppBaseService appBaseService,
      AddressAttrsService addressAttrsService) {
    super(ads, mapService, cityRepository, streetRepository, appBaseService, addressAttrsService);
  }

  private static boolean checkAddressUsedSale(Long addressId) {
    return JPA.all(SaleOrder.class)
            .filter("self.mainInvoicingAddress.id = ?1 OR self.deliveryAddress.id = ?1", addressId)
            .fetchOne()
        != null;
  }
}
