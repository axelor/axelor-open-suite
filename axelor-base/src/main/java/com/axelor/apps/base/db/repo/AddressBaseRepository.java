/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.service.AddressService;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class AddressBaseRepository extends AddressRepository {

  @Inject protected AddressService addressService;

  @Override
  public Address save(Address entity) {

    entity.setFullName(addressService.computeFullName(entity));
    try {
      addressService.updateLatLong(entity);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }

    return super.save(entity);
  }
}
