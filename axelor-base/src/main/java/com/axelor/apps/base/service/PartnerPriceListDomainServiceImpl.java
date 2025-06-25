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
