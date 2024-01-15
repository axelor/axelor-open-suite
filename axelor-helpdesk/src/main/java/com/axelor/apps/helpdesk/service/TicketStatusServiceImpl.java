package com.axelor.apps.helpdesk.service;

import com.axelor.apps.helpdesk.db.TicketStatus;
import com.axelor.apps.helpdesk.service.app.AppHelpdeskService;
import com.axelor.studio.db.AppHelpdesk;
import com.google.inject.Inject;

public class TicketStatusServiceImpl implements TicketStatusService {

  protected AppHelpdeskService appHelpdeskService;
  protected AppHelpdesk appHelpdesk;

  @Inject
  public TicketStatusServiceImpl(AppHelpdeskService appHelpdeskService) {
    this.appHelpdeskService = appHelpdeskService;
    this.appHelpdesk = appHelpdeskService.getHelpdeskApp();
  }

  @Override
  public TicketStatus findDefaultStatus() {
    return appHelpdesk.getDefaultTicketStatus();
  }

  @Override
  public TicketStatus findOngoingStatus() {

    return appHelpdesk.getInProgressTicketStatus();
  }

  @Override
  public TicketStatus findResolvedStatus() {

    return appHelpdesk.getResolvedTicketStatus();
  }

  @Override
  public TicketStatus findClosedStatus() {

    return appHelpdesk.getClosedTicketStatus();
  }
}
