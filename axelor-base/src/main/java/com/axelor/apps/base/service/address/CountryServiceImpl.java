package com.axelor.apps.base.service.address;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.inject.Beans;
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
        fetchedCountry = Beans.get(CountryRepository.class).findByAlpha2Code(country);
        break;
      case 3:
        if (!StringHelper.isDigital(country)) {
          fetchedCountry = Beans.get(CountryRepository.class).findByAlpha3Code(country);
        } else {
          fetchedCountry = Beans.get(CountryRepository.class).findByNumericCode(country);
        }
        break;
      default:
        fetchedCountry =
            Beans.get(CountryRepository.class)
                .all()
                .filter("UPPER(self.name) = :name")
                .bind("name", country.toUpperCase())
                .fetchOne();
    }
    return Optional.ofNullable(fetchedCountry);
  }
}
