package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerLinkTypeRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.PartnerLinkServiceImpl;
import com.google.inject.Inject;
import java.util.List;

public class PartnerLinkSupplychainServiceImpl extends PartnerLinkServiceImpl
    implements PartnerLinkSupplychainService {
  @Inject
  public PartnerLinkSupplychainServiceImpl(PartnerRepository partnerRepository) {
    super(partnerRepository);
  }

  @Override
  public Partner getPartnerIfOnlyOne(Partner partner) {
    List<Long> partnerIds =
        getPartnerIds(partner, PartnerLinkTypeRepository.TYPE_SELECT_INVOICED_BY);
    partnerIds.remove(0L);
    if (partnerIds.size() != 1) {
      return null;
    }

    return partnerRepository.find(partnerIds.get(0));
  }
}
