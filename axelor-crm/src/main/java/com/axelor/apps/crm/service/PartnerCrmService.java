package com.axelor.apps.crm.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.LostReason;

public interface PartnerCrmService {

  /**
   * Set the lead status to lost and set the lost reason with the given lost reason.
   *
   * @param lead a context lead object
   * @param lostReason the specified lost reason
   */
  public void losePartner(Partner partner, LostReason lostReason, String lostReasonStr)
      throws AxelorException;
}
