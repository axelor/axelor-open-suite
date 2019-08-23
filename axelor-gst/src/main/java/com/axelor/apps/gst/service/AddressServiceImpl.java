package com.axelor.apps.gst.service;

import com.axelor.apps.base.db.Address;

public class AddressServiceImpl implements AddressService {
  @Override
  public boolean checkAddressStateForInvoice(
      Address companyAddress,
      Address invoiceAddress,
      Address shippingAddress,
      Boolean isUseInvoiceAddressAsShiping) {

    if (!isUseInvoiceAddressAsShiping) {
      invoiceAddress = shippingAddress;
    }

    if (companyAddress.getState() != null
        && invoiceAddress.getState() != null
        && companyAddress.getState().equals(invoiceAddress.getState())) {
      return true;
    } else {
      return false;
    }
  }
}
