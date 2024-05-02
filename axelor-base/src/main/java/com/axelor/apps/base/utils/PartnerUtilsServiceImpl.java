/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.utils;

import static com.axelor.apps.base.db.repo.PartnerRepository.PARTNER_TYPE_INDIVIDUAL;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.PartnerComputeNameService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashSet;
import java.util.List;

public class PartnerUtilsServiceImpl implements PartnerUtilsService {

  protected AppBaseService appBaseService;
  protected SequenceService sequenceService;
  protected PartnerComputeNameService partnerComputeNameService;

  @Inject
  public PartnerUtilsServiceImpl(
      AppBaseService appBaseService,
      SequenceService sequenceService,
      PartnerComputeNameService partnerComputeNameService) {
    this.appBaseService = appBaseService;
    this.sequenceService = sequenceService;
    this.partnerComputeNameService = partnerComputeNameService;
  }

  @Override
  public void onSave(Partner partner) throws AxelorException {

    if (partner.getPartnerSeq() == null
        && appBaseService.getAppBase().getGeneratePartnerSequence()) {
      String seq =
          sequenceService.getSequenceNumber(
              SequenceRepository.PARTNER, Partner.class, "partnerSeq", partner);
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.PARTNER_1));
      }
      partner.setPartnerSeq(seq);
    }

    if (partner.getEmailAddress() != null) {
      long existEmailCount =
          Query.of(Partner.class)
              .filter(
                  "self.id != ?1 and self.emailAddress = ?2",
                  partner.getId(),
                  partner.getEmailAddress())
              .count();

      if (existEmailCount > 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_UNIQUE_KEY,
            I18n.get(BaseExceptionMessage.PARTNER_EMAIL_EXIST));
      }
    }

    updatePartnerAddress(partner);

    if (partner.getPartnerTypeSelect() == PARTNER_TYPE_INDIVIDUAL) {
      partner.setContactPartnerSet(new HashSet<>());
    }

    if (!partner.getIsContact() && partner.getContactPartnerSet() != null) {
      for (Partner contact : partner.getContactPartnerSet()) {
        if (contact.getMainPartner() == null) {
          contact.setMainPartner(partner);
        }
      }
    }

    partnerComputeNameService.setPartnerFullName(partner);
  }

  @Override
  public void updatePartnerAddress(Partner partner) throws AxelorException {
    Address address = partner.getMainAddress();

    if (!partner.getIsContact() && !partner.getIsEmployee()) {
      if (partner.getPartnerAddressList() != null) {
        partner.setMainAddress(checkDefaultAddress(partner));
      }
    } else if (address == null) {
      partner.getPartnerAddressList().removeIf(PartnerAddress::getIsDefaultAddr);

    } else if (partner.getPartnerAddressList() != null
        && partner.getPartnerAddressList().stream()
            .map(PartnerAddress::getAddress)
            .noneMatch(address::equals)) {
      PartnerAddress mainAddress = new PartnerAddress();
      mainAddress.setAddress(address);
      mainAddress.setIsDefaultAddr(true);
      mainAddress.setIsDeliveryAddr(true);
      mainAddress.setIsInvoicingAddr(true);
      partner.addPartnerAddressListItem(mainAddress);
    }
  }

  /**
   * Ensures that there is exactly one default invoicing address and no more than one default
   * delivery address. If the partner address list is valid, returns the default invoicing address.
   *
   * @param partner
   * @throws AxelorException
   */
  protected Address checkDefaultAddress(Partner partner) throws AxelorException {
    List<PartnerAddress> partnerAddressList = partner.getPartnerAddressList();
    Address defaultInvoicingAddress = null;
    Address defaultDeliveryAddress = null;

    if (partnerAddressList != null) {
      for (PartnerAddress partnerAddress : partnerAddressList) {
        if (partnerAddress.getIsDefaultAddr() && partnerAddress.getIsInvoicingAddr()) {
          if (defaultInvoicingAddress != null) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_INCONSISTENCY,
                I18n.get(BaseExceptionMessage.ADDRESS_8));
          }
          defaultInvoicingAddress = partnerAddress.getAddress();
        }

        if (partnerAddress.getIsDefaultAddr() && partnerAddress.getIsDeliveryAddr()) {
          if (defaultDeliveryAddress != null) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_INCONSISTENCY,
                I18n.get(BaseExceptionMessage.ADDRESS_9));
          }
          defaultDeliveryAddress = partnerAddress.getAddress();
        }
      }
    }
    return defaultInvoicingAddress;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void removeLinkedPartner(Partner partner) {
    if (partner.getLinkedUser() == null) {
      return;
    }
    UserRepository userRepository = Beans.get(UserRepository.class);
    User user = userRepository.find(partner.getLinkedUser().getId());
    if (user != null) {
      user.setPartner(null);
      userRepository.save(user);
    }
  }
}
