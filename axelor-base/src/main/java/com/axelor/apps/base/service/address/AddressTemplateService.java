package com.axelor.apps.base.service.address;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;

public interface AddressTemplateService {

  /**
   * Set formatted full name for the address from address template
   *
   * @param address
   */
  void setFormattedFullName(Address address) throws AxelorException;

  void checkRequiredAddressFields(Address address) throws AxelorException;
}
