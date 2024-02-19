/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
