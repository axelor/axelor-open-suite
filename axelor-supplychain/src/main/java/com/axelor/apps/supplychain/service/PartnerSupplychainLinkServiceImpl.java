package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerSupplychainLink;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PartnerSupplychainLinkServiceImpl implements PartnerSupplychainLinkService {

  @Override
  public String computePartnerFilter(Partner partner, String strFilter) {
    List<Long> partnerIds = new ArrayList<>();
    // manage the case where nothing is found
    partnerIds.add(0L);

    if (partner != null && partner.getPartner1SupplychainLinkList() != null) {
      partnerIds.addAll(
          partner.getPartner1SupplychainLinkList().stream()
              .filter(
                  partnerSupplychainLink ->
                      partnerSupplychainLink.getPartnerSupplychainLinkType() != null
                          && strFilter.equals(
                              partnerSupplychainLink
                                  .getPartnerSupplychainLinkType()
                                  .getTypeSelect())
                          && partnerSupplychainLink.getPartner2() != null)
              .map(PartnerSupplychainLink::getPartner2)
              .map(Partner::getId)
              .collect(Collectors.toList()));
    }

    return "self.id IN (" + Joiner.on(",").join(partnerIds) + ")";
  }
}
