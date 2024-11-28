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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.service.BlockingService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.Optional;

public class SaleOrderDomainServiceImpl implements SaleOrderDomainService {

  protected final BlockingService blockingService;

  @Inject
  public SaleOrderDomainServiceImpl(BlockingService blockingService) {
    this.blockingService = blockingService;
  }

  @Override
  public String getPartnerBaseDomain(Company company) {
    Long companyPartnerId =
        Optional.ofNullable(company).map(Company::getPartner).map(Partner::getId).orElse(0L);

    String domain =
        String.format(
            "self.id != %d AND self.isContact = false "
                + "AND (self.isCustomer = true or self.isProspect = true) "
                + "AND :company member of self.companySet",
            companyPartnerId);

    String blockedPartnerQuery =
        blockingService.listOfBlockedPartner(company, BlockingRepository.SALE_BLOCKING);

    if (!Strings.isNullOrEmpty(blockedPartnerQuery)) {
      domain += String.format(" AND self.id NOT in (%s)", blockedPartnerQuery);
    }

    return domain;
  }
}
