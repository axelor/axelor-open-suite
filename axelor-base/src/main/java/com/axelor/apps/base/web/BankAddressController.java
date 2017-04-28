package com.axelor.apps.base.web;

import com.axelor.apps.base.db.BankAddress;
import com.axelor.apps.base.db.repo.BankAddressRepository;
import com.axelor.apps.base.service.BankAddressService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class BankAddressController {
    @Inject
    BankAddressRepository bankAddressRepository;

    @Inject
    BankAddressService bankAddressService;

    public void fillFullName(ActionRequest request, ActionResponse response) {
          BankAddress bankAddress = request.getContext().asType(BankAddress.class);
          if (bankAddress.getAddress() != null) {
              String fullAddress = bankAddressService.computeFullAddress(bankAddress);
              response.setValue("fullAddress", fullAddress);
          }

    }
}
