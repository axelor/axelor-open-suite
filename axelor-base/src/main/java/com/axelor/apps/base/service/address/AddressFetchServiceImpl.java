/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.address;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.google.inject.Inject;

public class AddressFetchServiceImpl implements AddressFetchService {

  protected AddressRepository addressRepository;

  @Inject
  public AddressFetchServiceImpl(AddressRepository addressRepository) {
    this.addressRepository = addressRepository;
  }

  @Override
  public Address getAddress(
      String room,
      String floor,
      String streetName,
      String postBox,
      String zip,
      City city,
      Country country) {

    return addressRepository
        .all()
        .filter(
            "self.room = :room AND self.floor = :floor AND self.streetName = :streetName "
                + "AND self.postBox = :postBox AND self.zip = :zip AND self.city = :city AND self.country = :country")
        .bind("room", room)
        .bind("floor", floor)
        .bind("streetName", streetName)
        .bind("postBox", postBox)
        .bind("zip", zip)
        .bind("city", city)
        .bind("country", country)
        .fetchOne();
  }
}
