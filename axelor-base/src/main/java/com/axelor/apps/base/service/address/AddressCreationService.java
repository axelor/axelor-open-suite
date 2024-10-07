package com.axelor.apps.base.service.address;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;

public interface AddressCreationService {

  public Address createAddress(
      String room,
      String floor,
      String streetName,
      String postBox,
      String zip,
      City city,
      Country country);

  public Address createAndSaveAddress(Country country, City city, String zip, String streetName)
      throws AxelorException;

  /**
   * Auto-completes some fields of the address thanks to the input zip.
   *
   * @param address
   */
  public void autocompleteAddress(Address address);

  Address fetchAddress(Country country, City city, String zip, String streetName)
      throws AxelorException;
}
