package com.axelor.apps.base.service.address;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;
import java.util.Optional;

public interface CityService {
  City createAndSaveCity(String name, String zip, Country country);

  Optional<City> fetchCity(Country country, String city, String zip) throws AxelorException;
}
