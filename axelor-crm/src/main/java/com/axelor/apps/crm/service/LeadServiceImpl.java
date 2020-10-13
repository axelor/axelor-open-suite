/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.LostReason;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class LeadServiceImpl implements LeadService {

  @Inject protected SequenceService sequenceService;

  @Inject protected UserService userService;

  @Inject protected PartnerRepository partnerRepo;

  @Inject protected OpportunityRepository opportunityRepo;

  @Inject protected LeadRepository leadRepo;

  @Inject protected EventRepository eventRepo;

  /**
   * Convert lead into a partner
   *
   * @param lead
   * @return
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Lead convertLead(Lead lead, Partner partner, Partner contactPartner)
      throws AxelorException {

    if (partner != null && contactPartner != null) {
      contactPartner = partnerRepo.save(contactPartner);
      if (partner.getContactPartnerSet() == null) {
        partner.setContactPartnerSet(new HashSet<>());
      }
      partner.getContactPartnerSet().add(contactPartner);
      contactPartner.setMainPartner(partner);
    }
    if (partner != null) {
      partner = partnerRepo.save(partner);
      lead.setPartner(partner);
    }

    for (Event event : lead.getEventList()) {
      event.setPartner(partner);
      event.setContactPartner(contactPartner);
      eventRepo.save(event);
    }

    for (Opportunity opportunity : lead.getOpportunitiesList()) {
      opportunity.setPartner(partner);
      opportunityRepo.save(opportunity);
    }

    lead.setStatusSelect(LeadRepository.LEAD_STATUS_CONVERTED);

    return leadRepo.save(lead);
  }

  /**
   * Get sequence for partner
   *
   * @return
   * @throws AxelorException
   */
  public String getSequence() throws AxelorException {

    String seq = sequenceService.getSequenceNumber(SequenceRepository.PARTNER);
    if (seq == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(IExceptionMessage.PARTNER_1));
    }
    return seq;
  }

  /**
   * Assign user company to partner
   *
   * @param partner
   * @return
   */
  public Partner setPartnerCompany(Partner partner) {

    if (userService.getUserActiveCompany() != null) {
      partner.setCompanySet(new HashSet<Company>());
      partner.getCompanySet().add(userService.getUserActiveCompany());
    }

    return partner;
  }

  public Map<String, String> getSocialNetworkUrl(
      String name, String firstName, String companyName) {

    Map<String, String> urlMap = new HashMap<String, String>();
    String searchName =
        firstName != null && name != null
            ? firstName + "+" + name
            : name == null ? firstName : name;
    searchName = searchName == null ? "" : searchName;
    urlMap.put(
        "facebook",
        "<a class='fa fa-facebook' href='https://www.facebook.com/search/more/?q="
            + searchName
            + "&init=public"
            + "' target='_blank'/>");
    urlMap.put(
        "twitter",
        "<a class='fa fa-twitter' href='https://twitter.com/search?q="
            + searchName
            + "' target='_blank' />");
    urlMap.put(
        "linkedin",
        "<a class='fa fa-linkedin' href='http://www.linkedin.com/pub/dir/"
            + searchName.replace("+", "/")
            + "' target='_blank' />");
    if (companyName != null) {
      urlMap.put(
          "youtube",
          "<a class='fa fa-youtube' href='https://www.youtube.com/results?search_query="
              + companyName
              + "' target='_blank' />");
      urlMap.put(
          "google",
          "<a class='fa fa-google-plus' href='https://www.google.com/?gws_rd=cr#q="
              + companyName
              + "+"
              + searchName
              + "' target='_blank' />");
    } else {
      urlMap.put(
          "youtube",
          "<a class='fa fa-youtube' href='https://www.youtube.com/results?search_query="
              + searchName
              + "' target='_blank' />");
      urlMap.put(
          "google",
          "<a class='fa fa-google-plus' href='https://www.google.com/?gws_rd=cr#q="
              + searchName
              + "' target='_blank' />");
    }
    return urlMap;
  }

  @Transactional
  public void saveLead(Lead lead) {
    leadRepo.save(lead);
  }

  @SuppressWarnings("rawtypes")
  public Object importLead(Object bean, Map values) {

    assert bean instanceof Lead;
    Lead lead = (Lead) bean;
    User user = AuthUtils.getUser();
    lead.setUser(user);
    lead.setTeam(user.getActiveTeam());
    return lead;
  }

  /**
   * Check if the lead in view has a duplicate.
   *
   * @param lead a context lead object
   * @return if there is a duplicate lead
   */
  public boolean isThereDuplicateLead(Lead lead) {
    String newName = lead.getFullName();
    if (Strings.isNullOrEmpty(newName)) {
      return false;
    }
    Long leadId = lead.getId();
    if (leadId == null) {
      Lead existingLead =
          leadRepo
              .all()
              .filter("lower(self.fullName) = lower(:newName) ")
              .bind("newName", newName)
              .fetchOne();
      return existingLead != null;
    } else {
      Lead existingLead =
          leadRepo
              .all()
              .filter("lower(self.fullName) = lower(:newName) " + "and self.id != :leadId ")
              .bind("newName", newName)
              .bind("leadId", leadId)
              .fetchOne();
      return existingLead != null;
    }
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void loseLead(Lead lead, LostReason lostReason) {
    lead.setStatusSelect(LeadRepository.LEAD_STATUS_LOST);
    lead.setLostReason(lostReason);
  }

  public String processFullName(String enterpriseName, String name, String firstName) {
    StringBuilder fullName = new StringBuilder();

    if (!Strings.isNullOrEmpty(enterpriseName)) {
      fullName.append(enterpriseName);
      if (!Strings.isNullOrEmpty(name) || !Strings.isNullOrEmpty(firstName)) fullName.append(", ");
    }
    if (!Strings.isNullOrEmpty(name) && !Strings.isNullOrEmpty(firstName)) {
      fullName.append(firstName);
      fullName.append(" ");
      fullName.append(name);
    } else if (!Strings.isNullOrEmpty(firstName)) fullName.append(firstName);
    else if (!Strings.isNullOrEmpty(name)) fullName.append(name);

    return fullName.toString();
  }
}
