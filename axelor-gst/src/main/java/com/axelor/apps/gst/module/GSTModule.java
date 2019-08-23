package com.axelor.apps.gst.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.gst.service.AddressService;
import com.axelor.apps.gst.service.AddressServiceImpl;
import com.axelor.apps.gst.service.InvoiceLineServiceGST;
import com.axelor.apps.gst.service.InvoiceLineServiceGSTImpl;

public class GSTModule extends AxelorModule {

  @Override
  protected void configure() {
    //    bind(SequenceService.class).to(SequenceServiceImpl.class);
    //    bind(InvoiceLineServiceImpl.class).to(InvoiceLineServiceGSTImpl.class);
    bind(InvoiceLineServiceGST.class).to(InvoiceLineServiceGSTImpl.class);
    bind(AddressService.class).to(AddressServiceImpl.class);
    //    bind(InvoiceService.class).to(InvoiceServiceImpl.class);
  }
}
