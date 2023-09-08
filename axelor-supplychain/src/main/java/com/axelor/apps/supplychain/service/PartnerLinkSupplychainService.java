package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Partner;

public interface PartnerLinkSupplychainService {
  Partner getPartnerIfOnlyOne(Partner partner);
}
