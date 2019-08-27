package com.axelor.apps.gst.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.apps.gst.service.AddressService;
import com.axelor.apps.gst.service.AddressServiceImpl;
import com.axelor.apps.gst.service.InvoiceLineServiceGST;
import com.axelor.apps.gst.service.InvoiceLineServiceGSTImpl;
import com.axelor.apps.gst.service.InvoiceService;
import com.axelor.apps.gst.service.InvoiceServiceGSTImpl;
import com.axelor.apps.gst.service.InvoiceServiceImpl;
import com.axelor.apps.supplychain.service.InvoiceLineSupplychainService;

public class GSTModule extends AxelorModule {

  @Override
  protected void configure() {

    bind(InvoiceLineServiceGST.class).to(InvoiceLineServiceGSTImpl.class);
    bind(AddressService.class).to(AddressServiceImpl.class);
    bind(InvoiceService.class).to(InvoiceServiceImpl.class);
    bind(InvoiceLineSupplychainService.class).to(InvoiceLineServiceGSTImpl.class);
    bind(InvoiceServiceProjectImpl.class).to(InvoiceServiceGSTImpl.class);
  }
}
