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
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Optional;

public class CountryServiceImpl implements CountryService {

  protected final CountryRepository countryRepository;
  protected final AppBaseService appBaseService;

  @Inject
  public CountryServiceImpl(CountryRepository countryRepository, AppBaseService appBaseService) {
    this.countryRepository = countryRepository;
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Country createAndSaveCountry(String name, String code) throws AxelorException {
    Country country = new Country(name.toUpperCase());
    code = code.toUpperCase();
    switch (code.length()) {
      case 2:
        country.setAlpha2Code(code);
        break;
      case 3:
        if (!StringHelper.isDigital(code)) {
          country.setAlpha3Code(code);
        } else {
          country.setNumericCode(code);
        }
    }
    country.setAddressTemplate(appBaseService.getDefaultAddressTemplate());
    return countryRepository.save(country);
  }

  @Override
  public Optional<Country> fetchCountry(String country) {
    Country fetchedCountry;
    switch (country.length()) {
      case 2:
        fetchedCountry = countryRepository.findByAlpha2Code(country);
        break;
      case 3:
        if (!StringHelper.isDigital(country)) {
          fetchedCountry = countryRepository.findByAlpha3Code(country);
        } else {
          fetchedCountry = countryRepository.findByNumericCode(country);
        }
        break;
      default:
        fetchedCountry =
            countryRepository
                .all()
                .filter("UPPER(self.name) = :name")
                .bind("name", country.toUpperCase())
                .fetchOne();
    }
    return Optional.ofNullable(fetchedCountry);
  }
}
