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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerPriceList;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.repo.PartnerPriceListRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.StringTool;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PartnerPriceListServiceImpl implements PartnerPriceListService {

  protected AppBaseService appBaseService;

  @Inject
  public PartnerPriceListServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public void checkDates(PartnerPriceList partnerPriceList) throws AxelorException {
    Set<PriceList> priceListSet = partnerPriceList.getPriceListSet();

    if (priceListSet == null) {
      return;
    }
    Set<PriceList> sortedPriceListSet =
        priceListSet.stream()
            .sorted(
                Comparator.comparing(
                    priceList ->
                        priceList.getApplicationBeginDate() != null
                            ? priceList.getApplicationBeginDate()
                            : LocalDate.MIN))
            .collect(Collectors.toSet());
    LocalDate beginDate;
    LocalDate previousEndDate = LocalDate.MIN;
    String previousTitle = "";
    for (PriceList priceList : sortedPriceListSet) {
      beginDate =
          priceList.getApplicationBeginDate() != null
              ? priceList.getApplicationBeginDate()
              : LocalDate.MIN;
      if (beginDate.compareTo(previousEndDate) < 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(BaseExceptionMessage.PARTNER_PRICE_LIST_DATE_INCONSISTENT),
                previousTitle.replace("%", "%%"),
                priceList.getTitle().replace("%", "%%")),
            partnerPriceList);
      }
      previousEndDate =
          priceList.getApplicationEndDate() != null
              ? priceList.getApplicationEndDate()
              : LocalDate.MAX;
      previousTitle = priceList.getTitle();
    }
  }

  @Override
  public PriceList getDefaultPriceList(Partner partner, int priceListTypeSelect) {
    if (partner == null) {
      return null;
    }
    partner = Beans.get(PartnerRepository.class).find(partner.getId());
    PartnerPriceList partnerPriceList = getPartnerPriceList(partner, priceListTypeSelect);
    if (partnerPriceList == null) {
      return null;
    }
    Set<PriceList> priceListSet = partnerPriceList.getPriceListSet();
    if (priceListSet == null) {
      return null;
    }
    List<PriceList> priceLists =
        priceListSet.stream()
            .filter(
                priceList ->
                    priceList.getIsActive()
                        && (priceList.getApplicationBeginDate() == null
                            || priceList
                                    .getApplicationBeginDate()
                                    .compareTo(
                                        appBaseService.getTodayDate(
                                            Optional.ofNullable(AuthUtils.getUser())
                                                .map(User::getActiveCompany)
                                                .orElse(null)))
                                <= 0)
                        && (priceList.getApplicationEndDate() == null
                            || priceList
                                    .getApplicationEndDate()
                                    .compareTo(
                                        appBaseService.getTodayDate(
                                            Optional.ofNullable(AuthUtils.getUser())
                                                .map(User::getActiveCompany)
                                                .orElse(null)))
                                >= 0))
            .collect(Collectors.toList());
    if (priceLists.size() == 1) {
      return priceLists.get(0);
    } else {
      return null;
    }
  }

  public String getPriceListDomain(Partner partner, int priceListTypeSelect) {
    if (partner == null) {
      return "self.id IN (0)";
    }
    // get all non exclusive partner price lists
    List<PartnerPriceList> partnerPriceLists =
        Beans.get(PartnerPriceListRepository.class)
            .all()
            .filter("self.typeSelect = :_priceListTypeSelect " + "AND self.isExclusive = false")
            .bind("_priceListTypeSelect", priceListTypeSelect)
            .fetch();
    // get (maybe exclusive) list for the partner
    PartnerPriceList partnerPriceList = getPartnerPriceList(partner, priceListTypeSelect);
    if (partnerPriceList != null && partnerPriceList.getIsExclusive()) {
      partnerPriceLists.add(partnerPriceList);
    }
    if (partnerPriceLists.isEmpty()) {
      return "self.id IN (0)";
    }
    List<PriceList> priceLists =
        partnerPriceLists.stream()
            .flatMap(partnerPriceList1 -> partnerPriceList1.getPriceListSet().stream())
            .filter(
                priceList ->
                    priceList.getIsActive()
                        && (priceList.getApplicationBeginDate() == null
                            || priceList
                                    .getApplicationBeginDate()
                                    .compareTo(
                                        appBaseService.getTodayDate(
                                            Optional.ofNullable(AuthUtils.getUser())
                                                .map(User::getActiveCompany)
                                                .orElse(null)))
                                <= 0)
                        && (priceList.getApplicationEndDate() == null
                            || priceList
                                    .getApplicationEndDate()
                                    .compareTo(
                                        appBaseService.getTodayDate(
                                            Optional.ofNullable(AuthUtils.getUser())
                                                .map(User::getActiveCompany)
                                                .orElse(null)))
                                >= 0))
            .collect(Collectors.toList());
    return "self.id IN (" + StringTool.getIdListString(priceLists) + ")";
  }

  public PartnerPriceList getPartnerPriceList(Partner partner, int priceListTypeSelect) {
    if (priceListTypeSelect == PriceListRepository.TYPE_SALE) {
      return partner.getSalePartnerPriceList();
    } else if (priceListTypeSelect == PriceListRepository.TYPE_PURCHASE) {
      return partner.getPurchasePartnerPriceList();
    }
    return null;
  }
}
