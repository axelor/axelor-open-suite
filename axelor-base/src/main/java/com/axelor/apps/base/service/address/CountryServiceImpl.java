package com.axelor.apps.base.service.address;

import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.AddressTemplateRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class CountryServiceImpl implements CountryService {

  protected final CountryRepository countryRepository;
  protected final AddressTemplateRepository addressTemplateRepository;

  @Inject
  public CountryServiceImpl(
      CountryRepository countryRepository, AddressTemplateRepository addressTemplateRepository) {
    this.countryRepository = countryRepository;
    this.addressTemplateRepository = addressTemplateRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Country createAndSaveCountry(String name, String code) {
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
    country.setAddressTemplate(addressTemplateRepository.findByName("DEFAULT"));
    return countryRepository.save(country);
  }
}
