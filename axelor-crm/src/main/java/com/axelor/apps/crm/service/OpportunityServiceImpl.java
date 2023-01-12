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

import com.axelor.apps.base.db.AppCrm;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.OpportunityStatus;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.crm.db.repo.OpportunityStatusRepository;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;

public class OpportunityServiceImpl implements OpportunityService {

  protected OpportunityRepository opportunityRepo;
  protected AddressService addressService;
  protected OpportunityStatusRepository opportunityStatusRepo;
  protected AppBaseService appBaseService;

  @Inject
  public OpportunityServiceImpl(
      OpportunityRepository opportunityRepo,
      AddressService addressService,
      OpportunityStatusRepository opportunityStatusRepo,
      AppBaseService appBaseService) {
    this.opportunityRepo = opportunityRepo;
    this.addressService = addressService;
    this.opportunityStatusRepo = opportunityStatusRepo;
    this.appBaseService = appBaseService;
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

    AppCrm appCrm = (AppCrm) appBaseService.getApp("crm");

    OpportunityStatus closedWinOpportunityStatus = appCrm.getClosedWinOpportunityStatus();
    OpportunityStatus closedLostOpportunityStatus = appCrm.getClosedLostOpportunityStatus();

    if (isStagedClosedWon) {
      if (closedWinOpportunityStatus == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(CrmExceptionMessage.CRM_CLOSED_WIN_OPPORTUNITY_STATUS_MISSING));
      }

      opportunity.setOpportunityStatus(closedWinOpportunityStatus);
    } else {
      if (closedLostOpportunityStatus == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(CrmExceptionMessage.CRM_CLOSED_LOST_OPPORTUNITY_STATUS_MISSING));
      }

      opportunity.setOpportunityStatus(closedLostOpportunityStatus);
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

    AppCrm appCrm = (AppCrm) appBaseService.getApp("crm");

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
