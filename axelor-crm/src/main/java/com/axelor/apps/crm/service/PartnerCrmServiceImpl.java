package com.axelor.apps.crm.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.crm.db.LostReason;
import com.axelor.apps.crm.db.PartnerStatus;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PartnerCrmServiceImpl implements PartnerCrmService {

  protected AppCrmService appCrmService;

  @Inject
  public PartnerCrmServiceImpl(AppCrmService appCrmService) {
    this.appCrmService = appCrmService;
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
}
