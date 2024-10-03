package com.axelor.apps.base.service.address;

import com.axelor.apps.base.db.Country;

public interface CountryService {
  Country createAndSaveCountry(String name, String code);
}
