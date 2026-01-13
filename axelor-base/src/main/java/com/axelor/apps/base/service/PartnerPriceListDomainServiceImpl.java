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
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.db.Query;
import com.axelor.utils.helpers.StringHelper;
import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class PartnerPriceListDomainServiceImpl implements PartnerPriceListDomainService {

  protected final PartnerRepository partnerRepository;

  @Inject
  public PartnerPriceListDomainServiceImpl(PartnerRepository partnerRepository) {
    this.partnerRepository = partnerRepository;
  }

  @Override
  public String getSalePartnerPriceListDomain(Partner partner) {
    List<Partner> partnerList =
        getFilteredPartners(partner, "self.salePartnerPriceList IS NOT NULL");
    List<PartnerPriceList> partnerPriceListList =
        partnerList.stream()
            .map(Partner::getSalePartnerPriceList)
            .filter(PartnerPriceList::getIsExclusive)
            .distinct()
            .collect(Collectors.toList());

    return getPartnerPriceListDomain(partner, partnerPriceListList, PriceListRepository.TYPE_SALE);
  }

  protected List<Partner> getFilteredPartners(Partner partner, String filter) {
    Query<Partner> partnerQuery = partnerRepository.all();

    if (partner.getId() != null) {
      filter += " AND self.id != :partnerId";
      partnerQuery.bind("partnerId", partner.getId());
    }
    return partnerQuery.filter(filter).fetch();
  }

  @Override
  public String getPurchasePartnerPriceListDomain(Partner partner) {
    List<Partner> partnerList =
        getFilteredPartners(partner, "self.purchasePartnerPriceList IS NOT NULL");
    List<PartnerPriceList> partnerPriceListList =
        partnerList.stream()
            .map(Partner::getPurchasePartnerPriceList)
            .filter(PartnerPriceList::getIsExclusive)
            .distinct()
            .collect(Collectors.toList());

    return getPartnerPriceListDomain(
        partner, partnerPriceListList, PriceListRepository.TYPE_PURCHASE);
  }

  protected String getPartnerPriceListDomain(
      Partner partner, List<PartnerPriceList> partnerPriceListList, int type) {
    Currency currency = partner.getCurrency();
    String domain =
        "self.id NOT IN ("
            + StringHelper.getIdListString(partnerPriceListList)
            + ") AND self.typeSelect = "
            + type;
    if (currency != null) {
      domain += " AND self.currency.id = " + currency.getId();
    }
    return domain;
  }
}
