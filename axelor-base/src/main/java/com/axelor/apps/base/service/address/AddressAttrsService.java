package com.axelor.apps.base.service.address;

import com.axelor.apps.base.db.Address;
import java.util.Map;

public interface AddressAttrsService {

  Map<String, Map<String, Object>> getCountryAddressMetaFieldOnChangeAttrsMap(Address address);
}
