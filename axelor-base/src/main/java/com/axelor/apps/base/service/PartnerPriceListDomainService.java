package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Partner;

public interface PartnerPriceListDomainService {
  String getSalePartnerPriceListDomain(Partner partner);

  String getPurchasePartnerPriceListDomain(Partner partner);
}
