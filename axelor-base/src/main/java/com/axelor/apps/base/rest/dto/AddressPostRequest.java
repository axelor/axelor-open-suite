package com.axelor.apps.base.rest.dto;

import com.axelor.utils.api.RequestPostStructure;
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
}
