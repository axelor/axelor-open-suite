/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.helpdesk.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.base.service.MailServiceBaseImpl;
import com.axelor.apps.helpdesk.db.repo.TicketManagementRepository;
import com.axelor.apps.helpdesk.db.repo.TicketRepository;
import com.axelor.apps.helpdesk.service.MailServiceHelpDeskImpl;
import com.axelor.apps.helpdesk.service.TicketService;
import com.axelor.apps.helpdesk.service.TicketServiceImpl;
import com.axelor.apps.helpdesk.service.TimerTicketService;
import com.axelor.apps.helpdesk.service.TimerTicketServiceImpl;

public class HelpdeskModule extends AxelorModule {

  @Override
  protected void configure() {

    bind(TicketRepository.class).to(TicketManagementRepository.class);
    bind(TicketService.class).to(TicketServiceImpl.class);
    bind(MailServiceBaseImpl.class).to(MailServiceHelpDeskImpl.class);
    bind(TimerTicketService.class).to(TimerTicketServiceImpl.class);
  }
}
