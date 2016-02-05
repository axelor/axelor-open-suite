/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.base.ical.ICalendarService;
import com.axelor.apps.base.service.message.MessageServiceBaseImpl;
import com.axelor.apps.crm.db.repo.EventManagementRepository;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.db.repo.OpportunityManagementRepository;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.crm.message.MessageServiceCrmImpl;
import com.axelor.apps.crm.service.CalendarService;
import com.axelor.apps.crm.service.OpportunityService;
import com.axelor.apps.crm.service.OpportunityServiceImpl;


public class CrmModule extends AxelorModule {

    @Override
    protected void configure() {
        bind(MessageServiceBaseImpl.class).to(MessageServiceCrmImpl.class);
        bind(EventRepository.class).to(EventManagementRepository.class);
        bind(OpportunityRepository.class).to(OpportunityManagementRepository.class);
        bind(OpportunityService.class).to(OpportunityServiceImpl.class);
        bind(ICalendarService.class).to(CalendarService.class);
    }
}