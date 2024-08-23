package com.axelor.apps.base.service.address;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import java.io.UnsupportedEncodingException;

public interface AddressValidationService {
  boolean validateAddressByAdresseDataGouvFr(Address address)
      throws AxelorException, UnsupportedEncodingException;

  boolean validateAddressByNominatim(Address address) throws AxelorException;
}
