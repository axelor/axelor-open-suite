package com.axelor.apps.base.rest.dto;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.address.AddressService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.RequestPostStructure;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.persist.Transactional;
import java.util.Optional;
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

  public Country fetchCountry() throws AxelorException {
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
    if (fetchedCountry != null) {
      return fetchedCountry;
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_INCONSISTENCY,
        String.format(I18n.get(BaseExceptionMessage.NO_COUNTRY_FOUND), country));
  }

  public City fetchCity() throws AxelorException {
    if (city == null && zip == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.NO_CITY_AND_ZIP_FOUND));
    } else if (city == null) {
      return Beans.get(CityRepository.class).findByZipAndCountry(zip, fetchCountry()).fetchOne();
    } else if (zip == null) {
      return Beans.get(CityRepository.class)
          .all()
          .filter("self.country = :country AND UPPER(self.name) = :name")
          .bind("country", fetchCountry())
          .bind("name", city.toUpperCase())
          .fetchOne();
    } else {
      return Beans.get(CityRepository.class)
          .all()
          .filter("self.zip = :zip AND self.country = :country AND UPPER(self.name) = :name")
          .bind("zip", zip)
          .bind("country", fetchCountry())
          .bind("name", city.toUpperCase())
          .fetchOne();
    }
  }

  public Address fetchAddress() throws AxelorException {
    City fetchedCity = fetchCity();
    if (fetchedCity != null) {
      return Beans.get(AddressRepository.class)
          .all()
          .filter(
              "self.country = :country AND self.city = :city AND UPPER(self.streetName) = :streetName")
          .bind("country", fetchCountry())
          .bind("city", fetchCity())
          .bind("streetName", streetName.toUpperCase())
          .fetchOne();
    } else if (zip != null) {
      return Optional.ofNullable(
              Beans.get(AddressRepository.class)
                  .all()
                  .filter(
                      "self.country = :country AND self.zip = :zip AND UPPER(self.streetName) = :streetName")
                  .bind("country", fetchCountry())
                  .bind("zip", zip)
                  .bind("streetName", streetName.toUpperCase())
                  .fetchOne())
          .orElseThrow(
              () ->
                  new AxelorException(
                      TraceBackRepository.CATEGORY_INCONSISTENCY,
                      I18n.get(BaseExceptionMessage.NO_CITY_FOUND)));
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(BaseExceptionMessage.NO_CITY_FOUND));
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public Address createAddress() throws AxelorException {
    return Beans.get(AddressRepository.class)
        .save(
            Beans.get(AddressService.class)
                .createAddress(null, null, streetName, null, zip, fetchCity(), fetchCountry()));
  }
}
