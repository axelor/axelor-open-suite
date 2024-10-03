package com.axelor.apps.base.service.address;

import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;

public interface CityService {
  City createAndSaveCity(String name, String zip, Country country);
}
