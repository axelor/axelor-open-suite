package com.axelor.apps.base.service.address;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Country;
import java.util.Optional;

public interface CountryService {
  Country createAndSaveCountry(String name, String code) throws AxelorException;

  Optional<Country> fetchCountry(String country);
}
