package com.axelor.apps.helpdesk.service;

import com.axelor.apps.helpdesk.db.TicketStatus;
import com.axelor.apps.helpdesk.service.app.AppHelpdeskService;
import com.axelor.db.Query;
import com.axelor.studio.db.AppHelpdesk;
import com.google.inject.Inject;
import java.util.Optional;

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

    TicketStatus resolvedStatus = appHelpdesk.getResolvedTicketStatus();
    TicketStatus closedStatus = appHelpdesk.getClosedTicketStatus();
    TicketStatus inProgressStatus = appHelpdesk.getInProgressTicketStatus();

    return Query.of(TicketStatus.class)
        .filter(
            "self.id != :resolvedStatusId and self.id != :closedStatusId and self.id != :inProgressStatusId")
        .bind(
            "resolvedStatusId",
            Optional.ofNullable(resolvedStatus).map(TicketStatus::getId).orElse(null))
        .bind(
            "closedStatusId",
            Optional.ofNullable(closedStatus).map(TicketStatus::getId).orElse(null))
        .bind(
            "inProgressStatusId",
            Optional.ofNullable(inProgressStatus).map(TicketStatus::getId).orElse(null))
        .order("priority")
        .fetchOne();
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
