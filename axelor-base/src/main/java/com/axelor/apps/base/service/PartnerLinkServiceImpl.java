package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerLink;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PartnerLinkServiceImpl implements PartnerLinkService {
  protected PartnerRepository partnerRepository;

  @Inject
  public PartnerLinkServiceImpl(PartnerRepository partnerRepository) {
    this.partnerRepository = partnerRepository;
  }

  @Override
  public String computePartnerFilter(Partner partner, String strFilter) {
    List<Long> partnerIds = getPartnerIds(partner, strFilter);

    return "self.id IN (" + Joiner.on(",").join(partnerIds) + ")";
  }

  public List<Long> getPartnerIds(Partner partner, String strFilter) {
    List<Long> partnerIds = new ArrayList<>();
    // manage the case where nothing is found
    partnerIds.add(0L);
    if (partner != null && partner.getManagedByPartnerLinkList() != null) {
      // add current partner
      partnerIds.add(partner.getId());
      partnerIds.addAll(
          partner.getManagedByPartnerLinkList().stream()
              .filter(
                  partnerSupplychainLink ->
                      partnerSupplychainLink.getPartnerLinkType() != null
                          && strFilter.equals(
                              partnerSupplychainLink.getPartnerLinkType().getTypeSelect())
                          && partnerSupplychainLink.getPartner2() != null)
              .map(PartnerLink::getPartner2)
              .map(Partner::getId)
              .collect(Collectors.toList()));
    }
    return partnerIds;
  }
}
