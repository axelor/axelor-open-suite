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
package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

public class AddressBaseRepository extends AddressRepository {

  @Inject protected AddressService addressService;

  @Override
  public Address save(Address entity) {

    entity.setFullName(addressService.computeFullName(entity));
    try {
      EntityManager em = JPA.em().getEntityManagerFactory().createEntityManager();
      Address oldAddressObject = em.find(Address.class, entity.getId());
      if (oldAddressObject == null
          || !oldAddressObject.getFullName().equals(entity.getFullName())) {
        addressService.updateLatLong(entity);
      }
      addressService.setFormattedFullName(entity);
      addressService.checkRequiredAddressFields(entity);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }

    return super.save(entity);
  }
}
