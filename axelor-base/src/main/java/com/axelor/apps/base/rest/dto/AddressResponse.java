package com.axelor.apps.base.rest.dto;

import com.axelor.apps.base.db.Address;
import com.axelor.utils.api.ResponseStructure;

public class AddressResponse extends ResponseStructure {

  private final long id;
  private final long countyId;
  private final long cityId;
  private final String zip;
  private final String streetName;

  public AddressResponse(Address address) {
    super(address.getVersion());
    this.id = address.getId();
    this.countyId = address.getCountry().getId();
    this.cityId = address.getCity().getId();
    this.zip = address.getZip();
    this.streetName = address.getStreetName();
  }

  public long getId() {
    return id;
  }

  public long getCountyId() {
    return countyId;
  }

  public long getCityId() {
    return cityId;
  }

  public String getZip() {
    return zip;
  }

  public String getStreetName() {
    return streetName;
  }
}
