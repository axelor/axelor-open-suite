package com.axelor.apps.base.service.address;

import java.util.List;

/** Manage meta fields in addresses */
public interface AddressMetaService {

  /** returns the list of fields used in addresses */
  List<String> getAddressFormFieldsList();
}
