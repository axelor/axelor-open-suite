package com.axelor.apps.businesssupport.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.businessproject.service.TeamTaskBusinessProjectServiceImpl;
import com.axelor.apps.businesssupport.db.repo.TeamTaskBusinessSupportRepository;
import com.axelor.apps.businesssupport.service.TeamTaskBusinessSupportServiceImpl;
import com.axelor.apps.hr.db.repo.TeamTaskHRRepository;

public class BusinessSupportModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(TeamTaskBusinessProjectServiceImpl.class).to(TeamTaskBusinessSupportServiceImpl.class);
    bind(TeamTaskHRRepository.class).to(TeamTaskBusinessSupportRepository.class);
  }
}
