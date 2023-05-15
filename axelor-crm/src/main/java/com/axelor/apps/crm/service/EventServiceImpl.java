/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.DateService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.message.db.EmailAddress;
import com.axelor.message.db.repo.EmailAddressRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EventServiceImpl implements EventService {

  protected PartnerService partnerService;

  protected EventRepository eventRepo;

  protected EmailAddressRepository emailAddressRepo;

  protected PartnerRepository partnerRepo;

  protected LeadRepository leadRepo;

  protected DateService dateService;

  protected OpportunityRepository opportunityRepo;

  @Inject
  public EventServiceImpl(
      PartnerService partnerService,
      EventRepository eventRepo,
      EmailAddressRepository emailAddressRepo,
      PartnerRepository partnerRepo,
      LeadRepository leadRepo,
      DateService dateService,
      OpportunityRepository opportunityRepo) {
    this.partnerService = partnerService;
    this.eventRepo = eventRepo;
    this.emailAddressRepo = emailAddressRepo;
    this.partnerRepo = partnerRepo;
    this.leadRepo = leadRepo;
    this.dateService = dateService;
    this.opportunityRepo = opportunityRepo;
  }

  @Override
  @Transactional
  public void saveEvent(Event event) {
    eventRepo.save(event);
  }

  @Override
  public Event createEvent(
      LocalDateTime fromDateTime,
      LocalDateTime toDateTime,
      User user,
      String description,
      int type,
      String subject) {
    Event event = new Event();
    event.setSubject(subject);
    event.setStartDateTime(fromDateTime);
    event.setEndDateTime(toDateTime);
    event.setUser(user);
    event.setTypeSelect(type);
    if (!Strings.isNullOrEmpty(description)) {
      event.setDescription(description);
    }

    if (fromDateTime != null && toDateTime != null) {
      long duration = Duration.between(fromDateTime, toDateTime).getSeconds();
      event.setDuration(duration);
    }

    return event;
  }

  @Override
  public String getInvoicingAddressFullName(Partner partner) {

    Address address = partnerService.getInvoicingAddress(partner);
    if (address != null) {
      return address.getFullName();
    }

    return null;
  }

  @Override
  public EmailAddress getEmailAddress(Event event) {
    EmailAddress emailAddress = null;
    if (event.getPartner() != null
        && event.getPartner().getPartnerTypeSelect() == PartnerRepository.PARTNER_TYPE_INDIVIDUAL) {

      Partner partner = partnerRepo.find(event.getPartner().getId());
      if (partner.getEmailAddress() != null) {
        emailAddress = emailAddressRepo.find(partner.getEmailAddress().getId());
      }

    } else if (event.getContactPartner() != null) {

      Partner contactPartner = partnerRepo.find(event.getContactPartner().getId());
      if (contactPartner.getEmailAddress() != null) {
        emailAddress = emailAddressRepo.find(contactPartner.getEmailAddress().getId());
      }

    } else if (event.getPartner() == null
        && event.getContactPartner() == null
        && event.getEventLead() != null) {

      Lead lead = leadRepo.find(event.getEventLead().getId());
      if (lead.getEmailAddress() != null) {
        emailAddress = emailAddressRepo.find(lead.getEmailAddress().getId());
      }
    }
    return emailAddress;
  }

  @Override
  public void fillEventDates(Event event) throws AxelorException {
    switch (event.getStatusSelect()) {
      case EventRepository.STATUS_PLANNED:
        afterPlanned(event);
        break;

      case EventRepository.STATUS_REALIZED:
        afterRealized(event);
        break;

      case EventRepository.STATUS_CANCELED:
        afterCanceled(event);
        break;

      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD, I18n.get("Type not selected!"));
    }
  }

  @Override
  public void planEvent(Event event) {
    this.afterPlanned(event);
  }

  protected void afterPlanned(Event event) {
    this.updateLeadScheduledEventDate(event);
    this.updatePartnerScheduledEventDate(event);
    this.updateOpportunityScheduledEventDate(event);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void realizeEvent(Event event) {
    event.setStatusSelect(EventRepository.STATUS_REALIZED);
    this.afterRealized(event);
  }

  protected void afterRealized(Event event) {
    this.updateLeadLastEventDate(event);
    this.updateLeadScheduledEventDateAfterRealized(event);
    this.updatePartnerLastEventDate(event);
    this.updatePartnerScheduledEventDateAfterRealized(event);
    this.updateOpportunityLastEventDate(event);
    this.updateOpportunityScheduledEventDateAfterRealized(event);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancelEvent(Event event) {
    event.setStatusSelect(EventRepository.STATUS_CANCELED);
    this.afterCanceled(event);
  }

  @Transactional
  protected void afterCanceled(Event event) {
    LocalDateTime eventDateTime = event.getEndDateTime();
    Lead lead = event.getEventLead();
    if (lead != null
        && lead.getLastEventDateT() != null
        && lead.getLastEventDateT().equals(eventDateTime)) {
      List<Event> eventList =
          lead.getEventList().stream()
              .filter(
                  e ->
                      e != event
                          && e.getEndDateTime() != null
                          && e.getStatusSelect() == EventRepository.STATUS_REALIZED
                          && !e.getEndDateTime().isAfter(eventDateTime))
              .sorted(Comparator.comparing(Event::getEndDateTime).reversed())
              .collect(Collectors.toList());

      if (!eventList.isEmpty()) {
        lead.setLastEventDateT(eventList.get(0).getEndDateTime());
        leadRepo.save(lead);
      }
    }

    Partner partner = event.getPartner();
    if (partner != null
        && partner.getLastEventDateT() != null
        && partner.getLastEventDateT().equals(eventDateTime)) {
      this.fetchLatestEventEndDateT(
              event, eventRepo.all().filter("self.partner.id = ?", partner.getId()).fetch())
          .ifPresent(partner::setLastEventDateT);
    }

    Opportunity opportunity = event.getOpportunity();
    if (opportunity != null) {
      this.fetchLatestEventEndDateT(
              event, eventRepo.all().filter("self.opportunity.id = ?", opportunity.getId()).fetch())
          .ifPresent(opportunity::setLastEventDateT);
    }
  }

  @Transactional
  protected void updateLeadLastEventDate(Event event) {
    Lead lead = event.getEventLead();
    if (lead != null
        && event.getEndDateTime() != null
        && (lead.getLastEventDateT() == null
            || !lead.getLastEventDateT().isAfter(event.getEndDateTime()))) {
      lead.setLastEventDateT(event.getEndDateTime());
    }
  }

  @Transactional
  protected void updateOpportunityLastEventDate(Event event) {
    Opportunity opportunity = event.getOpportunity();
    if (opportunity != null
        && event.getEndDateTime() != null
        && (opportunity.getLastEventDateT() == null
            || !opportunity.getLastEventDateT().isAfter(event.getEndDateTime()))) {
      opportunity.setLastEventDateT(event.getEndDateTime());
    }
  }

  @Transactional
  protected void updatePartnerLastEventDate(Event event) {
    Partner partner = event.getPartner();
    if (partner != null
        && event.getEndDateTime() != null
        && (partner.getLastEventDateT() == null
            || !partner.getLastEventDateT().isAfter(event.getEndDateTime()))) {
      partner.setLastEventDateT(event.getEndDateTime());
    }
  }

  @Transactional
  protected void updatePartnerScheduledEventDate(Event event) {
    Partner partner = event.getPartner();
    LocalDateTime startDateTime = event.getStartDateTime();
    if (partner != null
        && startDateTime != null
        && event.getStatusSelect() == EventRepository.STATUS_PLANNED
        && (partner.getScheduledEventDateT() == null
            || partner.getScheduledEventDateT().isAfter(startDateTime))) {
      partner.setScheduledEventDateT(startDateTime);
    }
  }

  @Transactional
  protected void updateLeadScheduledEventDate(Event event) {
    Lead lead = event.getEventLead();
    LocalDateTime startDateTime = event.getStartDateTime();
    if (lead != null
        && startDateTime != null
        && event.getStatusSelect() == EventRepository.STATUS_PLANNED
        && (lead.getNextScheduledEventDateT() == null
            || lead.getNextScheduledEventDateT().isAfter(event.getEndDateTime()))) {
      lead.setNextScheduledEventDateT(startDateTime);
    }
  }

  @Transactional
  protected void updateOpportunityScheduledEventDate(Event event) {
    Opportunity opportunity = event.getOpportunity();
    LocalDateTime startDateTime = event.getStartDateTime();
    if (opportunity != null
        && startDateTime != null
        && event.getStatusSelect() == EventRepository.STATUS_PLANNED
        && (opportunity.getNextScheduledEventDateT() == null
            || opportunity.getNextScheduledEventDateT().isAfter(event.getEndDateTime()))) {
      opportunity.setNextScheduledEventDateT(startDateTime);
    }
  }

  @Transactional
  protected void updatePartnerScheduledEventDateAfterRealized(Event event) {
    Partner partner = event.getPartner();
    if (partner != null && event.getStartDateTime() != null) {
      this.fetchNextEventStartDateT(
              event, eventRepo.all().filter("self.partner.id = ?", partner.getId()).fetch())
          .ifPresent(partner::setScheduledEventDateT);
    }
  }

  @Transactional
  protected void updateLeadScheduledEventDateAfterRealized(Event event) {
    Lead lead = event.getEventLead();
    if (lead != null && event.getStartDateTime() != null && !lead.getEventList().isEmpty()) {
      this.fetchNextEventStartDateT(event, lead.getEventList())
          .ifPresent(lead::setNextScheduledEventDateT);
      leadRepo.save(lead);
    }
  }

  @Transactional
  protected void updateOpportunityScheduledEventDateAfterRealized(Event event) {
    Opportunity opportunity = event.getOpportunity();
    if (opportunity != null
        && event.getStartDateTime() != null
        && !opportunity.getEventList().isEmpty()) {
      this.fetchNextEventStartDateT(event, opportunity.getEventList())
          .ifPresent(opportunity::setNextScheduledEventDateT);
      opportunityRepo.save(opportunity);
    }
  }

  protected Optional<LocalDateTime> fetchNextEventStartDateT(Event event, List<Event> eventList) {
    return eventList.stream()
        .filter(
            e ->
                e != event
                    && e.getStartDateTime() != null
                    && e.getStatusSelect() == EventRepository.STATUS_PLANNED
                    && e.getStartDateTime().isAfter(event.getStartDateTime()))
        .min(Comparator.comparing(Event::getStartDateTime))
        .map(Event::getStartDateTime);
  }

  protected Optional<LocalDateTime> fetchLatestEventEndDateT(Event event, List<Event> eventList) {
    Optional<Event> optEvent =
        eventList.stream()
            .filter(
                e ->
                    e != event
                        && e.getEndDateTime() != null
                        && e.getStatusSelect().equals(EventRepository.STATUS_REALIZED)
                        && !e.getEndDateTime().isAfter(event.getEndDateTime()))
            .max(Comparator.comparing(Event::getEndDateTime));

    return optEvent.map(Event::getEndDateTime);
  }
}
