package com.axelor.apps.helpdesk.service.app;

import com.axelor.studio.db.AppHelpdesk;
import com.axelor.studio.db.repo.AppHelpdeskRepository;
import com.google.inject.Inject;

public class AppHelpdeskServiceImpl implements AppHelpdeskService {

  protected AppHelpdeskRepository appHelpdeskRepository;

  @Inject
  public AppHelpdeskServiceImpl(AppHelpdeskRepository appHelpdeskRepository) {
    this.appHelpdeskRepository = appHelpdeskRepository;
  }

  @Override
  public AppHelpdesk getHelpdeskApp() {
    return appHelpdeskRepository.all().fetchOne();
  }
}
