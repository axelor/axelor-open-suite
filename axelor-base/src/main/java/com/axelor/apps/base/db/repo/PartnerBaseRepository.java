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
package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.utils.PartnerUtilsService;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import javax.persistence.PersistenceException;

public class PartnerBaseRepository extends PartnerRepository {

  protected PartnerAddressRepository partnerAddressRepository;
  protected PartnerUtilsService partnerUtilsService;

  @Inject
  public PartnerBaseRepository(
      PartnerAddressRepository partnerAddressRepository, PartnerUtilsService partnerUtilsService) {
    this.partnerAddressRepository = partnerAddressRepository;
    this.partnerUtilsService = partnerUtilsService;
  }

  @Override
  public Partner save(Partner partner) {
    try {
      partnerUtilsService.onSave(partner);
      return super.save(partner);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  @Override
  public Partner copy(Partner partner, boolean deep) {

    Partner copy = super.copy(partner, deep);

    copy.setPartnerSeq(null);
    copy.setEmailAddress(null);

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

  @Override
  public void remove(Partner partner) {
    partnerUtilsService.removeLinkedPartner(partner);
    super.remove(partner);
  }
}
