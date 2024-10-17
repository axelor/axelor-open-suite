package com.axelor.apps.base.rest.dto;

import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.NotBlank;

public class AddressPostRequest extends RequestPostStructure {

  @NotBlank private String country;
  private String city;
  private String zip;
  @NotBlank private String streetName;

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getCity() {
    if (city == null || city.isBlank()) {
      return null;
    }
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getZip() {
    if (zip == null || zip.isBlank()) {
      return null;
    }
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
}
