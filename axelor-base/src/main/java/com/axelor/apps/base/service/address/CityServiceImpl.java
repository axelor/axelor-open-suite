package com.axelor.apps.base.service.address;

import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.CityRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

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
}
