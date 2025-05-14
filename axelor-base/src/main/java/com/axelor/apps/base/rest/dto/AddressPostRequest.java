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

import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.NotBlank;

public class AddressPostRequest extends RequestPostStructure {

  @NotBlank private String country;
  private String city;
  private String zip;
  @NotBlank private String streetName;

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getCity() {
    if (city == null || city.isBlank()) {
      return null;
    }
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getZip() {
    if (zip == null || zip.isBlank()) {
      return null;
    }
    return zip;
  }

  public void setZip(String zip) {
    this.zip = zip;
  }

  public String getStreetName() {
    return streetName;
  }

  public void setStreetName(String streetName) {
    this.streetName = streetName;
  }
}
