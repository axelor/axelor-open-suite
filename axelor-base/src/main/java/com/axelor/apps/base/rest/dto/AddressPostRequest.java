package com.axelor.apps.base.rest.dto;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.RequestPostStructure;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class AddressPostRequest extends RequestPostStructure {

  @NotNull @NotEmpty private String country;
  private String city;
  private String zip;
  @NotNull @NotEmpty private String streetName;

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getZip() {
    return zip;
  }

  public void setZip(String zip) {
    this.zip = zip;
  }

  public String getStreetName() {
    return streetName;
  }

  public void setStreetName(String streetName) {
    this.streetName = streetName;
  }

  public City fetchCity(Country country) throws AxelorException {
    if (city == null && zip == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.CITY_AND_ZIP_BOTH_EMPTY));
    } else if (city == null) {
      return Beans.get(CityRepository.class).findByZipAndCountry(zip, country).fetchOne();
    } else if (zip == null) {
      return Beans.get(CityRepository.class)
          .all()
          .filter("self.country = :country AND UPPER(self.name) = :name")
          .bind("country", country)
          .bind("name", city.toUpperCase())
          .fetchOne();
    } else {
      List<City> cityList =
          Beans.get(CityRepository.class)
              .all()
              .filter("self.country = :country AND UPPER(self.name) = :name")
              .bind("country", country)
              .bind("name", city.toUpperCase())
              .fetch();
      if (cityList.size() == 1) {
        return cityList.get(0);
      } else {
        return cityList.stream().filter(c -> zip.equals(c.getZip())).findFirst().orElse(null);
      }
    }
  }
}
