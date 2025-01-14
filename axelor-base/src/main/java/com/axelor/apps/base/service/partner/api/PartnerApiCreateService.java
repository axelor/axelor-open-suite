package com.axelor.apps.base.service.partner.api;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.google.inject.persist.Transactional;

public interface PartnerApiCreateService {

  @Transactional
  void setData(Partner partner, String result) throws AxelorException;
}
