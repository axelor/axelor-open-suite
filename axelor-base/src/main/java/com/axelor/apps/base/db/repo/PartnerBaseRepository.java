/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.beust.jcommander.internal.Lists;
import com.google.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.persistence.PersistenceException;

public class PartnerBaseRepository extends PartnerRepository {

  @Inject PartnerService partnerService;

  @Inject AppBaseService appBaseService;

  @Override
  public Partner save(Partner partner) {
    try {

      if (partner.getPartnerSeq() == null
          && appBaseService.getAppBase().getGeneratePartnerSequence()) {
        String seq = Beans.get(SequenceService.class).getSequenceNumber(SequenceRepository.PARTNER);
        if (seq == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.PARTNER_1));
        }
        partner.setPartnerSeq(seq);
      }

      if (partner.getEmailAddress() != null) {
        long existEmailCount =
            this.all()
                .filter(
                    "self.id != ?1 and self.emailAddress = ?2",
                    partner.getId(),
                    partner.getEmailAddress())
                .count();

        if (existEmailCount > 0) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_NO_UNIQUE_KEY,
              I18n.get(IExceptionMessage.PARTNER_EMAIL_EXIST));
        }
      }

      if (!partner.getIsContact() && !partner.getIsEmployee()) {
        partner.setMainAddress(null);
        if (partner.getPartnerAddressList() != null) {
          for (PartnerAddress partnerAddress : partner.getPartnerAddressList()) {
            if (partnerAddress.getIsDefaultAddr()) {
              partner.setMainAddress(partnerAddress.getAddress());
              break;
            }
          }
        }
      }

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

      partnerService.setPartnerFullName(partner);
      partnerService.setCompanyStr(partner);
      return super.save(partner);
    } catch (Exception e) {
      throw new PersistenceException(e);
    }
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (!context.containsKey("json-enhance")) {
      return json;
    }
    try {
      Long id = (Long) json.get("id");
      Partner partner = find(id);
      json.put("address", partnerService.getDefaultAddress(partner));
    } catch (Exception e) {
      e.printStackTrace();
    }

    return json;
  }

  @Override
  public Partner copy(Partner partner, boolean deep) {

    Partner copy = super.copy(partner, deep);

    copy.setPartnerSeq(null);
    copy.setEmailAddress(null);

    PartnerAddressRepository partnerAddressRepository = Beans.get(PartnerAddressRepository.class);

    List<PartnerAddress> partnerAddressList = Lists.newArrayList();

    if (deep && copy.getPartnerAddressList() != null) {
      for (PartnerAddress partnerAddress : copy.getPartnerAddressList()) {

        partnerAddressList.add(partnerAddressRepository.copy(partnerAddress, deep));
      }
    }
    copy.setPartnerAddressList(partnerAddressList);
    copy.setBlockingList(null);
    copy.setBankDetailsList(null);

    return copy;
  }
}
