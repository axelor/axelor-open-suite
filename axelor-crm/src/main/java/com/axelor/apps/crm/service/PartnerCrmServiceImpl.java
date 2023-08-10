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
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.crm.db.LostReason;
import com.axelor.apps.crm.db.PartnerStatus;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Objects;

public class PartnerCrmServiceImpl implements PartnerCrmService {

  protected AppCrmService appCrmService;

  protected PartnerRepository partnerRepository;

  @Inject
  public PartnerCrmServiceImpl(AppCrmService appCrmService, PartnerRepository partnerRepository) {
    this.appCrmService = appCrmService;
    this.partnerRepository = partnerRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void losePartner(Partner partner, LostReason lostReason, String lostReasonStr)
      throws AxelorException {
    PartnerStatus partnerStatus = partner.getPartnerStatus();

    PartnerStatus lostPartnerStatus = appCrmService.getClosedLostPartnerStatus();

    if (partnerStatus == null || partnerStatus.equals(lostPartnerStatus)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(CrmExceptionMessage.PARTNER_LOSE_WRONG_STATUS));
    }
    partner.setPartnerStatus(lostPartnerStatus);
    partner.setLostReason(lostReason);
    partner.setLostReasonStr(lostReasonStr);
  }

  @Override
  @Transactional
  public void kanbanPartnerOnMove(Partner partner) throws AxelorException {
    PartnerStatus partnerStatus = partner.getPartnerStatus();
    PartnerStatus closedWinPartnerStatus = appCrmService.getClosedWinPartnerStatus();
    PartnerStatus closedLostPartnerStatus = appCrmService.getClosedLostPartnerStatus();

    if (Objects.isNull(partnerStatus)) {
      return;
    }
    if (partnerStatus.equals(closedWinPartnerStatus)) {
      partner = partnerRepository.find(partner.getId());
      partner.setIsCustomer(true);
      partner.setIsProspect(false);
      partner.setPartnerStatus(partnerStatus);
      partnerRepository.save(partner);
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(CrmExceptionMessage.PROSPECT_CLOSE_WIN_KANBAN));
    }
    if (partnerStatus.equals(closedLostPartnerStatus)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(CrmExceptionMessage.PROSPECT_CLOSE_LOST_KANBAN));
    }
  }
}
