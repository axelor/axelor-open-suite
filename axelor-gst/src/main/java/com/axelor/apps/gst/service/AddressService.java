package com.axelor.apps.gst.service;

import com.axelor.apps.base.db.Address;

public interface AddressService {

  boolean checkAddressStateForInvoice(
      Address companyAddress,
      Address invoiceAddress,
      Address shippingAddress,
      Boolean isUseInvoiceAddressAsShiping);
}
