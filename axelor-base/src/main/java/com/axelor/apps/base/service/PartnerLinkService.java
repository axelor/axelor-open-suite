package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Partner;

public interface PartnerLinkService {
  /**
   * Computes the filter for the given partner field.
   *
   * @param partner the main partner to search for related partners
   * @param strFilter the type of the filter needed
   * @return the computed filter to be used as a JPQL domain attribute
   */
  String computePartnerFilter(Partner partner, String strFilter);
}
