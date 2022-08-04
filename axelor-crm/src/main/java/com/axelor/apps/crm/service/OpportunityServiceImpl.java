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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.OpportunityStatus;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.apps.crm.db.repo.OpportunityStatusRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class OpportunityServiceImpl implements OpportunityService {

  protected OpportunityRepository opportunityRepo;
  protected AddressService addressService;
  protected OpportunityStatusRepository opportunityStatusRepo;

  @Inject
  public OpportunityServiceImpl(
      OpportunityRepository opportunityRepo,
      AddressService addressService,
      OpportunityStatusRepository opportunityStatusRepo) {
    this.opportunityRepo = opportunityRepo;
    this.addressService = addressService;
    this.opportunityStatusRepo = opportunityStatusRepo;
  }

  @Transactional
  public void saveOpportunity(Opportunity opportunity) {
    opportunityRepo.save(opportunity);
  }


  @Override
  public void setSequence(Opportunity opportunity) throws AxelorException {
    Company company = opportunity.getCompany();
    String seq =
        Beans.get(SequenceService.class).getSequenceNumber(SequenceRepository.OPPORTUNITY, company);
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
    Partner partner = opportunity.getPartner();
    Partner contact = opportunity.getContact();
    if (partner != null) {
      return partner.getFullName();

    } else if (contact != null) {
      return contact.getFullName();
    }
    return null;
  }

  @Override
  public OpportunityStatus getDefaultOpportunityStatus() {
    return opportunityStatusRepo.getDefaultStatus();
  }
}
