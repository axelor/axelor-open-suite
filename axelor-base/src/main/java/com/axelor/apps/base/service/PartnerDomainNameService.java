package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import java.util.List;

public interface PartnerDomainNameService {

  List<Partner> getPartnersWithSameDomainNameAndUpdateDomainNameList(Partner partner)
      throws AxelorException;
}
