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

public interface PartnerPriceListService {

  /**
   * Allows to check the dates in a price list.
   *
   * @param partnerPriceList
   * @throws AxelorException if two price lists are scheduled on the same time.
   */
  void checkDates(PartnerPriceList partnerPriceList) throws AxelorException;

  /**
   * @param partner
   * @param priceListTypeSelect
   * @return the default price list from the partner null if partner is null, or no price list was
   *     found for the given partner
   */
  PriceList getDefaultPriceList(Partner partner, int priceListTypeSelect);

  /**
   * @param partner
   * @param priceListTypeSelect
   * @return the domain for the partner and the type
   */
  String getPriceListDomain(Partner partner, int priceListTypeSelect);

  /**
   * @param partner
   * @param priceListTypeSelect
   * @return the partner price list for the given type
   */
  public PartnerPriceList getPartnerPriceList(Partner partner, int priceListTypeSelect);
}
