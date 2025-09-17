package com.axelor.apps.base.service.partner;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;

public class PartnerCreationServiceImpl implements PartnerCreationService {

  protected PartnerRepository partnerRepository;
  protected UserService userService;
  protected AppBaseService appBaseService;

  @Inject
  public PartnerCreationServiceImpl(
      PartnerRepository partnerRepository, UserService userService, AppBaseService appBaseService) {
    this.partnerRepository = partnerRepository;
    this.userService = userService;
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Partner createPartner(
      Integer partnerTypeSelect,
      Integer titleSelect,
      String firstName,
      String name,
      Partner mainPartner,
      String description,
      boolean isContact,
      boolean isCustomer,
      boolean isSupplier,
      boolean isProspect)
      throws AxelorException {

    Partner partner = new Partner();
    partner.setName(name);
    this.validateAndSetFlags(isContact, isCustomer, isSupplier, isProspect, partner);

    User user = userService.getUser();
    partner.setDescription(description);
    partner.setLocalization(appBaseService.getAppBase().getDefaultPartnerLocalization());
    partner.setUser(user);
    partner.setTeam(user.getActiveTeam());
    partner.setPartnerTypeSelect(partnerTypeSelect);

    if (isContact) {
      partner.setPartnerTypeSelect(PartnerRepository.PARTNER_TYPE_INDIVIDUAL);
      partnerTypeSelect = partner.getPartnerTypeSelect();
      if (mainPartner != null) {
        partner.setMainPartner(mainPartner);
        mainPartner.addContactPartnerSetItem(partner);
      }
    }

    Company userActiveCompany = userService.getUserActiveCompany();
    if (userActiveCompany != null) {
      partner.addCompanySetItem(userActiveCompany);
      if (!isContact) {
        partner.setCurrency(userActiveCompany.getCurrency());
      }
      if (partnerTypeSelect == null) {
        partner.setPartnerTypeSelect(userActiveCompany.getDefaultPartnerTypeSelect());
      }
    }

    if (partner.getPartnerTypeSelect() != PartnerRepository.PARTNER_TYPE_COMPANY) {
      partner.setTitleSelect(titleSelect);
      partner.setFirstName(firstName);
    }
    return partnerRepository.save(partner);
  }

  protected void validateAndSetFlags(
      boolean isContact,
      boolean isCustomer,
      boolean isSupplier,
      boolean isProspect,
      Partner partner)
      throws AxelorException {

    if (!isContact && !isCustomer && !isSupplier && !isProspect) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(BaseExceptionMessage.PARTNER_BOOLEAN_MISSING));
    }
    if (isContact && (isCustomer || isSupplier || isProspect)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(BaseExceptionMessage.PARTNER_INVALID_BOOLEAN_1));
    }
    if (isCustomer && isProspect) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(BaseExceptionMessage.PARTNER_INVALID_BOOLEAN_2));
    }

    partner.setIsContact(isContact);
    partner.setIsCustomer(isCustomer);
    partner.setIsSupplier(isSupplier);
    partner.setIsProspect(isProspect);
  }
}
