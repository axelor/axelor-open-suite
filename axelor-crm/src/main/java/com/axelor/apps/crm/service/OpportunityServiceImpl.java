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
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.OpportunityStatus;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.crm.db.repo.OpportunityStatusRepository;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppCrm;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;

public class OpportunityServiceImpl implements OpportunityService {

  protected OpportunityRepository opportunityRepo;
  protected AddressService addressService;
  protected OpportunityStatusRepository opportunityStatusRepo;
  protected AppCrmService appCrmService;

  @Inject
  public OpportunityServiceImpl(
      OpportunityRepository opportunityRepo,
      AddressService addressService,
      OpportunityStatusRepository opportunityStatusRepo,
      AppCrmService appCrmService) {
    this.opportunityRepo = opportunityRepo;
    this.addressService = addressService;
    this.opportunityStatusRepo = opportunityStatusRepo;
    this.appCrmService = appCrmService;
  }

  @Transactional
  public void saveOpportunity(Opportunity opportunity) {
    opportunityRepo.save(opportunity);
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
  public OpportunityStatus getDefaultOpportunityStatus() {
    return opportunityStatusRepo.getDefaultStatus();
  }

  @Override
  public void setOpportunityStatus(Opportunity opportunity, boolean isStagedClosedWon)
      throws AxelorException {

    if (isStagedClosedWon) {
      opportunity.setOpportunityStatus(appCrmService.getClosedWinOpportunityStatus());
    } else {
      opportunity.setOpportunityStatus(appCrmService.getClosedLostOpportunityStatus());
    }

    saveOpportunity(opportunity);
  }

  @Override
  public void setOpportunityStatusNextStage(Opportunity opportunity) {
    OpportunityStatus status = opportunity.getOpportunityStatus();
    status = Beans.get(OpportunityStatusRepository.class).findByNextSequence(status.getSequence());
    opportunity.setOpportunityStatus(status);
    saveOpportunity(opportunity);
  }

  @Override
  public List<Long> getClosedOpportunityStatusIdList() {
    List<Long> closedOpportunityStatusIdList = new ArrayList<>();

    AppCrm appCrm = appCrmService.getAppCrm();

    OpportunityStatus closedWinOpportunityStatus = appCrm.getClosedWinOpportunityStatus();
    OpportunityStatus closedLostOpportunityStatus = appCrm.getClosedLostOpportunityStatus();

    if (closedWinOpportunityStatus != null) {
      closedOpportunityStatusIdList.add(closedWinOpportunityStatus.getId());
    }

    if (closedLostOpportunityStatus != null) {
      closedOpportunityStatusIdList.add(closedLostOpportunityStatus.getId());
    }

    return closedOpportunityStatusIdList;
  }
}
