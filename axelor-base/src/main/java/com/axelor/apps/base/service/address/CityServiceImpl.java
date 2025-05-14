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
package com.axelor.apps.base.service.address;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Optional;

public class CityServiceImpl implements CityService {

  protected final CityRepository cityRepository;

  @Inject
  public CityServiceImpl(CityRepository cityRepository) {
    this.cityRepository = cityRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public City createAndSaveCity(String name, String zip, Country country) {
    City city = new City(name.toUpperCase());
    city.setZip(zip);
    city.setCountry(country);
    return cityRepository.save(city);
  }

  @Override
  public Optional<City> fetchCity(Country country, String city, String zip) throws AxelorException {
    if (city == null && zip == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.CITY_AND_ZIP_BOTH_EMPTY));
    } else if (city == null) {
      return cityRepository.findByZipAndCountry(zip, country).fetchStream().findFirst();
    } else if (zip == null) {
      return cityRepository
          .all()
          .filter("self.country = :country AND UPPER(self.name) = :name")
          .bind("country", country)
          .bind("name", city.toUpperCase())
          .fetchStream()
          .findFirst();
    } else {
      List<City> cityList =
          cityRepository
              .all()
              .filter("self.country = :country AND UPPER(self.name) = :name")
              .bind("country", country)
              .bind("name", city.toUpperCase())
              .fetch();
      if (cityList.size() == 1) {
        return cityList.stream().findFirst();
      } else {
        Optional<City> cityWithCorrectZip =
            cityList.stream().filter(c -> zip.equals(c.getZip())).findFirst();
        if (cityWithCorrectZip.isPresent()) {
          return cityWithCorrectZip;
        }
        return cityList.stream().filter(c -> c.getZip() == null).findFirst();
      }
    }
  }
}
