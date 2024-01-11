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
package com.axelor.apps.crm.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.LeadStatus;
import com.axelor.apps.crm.db.LostReason;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.crm.db.repo.LeadStatusRepository;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.message.db.repo.MultiRelatedRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class LeadServiceImpl implements LeadService {

  protected SequenceService sequenceService;
  protected UserService userService;
  protected PartnerRepository partnerRepo;
  protected LeadRepository leadRepo;
  protected EventRepository eventRepo;
  protected MultiRelatedRepository multiRelatedRepository;
  protected LeadStatusRepository leadStatusRepo;
  protected AppCrmService appCrmService;

  @Inject
  public LeadServiceImpl(
      SequenceService sequenceService,
      UserService userService,
      PartnerRepository partnerRepo,
      LeadRepository leadRepo,
      EventRepository eventRepo,
      MultiRelatedRepository multiRelatedRepository,
      LeadStatusRepository leadStatusRepo,
      AppCrmService appCrmService) {
    this.sequenceService = sequenceService;
    this.userService = userService;
    this.partnerRepo = partnerRepo;
    this.leadRepo = leadRepo;
    this.eventRepo = eventRepo;
    this.multiRelatedRepository = multiRelatedRepository;
    this.leadStatusRepo = leadStatusRepo;
    this.appCrmService = appCrmService;
  }

  /**
   * Get sequence for partner
   *
   * @return
   * @throws AxelorException
   */
  public String getSequence() throws AxelorException {

    String seq =
        sequenceService.getSequenceNumber(SequenceRepository.PARTNER, Partner.class, "partnerSeq");
    if (seq == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.PARTNER_1));
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
        "linkedin",
        "<a class='fa fa-linkedin' href='http://www.linkedin.com/pub/dir/"
            + searchName.replace("+", "/")
            + "' target='_blank' />");
    if (companyName != null) {
      urlMap.put(
          "google",
          "<a class='fa fa-google' href='https://www.google.com/search?q="
              + companyName
              + "+"
              + searchName
              + "&gws_rd=cr"
              + "' target='_blank' />");
    } else {
      urlMap.put(
          "google",
          "<a class='fa fa-google' href='https://www.google.com/search?q="
              + searchName
              + "&gws_rd=cr"
              + "' target='_blank' />");
    }
    return urlMap;
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

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void assignToMeLead(Lead lead) throws AxelorException {
    LeadStatus leadStatus = lead.getLeadStatus();

    LeadStatus lostLeadStatus = appCrmService.getLostLeadStatus();

    if (leadStatus == null || leadStatus.equals(lostLeadStatus)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(CrmExceptionMessage.LEAD_ASSIGN_TO_ME_WRONG_STATUS));
    }
    lead.setUser(AuthUtils.getUser());
    leadRepo.save(lead);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void assignToMeMultipleLead(List<Lead> leadList) throws AxelorException {
    for (Lead lead : leadList) {
      assignToMeLead(lead);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void loseLead(Lead lead, LostReason lostReason, String lostReasonStr)
      throws AxelorException {
    LeadStatus leadStatus = lead.getLeadStatus();

    LeadStatus lostLeadStatus = appCrmService.getLostLeadStatus();

    if (leadStatus == null || leadStatus.equals(lostLeadStatus)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(CrmExceptionMessage.LEAD_LOSE_WRONG_STATUS));
    }
    lead.setLeadStatus(lostLeadStatus);
    lead.setLostReason(lostReason);
    lead.setLostReasonStr(lostReasonStr);
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

  @Override
  public LeadStatus getDefaultLeadStatus() {
    return leadStatusRepo.getDefaultStatus();
  }
}
