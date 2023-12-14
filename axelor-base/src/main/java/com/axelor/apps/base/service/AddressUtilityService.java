package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Country;
import java.io.IOException;

public interface AddressUtilityService {

  public int export(String path) throws IOException;

  public Address getAddress(
      String room, String floor, String streetName, String postBox, Country country);
}
