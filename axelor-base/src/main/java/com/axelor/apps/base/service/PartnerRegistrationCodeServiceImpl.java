package com.axelor.apps.base.service;

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.RegistrationNumberTemplate;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.StringUtils;
import com.google.common.base.Strings;
import java.util.Optional;

public class PartnerRegistrationCodeServiceImpl implements PartnerRegistrationCodeService {
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
  public Class<? extends RegistrationNumberValidator> getRegistrationNumberValidatorClass(
      Partner partner) {
    Address mainAddress = partner.getMainAddress();
    if (mainAddress == null || mainAddress.getCountry() == null) {
      return null;
    }
    Country businessCountry = partner.getMainAddress().getCountry();
    RegistrationNumberTemplate registrationNumberTemplate =
        businessCountry.getRegistrationNumberTemplate();

    if (registrationNumberTemplate != null) {
      Class<? extends RegistrationNumberValidator> klass = null;
      try {
        String origin = registrationNumberTemplate.getValidationMethodSelect();
        if (!Strings.isNullOrEmpty(origin)) {
          klass = (Class<? extends RegistrationNumberValidator>) Class.forName(origin);
        }
      } catch (ClassNotFoundException e) {
        TraceBackService.trace(e, String.valueOf(ResponseMessageType.ERROR));
      } finally {
        if (klass == null) {
          return RegistrationNumberValidatorDefault.class;
        }
        return klass;
      }
    }
    return null;
  }

  protected Address getAddressFromPartner(Partner partner) {
    Address address = null;
    if (partner.getMainAddress() != null) {
      address = partner.getMainAddress();
    } else {
      address =
          Optional.ofNullable(partner.getPartnerAddressList())
              .flatMap(addressList -> addressList.stream().findFirst())
              .map(PartnerAddress::getAddress)
              .orElse(null);
    }
    return address;
  }

  @Override
  public boolean isSirenHidden(Partner partner) {
    boolean isIndividual =
        partner.getPartnerTypeSelect() == PartnerRepository.PARTNER_TYPE_INDIVIDUAL;
    Address address = getAddressFromPartner(partner);
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
    Address address = getAddressFromPartner(partner);
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
    Address address = getAddressFromPartner(partner);
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
