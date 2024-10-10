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
