package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;

public interface PartnerSupplychainService {

  public void updateBlockedAccount(Partner partner) throws AxelorException;

  public boolean isBlockedPartnerOrParent(Partner partner);
}
