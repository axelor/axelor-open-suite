package com.axelor.apps.event.module;

import com.axelor.app.AxelorModule;

public class EventModule extends AxelorModule {

  @Override
  protected void configure() {

    bind(ConvertCSVFileService.class).to(ConvertCSVFileServiceImpl.class);
    //bind(ImportDemoDataService.class).to(ImportDemoDataServiceImpl.class);
   
  }
}
