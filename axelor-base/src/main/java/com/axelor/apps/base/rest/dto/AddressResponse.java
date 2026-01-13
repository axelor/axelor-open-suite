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
package com.axelor.apps.base.rest.dto;

import com.axelor.apps.base.db.Address;
import com.axelor.utils.api.ResponseStructure;

public class AddressResponse extends ResponseStructure {

  private final long id;
  private final long countyId;
  private final long cityId;
  private final String zip;
  private final String streetName;

  public AddressResponse(Address address) {
    super(address.getVersion());
    this.id = address.getId();
    this.countyId = address.getCountry().getId();
    this.cityId = address.getCity().getId();
    this.zip = address.getZip();
    this.streetName = address.getStreetName();
  }

  public long getId() {
    return id;
  }

  public long getCountyId() {
    return countyId;
  }

  public long getCityId() {
    return cityId;
  }

  public String getZip() {
    return zip;
  }

  public String getStreetName() {
    return streetName;
  }
}
