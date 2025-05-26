package com.axelor.apps.base.service;

import static com.axelor.apps.base.db.repo.PartnerRepository.PARTNER_TYPE_INDIVIDUAL;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PartnerConvertServiceImpl implements PartnerConvertService {

  protected final PartnerService partnerService;

  @Inject
  public PartnerConvertServiceImpl(PartnerService partnerService) {
    this.partnerService = partnerService;
  }

  @Transactional
  @Override
  public void convertToIndividualPartner(Partner partner) {
    partner.setIsContact(false);
    partner.setPartnerTypeSelect(PARTNER_TYPE_INDIVIDUAL);
    Address mainAddress = partner.getMainAddress();
    if (mainAddress != null) {
      partnerService.addPartnerAddress(partner, mainAddress, true, false, false);
    }
    partner.setMainAddress(null);
  }
}
