package com.axelor.apps.gst.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.gst.service.invoice.InvoiceGstServiceImpl;
import com.axelor.apps.gst.service.invoice.InvoiceLineGstService;
import com.axelor.apps.gst.service.invoice.InvoiceLineGstServiceImpl;

public class GstModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(InvoiceLineGstService.class).to(InvoiceLineGstServiceImpl.class);
    bind(InvoiceGstServiceImpl.class);
  }
}
