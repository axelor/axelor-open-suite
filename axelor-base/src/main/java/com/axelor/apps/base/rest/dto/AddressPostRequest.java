package com.axelor.apps.base.rest.dto;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.address.CityService;
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
        Optional<City> cityWithCorrectZip =
            cityList.stream().filter(c -> zip.equals(c.getZip())).findFirst();
        return cityWithCorrectZip.orElseGet(
            () -> cityList.stream().filter(c -> c.getZip() == null).findFirst().orElse(null));
      }
    }
  }

  public Optional<Address> fetchAddress() throws AxelorException {
    City fetchedCity = fetchCity();
    if (fetchedCity != null) {
      return fetchAddressWithFoundCity(fetchCountry(), fetchedCity);
    } else if (zip != null) {
      Optional<Address> address = fetchAddressWithZip(fetchCountry());
      if (address.isPresent()) {
        return address;
      }
    }
    if (city != null) {
      Beans.get(CityService.class).createAndSaveCity(city, zip, fetchCountry());
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.NO_ADDRESS_FOUND_WITH_INFO));
    }
    return Optional.empty();
  }

  protected Optional<Address> fetchAddressWithFoundCity(Country country, City city) {
    if (zip != null) {
      return Beans.get(AddressRepository.class)
          .all()
          .filter(
              "self.country = :country AND self.zip = :zip AND self.city = :city AND UPPER(self.streetName) = :streetName")
          .bind("country", country)
          .bind("zip", zip)
          .bind("city", city)
          .bind("streetName", streetName.toUpperCase())
          .fetchStream()
          .findFirst();
    } else {
      return Beans.get(AddressRepository.class)
          .all()
          .filter(
              "self.country = :country AND self.city = :city AND UPPER(self.streetName) = :streetName")
          .bind("country", country)
          .bind("city", city)
          .bind("streetName", streetName.toUpperCase())
          .fetchStream()
          .findFirst();
    }
  }

  protected Optional<Address> fetchAddressWithZip(Country country) {
    return Beans.get(AddressRepository.class)
        .all()
        .filter(
            "self.country = :country AND self.zip = :zip AND UPPER(self.streetName) = :streetName")
        .bind("country", country)
        .bind("zip", zip)
        .bind("streetName", streetName.toUpperCase())
        .fetchStream()
        .findFirst();
  }
}
