/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.repo.LeadManagementRepository;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class OpportunityServiceImpl implements OpportunityService {

  @Inject protected OpportunityRepository opportunityRepo;

  @Inject protected AddressService addressService;

  @Transactional
  public void saveOpportunity(Opportunity opportunity) {
    opportunityRepo.save(opportunity);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Partner createClientFromLead(Opportunity opportunity) throws AxelorException {
    Lead lead = opportunity.getLead();
    if (lead == null) {
      throw new AxelorException(
          opportunity,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(CrmExceptionMessage.LEAD_PARTNER));
    }

    String name = lead.getFullName();

    Address address = null;
    if (lead.getPrimaryAddress() != null) {
      // avoids printing 'null'
      String addressL6 =
          lead.getPrimaryPostalCode() == null ? "" : lead.getPrimaryPostalCode() + " ";
      addressL6 += lead.getPrimaryCity() == null ? "" : lead.getPrimaryCity().getName();

      address =
          addressService.createAddress(
              null, null, lead.getPrimaryAddress(), null, addressL6, lead.getPrimaryCountry());
      address.setFullName(addressService.computeFullName(address));
    }

    EmailAddress email = null;
    if (lead.getEmailAddress() != null) {
      email = new EmailAddress(lead.getEmailAddress().getAddress());
    }

    Partner partner =
        Beans.get(PartnerService.class)
            .createPartner(
                name,
                null,
                lead.getFixedPhone(),
                lead.getMobilePhone(),
                email,
                opportunity.getCurrency(),
                address,
                address,
                true);

    opportunity.setPartner(partner);
    opportunityRepo.save(opportunity);

    return partner;
  }

  @Override
  public void setSequence(Opportunity opportunity) throws AxelorException {
    Company company = opportunity.getCompany();
    String seq =
        Beans.get(SequenceService.class)
            .getSequenceNumber(
                SequenceRepository.OPPORTUNITY, company, Opportunity.class, "opportunitySeq");
    if (seq == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(CrmExceptionMessage.OPPORTUNITY_1),
          company != null ? company.getName() : null);
    }
    opportunity.setOpportunitySeq(seq);
  }

  @Override
  public String computeAndGetName(Opportunity opportunity) {
    Lead lead = opportunity.getLead();
    Partner partner = opportunity.getPartner();
    Partner contact = opportunity.getContact();
    if (partner != null) {
      return partner.getFullName();

    } else if (lead != null) {
      if (!Strings.isNullOrEmpty(lead.getEnterpriseName())) {
        return lead.getEnterpriseName();
      }
      return lead.getFullName();

    } else if (contact != null) {
      return contact.getFullName();
    }
    return null;
  }

  public void closeLead(Opportunity opportunity) {

    if (opportunity.getLead() == null) {
      return;
    }

    Lead lead = opportunity.getLead();
    List<Opportunity> opportunities = lead.getOpportunitiesList();
    if (opportunities.size() == 1) {
      if (opportunity.getSalesStageSelect() == OpportunityRepository.SALES_STAGE_CLOSED_LOST) {
        setLeadStatus(lead, LeadRepository.LEAD_STATUS_CLOSED);
      }
    } else {
      if (opportunities.stream()
          .allMatch(
              opp ->
                  opp.getSalesStageSelect() == OpportunityRepository.SALES_STAGE_CLOSED_LOST
                      || opp.getSalesStageSelect()
                          == OpportunityRepository.SALES_STAGE_CLOSED_WON)) {
        setLeadStatus(lead, LeadRepository.LEAD_STATUS_CLOSED);
      } else {
        setLeadStatus(lead, LeadRepository.LEAD_STATUS_IN_PROCESS);
      }
    }
  }

  @Transactional
  public void setLeadStatus(Lead lead, Integer status) {

    lead.setStatusSelect(status);
    Beans.get(LeadManagementRepository.class).save(lead);
  }
}
