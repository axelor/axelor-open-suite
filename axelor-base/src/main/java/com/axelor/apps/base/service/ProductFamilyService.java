package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ProductFamily;

public class ProductFamilyService {
  public void avoidCompanyDuplicates(ProductFamily family) throws Exception {
    if (family.getAccountManagementList().isEmpty()
        || family.getAccountManagementList().size() == 1) {
      return;
    }
    int listSize = family.getAccountManagementList().size();

    for (int i = 0; i < listSize - 1; i++) {
      if (family
          .getAccountManagementList()
          .get(i)
          .getCompany()
          .equals(family.getAccountManagementList().get(listSize - 1).getCompany())) {
        family.getAccountManagementList().remove(listSize - 1);
        throw new Exception(
            "You can't add an Account Management which has the same Company than a previous one.");
      }
    }
  }
}
