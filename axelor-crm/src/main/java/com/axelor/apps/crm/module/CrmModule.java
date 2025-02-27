/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.base.db.repo.ICalendarRepository;
import com.axelor.apps.base.ical.ICalendarEventFactory;
import com.axelor.apps.base.ical.ICalendarService;
import com.axelor.apps.base.service.message.MessageServiceBaseImpl;
import com.axelor.apps.base.service.partner.api.PartnerGenerateServiceImpl;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.repo.CrmBatchCrmRepository;
import com.axelor.apps.crm.db.repo.CrmBatchRepository;
import com.axelor.apps.crm.db.repo.EventManagementRepository;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.db.repo.LeadManagementRepository;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.crm.db.repo.OpportunityManagementRepository;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.crm.db.repo.TourManagementRepository;
import com.axelor.apps.crm.db.repo.TourRepository;
import com.axelor.apps.crm.message.MessageServiceCrmImpl;
import com.axelor.apps.crm.service.CalendarService;
import com.axelor.apps.crm.service.CatalogService;
import com.axelor.apps.crm.service.CatalogServiceImpl;
import com.axelor.apps.crm.service.ConvertLeadWizardService;
import com.axelor.apps.crm.service.ConvertLeadWizardServiceImpl;
import com.axelor.apps.crm.service.ConvertWizardOpportunityService;
import com.axelor.apps.crm.service.ConvertWizardOpportunityServiceImpl;
import com.axelor.apps.crm.service.CrmActivityService;
import com.axelor.apps.crm.service.CrmActivityServiceImpl;
import com.axelor.apps.crm.service.CrmReportingService;
import com.axelor.apps.crm.service.CrmReportingServiceImpl;
import com.axelor.apps.crm.service.EventService;
import com.axelor.apps.crm.service.EventServiceImpl;
import com.axelor.apps.crm.service.LeadComputeNameService;
import com.axelor.apps.crm.service.LeadComputeNameServiceImpl;
import com.axelor.apps.crm.service.LeadDuplicateService;
import com.axelor.apps.crm.service.LeadDuplicateServiceImpl;
import com.axelor.apps.crm.service.LeadService;
import com.axelor.apps.crm.service.LeadServiceImpl;
import com.axelor.apps.crm.service.OpportunitySequenceService;
import com.axelor.apps.crm.service.OpportunitySequenceServiceImpl;
import com.axelor.apps.crm.service.OpportunityService;
import com.axelor.apps.crm.service.OpportunityServiceImpl;
import com.axelor.apps.crm.service.PartnerCrmService;
import com.axelor.apps.crm.service.PartnerCrmServiceImpl;
import com.axelor.apps.crm.service.PartnerEmailDomainToolService;
import com.axelor.apps.crm.service.PartnerEmailDomainToolServiceImpl;
import com.axelor.apps.crm.service.TourLineService;
import com.axelor.apps.crm.service.TourLineServiceImpl;
import com.axelor.apps.crm.service.TourService;
import com.axelor.apps.crm.service.TourServiceImpl;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.apps.crm.service.app.AppCrmServiceImpl;
import com.axelor.apps.crm.service.partner.api.CrmPartnerGenerateServiceImpl;

public class CrmModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(EventRepository.class).to(EventManagementRepository.class);
    bind(LeadRepository.class).to(LeadManagementRepository.class);
    bind(OpportunityRepository.class).to(OpportunityManagementRepository.class);
    bind(OpportunityService.class).to(OpportunityServiceImpl.class);
    bind(ICalendarService.class).to(CalendarService.class);
    bind(AppCrmService.class).to(AppCrmServiceImpl.class);
    bind(EventService.class).to(EventServiceImpl.class);
    bind(CrmBatchRepository.class).to(CrmBatchCrmRepository.class);
    bind(LeadService.class).to(LeadServiceImpl.class);
    ICalendarEventFactory.register(ICalendarRepository.CRM_SYNCHRO, Event::new);
    bind(MessageServiceBaseImpl.class).to(MessageServiceCrmImpl.class);
    bind(CrmReportingService.class).to(CrmReportingServiceImpl.class);
    bind(ConvertLeadWizardService.class).to(ConvertLeadWizardServiceImpl.class);
    bind(ConvertWizardOpportunityService.class).to(ConvertWizardOpportunityServiceImpl.class);
    bind(PartnerCrmService.class).to(PartnerCrmServiceImpl.class);
    bind(CatalogService.class).to(CatalogServiceImpl.class);
    bind(PartnerEmailDomainToolService.class).to(PartnerEmailDomainToolServiceImpl.class);
    bind(CrmActivityService.class).to(CrmActivityServiceImpl.class);
    bind(LeadDuplicateService.class).to(LeadDuplicateServiceImpl.class);
    bind(TourService.class).to(TourServiceImpl.class);
    bind(TourLineService.class).to(TourLineServiceImpl.class);
    bind(TourRepository.class).to(TourManagementRepository.class);
    bind(LeadComputeNameService.class).to(LeadComputeNameServiceImpl.class);
    bind(OpportunitySequenceService.class).to(OpportunitySequenceServiceImpl.class);
    bind(PartnerGenerateServiceImpl.class).to(CrmPartnerGenerateServiceImpl.class);
  }
}
