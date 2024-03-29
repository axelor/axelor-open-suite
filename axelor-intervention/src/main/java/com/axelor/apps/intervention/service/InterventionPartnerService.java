package com.axelor.apps.intervention.service;

import com.axelor.apps.base.db.Partner;

public interface InterventionPartnerService {
  Partner getDefaultInvoicedPartner(Partner partner);
}
