/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.PartnerPriceList;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

public class PartnerPriceListServiceImpl implements PartnerPriceListService {

    @Override
    public void checkDates(PartnerPriceList partnerPriceList) throws AxelorException {
        Set<PriceList> priceListSet = partnerPriceList.getPriceListSet();

        if (priceListSet == null) {
            return;
        }
        Set<PriceList> sortedPriceListSet = priceListSet.stream()
                .sorted(Comparator.comparing(PriceList::getApplicationBeginDate))
                .collect(Collectors.toSet());
        LocalDate beginDate;
        LocalDate previousEndDate = LocalDate.MIN;
        String previousTitle = "";
        for (PriceList priceList : sortedPriceListSet) {
            beginDate = priceList.getApplicationBeginDate() != null
                    ? priceList.getApplicationBeginDate()
                    : LocalDate.MIN;
            if (beginDate.compareTo(previousEndDate) < 0) {
                throw new AxelorException(IException.INCONSISTENCY,
                        String.format(
                                I18n.get(IExceptionMessage.PARTNER_PRICE_LIST_DATE_INCONSISTENT),
                                previousTitle,
                                priceList.getTitle()
                        ),
                        partnerPriceList);
            }
            previousEndDate = priceList.getApplicationEndDate() != null
                    ? priceList.getApplicationEndDate()
                    : LocalDate.MAX;
            previousTitle = priceList.getTitle();
        }
    }
}
