package com.axelor.apps.base.service.partner.registrationnumber;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.RegistrationNumberTemplate;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.common.StringUtils;

public class PartnerRegistrationCodeViewServiceImpl implements PartnerRegistrationCodeViewService {
  @Override
  public String getRegistrationCodeTitleFromTemplate(Partner partner) {
    if (partner.getMainAddress() != null && partner.getMainAddress().getCountry() != null) {
      Country businessCountry = partner.getMainAddress().getCountry();
      RegistrationNumberTemplate registrationNumberTemplate =
          businessCountry.getRegistrationNumberTemplate();
      if (registrationNumberTemplate != null
          && !StringUtils.isBlank(registrationNumberTemplate.getTitleToDisplay())) {
        return registrationNumberTemplate.getTitleToDisplay();
      }
    }
    return null;
  }

  @Override
  public boolean isSirenHidden(Partner partner) {
    boolean isIndividual =
        partner.getPartnerTypeSelect() == PartnerRepository.PARTNER_TYPE_INDIVIDUAL;
    Address address = partner.getMainAddress();
    if (address != null && address.getCountry() != null) {
      RegistrationNumberTemplate registrationNumberTemplate =
          address.getCountry().getRegistrationNumberTemplate();
      if (registrationNumberTemplate == null) {
        return isIndividual;
      }
      return isIndividual || !registrationNumberTemplate.getUseSiren();
    }
    return false;
  }

  @Override
  public boolean isNicHidden(Partner partner) {
    boolean isIndividual =
        partner.getPartnerTypeSelect() == PartnerRepository.PARTNER_TYPE_INDIVIDUAL;
    Address address = partner.getMainAddress();
    if (address != null && address.getCountry() != null) {
      RegistrationNumberTemplate registrationNumberTemplate =
          address.getCountry().getRegistrationNumberTemplate();
      if (registrationNumberTemplate == null) {
        return isIndividual;
      }
      return isIndividual || !registrationNumberTemplate.getUseNic();
    }
    return false;
  }

  @Override
  public boolean isTaxNbrHidden(Partner partner) {
    boolean isCustomer = partner.getIsCustomer();
    boolean hideTaxNbr = !isCustomer;
    Address address = partner.getMainAddress();
    if (address != null && address.getCountry() != null) {
      RegistrationNumberTemplate registrationNumberTemplate =
          address.getCountry().getRegistrationNumberTemplate();
      if (registrationNumberTemplate == null) {
        return hideTaxNbr;
      }
      boolean useTaxNbr = registrationNumberTemplate.getUseTaxNbr();
      hideTaxNbr = !useTaxNbr || !isCustomer;
    }
    return hideTaxNbr;
  }
}
