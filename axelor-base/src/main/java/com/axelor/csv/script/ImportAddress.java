/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.csv.script;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.service.AddressService;
import com.google.inject.Inject;
import java.util.Map;

public class ImportAddress {

  @Inject protected AddressService addressService;

  public Object importAddress(Object bean, Map<String, Object> values) {

    Address address = (Address) bean;
    address.setFullName(addressService.computeFullName(address));

    return address;
  }
}
