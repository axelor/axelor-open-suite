package com.axelor.apps.base.service.address;

import java.util.List;

public class AddressMetaServiceImpl implements AddressMetaService {

  @Override
  public List<String> getAddressFormFieldsList() {

    return List.of(
        "department",
        "subDepartment",
        "buildingName",
        "townName",
        "townLocationName",
        "districtName",
        "countrySubDivision",
        "room",
        "floor",
        "buildingNumber",
        "streetName",
        "postBox",
        "city",
        "zip");
  }
}
