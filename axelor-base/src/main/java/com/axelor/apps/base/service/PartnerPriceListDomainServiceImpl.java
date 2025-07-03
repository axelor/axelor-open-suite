/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerPriceList;
import com.axelor.apps.base.db.repo.PartnerPriceListRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class PartnerPriceListDomainServiceImpl implements PartnerPriceListDomainService {

  protected final PartnerPriceListRepository partnerPriceListRepository;
  protected final PartnerRepository partnerRepository;

  @Inject
  public PartnerPriceListDomainServiceImpl(
      PartnerPriceListRepository partnerPriceListRepository, PartnerRepository partnerRepository) {
    this.partnerPriceListRepository = partnerPriceListRepository;
    this.partnerRepository = partnerRepository;
  }

  @Override
  public String getSalePartnerPriceListDomain(Partner partner) {
    Currency currency = partner.getCurrency();
    List<Partner> partnerList =
        partnerRepository
            .all()
            .filter("self.id != :partnerId AND self.salePartnerPriceList IS NOT NULL")
            .bind("partnerId", partner.getId())
            .fetch();
    List<PartnerPriceList> partnerPriceListList =
        partnerList.stream().map(Partner::getSalePartnerPriceList).collect(Collectors.toList());
    partnerPriceListList =
        partnerPriceListList.stream()
            .filter(PartnerPriceList::getIsExclusive)
            .distinct()
            .collect(Collectors.toList());

    return "self.id NOT IN ("
        + StringHelper.getIdListString(partnerPriceListList)
        + ") AND self.typeSelect = 1 AND self.currency = "
        + currency.getId();
  }

  @Override
  public String getPurchasePartnerPriceListDomain(Partner partner) {
    Currency currency = partner.getCurrency();
    List<Partner> partnerList =
        partnerRepository
            .all()
            .filter("self.id != :partnerId AND self.purchasePartnerPriceList IS NOT NULL")
            .bind("partnerId", partner.getId())
            .bind("currency", currency)
            .fetch();
    List<PartnerPriceList> partnerPriceListList =
        partnerList.stream().map(Partner::getPurchasePartnerPriceList).collect(Collectors.toList());
    partnerPriceListList =
        partnerPriceListList.stream()
            .filter(PartnerPriceList::getIsExclusive)
            .distinct()
            .collect(Collectors.toList());

    return "self.id NOT IN ("
        + StringHelper.getIdListString(partnerPriceListList)
        + ") AND self.typeSelect = 2 AND self.currency.id = "
        + currency.getId();
  }
}
