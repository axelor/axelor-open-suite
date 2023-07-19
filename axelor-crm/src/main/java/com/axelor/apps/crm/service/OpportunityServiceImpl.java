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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.crm.db.LostReason;
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
import java.util.Map;
import java.util.Objects;

public class OpportunityServiceImpl implements OpportunityService {

  protected OpportunityRepository opportunityRepo;
  protected OpportunityStatusRepository opportunityStatusRepo;
  protected AppCrmService appCrmService;
  protected PartnerRepository partnerRepository;

  @Inject
  public OpportunityServiceImpl(
      OpportunityRepository opportunityRepo,
      OpportunityStatusRepository opportunityStatusRepo,
      AppCrmService appCrmService,
      PartnerRepository partnerRepository) {
    this.opportunityRepo = opportunityRepo;
    this.opportunityStatusRepo = opportunityStatusRepo;
    this.appCrmService = appCrmService;
    this.partnerRepository = partnerRepository;
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
  public OpportunityStatus getDefaultOpportunityStatus() throws AxelorException {
    return appCrmService.getOpportunityDefaultStatus();
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void setOpportunityStatusStagedClosedWon(Opportunity opportunity) throws AxelorException {
    opportunity.setOpportunityStatus(appCrmService.getClosedWinOpportunityStatus());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void setOpportunityStatusStagedClosedLost(Opportunity opportunity) throws AxelorException {
    opportunity.setOpportunityStatus(appCrmService.getClosedLostOpportunityStatus());
  }

  @Override
  @Transactional
  public void setOpportunityStatusNextStage(Opportunity opportunity) {
    OpportunityStatus status = opportunity.getOpportunityStatus();
    status = Beans.get(OpportunityStatusRepository.class).findByNextSequence(status.getSequence());
    opportunity.setOpportunityStatus(status);
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

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<Opportunity> winningProcess(Opportunity opportunity, Map<String, Boolean> map)
      throws AxelorException {

    setOpportunityStatusStagedClosedWon(opportunity);

    Partner partner = opportunity.getPartner();

    partner.setIsCustomer(map.get("isCustomer"));
    partner.setIsProspect(map.get("isProspect"));
    partner.setIsSupplier(map.get("isSupplier"));
    partner.setIsEmployee(map.get("isEmployee"));
    partner.setIsContact(map.get("isContact"));
    partner.setIsInternal(map.get("isInternal"));
    partner.setIsPartner(map.get("isPartner"));
    partnerRepository.save(partner);

    return getOtherOpportunities(opportunity);
  }

  protected List<Opportunity> getOtherOpportunities(Opportunity opportunity)
      throws AxelorException {
    return opportunityRepo
        .all()
        .filter(
            "self.partner = :partner AND self.id != :id AND self.opportunityStatus NOT IN (:closedWon, :closedLost)")
        .bind("partner", opportunity.getPartner())
        .bind("id", opportunity.getId())
        .bind("closedWon", appCrmService.getClosedWinOpportunityStatus())
        .bind("closedLost", appCrmService.getClosedLostOpportunityStatus())
        .fetch();
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void lostProcess(
      List<Opportunity> otherOpportunities, LostReason lostReason, String lostReasonStr)
      throws AxelorException {

    for (Opportunity opportunity : otherOpportunities) {
      lostProcess(opportunity, lostReason, lostReasonStr);
    }
  }

  protected void lostProcess(Opportunity opportunity, LostReason lostReason, String lostReasonStr)
      throws AxelorException {
    setOpportunityStatusStagedClosedLost(opportunity);
    opportunity.setLostReason(lostReason);
    opportunity.setLostReasonStr(lostReasonStr);
    saveOpportunity(opportunity);
  }

  @Override
  public void kanbanOpportunityOnMove(Opportunity opportunity) throws AxelorException {
    OpportunityStatus opportunityStatus = opportunity.getOpportunityStatus();
    OpportunityStatus closedLostOpportunityStatus = appCrmService.getClosedLostOpportunityStatus();

    if (Objects.isNull(opportunityStatus)) {
      return;
    }
    if (opportunityStatus.equals(closedLostOpportunityStatus)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(CrmExceptionMessage.OPPORTUNITY_CLOSE_LOST_KANBAN));
    }
  }
}
